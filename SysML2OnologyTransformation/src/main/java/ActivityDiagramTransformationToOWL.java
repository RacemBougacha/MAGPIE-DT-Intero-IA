import java.util.List;

import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.FinalNode;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.JoinNode;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class ActivityDiagramTransformationToOWL {
	
	  /**
     * Transforme toutes les activités SysML en OWL et les associe à leur élément parent
     */
    public static void transformActivityDiagramsToOWL(
            Package sysmlModel, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        
        List<Activity> activities = SysMLModelLoader.extractActivityDiagrams(sysmlModel);

        for (Activity activity : activities) {
        	System.out.println(activity.getName());
            Element parentElement = activity.getOwner();
            if (parentElement instanceof NamedElement) {
            	 // Création d'une classe OWL pour l'élément parent SysML
                OWLClass parentOwlClass = factory.getOWLClass(IRI.create(BASE_IRI + ((NamedElement) parentElement).getName().replace(" ", "")));
                manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(parentOwlClass));
                
                // Création d'une classe OWL pour l'Activity
                OWLClass activityClass = factory.getOWLClass(IRI.create(BASE_IRI + activity.getName().replace(" ", "")));
                manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(activityClass));
                
                // Définition de la relation de sous-classe (Activity est une sous-classe de son parent)
                OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(activityClass, parentOwlClass);
                manager.addAxiom(ontology, subclassAxiom);

                System.out.println("Created OWL Class for Activity: " + activity.getName() + " (subClassOf " + ((NamedElement) parentElement).getName() + ")");
                
             // Transformer les sous-activités (appelées depuis cette activité)
                for (ActivityNode node : activity.getNodes()) {
                    if (node instanceof CallBehaviorAction) {
                        Behavior calledBehavior = ((CallBehaviorAction) node).getBehavior();
                        if (calledBehavior instanceof Activity) {
                        	 // Création d'une classe OWL pour la sous-activité
                            OWLClass subActivityClass = factory.getOWLClass(IRI.create(BASE_IRI + calledBehavior.getName().replace(" ", "")));
                            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(subActivityClass));

                            // Relation subActivity -> Activity
                            OWLSubClassOfAxiom subclassAxiomsubAct = factory.getOWLSubClassOfAxiom(subActivityClass, activityClass);
                            manager.addAxiom(ontology, subclassAxiomsubAct);

                            System.out.println("Created OWL Class for SubActivity: " + calledBehavior.getName() + " (subClassOf " + activityClass.getIRI().getShortForm() + ")");
                        }
                        
                    }
                }
                
                // transforme les Initial & Final node - JoinNode & ForkNode - DecisionNode
                for (ActivityNode node : activity.getNodes()) {
                    if (node instanceof InitialNode || node instanceof FinalNode || node instanceof ForkNode || node instanceof JoinNode || node instanceof DecisionNode) {
                        String nodeName = node.getName() != null ? node.getName().replace(" ", "") : node instanceof InitialNode ? "InitialNode" : node instanceof FinalNode ? "FinalNode" :
                                          node instanceof ForkNode ? "ForkNode" : node instanceof ForkNode ? "JoinNode" : "DecisionNode";
                        OWLClass nodeClass = factory.getOWLClass(IRI.create(BASE_IRI + nodeName));

                        // Déclaration de l'OWLClass pour le nœud
                        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(nodeClass));

                        // Définition de la relation SubClassOf entre le nœud et l'Activity
                        createSubclass(node, activityClass, ontology, manager, factory, BASE_IRI);
                    }
                
            }
             // Process Actions inside the Activity
                for (ActivityNode node : activity.getOwnedNodes()) {
                    if (node instanceof Action) {
                    	OWLClass actionClass = createSubclass(node, activityClass, ontology, manager, factory, BASE_IRI);
                    	 // Process InputPins
                        for (InputPin inputPin : ((Action)node).getInputs()) {
                            createSubclass(inputPin, actionClass, ontology, manager, factory, BASE_IRI);
                        }

                        // Process OutputPins
                        for (OutputPin outputPin : ((Action)node).getOutputs()) {
                            createSubclass(outputPin, actionClass, ontology, manager, factory, BASE_IRI);
                        }
                    }
                }

            
         // Process Activity Parameter Nodes
            for (ActivityNode node : activity.getOwnedNodes()) {
                if (node instanceof ActivityParameterNode) {
                    createSubclass(node, activityClass, ontology, manager, factory, BASE_IRI);
                }
                              
                }
            
            
            /**
             * Transforme les flux entre activités, actions, paramètres et pins en OWL.
             */
            for (ActivityEdge edge : activity.getEdges()) {
                if (edge instanceof ControlFlow || edge instanceof ObjectFlow) {
                	 // Get flow name, or generate one if missing
                    String flowName = (edge.getName() != null) ? edge.getName().replace(" ", "") : "UnnamedFlow";

                    // Extract source and target nodes
                    ActivityNode sourceNode = edge.getSource();
                    ActivityNode targetNode = edge.getTarget();

                    if (sourceNode == null || targetNode == null) {
                        System.out.println("Skipping flow: " + flowName + " (missing source or target)");
                        return;
                    }

                    // Create OWLObjectProperty
                    OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(BASE_IRI + flowName));
                    manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(objectProperty));

                    // Define domain and range
                    OWLClass sourceClass = factory.getOWLClass(IRI.create(BASE_IRI + sourceNode.getName().replace(" ", "")));
                    OWLClass targetClass = factory.getOWLClass(IRI.create(BASE_IRI + targetNode.getName().replace(" ", "")));

                    OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(objectProperty, sourceClass);
                    OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(objectProperty, targetClass);

                    manager.addAxiom(ontology, domainAxiom);
                    manager.addAxiom(ontology, rangeAxiom);

                    System.out.println("Created ObjectProperty: " + flowName + " (" + sourceNode.getName() + " → " + targetNode.getName() + ")");
                }
        }
            
            /**
             * Convertit les paramètres d'une activité SysML en individus OWL et les associe à un OWLClass existant.
             */
            for (Parameter parameter : activity.getOwnedParameters()) {
            String paramName = parameter.getName() != null ? parameter.getName().replace(" ", "") : "UnnamedParameter";
            OWLNamedIndividual paramIndividual = factory.getOWLNamedIndividual(IRI.create(BASE_IRI + paramName));

            // Vérifier si le type du paramètre existe déjà comme OWLClass
            if (parameter.getType() != null) {
                OWLClass paramTypeClass = factory.getOWLClass(IRI.create(BASE_IRI + parameter.getType().getName().replace(" ", "")));

                // Déclaration de l'individu comme instance de l'OWLClass du paramètre
                OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(paramTypeClass, paramIndividual);
                manager.addAxiom(ontology, classAssertion);
            }

            }
            }
        }
        
    }
    
    /**
     * Converts an Activity Parameter Node or an Action into an OWLClass and links it as a subclass of an Activity.
     */
    private static OWLClass createSubclass(
            NamedElement element, OWLClass parentClass, OWLOntology ontology, 
            OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        
        String elementName = (element.getName() != null) ? element.getName().replace(" ", "") : "UnnamedElement";
        OWLClass nodeClass = factory.getOWLClass(IRI.create(BASE_IRI + elementName));

        // Declare the OWL Class
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(nodeClass));

        // Define SubClassOf relationship
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(nodeClass, parentClass);
        manager.addAxiom(ontology, subclassAxiom);

        System.out.println("Created subclass: " + elementName + " ⊆ " + parentClass);
        return nodeClass;
    }
    
    


    

}





