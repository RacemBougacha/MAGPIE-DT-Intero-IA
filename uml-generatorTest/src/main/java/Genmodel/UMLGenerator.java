package Genmodel;

import service.LLMService;
import service.PlantUMLService;
import service.PapyrusService;
import service.LanguageDetector;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class UMLGenerator {
    private final LLMService llmService;
    private final PlantUMLService plantUMLService;
    private final PapyrusService papyrusService;
    
    public static final String CLASS_DIAGRAM = "class";
    public static final String STATE_DIAGRAM = "state";
    public static final String SEQUENCE_DIAGRAM = "sequence";
    public static final String ACTIVITY_DIAGRAM = "activity";
    
    public UMLGenerator(LLMService llmService, PlantUMLService plantUMLService, PapyrusService papyrusService) {
        this.llmService = llmService;
        this.plantUMLService = plantUMLService;
        this.papyrusService = papyrusService;
    }
    
    public void setOllamaEndpoint(String endpoint) {
        llmService.setOllamaEndpoint(endpoint);
    }
    
    public String generatePapyrusUMLCode(String textDescription, String diagramType) {
        String language = LanguageDetector.detectLanguage(textDescription);
        String prompt = buildPapyrusPrompt(textDescription, diagramType, language);
        
        String rawXmi = llmService.generateText(prompt);
        return PapyrusService.cleanAndValidateXMI(rawXmi);
    }
    
    private String buildPapyrusPrompt(String textDescription, String diagramType, String language) {
        switch(diagramType) {
            case SEQUENCE_DIAGRAM:
                return language.equals("en") ?
                    "Generate a Papyrus-compatible XMI UML sequence diagram from this description.\n" +
                    "Provide ONLY the XMI code without any explanation or additional text.\n" +
                    "Follow these rules:\n" +
                    "1. Use proper XMI 2.1 format with UML 2.5 namespaces\n" +
                    "2. Include all necessary elements: Interaction, Lifelines, Messages\n" +
                    "3. For sequence diagrams, include execution specifications for activations\n" +
                    "4. Format the XML properly with indentation\n\n" +
                    "Description:\n" + textDescription
                    :
                    "Génère un code XMI UML compatible Papyrus pour un diagramme de séquence à partir de cette description.\n" +
                    "Ne fournis QUE le code XMI sans explication.\n" +
                    "Règles à suivre :\n" +
                    "1. Utilisez le format XMI 2.1 avec les namespaces UML 2.5\n" +
                    "2. Incluez tous les éléments nécessaires : Interaction, Lifelines, Messages\n" +
                    "3. Pour les diagrammes de séquence, incluez les spécifications d'exécution\n" +
                    "4. Formatez le XML correctement avec indentation\n\n" +
                    "Description :\n" + textDescription;
            case CLASS_DIAGRAM:
                return language.equals("en") ?
                    "Generate a valid Papyrus UML XMI 2.1 file for a class diagram from this description.\n" +
                    "Follow these strict rules:\n" +
                    "1. Use XMI 2.1 format with UML 2.5 namespaces\n" +
                    "2. Include all required elements: Classes, Attributes, Operations, Associations\n" +
                    "3. For associations, include proper memberEnd references\n" +
                    "4. Format the XML with proper indentation\n" +
                    "5. Include all required namespace declarations\n" +
                    "6. Generate complete XMI file with <?xml declaration\n\n" +
                    "Example structure:\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:Class\" ...>\n" +
                    "      <ownedAttribute .../>\n" +
                    "      <ownedOperation .../>\n" +
                    "    </packagedElement>\n" +
                    "    <packagedElement xmi:type=\"uml:Association\" .../>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription
                    :
                    "Génère un fichier XMI 2.1 UML Papyrus valide pour un diagramme de classe apartir de cette description" + 
                    "Suivez ces règles strictes :\n" +
                    "1. Utilisez le format XMI 2.1 avec les espaces de noms UML 2.5\n" +
                    "2. Incluez tous les éléments requis : classes, attributs, opérations, associations\n" +
                    "3. Pour les associations, incluez les références memberEnd appropriées\n" +
                    "4. Formatez le XML avec l'indentation appropriée\n" +
                    "5. Incluez toutes les déclarations d'espaces de noms requises\n" +
                    "6. Générez le fichier XMI complet avec la déclaration <?xml\n\n" +
                    "Exemple de structure :\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:Class\" ...>\n" +
                    "      <ownedAttribute .../>\n" +
                    "      <ownedOperation .../>\n" +
                    "    </packagedElement>\n" +
                    "    <packagedElement xmi:type=\"uml:Association\" .../>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription;
            case STATE_DIAGRAM:
                return language.equals("en") ?
                    "Generate a valid Papyrus UML XMI 2.1 file for a state machine diagram from this description.\n" +
                    "Follow these strict rules:\n" +
                    "1. Use XMI 2.1 format with UML 2.5 namespaces\n" +
                    "2. Include all required elements: StateMachine, States, Transitions, Pseudostates (initial/final)\n" +
                    "3. Properly nest regions and sub-states if applicable\n" +
                    "4. Format the XML with proper indentation\n" +
                    "5. Include all required namespace declarations\n" +
                    "6. Generate complete XMI file with <?xml declaration\n\n" +
                    "Example structure:\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:StateMachine\" name=\"MyStateMachine\" ...>\n" +
                    "      <region xmi:type=\"uml:Region\" ...>\n" +
                    "        <subvertex xmi:type=\"uml:Pseudostate\" kind=\"initial\" .../>\n" +
                    "        <subvertex xmi:type=\"uml:State\" name=\"State1\" .../>\n" +
                    "        <subvertex xmi:type=\"uml:FinalState\" .../>\n" +
                    "        <transition source=\"...\" target=\"...\" .../>\n" +
                    "      </region>\n" +
                    "    </packagedElement>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription
                    :
                    "Génère un fichier XMI 2.1 UML Papyrus valide pour un diagramme d’états à partir de cette description.\n" +
                    "Suivez ces règles strictes :\n" +
                    "1. Utilisez le format XMI 2.1 avec les espaces de noms UML 2.5\n" +
                    "2. Incluez tous les éléments requis : StateMachine, États, Transitions, Pseudostates (initial/final)\n" +
                    "3. Imbrication correcte des régions et sous-états si nécessaire\n" +
                    "4. Formatez le XML avec une indentation appropriée\n" +
                    "5. Incluez toutes les déclarations d’espaces de noms requises\n" +
                    "6. Générez le fichier XMI complet avec la déclaration <?xml\n\n" +
                    "Exemple de structure :\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:StateMachine\" name=\"MonStateMachine\" ...>\n" +
                    "      <region xmi:type=\"uml:Region\" ...>\n" +
                    "        <subvertex xmi:type=\"uml:Pseudostate\" kind=\"initial\" .../>\n" +
                    "        <subvertex xmi:type=\"uml:State\" name=\"État1\" .../>\n" +
                    "        <subvertex xmi:type=\"uml:FinalState\" .../>\n" +
                    "        <transition source=\"...\" target=\"...\" .../>\n" +
                    "      </region>\n" +
                    "    </packagedElement>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription;
            case ACTIVITY_DIAGRAM:
                return language.equals("en") ?
                    "Generate a valid Papyrus UML XMI 2.1 file for an activity diagram from this description.\n" +
                    "Follow these strict rules:\n" +
                    "1. Use XMI 2.1 format with UML 2.5 namespaces\n" +
                    "2. Include all required elements: Activity, Action nodes, Control nodes (initial, decision, merge, final)\n" +
                    "3. For control flows, use proper edge elements\n" +
                    "4. Format the XML with proper indentation\n" +
                    "5. Include all required namespace declarations\n" +
                    "6. Generate complete XMI file with <?xml declaration\n\n" +
                    "Example structure:\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:Activity\" name=\"MyActivity\" ...>\n" +
                    "      <node xmi:type=\"uml:InitialNode\" .../>\n" +
                    "      <node xmi:type=\"uml:OpaqueAction\" name=\"Action1\" .../>\n" +
                    "      <node xmi:type=\"uml:ActivityFinalNode\" .../>\n" +
                    "      <edge xmi:type=\"uml:ControlFlow\" source=\"...\" target=\"...\" .../>\n" +
                    "    </packagedElement>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription
                    :
                    "Génère un fichier XMI 2.1 UML Papyrus valide pour un diagramme d’activités à partir de cette description.\n" +
                    "Suivez ces règles strictes :\n" +
                    "1. Utilisez le format XMI 2.1 avec les espaces de noms UML 2.5\n" +
                    "2. Incluez tous les éléments requis : Activité, actions, nœuds de contrôle (initial, décision, fusion, final)\n" +
                    "3. Pour les flux de contrôle, utilisez les éléments edge correctement reliés\n" +
                    "4. Formatez le XML avec une indentation appropriée\n" +
                    "5. Incluez toutes les déclarations d’espaces de noms requises\n" +
                    "6. Générez le fichier XMI complet avec la déclaration <?xml\n\n" +
                    "Exemple de structure :\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<xmi:XMI xmi:version=\"2.1\" xmlns:xmi=\"http://www.omg.org/XMI\" ...>\n" +
                    "  <uml:Model xmi:id=\"_Model_1\" name=\"Model\">\n" +
                    "    <packagedElement xmi:type=\"uml:Activity\" name=\"MonActivité\" ...>\n" +
                    "      <node xmi:type=\"uml:InitialNode\" .../>\n" +
                    "      <node xmi:type=\"uml:OpaqueAction\" name=\"Action1\" .../>\n" +
                    "      <node xmi:type=\"uml:ActivityFinalNode\" .../>\n" +
                    "      <edge xmi:type=\"uml:ControlFlow\" source=\"...\" target=\"...\" .../>\n" +
                    "    </packagedElement>\n" +
                    "  </uml:Model>\n" +
                    "</xmi:XMI>\n\n" +
                    "Description:\n" + textDescription;
            default: return " ";
        }
    }
    
    public String generateUMLCode(String textDescription, String diagramType) {
        String language = LanguageDetector.detectLanguage(textDescription);
        String prompt;
        
        switch(diagramType) {
            case CLASS_DIAGRAM:
                prompt = language.equals("en") ?
                    "Generate a PlantUML class diagram from this description. " +
                    "Provide ONLY the PlantUML code without any explanation. " +
                    "Follow these rules strictly:\n" +
                    "1. Start with @startuml and end with @enduml\n" +
                    "2. Define each class using: class ClassName {\n  attribute\n  method()\n}\n" +
                    "3. Use + for public, - for private, # for protected members\n" +
                    "4. Define relationships like: ClassA --> ClassB : AssociationName\n" +
                    "5. Use inheritance with: ClassA <|-- ClassB\n" +
                    "6. Use interfaces with: interface InterfaceName\n" +
                    "7. For interface realization: ClassA ..|> InterfaceName\n\n" +
                    "Description:\n" + textDescription
                    :
                    "Génère un diagramme de classes PlantUML à partir de cette description. " +
                    "Ne donne QUE le code PlantUML sans explication. " +
                    "Respecte strictement ces règles :\n" +
                    "1. Commence par @startuml et termine par @enduml\n" +
                    "2. Définis chaque classe avec : class NomClasse {\n  attribut\n  méthode()\n}\n" +
                    "3. Utilise + pour public, - pour privé, # pour protégé\n" +
                    "4. Définis les relations comme : ClasseA --> ClasseB : NomAssociation\n" +
                    "5. Pour l’héritage : ClasseA <|-- ClasseB\n" +
                    "6. Pour les interfaces : interface NomInterface\n" +
                    "7. Pour la réalisation : ClasseA ..|> NomInterface\n\n" +
                    "Description:\n" + textDescription;
                break;
                
            case STATE_DIAGRAM:
                prompt = language.equals("en") ?
                    "Generate a PlantUML state diagram from this description. " + 
                    "Provide ONLY the PlantUML code without any explanation. " +
                    "Follow these rules strictly:\n" +
                    "1. Start with @startuml and end with @enduml\n" +
                    "2. Define each state with: state StateName\n" +
                    "3. For nested states, use proper syntax: state StateName { }\n" +
                    "4. For transitions, use: StateA --> StateB : Label\n" +
                    "5. Initial state should be [*] with transition: [*] --> FirstState\n" +
                    "6. Final state (if any) should be: LastState --> [*]\n\n" +
                    "Description:\n" + textDescription :
                    "Génère un diagramme PlantUML d'état à partir de cette description. " + 
                    "Ne donne QUE le code PlantUML sans explication. " +
                    "Respecte strictement ces règles :\n" +
                    "1. Commence par @startuml et termine par @enduml\n" +
                    "2. Définis chaque état avec: state NomEtat\n" +
                    "3. Pour les états imbriqués, utilise la syntaxe correcte: state NomEtat { }\n" +
                    "4. Pour les transitions, utilise: EtatA --> EtatB : Étiquette\n" +
                    "5. L'état initial doit être [*] avec transition: [*] --> PremierEtat\n" +
                    "6. L'état final (si applicable): DernierEtat --> [*]\n\n" +
                    "Description:\n" + textDescription;
                break;
                
            case SEQUENCE_DIAGRAM:
                prompt = language.equals("en") ?
                    "Generate a PlantUML sequence diagram from this description. " + 
                    "Provide ONLY the PlantUML code without any explanation. " +
                    "Follow these rules strictly:\n" +
                    "1. Start with @startuml and end with @enduml\n" +
                    "2. Define participants with: participant \"Name\" as alias\n" +
                    "3. For messages, use: sender -> receiver: message\n" +
                    "4. For reply messages, use: sender --> receiver: response\n" +
                    "5. For activations, use: activate actor and deactivate actor\n" +
                    "6. For notes, use: note left of actor: text or note right of actor: text\n\n" +
                    "Description:\n" + textDescription :
                    "Génère un diagramme PlantUML de séquence à partir de cette description. " + 
                    "Ne donne QUE le code PlantUML sans explication. " +
                    "Respecte strictement ces règles :\n" +
                    "1. Commence par @startuml et termine par @enduml\n" +
                    "2. Définis les participants avec: participant \"Nom\" as alias\n" +
                    "3. Pour les messages, utilise: émetteur -> récepteur: message\n" +
                    "4. Pour les réponses, utilise: émetteur --> récepteur: réponse\n" +
                    "5. Pour les activations, utilise: activate acteur et deactivate acteur\n" +
                    "6. Pour les notes, utilise: note left of acteur: texte ou note right of acteur: texte\n\n" +
                    "Description:\n" + textDescription;
                break;
                
            case ACTIVITY_DIAGRAM:
                prompt = language.equals("en") ?
                    "Generate a PlantUML activity diagram from this description. " +
                    "Provide ONLY the PlantUML code without any explanation. " +
                    "Follow these rules strictly:\n" +
                    "1. Start with @startuml and end with @enduml\n" +
                    "2. For start node use: start\n" +
                    "3. For end node use: stop\n" +
                    "4. For activities use: :Activity Name;\n" +
                    "5. For decisions use: if (condition) then (yes)\n" +
                    "6. For merges use: endif\n" +
                    "7. For flows use: ->\n\n" +
                    "Description:\n" + textDescription :
                    "Génère un diagramme PlantUML d'activité à partir de cette description. " +
                    "Ne donne QUE le code PlantUML sans explication. " +
                    "Respecte strictement ces règles :\n" +
                    "1. Commence par @startuml et termine par @enduml\n" +
                    "2. Pour le nœud de départ utilisez: start\n" +
                    "3. Pour le nœud de fin utilisez: stop\n" +
                    "4. Pour les activités utilisez: :Nom de l'activité;\n" +
                    "5. Pour les décisions utilisez: if (condition) then (oui)\n" +
                    "6. Pour les fusions utilisez: endif\n" +
                    "7. Pour les flux utilisez: ->\n\n" +
                    "Description:\n" + textDescription;
                break;
                
            default:
                prompt = language.equals("en") ?
                    "Generate a PlantUML class diagram from this description, " + 
                    "only provide the PlantUML code without any explanation (with @startuml and @enduml tags):\n\n" + 
                    textDescription :
                    "Génère un diagramme PlantUML de classe à partir de cette description, " + 
                    "ne donne que le code PlantUML sans explication (avec les balises @startuml et @enduml):\n\n" + 
                    textDescription;
                break;
        }
        
        String rawCode = llmService.generateText(prompt);
        boolean isStateDiagram = diagramType.equals(STATE_DIAGRAM);
        return plantUMLService.validateAndCorrectUMLCode(rawCode, isStateDiagram);
    }
    
    public BufferedImage generateUMLDiagram(String plantUmlCode) {
        boolean isStateDiagram = plantUmlCode.toLowerCase().contains("state ");
        String validatedCode = plantUMLService.validateAndCorrectUMLCode(plantUmlCode, isStateDiagram);
        return plantUMLService.generateDiagram(validatedCode);
    }
    
    public boolean saveUMLFile(String plantUmlCode, Path filePath) {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(plantUmlCode);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean saveUMLImage(BufferedImage image, Path filePath) {
        return plantUMLService.saveDiagramImage(image, filePath);
    }
    
    public boolean exportToPapyrusUML(String plantUmlCode, Path filePath, String diagramType) {
        switch (diagramType) {
            case CLASS_DIAGRAM: 
                return papyrusService.convertToUML(plantUmlCode, filePath, PapyrusService.DiagramType.CLASS);
            case STATE_DIAGRAM:
                return papyrusService.convertToUML(plantUmlCode, filePath, PapyrusService.DiagramType.STATE);
            case SEQUENCE_DIAGRAM:
                return papyrusService.convertToUML(plantUmlCode, filePath, PapyrusService.DiagramType.SEQUENCE);
            case ACTIVITY_DIAGRAM:
                return papyrusService.convertToUML(plantUmlCode, filePath, PapyrusService.DiagramType.ACTIVITY);
            default:
                return papyrusService.convertToUML(plantUmlCode, filePath, PapyrusService.DiagramType.CLASS);
        }
    }
    
    public boolean isModelReady() {
        return llmService.isModelLoaded();
    }
    
    public boolean loadModel(String modelPath) {
        return llmService.loadModel(modelPath);
    }
}