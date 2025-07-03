package service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PapyrusService {
    
    private final Map<String, String> elementIds = new HashMap<>();
    
    public boolean convertToUML(String plantUmlCode, Path outputPath, DiagramType diagramType) {
        try {
            String xmiContent = generateValidXMI(plantUmlCode, diagramType);
            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(xmiContent);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public enum DiagramType {
        CLASS,
        STATE,
        SEQUENCE,
        ACTIVITY
    }

    private String generateValidXMI(String plantUmlCode, DiagramType diagramType) {
        elementIds.clear();
        
        StringBuilder xmi = new StringBuilder();
        xmi.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmi.append("<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\"\n");
        xmi.append("    xmlns:uml=\"http://www.eclipse.org/uml2/5.0.0/UML\"\n");
        xmi.append("    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\">\n"); // Retirer SysML namespace si non nécessaire
        
        // Modifier la structure pour créer d'abord le Model
        String modelId = generateId("Model");
        xmi.append("  <uml:Model xmi:id=\"").append(modelId).append("\" name=\"UMLModel\">\n");
        
        // Ajouter les types primitifs DANS le Model
        xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"_String\" name=\"String\"/>\n");
        xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"_Integer\" name=\"Integer\"/>\n");
        xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"_Boolean\" name=\"Boolean\"/>\n");
        xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"_Real\" name=\"Real\"/>\n");
        xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"_Date\" name=\"Date\"/>\n");
        
        String packageId = generateId("Package");
        xmi.append("    <packagedElement xmi:type=\"uml:Package\" xmi:id=\"").append(packageId)
           .append("\" name=\"MainPackage\">\n");
        
        switch (diagramType) {
            case CLASS:
                generateSysMLBlockDiagramContent(xmi, plantUmlCode, packageId);
                break;
            case STATE:
                generateStateDiagramContent(xmi, plantUmlCode, packageId);
                break;
            case SEQUENCE:
                generateSequenceDiagramContent(xmi, plantUmlCode, packageId);
                break;
            case ACTIVITY:
                generateActivityDiagramContent(xmi, plantUmlCode, packageId);
                break;
        }
        
        xmi.append("    </packagedElement>\n");
        xmi.append("  </uml:Model>\n");
        addSysMLStereotypeApplications(xmi);
        xmi.append("</xmi:XMI>");
        
        return xmi.toString();
    }

    private void addSysMLProfiles(StringBuilder xmi, String modelId) {
        String profileAppId = generateId("ProfileApplication");
        xmi.append("    <profileApplication xmi:id=\"").append(profileAppId).append("\">\n");
        xmi.append("      <eAnnotations xmi:id=\"").append(generateId("ProfileAnnotation"))
           .append("\" source=\"http://www.eclipse.org/uml2/2.0.0/UML\">\n");
        xmi.append("        <references xmi:type=\"ecore:EPackage\" href=\"http://www.eclipse.org/papyrus/sysml/1.6/SysML#/\"/>\n");
        xmi.append("      </eAnnotations>\n");
        xmi.append("      <appliedProfile href=\"pathmap://SysML16_PROFILES/SysML.profile.uml#SysML\"/>\n");
        xmi.append("    </profileApplication>\n");
    }

    private void addPrimitiveTypes(StringBuilder xmi) {
        String[] primitives = {"String", "Integer", "Boolean", "Real", "Date"};
        for (String type : primitives) {
            String id = generateId(type);
            xmi.append("    <packagedElement xmi:type=\"uml:PrimitiveType\" xmi:id=\"")
               .append(id).append("\" name=\"").append(type).append("\"/>\n");
        }
    }

    private void generateSysMLBlockDiagramContent(StringBuilder xmi, String plantUmlCode, String packageId) {
        extractAndCreateSysMLBlocks(xmi, plantUmlCode);
        extractAndCreateSysMLRelationships(xmi, plantUmlCode);
    }

    private void extractAndCreateSysMLBlocks(StringBuilder xmi, String plantUmlCode) {
        Pattern classPattern = Pattern.compile("class\\s+([\\w]+)(?:\\s+\\{([^}]*)\\})?");
        Matcher classMatcher = classPattern.matcher(plantUmlCode);
        
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String classContent = classMatcher.group(2);
            
            String blockId = generateId("Block_" + className);
            xmi.append("      <packagedElement xmi:type=\"uml:Class\" xmi:id=\"").append(blockId)
               .append("\" name=\"").append(className).append("\">\n");
            
            if (classContent != null && !classContent.trim().isEmpty()) {
                processBlockProperties(xmi, classContent, blockId);
            }
            
            xmi.append("      </packagedElement>\n");
            elementIds.put(className, blockId);
        }
    }

    private void processBlockProperties(StringBuilder xmi, String classContent, String blockId) {
        Pattern attrPattern = Pattern.compile("([+-])\\s*([\\w]+)\\s*:\\s*([\\w<>\\[\\]]+)");
        Matcher attrMatcher = attrPattern.matcher(classContent);
        
        while (attrMatcher.find()) {
            String visibility = attrMatcher.group(1).equals("+") ? "public" : "private";
            String propertyName = attrMatcher.group(2);
            String propertyType = attrMatcher.group(3);
            
            String propertyId = generateId("Property_" + propertyName);
            String typeReference = elementIds.getOrDefault(propertyType, elementIds.getOrDefault("String", "_String"));
            
            xmi.append("        <ownedAttribute xmi:id=\"").append(propertyId)
               .append("\" name=\"").append(propertyName)
               .append("\" visibility=\"").append(visibility).append("\"");
            
            if (!propertyType.contains("[") && !propertyType.contains("<")) {
                xmi.append(" type=\"").append(typeReference).append("\"");
            }
            
            xmi.append("/>\n");
        }
        
        Pattern opPattern = Pattern.compile("([+-])\\s*([\\w]+)\\(([^)]*)\\)(?:\\s*:\\s*([\\w<>\\[\\]]+))?");
        Matcher opMatcher = opPattern.matcher(classContent);
        
        while (opMatcher.find()) {
            String visibility = opMatcher.group(1).equals("+") ? "public" : "private";
            String operationName = opMatcher.group(2);
            String parameters = opMatcher.group(3);
            String returnType = opMatcher.group(4);
            
            String operationId = generateId("Operation_" + operationName);
            
            xmi.append("        <ownedOperation xmi:id=\"").append(operationId)
               .append("\" name=\"").append(operationName)
               .append("\" visibility=\"").append(visibility).append("\">\n");
            
            if (parameters != null && !parameters.trim().isEmpty()) {
                processOperationParameters(xmi, parameters, operationId);
            }
            
            if (returnType != null && !returnType.trim().isEmpty()) {
                String typeReference = elementIds.getOrDefault(returnType, elementIds.getOrDefault("String", "_String"));
                xmi.append("          <ownedParameter xmi:id=\"").append(generateId("ReturnParam"))
                   .append("\" direction=\"return\" type=\"").append(typeReference).append("\"/>\n");
            }
            
            xmi.append("        </ownedOperation>\n");
        }
    }

    private void processOperationParameters(StringBuilder xmi, String parameters, String operationId) {
        String[] paramList = parameters.split(",");
        for (String param : paramList) {
            param = param.trim();
            if (!param.isEmpty()) {
                String[] parts = param.split(":");
                if (parts.length >= 2) {
                    String paramName = parts[0].trim();
                    String paramType = parts[1].trim();
                    
                    String paramId = generateId("Param_" + paramName);
                    String typeReference = elementIds.getOrDefault(paramType, elementIds.getOrDefault("String", "_String"));
                    
                    xmi.append("          <ownedParameter xmi:id=\"").append(paramId)
                       .append("\" name=\"").append(paramName)
                       .append("\" type=\"").append(typeReference).append("\"/>\n");
                }
            }
        }
    }

 // Correction pour les associations dans les diagrammes de classe
    private void extractAndCreateSysMLRelationships(StringBuilder xmi, String plantUmlCode) {
        // Improved pattern for associations
        Pattern assocPattern = Pattern.compile("([\\w]+)\\s+(?:\"([^\"]+)\")?\\s*(-+|\\.|\\*|o|\\.\\.|\\*\\*|oo)(?:-+|\\.|\\*|o|\\.\\.|\\*\\*|oo)\\s*(?:\"([^\"]+)\")?\\s*([\\w]+)");
        Matcher assocMatcher = assocPattern.matcher(plantUmlCode);
        
        while (assocMatcher.find()) {
            String sourceClass = assocMatcher.group(1);
            String sourceRole = assocMatcher.group(2);
            String relationshipType = assocMatcher.group(3);
            String targetRole = assocMatcher.group(4);
            String targetClass = assocMatcher.group(5);
            
            if (sourceRole == null) sourceRole = "";
            if (targetRole == null) targetRole = "";
            
            createSysMLAssociation(xmi, sourceClass, targetClass, sourceRole, targetRole);
        }
        
        // Pattern for inheritance relations
        Pattern inheritPattern = Pattern.compile("([\\w]+)\\s+(?:<\\|--|\\<\\|-|extends|implements)\\s+([\\w]+)");
        Matcher inheritMatcher = inheritPattern.matcher(plantUmlCode);
        
        while (inheritMatcher.find()) {
            String subClass = inheritMatcher.group(1);
            String superClass = inheritMatcher.group(2);
            
            createSysMLGeneralization(xmi, subClass, superClass);
        }
    }


    private void createSysMLAssociation(StringBuilder xmi, String sourceClass, String targetClass, 
                                     String sourceRole, String targetRole) {
        String sourceId = elementIds.getOrDefault(sourceClass, "");
        String targetId = elementIds.getOrDefault(targetClass, "");
        
        if (sourceId.isEmpty() || targetId.isEmpty()) return;
        
        String assocId = generateId("Association");
        String endAId = generateId("AssocEnd_A");
        String endBId = generateId("AssocEnd_B");
        
        xmi.append("      <packagedElement xmi:type=\"uml:Association\" xmi:id=\"")
           .append(assocId).append("\" memberEnd=\"")
           .append(endAId).append(" ").append(endBId).append("\">\n");
        
        xmi.append("        <ownedEnd xmi:id=\"").append(endAId)
           .append("\" type=\"").append(sourceId).append("\" association=\"")
           .append(assocId).append("\"");
        
        if (sourceRole != null && !sourceRole.isEmpty()) {
            xmi.append(" name=\"").append(sourceRole).append("\"");
        }
        
        xmi.append("/>\n");
        
        xmi.append("        <ownedEnd xmi:id=\"").append(endBId)
           .append("\" type=\"").append(targetId).append("\" association=\"")
           .append(assocId).append("\"");
        
        if (targetRole != null && !targetRole.isEmpty()) {
            xmi.append(" name=\"").append(targetRole).append("\"");
        }
        
        xmi.append("/>\n");
        
        xmi.append("      </packagedElement>\n");
    }

    private void createSysMLGeneralization(StringBuilder xmi, String subClass, String superClass) {
        String subId = elementIds.getOrDefault(subClass, "");
        String superId = elementIds.getOrDefault(superClass, "");
        
        if (subId.isEmpty() || superId.isEmpty()) return;
        
        String genId = generateId("Generalization");
        
        xmi.append("      <packagedElement xmi:type=\"uml:Generalization\" xmi:id=\"")
           .append(genId).append("\" specific=\"").append(subId)
           .append("\" general=\"").append(superId).append("\"/>\n");
    }

    private void generateStateDiagramContent(StringBuilder xmi, String plantUmlCode, String packageId) {
        String smId = generateId("StateMachine");
        xmi.append("      <packagedElement xmi:type=\"uml:StateMachine\" xmi:id=\"")
           .append(smId).append("\" name=\"SysMLStateMachine\">\n");
        
        String regionId = generateId("Region");
        xmi.append("        <region xmi:id=\"").append(regionId).append("\" name=\"MainRegion\">\n");
        
        Map<String, String> stateIds = extractStates(xmi, plantUmlCode, regionId);
        extractTransitions(xmi, plantUmlCode, stateIds, regionId);
        
        xmi.append("        </region>\n");
        xmi.append("      </packagedElement>\n");
    }

    private Map<String, String> extractStates(StringBuilder xmi, String plantUmlCode, String regionId) {
        Map<String, String> stateIds = new HashMap<>();
        
        String initialStateId = generateId("Initial");
        xmi.append("          <subvertex xmi:type=\"uml:Pseudostate\" xmi:id=\"")
           .append(initialStateId).append("\" name=\"Initial\"/>\n");
        stateIds.put("[*]", initialStateId);
        
        String finalStateId = generateId("Final");
        xmi.append("          <subvertex xmi:type=\"uml:FinalState\" xmi:id=\"")
           .append(finalStateId).append("\" name=\"Final\"/>\n");
        stateIds.put("[*]2", finalStateId);
        
        Pattern statePattern = Pattern.compile("state\\s+([\\w]+)(?:\\s+as\\s+\"([^\"]+)\")?");
        Matcher stateMatcher = statePattern.matcher(plantUmlCode);
        
        while (stateMatcher.find()) {
            String stateName = stateMatcher.group(1);
            String stateLabel = stateMatcher.group(2);
            
            if (stateLabel == null) stateLabel = stateName;
            
            String stateId = generateId("State_" + stateName);
            xmi.append("          <subvertex xmi:type=\"uml:State\" xmi:id=\"")
               .append(stateId).append("\" name=\"").append(stateLabel).append("\"/>\n");
            
            stateIds.put(stateName, stateId);
        }
        
        return stateIds;
    }

    private void extractTransitions(StringBuilder xmi, String plantUmlCode, Map<String, String> stateIds, String regionId) {
        Pattern transPattern = Pattern.compile("([\\w\\[\\]\\*]+)\\s*--\\>\\s*([\\w\\[\\]\\*]+)(?:\\s*:\\s*([^\\n]+))?");
        Matcher transMatcher = transPattern.matcher(plantUmlCode);
        
        int transitionCount = 0;
        while (transMatcher.find()) {
            String sourceState = transMatcher.group(1);
            String targetState = transMatcher.group(2);
            String transitionLabel = transMatcher.group(3);
            
            if (targetState.equals("[*]")) {
                targetState = "[*]2";
            }
            
            String sourceId = stateIds.getOrDefault(sourceState, "");
            String targetId = stateIds.getOrDefault(targetState, "");
            
            if (sourceId.isEmpty() || targetId.isEmpty()) continue;
            
            String transitionId = generateId("Transition_" + (++transitionCount));
            
            xmi.append("          <transition xmi:id=\"").append(transitionId)
               .append("\" source=\"").append(sourceId)
               .append("\" target=\"").append(targetId).append("\"");
            
            if (transitionLabel != null && !transitionLabel.trim().isEmpty()) {
                xmi.append(" name=\"").append(transitionLabel.trim()).append("\"");
                
            //    String triggerId = generateId("Trigger");
                xmi.append(">\n");
               /* xmi.append("            <trigger xmi:id=\"").append(triggerId)
                   .append("\" name=\"").append(transitionLabel.trim()).append("\"/>\n");*/
                xmi.append("          </transition>\n");
            } else {
                xmi.append("/>\n");
            }
        }
    }

    private void generateSequenceDiagramContent(StringBuilder xmi, String plantUmlCode, String packageId) {
        // Créer l'interaction
        String interactionId = generateId("Interaction");
        xmi.append("    <packagedElement xmi:type=\"uml:Interaction\" xmi:id=\"")
           .append(interactionId).append("\" name=\"MainInteraction\">\n");

        // Étape 1: Créer les lifelines et leurs classes associées
        Map<String, String> lifelineMap = new HashMap<>();
        Pattern participantPattern = Pattern.compile("(participant|actor)\\s+\"?(.*?)\"?\\s+(as\\s+)?(\\w+)");
        Matcher participantMatcher = participantPattern.matcher(plantUmlCode);
        
        while (participantMatcher.find()) {
            String type = participantMatcher.group(1);
            String name = participantMatcher.group(2).trim();
            String alias = participantMatcher.group(4) != null ? 
                          participantMatcher.group(4) : name.replaceAll("\\s+", "");
            
            // Créer la classe
            String classId = generateId("Class_" + alias);
            xmi.append("      <packagedElement xmi:type=\"uml:Class\" xmi:id=\"")
               .append(classId).append("\" name=\"").append(name).append("\"/>\n");
            
            // Créer la propriété
            String propertyId = generateId("Property_" + alias);
            xmi.append("      <ownedAttribute xmi:type=\"uml:Property\" xmi:id=\"")
               .append(propertyId).append("\" name=\"").append(alias)
               .append("\" type=\"").append(classId).append("\"/>\n");
            
            // Créer la lifeline
            String lifelineId = generateId("Lifeline_" + alias);
            xmi.append("      <lifeline xmi:type=\"uml:Lifeline\" xmi:id=\"")
               .append(lifelineId).append("\" name=\"").append(name)
               .append("\" represents=\"").append(propertyId).append("\"/>\n");
            
            lifelineMap.put(alias, lifelineId);
        }

        // Étape 2: Traiter les messages et fragments
        processSequenceElements(xmi, plantUmlCode, lifelineMap, interactionId);

        xmi.append("    </packagedElement>\n");
    }
    
 // Correction pour les diagrammes de séquence
    private void createMessage(StringBuilder xmi, String source, String target, 
                              String arrowType, String label, Map<String, String> lifelineIds) {
        String messageId = generateId("Message");
        String sendEventId = generateId("SendEvent");
        String receiveEventId = generateId("ReceiveEvent");
        
        String sourceLifelineId = lifelineIds.get(source);
        String targetLifelineId = lifelineIds.get(target);
        
        if (sourceLifelineId == null || targetLifelineId == null) {
            return; // Skip if lifelines not found
        }
        
        // Create send event first
        xmi.append("        <fragment xmi:type=\"uml:MessageOccurrenceSpecification\" xmi:id=\"")
           .append(sendEventId).append("\" name=\"").append(label).append("_send")
           .append("\" covered=\"").append(sourceLifelineId).append("\" message=\"")
           .append(messageId).append("\"/>\n");
        
        // Create receive event
        xmi.append("        <fragment xmi:type=\"uml:MessageOccurrenceSpecification\" xmi:id=\"")
           .append(receiveEventId).append("\" name=\"").append(label).append("_receive")
           .append("\" covered=\"").append(targetLifelineId).append("\" message=\"")
           .append(messageId).append("\"/>\n");
        
        // Create the actual message with references to the events
        xmi.append("        <message xmi:id=\"").append(messageId)
           .append("\" name=\"").append(label)
           .append("\" messageSort=\"").append("->".equals(arrowType) ? "synchCall" : "asynchCall")
           .append("\" sendEvent=\"").append(sendEventId)
           .append("\" receiveEvent=\"").append(receiveEventId).append("\"/>\n");
    }


    private void processSequenceElements(StringBuilder xmi, String plantUmlCode, 
            Map<String, String> lifelineIds, String interactionId) {
		// Traitement des messages simples
		Pattern messagePattern = Pattern.compile("([\\w]+)\\s*(-+>|--+>)\\s*([\\w]+)\\s*:\\s*([^\\n]+)");
		Matcher messageMatcher = messagePattern.matcher(plantUmlCode);
		
		while (messageMatcher.find()) {
		String source = messageMatcher.group(1);
		String arrow = messageMatcher.group(2);
		String target = messageMatcher.group(3);
		String label = messageMatcher.group(4).trim();
		
		// Détermine le type de flèche
		String arrowType = arrow.contains("--") ? "-->" : "->";
		
		createMessage(xmi, source, target, arrowType, label, lifelineIds);
		}
		
		// Traitement des activations/désactivations
		Pattern activationPattern = Pattern.compile("(activate|deactivate)\\s+([\\w]+)");
		Matcher activationMatcher = activationPattern.matcher(plantUmlCode);
		
		Map<String, String> activeExecutions = new HashMap<>();
		
		while (activationMatcher.find()) {
		String action = activationMatcher.group(1);
		String lifeline = activationMatcher.group(2);
		
		String lifelineId = lifelineIds.get(lifeline);
		if (lifelineId == null) continue;
		
		if ("activate".equals(action)) {
		// Start execution
		String execId = generateId("ExecutionSpec_" + lifeline);
		String startId = generateId("Start_" + lifeline);
		
		xmi.append("        <fragment xmi:type=\"uml:BehaviorExecutionSpecification\" xmi:id=\"")
		.append(execId).append("\" name=\"Execution_").append(lifeline)
		.append("\" covered=\"").append(lifelineId).append("\">\n");
		
		xmi.append("          <start xmi:idref=\"").append(startId).append("\"/>\n");
		xmi.append("        </fragment>\n");
		
		activeExecutions.put(lifeline, execId);
		} else if ("deactivate".equals(action)) {
		// End execution
		String execId = activeExecutions.remove(lifeline);
		if (execId != null) {
		String finishId = generateId("Finish_" + lifeline);
		
		// Find and update the execution specification
		xmi.append("        <fragment xmi:type=\"uml:MessageOccurrenceSpecification\" xmi:id=\"")
		.append(finishId).append("\" name=\"Finish_").append(lifeline)
		.append("\" covered=\"").append(lifelineId).append("\"/>\n");
		
		// Need to update the existing execution spec with finish reference
		// This would be easier with DOM manipulation rather than string building
		}
		}
		}
		}

    private void createMessage(StringBuilder xmi, String sourceId, String targetId, 
                              String arrowType, String label, String parentId) {
        String messageId = generateId("Message");
        String sendEventId = generateId("SendEvent");
        String receiveEventId = generateId("ReceiveEvent");
        
        xmi.append("      <message xmi:id=\"").append(messageId)
           .append("\" name=\"").append(label).append("\" messageSort=\"")
           .append("->".equals(arrowType) ? "synchCall" : "asynchCall").append("\">\n");
        
        xmi.append("        <sendEvent xmi:type=\"uml:MessageOccurrenceSpecification\" xmi:id=\"")
           .append(sendEventId).append("\" covered=\"").append(sourceId).append("\"/>\n");
        
        xmi.append("        <receiveEvent xmi:type=\"uml:MessageOccurrenceSpecification\" xmi:id=\"")
           .append(receiveEventId).append("\" covered=\"").append(targetId).append("\"/>\n");
        
        xmi.append("      </message>\n");
    }
    
    private void extractCombinedFragments(StringBuilder xmi, String plantUmlCode, 
            Map<String, String> lifelineIds, String interactionId) {
		// Détection des fragments alt
		Pattern altPattern = Pattern.compile("alt\\s+(.*?)\\n(.*?)end", Pattern.DOTALL);
		Matcher altMatcher = altPattern.matcher(plantUmlCode);
		
		int fragmentCount = 0;
		while (altMatcher.find()) {
		String condition = altMatcher.group(1).trim();
		String content = altMatcher.group(2);
		
		String fragmentId = generateId("CombinedFragment_" + (++fragmentCount));
		String operandId = generateId("Operand_" + fragmentCount);
		
		xmi.append("        <fragment xmi:type=\"uml:CombinedFragment\" xmi:id=\"")
		.append(fragmentId).append("\" interactionOperator=\"alt\">\n");
		
		xmi.append("          <operand xmi:id=\"").append(operandId)
		.append("\" guard=\"").append(condition).append("\">\n");
		
		// Extraire les messages à l'intérieur du fragment
		extractMessages(xmi, content, lifelineIds, interactionId);
		
		xmi.append("          </operand>\n");
		xmi.append("        </fragment>\n");
		}
		
		// Détection des notes
		Pattern notePattern = Pattern.compile("note\\s+(left|right)\\s+of\\s+([^\\n:]+):\\s*([^\\n]+)");
		Matcher noteMatcher = notePattern.matcher(plantUmlCode);

		while (noteMatcher.find()) {
		String position = noteMatcher.group(1);
		String participant = noteMatcher.group(2).trim();
		String text = noteMatcher.group(3).trim();
		
		String noteId = generateId("Note_" + participant);
		String participantId = lifelineIds.get(participant);
		
		if (participantId != null) {
		xmi.append("        <ownedComment xmi:id=\"").append(noteId)
		.append("\" body=\"").append(text).append("\" annotatedElement=\"")
		.append(participantId).append("\"/>\n");
		}
		}
		}
    
    private void addSequenceDiagramElement(StringBuilder xmi, String interactionId) {
        String diagramId = generateId("SequenceDiagram");
        xmi.append("  <uml:Diagram xmi:type=\"uml:Diagram\" xmi:id=\"")
           .append(diagramId)
           .append("\" name=\"SequenceDiagram\" element=\"")
           .append(interactionId)
           .append("\">\n");
        xmi.append("    <owner xmi:type=\"uml:Model\" href=\"#_Model\"/>\n");
        xmi.append("  </uml:Diagram>\n");
    }

    private Map<String, String> extractLifelines(StringBuilder xmi, String plantUmlCode, String interactionId) {
        Map<String, String> lifelineIds = new HashMap<>();
        
        // Regex améliorée pour les participants
        Pattern participantPattern = Pattern.compile("(?:participant|actor)\\s+\"?([^\"]+)\"?\\s*(?:as\\s+([\\w]+))?");
        Matcher participantMatcher = participantPattern.matcher(plantUmlCode);
        
        while (participantMatcher.find()) {
            String participantName = participantMatcher.group(1).trim();
            String participantAlias = participantMatcher.group(2);
            
            String alias = (participantAlias != null) ? participantAlias : participantName.replaceAll("\\s+", "");
            
            // Créer un classifier (normalement une classe) pour la lifeline
            String classifierId = generateId("Class_" + alias);
            xmi.append("      <packagedElement xmi:type=\"uml:Class\" xmi:id=\"")
               .append(classifierId)
               .append("\" name=\"").append(participantName).append("\"/>\n");
            
            // Créer la propriété représentée
            String propertyId = generateId("Property_" + alias);
            xmi.append("      <ownedAttribute xmi:type=\"uml:Property\" xmi:id=\"")
               .append(propertyId)
               .append("\" name=\"").append(participantName)
               .append("\" type=\"").append(classifierId).append("\"/>\n");
            
            // Créer la lifeline
            String lifelineId = generateId("Lifeline_" + alias);
            xmi.append("        <lifeline xmi:type=\"uml:Lifeline\" xmi:id=\"")
               .append(lifelineId)
               .append("\" name=\"").append(participantName)
               .append("\" represents=\"").append(propertyId).append("\"/>\n");
            
            lifelineIds.put(alias, lifelineId);
        }
        
        return lifelineIds;
    }

    private void extractMessages(StringBuilder xmi, String plantUmlCode, 
            Map<String, String> lifelineIds, String interactionId) {
		// Regex améliorée pour détecter les activations/désactivations
		Pattern messagePattern = Pattern.compile(
		"([\\w]+)\\s*(->|-->)\\s*([\\w]+)\\s*:\\s*([^\\n]+)|" +
		"activate\\s+([\\w]+)|" +
		"deactivate\\s+([\\w]+)");
		
		Matcher messageMatcher = messagePattern.matcher(plantUmlCode);
		
		int messageCount = 0;
		while (messageMatcher.find()) {
		// Gestion des messages normaux
		if (messageMatcher.group(1) != null) {
		String sourceLifeline = messageMatcher.group(1);
		String arrowType = messageMatcher.group(2);
		String targetLifeline = messageMatcher.group(3);
		String messageLabel = messageMatcher.group(4).trim();
		
		String sourceId = lifelineIds.getOrDefault(sourceLifeline, "");
		String targetId = lifelineIds.getOrDefault(targetLifeline, "");
		
		if (sourceId.isEmpty() || targetId.isEmpty()) continue;
		
		String messageId = generateId("Message_" + (++messageCount));
		boolean isSync = "->".equals(arrowType);
		
		xmi.append("        <message xmi:id=\"").append(messageId)
		.append("\" name=\"").append(messageLabel)
		.append("\" messageSort=\"").append(isSync ? "synchCall" : "asynchCall")
		.append("\" sendEvent=\"").append(sourceId)
		.append("\" receiveEvent=\"").append(targetId).append("\"/>\n");
		}
		// Gestion des activations
		else if (messageMatcher.group(5) != null) {
		String lifeline = messageMatcher.group(5);
		String execId = generateId("ExecutionSpec_" + lifeline);
		String lifelineId = lifelineIds.get(lifeline);
		
		if (lifelineId != null) {
		 xmi.append("        <executionSpecification xmi:id=\"").append(execId)
		    .append("\" start=\"")
		    .append(generateId("Start_" + lifeline))
		    .append("\" finish=\"")
		    .append(generateId("Finish_" + lifeline))
		    .append("\" covered=\"").append(lifelineId).append("\"/>\n");
		}
		}
		}
		}

    private void addSysMLStereotypeApplications(StringBuilder xmi) {
        // Créer une copie des entrées pour éviter les modifications concurrentes
        Map<String, String> entriesCopy = new HashMap<>(elementIds);

        for (Map.Entry<String, String> entry : entriesCopy.entrySet()) {
            if (entry.getKey().startsWith("Block_")) {
                String blockId = entry.getValue();
                String stereoAppId = generateId("BlockApp_" + entry.getKey());

                xmi.append("  <SysML:Block xmi:id=\"").append(stereoAppId)
                   .append("\" base_Class=\"").append(blockId).append("\"/>\n");
            }
        }
    }


    private String generateId(String prefix) {
        String id = "_" + prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        elementIds.put(prefix, id);
        return id;
    }
    
    public static boolean savePapyrusUML(String xmiContent, Path outputPath) {
        try {
            // Validation basique du XMI
            if (!xmiContent.contains("http://www.omg.org/XMI") || 
                !xmiContent.contains("http://www.eclipse.org/uml2/5.0.0/UML")) {
                throw new IllegalArgumentException("Invalid XMI format");
            }

            try (FileWriter writer = new FileWriter(outputPath.toFile())) {
                writer.write(xmiContent);
                return true;
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void generateActivityDiagramContent(StringBuilder xmi, String plantUmlCode, String packageId) {
        String activityId = generateId("Activity");
        xmi.append("      <packagedElement xmi:type=\"uml:Activity\" xmi:id=\"")
           .append(activityId).append("\" name=\"MainActivity\">\n");
        
        // Create a partition (swimlane) for the main activity
        String partitionId = generateId("Partition");
        xmi.append("        <group xmi:type=\"uml:ActivityPartition\" xmi:id=\"")
           .append(partitionId).append("\" name=\"MainPartition\">\n");
        
        // Process all activity nodes
        processActivityNodes(xmi, plantUmlCode, partitionId);
        
        xmi.append("        </group>\n");
        xmi.append("      </packagedElement>\n");
    }

    private void processActivityNodes(StringBuilder xmi, String plantUmlCode, String partitionId) {
        Map<String, String> nodeIds = new HashMap<>();
        Map<String, String> decisionNodes = new HashMap<>();
        
        // 1. Process initial node
        String initialNodeId = generateId("InitialNode");
        xmi.append("          <node xmi:type=\"uml:InitialNode\" xmi:id=\"")
           .append(initialNodeId).append("\" name=\"Start\" inPartition=\"")
           .append(partitionId).append("\"/>\n");
        nodeIds.put("start", initialNodeId);
        
        // 2. Process final nodes
        String finalNodeId = generateId("FinalNode");
        xmi.append("          <node xmi:type=\"uml:ActivityFinalNode\" xmi:id=\"")
           .append(finalNodeId).append("\" name=\"Stop\" inPartition=\"")
           .append(partitionId).append("\"/>\n");
        nodeIds.put("stop", finalNodeId);
        
        // 3. Process activities (OpaqueActions)
        Pattern activityPattern = Pattern.compile(":([^;]+);");
        Matcher activityMatcher = activityPattern.matcher(plantUmlCode);
        
        int activityCount = 0;
        while (activityMatcher.find()) {
            String activityName = activityMatcher.group(1).trim();
            String activityId = generateId("Activity_" + (++activityCount));
            xmi.append("          <node xmi:type=\"uml:OpaqueAction\" xmi:id=\"")
               .append(activityId).append("\" name=\"").append(activityName)
               .append("\" inPartition=\"").append(partitionId).append("\">\n");
            xmi.append("            <body>").append(activityName).append("</body>\n");
            xmi.append("          </node>\n");
            nodeIds.put(activityName, activityId);
        }
        
        // 4. Process decision nodes
        Pattern decisionPattern = Pattern.compile("if\\s*\\(([^)]+)\\)\\s*then\\s*\\(([^)]+)\\)");
        Matcher decisionMatcher = decisionPattern.matcher(plantUmlCode);
        
        int decisionCount = 0;
        while (decisionMatcher.find()) {
            String condition = decisionMatcher.group(1).trim();
            String decisionId = generateId("Decision_" + (++decisionCount));
            xmi.append("          <node xmi:type=\"uml:DecisionNode\" xmi:id=\"")
               .append(decisionId).append("\" name=\"Decision_").append(condition)
               .append("\" inPartition=\"").append(partitionId).append("\"/>\n");
            decisionNodes.put(condition, decisionId);
            nodeIds.put("decision_" + decisionCount, decisionId);
        }
        
        // 5. Process merge nodes (for endif)
        Pattern mergePattern = Pattern.compile("endif");
        Matcher mergeMatcher = mergePattern.matcher(plantUmlCode);
        
        int mergeCount = 0;
        while (mergeMatcher.find()) {
            String mergeId = generateId("Merge_" + (++mergeCount));
            xmi.append("          <node xmi:type=\"uml:MergeNode\" xmi:id=\"")
               .append(mergeId).append("\" name=\"Merge_").append(mergeCount)
               .append("\" inPartition=\"").append(partitionId).append("\"/>\n");
            nodeIds.put("merge_" + mergeCount, mergeId);
        }
        
        // 6. Process flows between nodes
        processActivityFlows(xmi, plantUmlCode, nodeIds, decisionNodes, partitionId);
    }

    private void processActivityFlows(StringBuilder xmi, String plantUmlCode, 
                                    Map<String, String> nodeIds, 
                                    Map<String, String> decisionNodes,
                                    String partitionId) {
        // Process simple flows (->)
        Pattern flowPattern = Pattern.compile("(\\w+)\\s*->\\s*(\\w+)");
        Matcher flowMatcher = flowPattern.matcher(plantUmlCode);
        
        int edgeCount = 0;
        while (flowMatcher.find()) {
            String source = flowMatcher.group(1);
            String target = flowMatcher.group(2);
            
            String sourceId = nodeIds.get(source);
            String targetId = nodeIds.get(target);
            
            if (sourceId != null && targetId != null) {
                String edgeId = generateId("Edge_" + (++edgeCount));
                xmi.append("          <edge xmi:type=\"uml:ControlFlow\" xmi:id=\"")
                   .append(edgeId).append("\" source=\"").append(sourceId)
                   .append("\" target=\"").append(targetId)
                   .append("\" inPartition=\"").append(partitionId).append("\"/>\n");
            }
        }
        
        // Process decision flows (if/then/else)
        Pattern decisionFlowPattern = Pattern.compile(
            "if\\s*\\(([^)]+)\\)\\s*then\\s*\\(([^)]+)\\)\\s*->\\s*(\\w+)|" +
            "else\\s*\\(([^)]+)\\)\\s*->\\s*(\\w+)");
        Matcher decisionFlowMatcher = decisionFlowPattern.matcher(plantUmlCode);
        
        while (decisionFlowMatcher.find()) {
            if (decisionFlowMatcher.group(1) != null) {
                // then branch
                String condition = decisionFlowMatcher.group(1);
                String target = decisionFlowMatcher.group(3);
                String decisionId = decisionNodes.get(condition);
                String targetId = nodeIds.get(target);
                
                if (decisionId != null && targetId != null) {
                    String edgeId = generateId("DecisionEdge_" + (++edgeCount));
                    xmi.append("          <edge xmi:type=\"uml:ControlFlow\" xmi:id=\"")
                       .append(edgeId).append("\" source=\"").append(decisionId)
                       .append("\" target=\"").append(targetId)
                       .append("\" inPartition=\"").append(partitionId).append("\">\n");
                    xmi.append("            <guard xmi:type=\"uml:LiteralString\" xmi:id=\"")
                       .append(generateId("Guard")).append("\" value=\"")
                       .append(decisionFlowMatcher.group(2)).append("\"/>\n");
                    xmi.append("          </edge>\n");
                }
            } else if (decisionFlowMatcher.group(4) != null) {
                // else branch
                String condition = decisionFlowMatcher.group(4);
                String target = decisionFlowMatcher.group(5);
                String decisionId = decisionNodes.get(condition);
                String targetId = nodeIds.get(target);
                
                if (decisionId != null && targetId != null) {
                    String edgeId = generateId("DecisionEdge_" + (++edgeCount));
                    xmi.append("          <edge xmi:type=\"uml:ControlFlow\" xmi:id=\"")
                       .append(edgeId).append("\" source=\"").append(decisionId)
                       .append("\" target=\"").append(targetId)
                       .append("\" inPartition=\"").append(partitionId).append("\">\n");
                    xmi.append("            <guard xmi:type=\"uml:LiteralString\" xmi:id=\"")
                       .append(generateId("Guard")).append("\" value=\"else\"/>\n");
                    xmi.append("          </edge>\n");
                }
            }
        }
        
        // Process fork/join nodes (parallel activities)
        Pattern forkPattern = Pattern.compile("fork|fork again");
        Matcher forkMatcher = forkPattern.matcher(plantUmlCode);
        
        int forkCount = 0;
        while (forkMatcher.find()) {
            String forkId = generateId("Fork_" + (++forkCount));
            xmi.append("          <node xmi:type=\"uml:ForkNode\" xmi:id=\"")
               .append(forkId).append("\" name=\"Fork_").append(forkCount)
               .append("\" inPartition=\"").append(partitionId).append("\"/>\n");
            nodeIds.put("fork_" + forkCount, forkId);
        }
        
        Pattern joinPattern = Pattern.compile("end fork");
        Matcher joinMatcher = joinPattern.matcher(plantUmlCode);
        
        int joinCount = 0;
        while (joinMatcher.find()) {
            String joinId = generateId("Join_" + (++joinCount));
            xmi.append("          <node xmi:type=\"uml:JoinNode\" xmi:id=\"")
               .append(joinId).append("\" name=\"Join_").append(joinCount)
               .append("\" inPartition=\"").append(partitionId).append("\"/>\n");
            nodeIds.put("join_" + joinCount, joinId);
        }
    }

    public static String cleanAndValidateXMI(String rawXmi) {
        // Remove any Markdown tags
        String cleaned = rawXmi.replaceAll("```xml", "").replaceAll("```", "").trim();
        
        // Basic validation
        if (!cleaned.startsWith("<?xml") || !cleaned.contains("xmi:XMI")) {
            throw new IllegalArgumentException("Invalid XMI format - missing XML declaration or root element");
        }
        
        // Check required namespaces
        String[] requiredNamespaces = {
            "xmlns:xmi=\"http://www.omg.org/XMI\"",
            "xmlns:uml=\"http://www.eclipse.org/uml2/5.0.0/UML\""
        };
        
        for (String ns : requiredNamespaces) {
            if (!cleaned.contains(ns)) {
                throw new IllegalArgumentException("Missing required namespace: " + ns);
            }
        }
        
        // Check basic structure
        if (!cleaned.contains("<uml:Model") || !cleaned.contains("</uml:Model>")) {
            throw new IllegalArgumentException("XMI must contain a uml:Model element");
        }
        
        return cleaned;
    }
}