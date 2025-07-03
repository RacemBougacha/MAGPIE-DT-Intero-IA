import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class StateMachineDiagramTransformationToOWL {
	

    /**
     * Transformation des State Machines en OWL
     */
    public static void transformStateMachinesToOWL(Package sysmlModel, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
    	// Map pour stocker la correspondance entre SysML States et OWL Classes
         Map<State, OWLClass> stateToOwlClassMap = new HashMap<>();

        /**
         * Transformation des State Machines en OWL, en associant chaque machine d'état à son élément parent SysML
         */
            List<StateMachine> stateMachines = SysMLModelLoader.extractStateMachines(sysmlModel);

            for (StateMachine stateMachine : stateMachines) {
                Element parentElement = stateMachine.getOwner(); // Récupère l'élément SysML contenant la machine d'état
                if (parentElement != null && parentElement instanceof NamedElement) {
                    transformStateMachineToOWL((NamedElement) parentElement, stateMachine, ontology, manager, factory, BASE_IRI, stateToOwlClassMap);
                }
            }
        }

        /**
         * Convertit une machine d'état SysML en OWL en associant son parent
         */
        private static void transformStateMachineToOWL(NamedElement parentElement, StateMachine stateMachine, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI,  Map<State, OWLClass> stateToOwlClassMap) {
            
            // Création de l'OWLClass pour l'élément SysML parent (ex: un bloc contenant le State Machine)
            OWLClass parentOwlClass = factory.getOWLClass(IRI.create(BASE_IRI + parentElement.getName().replace(" ", "")));
            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(parentOwlClass));

            // Création de l'OWLClass pour la State Machine et lier au parent (subClassOf)
            OWLClass stateMachineClass = factory.getOWLClass(IRI.create(BASE_IRI + stateMachine.getName().replace(" ", "")));
            manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(stateMachineClass));
            manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(stateMachineClass, parentOwlClass));

            System.out.println("Created OWL Class for State Machine: " + stateMachine.getName() + " (subClassOf " + parentElement.getName() + ")");

            // Parcourir toutes les régions de la machine d'état
            
            for (Region region : stateMachine.getRegions()) {
            	List<Transition> listTransition=region.getTransitions(); 
                // Transformer les états en sous-classes OWL de la State Machine
                for (Vertex vertex : region.getSubvertices()) {
                    if (vertex instanceof State) {
                        State state = (State) vertex;
                        OWLClass stateClass = factory.getOWLClass(IRI.create(BASE_IRI + state.getName().replace(" ", "")));
                        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(stateClass));
                        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(stateClass, stateMachineClass));

                        stateToOwlClassMap.put(state, stateClass);
                        System.out.println("Created OWL Class for State: " + state.getName() + " (subClassOf " + stateMachine.getName() + ")");
                        
                        
                        for (Region subregion : state.getRegions()) {

                            // Transformer les états en sous-classes OWL de la State Machine
                            for (Vertex subvertex : subregion.getSubvertices()) {
                                if (subvertex instanceof State) {
                                    State substate = (State) subvertex;
                                    OWLClass substateClass = factory.getOWLClass(IRI.create(BASE_IRI + substate.getName().replace(" ", "")));
                                    manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(substateClass));
                                    manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(substateClass, stateClass));

                                    stateToOwlClassMap.put(substate, substateClass);
                                    System.out.println("Created OWL Class for State: " + substate.getName() + " (subClassOf " + state.getName() + ")");
                                    listTransition.addAll(subregion.getTransitions());
                                }
            
                            }}}}
                
               
                // Transformer les transitions en propriétés OWL
                for (Transition transition : listTransition) {
                    createObjectPropertyForTransition(transition, ontology, manager, factory, BASE_IRI, stateToOwlClassMap);
                }
            }
        }

        /**
         * Convertit une transition SysML en Object Property OWL
         */
        private static void createObjectPropertyForTransition(Transition transition, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI,  Map<State, OWLClass> stateToOwlClassMap) {
            Vertex source = transition.getSource();
            Vertex target = transition.getTarget();

            if (source instanceof State && target instanceof State) {
                State sourceState = (State) source;
                State targetState = (State) target;

                if (stateToOwlClassMap.containsKey(sourceState) && stateToOwlClassMap.containsKey(targetState)) {
                    OWLClass sourceClass = stateToOwlClassMap.get(sourceState);
                    OWLClass targetClass = stateToOwlClassMap.get(targetState);
                    if(!(transition.getName()== null ))
                    { String propertyName = transition.getName();
                    
                    OWLObjectProperty transitionProperty = factory.getOWLObjectProperty(IRI.create(BASE_IRI + propertyName.replace(" ", "")));

                    OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(transitionProperty, sourceClass);
                    OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(transitionProperty, targetClass);

                    manager.addAxiom(ontology, domainAxiom);
                    manager.addAxiom(ontology, rangeAxiom);

                    System.out.println("Created OWL ObjectProperty: " + propertyName + " from " + sourceState.getName() + " to " + targetState.getName());}
                }
            }
        }

}
