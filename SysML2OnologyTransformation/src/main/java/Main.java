import java.io.File;


import org.eclipse.uml2.uml.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class Main {


	private static final String BASE_IRI = "http://example.com/sysml2owl#";
	
	 
	// Load SysML model
    static Model sysmlModel = SysMLModelLoader.loadSysMLModel("C:\\Users\\RB275872\\Desktop\\SC papers\\IA MBSE DT Paper\\Sys1SeqDiagram.uml");//"C:\\Users\\RB275872\\Desktop\\Landing Gear System.uml");//"C:\\Users\\RB275872\\Desktop\\WorkspaceGENVIA\\SystemeDeProductionGenvia\\SystemeDeProductionGenvia.uml");//"C:\\Users\\RB275872\\Desktop\\Landing Gear System.uml");
    
    // Initialize OWL API
    public static OWLOntology ontology=null;
    static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    static OWLDataFactory factory = manager.getOWLDataFactory(); 
    
    public static void initialiseOntology(String iri) {
    	IRI ontologyIRI = IRI.create(iri);
    	try {
    		ontology = manager.createOntology(ontologyIRI);
    	} catch (OWLOntologyCreationException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
	  // Save the OWL ontology to a file
    public static void saveOntology(OWLOntology ontology ) {
    	File outputFile = new File("C:\\Users\\RB275872\\Desktop\\\\SC papers\\\\IA MBSE DT Paper\\\\SysML111Ontology.owl");
    	try {
    		manager.saveOntology(ontology, IRI.create(outputFile.toURI()));
    	} catch (OWLOntologyStorageException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	System.out.println("OWL Ontology created: " + outputFile.getAbsolutePath());
    }
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SysML2OnologyTransformation m = new SysML2OnologyTransformation();
		try {
			System.out.print(sysmlModel.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initialiseOntology(BASE_IRI);
		PackageBlockDiagramtransformationToOWL.transformPackageToOWLClass(sysmlModel, ontology, manager, factory, BASE_IRI);
		//m.transformRelationshipsToOWL(sysmlModel, ontology, manager, factory, BASE_IRI);
		StateMachineDiagramTransformationToOWL.transformStateMachinesToOWL(sysmlModel, ontology, manager, factory, BASE_IRI);
		ActivityDiagramTransformationToOWL.transformActivityDiagramsToOWL(sysmlModel, ontology, manager, factory, BASE_IRI);
		SequenceDiagramTransformationToOWL.transformSequenceToOWL(sysmlModel, ontology, manager, factory, BASE_IRI);
		
		saveOntology(ontology);
	}
}
