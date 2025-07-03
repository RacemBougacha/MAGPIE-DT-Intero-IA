import java.util.List;
import java.util.UUID;

import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Element;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class SequenceDiagramTransformationToOWL {
public static void transformSequenceToOWL(Package sysmlModel, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        
        // Extraire toutes les interactions (diagrammes de séquence)
        List<Interaction> interactions = SysMLModelLoader.extractSequenceDiagrams(sysmlModel);
        

        for (Interaction interaction : interactions) {
            // Créer une OWLClass pour l'interaction
            OWLClass interactionClass = factory.getOWLClass(IRI.create(BASE_IRI + interaction.getName().replace(" ", "")));
            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(interactionClass));

            // Récupérer le package parent de l'interaction
            Package parentPackage = interaction.getNearestPackage();
            if (parentPackage != null) {
                OWLClass packageClass = factory.getOWLClass(IRI.create(BASE_IRI + ((NamedElement) parentPackage).getName().replace(" ", "")));

                // Faire de l'interaction une sous-classe du package parent
                OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(interactionClass, packageClass);
                manager.addAxiom(ontology, subclassAxiom);

                System.out.println("Création de OWLClass: " + interaction.getName() + " sous-classe de " + parentPackage.getName());
            }
            
         // Extraire toutes les lifelines ( diagramme de séquence)
         // Parcourir les propriétés comme les lifelines
            for (Lifeline lifeline : interaction.getLifelines()) {
                if (lifeline.getRepresents() != null) {
                    String propertyName = lifeline.getName().replace(" ", "");
                    String propertyType = lifeline.getRepresents().getType().getName().replace(" ", "");

                    // Créer l'individu pour la propriété
                    OWLNamedIndividual propInd = factory.getOWLNamedIndividual(IRI.create(BASE_IRI + propertyName));

                    // Associer l'individu à sa classe OWL existante (par exemple, Block SysML)
                    OWLClass typeClass = factory.getOWLClass(IRI.create(BASE_IRI + propertyType));
                    OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(typeClass, propInd);
                    manager.addAxiom(ontology, classAssertion);

                    System.out.println("Created individual " + propertyName + "_Instance of type " + propertyType);
                }
            }
           // transformCombinedFragmentsToOWL (interaction, ontology, manager, factory,BASE_IRI);
            transformMessagesToOWL(interaction, ontology, manager, factory, BASE_IRI);
            transformMessageOccurrencesToOWL(interaction, ontology, manager, factory, BASE_IRI);
            transformCoveredLifelinesFromMOS(interaction, ontology, manager, factory, BASE_IRI);
        }
    }

	public static void transformMessagesToOWL(Interaction interaction, OWLOntology ontology,  OWLOntologyManager manager,  OWLDataFactory factory, String BASE_IRI) {
	
	// 1. OWLClass de l'Interaction (le diagramme)
	String interactionName = interaction.getName() != null ? interaction.getName().replace(" ", "") : "UnnamedInteraction";
	OWLClass interactionClass = factory.getOWLClass(IRI.create(BASE_IRI + interactionName));
	manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(interactionClass));
	
	// 2. OWLClass abstraite "Messages"
	OWLClass messagesClass = factory.getOWLClass(IRI.create(BASE_IRI + "Messages"+interactionName));
	manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(messagesClass));
	OWLSubClassOfAxiom messagesSubInteraction = factory.getOWLSubClassOfAxiom(messagesClass, interactionClass);
	manager.addAxiom(ontology, messagesSubInteraction);
	
	// 3. Pour chaque message dans l’interaction
		for (Message message : interaction.getMessages()) {
			String msgName = message.getName() != null ? message.getName().replace(" ", "") : "UnnamedMessage";
			
			OWLClass messageClass = factory.getOWLClass(IRI.create(BASE_IRI + msgName));
			manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(messageClass));
			
			// 4. Chaque message est subclass de "Messages"
			OWLSubClassOfAxiom msgSubclassAxiom = factory.getOWLSubClassOfAxiom(messageClass, messagesClass);
			manager.addAxiom(ontology, msgSubclassAxiom);
			
			System.out.println("Created Message class: " + msgName + " subclass of Messages");
		}
	}


	public static void transformCombinedFragmentsToOWL(Interaction interaction, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {

		for (InteractionFragment fragment : interaction.getFragments()) {
			if (fragment instanceof CombinedFragment) {
					CombinedFragment combined = (CombinedFragment) fragment;
	
					// Crée une OWLClass pour le CombinedFragment
					String fragmentName = (combined.getName() != null) ? combined.getName().replace(" ", "") : "UnnamedCombinedFragment";
					OWLClass combinedClass = factory.getOWLClass(IRI.create(BASE_IRI + fragmentName));
					manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(combinedClass));
	
					// Identifier le conteneur parent pour déterminer le superclass
					Element container = combined.getOwner();
					OWLClass parentClass;
					
					if (container instanceof Interaction) {
					// Sous-classe de l'interaction directement
						parentClass = factory.getOWLClass(IRI.create(BASE_IRI + interaction.getName().replace(" ", "")));
						
						
					OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(combinedClass, parentClass);
					manager.addAxiom(ontology, subClassAxiom);
					
					System.out.println("Created OWLClass: " + fragmentName + " subclass of " + parentClass.getIRI().getShortForm());
					}
					
					// On parcourt tous les operands du combined fragment
		            int index = 1;
		            for (InteractionOperand operand : combined.getOperands()) {
	
		                // Nom de l'opérande, sinon généré
		                String operandName = operand.getName() != null ? operand.getName().replace(" ", "") : combined.getName() + "_Operand" + index;
	
		                // Créer OWLClass pour l'opérande
		                OWLClass operandClass = factory.getOWLClass(IRI.create(BASE_IRI + operandName));
		                manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(operandClass));
	
		                // Déterminer le OWLClass parent = le CombinedFragment
		                String combinedName = combined.getName() != null ? combined.getName().replace(" ", "") : "UnnamedCombinedFragment";
		                OWLClass parentOpClass = factory.getOWLClass(IRI.create(BASE_IRI + combinedName));
	
		                // Créer l'axiome subclassof
		                OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(operandClass, parentOpClass);
		                manager.addAxiom(ontology, subClassAxiom);
	
		                System.out.println("Created OWLClass: " + operandName + " subclass of " + combinedName);
		               
		                
		             // Parcours des fragments contenus dans l'opérande
		                for (InteractionFragment subFragment : operand.getFragments()) {
		                    if (subFragment instanceof CombinedFragment) {
		                        CombinedFragment nestedFragment = (CombinedFragment) subFragment;
	
		                        String nestedName = nestedFragment.getName() != null ? nestedFragment.getName().replace(" ", "") : "UnnamedNestedCombinedFragment_" + index;
	
		                        // Créer l'OWLClass pour le CombinedFragment imbriqué
		                        OWLClass nestedClass = factory.getOWLClass(IRI.create(BASE_IRI + nestedName));
		                        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(nestedClass));
	
		                        // Lier comme sous-classe de l'opérande
		                        OWLSubClassOfAxiom subClassAxiomOp = factory.getOWLSubClassOfAxiom(nestedClass, operandClass);
		                        manager.addAxiom(ontology, subClassAxiomOp);
	
		                        System.out.println("Created nested CombinedFragment: " + nestedName + " subclass of " + operandName);
		                    }
		                }
	
		                index++;
		            }
				}
				}
			}
			
	public static void transformMessageOccurrencesToOWL(Interaction interaction,
	            OWLOntology ontology,
	            OWLOntologyManager manager,
	            OWLDataFactory factory,
	            String BASE_IRI) {
	
		for (Message message : interaction.getMessages()) {
			String messageName = (message.getName() != null) ? message.getName().replace(" ", "") : "UnnamedMessage";
			OWLClass messageClass = factory.getOWLClass(IRI.create(BASE_IRI + messageName));
	
			// Traite les send et receive event s’ils sont des MessageOccurrenceSpecification
			for (MessageEnd end : List.of(message.getSendEvent(), message.getReceiveEvent())) {
			if (end instanceof MessageOccurrenceSpecification) {
			MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification) end;
			
			String mosName = (mos.getName() != null) ? mos.getName().replace(" ", "") : "UnnamedMOS_" + UUID.randomUUID();
			OWLClass mosClass = factory.getOWLClass(IRI.create(BASE_IRI + mosName));
			manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(mosClass));
			
			// Subclass of Message class
			OWLSubClassOfAxiom mosToMessage = factory.getOWLSubClassOfAxiom(mosClass, messageClass);
			manager.addAxiom(ontology, mosToMessage);
			
			// Recherche de l'InteractionOperand parent (si applicable)
			/*Element parent = mos.getOwner();
			while (parent != null && !(parent instanceof InteractionOperand)) {
			parent = parent.getOwner();
			}
			
			if (parent instanceof InteractionOperand) {
			InteractionOperand operand = (InteractionOperand) parent;
			String operandName = (operand.getName() != null) ? operand.getName().replace(" ", "") : "UnnamedOperand";
			OWLClass operandClass = factory.getOWLClass(IRI.create(BASE_IRI + operandName));
			manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(operandClass));
			
			OWLSubClassOfAxiom mosToOperand = factory.getOWLSubClassOfAxiom(mosClass, operandClass);
			manager.addAxiom(ontology, mosToOperand);
			}*/
			
			System.out.println("Created MessageOccurrenceSpecification OWLClass: " + mosName +
			" subclass of " + messageName );
			//(parent instanceof InteractionOperand ? " and operand" : ""));
	}
	}
	}
	}
	
			public static void transformCoveredLifelinesFromMOS(Interaction interaction,
		            OWLOntology ontology,
		            OWLOntologyManager manager,
		            OWLDataFactory factory,
		            String BASE_IRI) {
		
		for (Message message : interaction.getMessages()) {
		for (MessageEnd end : List.of(message.getSendEvent(), message.getReceiveEvent())) {
		if (end instanceof MessageOccurrenceSpecification) {
		MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification) end;
		
		String mosName = (mos.getName() != null) ? mos.getName().replace(" ", "") : "UnnamedMOS_" + UUID.randomUUID();
		OWLClass mosClass = factory.getOWLClass(IRI.create(BASE_IRI + mosName));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(mosClass));
		
		for (Lifeline lifeline : mos.getCovereds()) {
		String lifelineName = lifeline.getName().replace(" ", "");
		OWLNamedIndividual lifelineIndiv = factory.getOWLNamedIndividual(IRI.create(BASE_IRI + lifelineName));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(lifelineIndiv));
		
		// Créer une ObjectProperty pour CoveredBy
		String propertyName = mosName + "CoveredBy";
		OWLObjectProperty coveredByProp = factory.getOWLObjectProperty(IRI.create(BASE_IRI + propertyName));
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(coveredByProp));
		
		// Définir le domaine de la propriété
		OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(coveredByProp, mosClass);
		manager.addAxiom(ontology, domainAxiom);
		
		// Définir une assertion : l’individu du MOS est relié au lifeline
		//OWLNamedIndividual mosIndiv = factory.getOWLNamedIndividual(IRI.create(BASE_IRI + mosName + "_Indiv"));
		//manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(mosClass, mosIndiv));
		
		OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(
		coveredByProp, lifelineIndiv, lifelineIndiv);
		manager.addAxiom(ontology, assertion);
		
		System.out.println("CoveredBy assertion: "  + " → " + coveredByProp + " → " + lifelineIndiv);
		}
		}
		}
		}
		}





	

}