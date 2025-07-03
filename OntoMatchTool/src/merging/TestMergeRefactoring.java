package merging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;




import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLOntologyMerger;



import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

public class TestMergeRefactoring {
	
	public static void main(String[] args) throws URISyntaxException, AlignmentException, FileNotFoundException, OWLException {
		
		URI uri1 = null;
    	URI uri2 = null;
    	String u1 = "file:///C:/Users/RB275872/Desktop/NewTestMergingWithRefactoring/ontology1.owl";
    	String u2= "file:///C:/Users/RB275872/Desktop/NewTestMergingWithRefactoring/ontology2.owl";
    	String method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
    	Alignment al = null;
    	uri1 = new URI(u1);
    	uri2 = new URI(u2);
    	
	
    	Properties params = new Properties();

		// (Sol2) Match the ontologies with a local algorithm
		
		 
			  // Unfortunatelly no alignment was available
				  AlignmentProcess ap = new StringDistAlignment();
				  ap.init( uri1, uri2 );
				  params.setProperty("stringFunction","smoaDistance");
				  params.setProperty("noinst","1");
				  ap.align( al, params );
				  al = ap;
				  System.out.println(al.nbCells());  
		 
		
		File Align = new File("C:\\Users\\RB275872\\Desktop\\NewTestMergingWithRefactoring\\AlignmentResult.owl");
		  PrintWriter writer = new PrintWriter ( new FileOutputStream( Align), true );
		  AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
		  al.render(renderer);
		  writer.flush();
		  writer.close();
		  
		  /* merge the ontologies */
		  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		  OWLDataFactory datafactory = manager.getOWLDataFactory();
		  OWLOntology onto1= manager.loadOntologyFromOntologyDocument(IRI.create(uri1));
		  OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(IRI.create(uri2));
		  OWLOntology onto3 = manager.loadOntologyFromOntologyDocument(Align);
		  OWLOntologyMerger merger = new OWLOntologyMerger(manager);
		  String mergedOntologyIRI = "http://example.com/merged";
		  OWLOntology mergedOntology = merger.createMergedOntology(manager, IRI.create("http://example.com/merged")) ;
		  
		// save the merged ontology to a file
		  File merged=new File ("C:\\Users\\RB275872\\Desktop\\NewTestMergingWithRefactoring\\mergedResult.owl");
		  manager.saveOntology(mergedOntology,  new FileOutputStream(merged));
		  
		  /**********************************************************************************************/
		  
		  ArrayList<OWLOntology> ontologiesSet = new ArrayList<OWLOntology>();
		  ontologiesSet.add(mergedOntology);
		  String mergedOntologyIRI2 = "http://merging";
			OWLOntology mergedOntology2 = manager.createOntology(IRI.create(mergedOntologyIRI2));
			Set<OWLAxiom> mergedOntologyAxioms = new HashSet<OWLAxiom>();

			HashMap<String, String> hash_entitiesMergedNames = new HashMap<String, String>();
			HashMap<String, HashSet<String>> hash_mergedNamesEntities = new HashMap<String, HashSet<String>>();
			HashMap<String, String> classes = new HashMap<String, String>();
			HashMap<String, String> objectProps = new HashMap<String, String>();
			HashMap<String, String> dataProps = new HashMap<String, String>();
			HashMap<String, String> instances = new HashMap<String, String>();

			int ontologyCounter = 0;

			for(OWLOntology ontology_n : ontologiesSet){
				ontologyCounter++;
				String ontoCounter = String.valueOf(ontologyCounter);

				/** you can enter the first method and comment the parsing of disjointClasses axioms (which are the cause of future unsatisfiabilities in the merged ontology) */
				RefactorCreation.createAxiomsOfParsedClasses(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, classes);
				RefactorCreation.createAxiomsOfParsedObjectProperties(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, objectProps);
				RefactorCreation.createAxiomsOfParsedDataProperties(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, dataProps);
				RefactorCreation.createAxiomsOfParsedIndividuals(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, instances);

				RefactorCreation.createAxiomsOfParsedSubPropertyChainOfAxioms(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter);
				//createAxiomsOfParsedHasKeyAxioms(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI, ontoCounter);
				RefactorCreation.createAxiomsOfParsedNonBuiltInAnnotationProperties(mergedOntologyAxioms, ontology_n, datafactory);
				//createAxiomsOfParsedDataTypes(mergedOntologyAxioms, ontology_n, datafactory);
				RefactorCreation.createAxiomsOfParsedAnonymousIndividuals(mergedOntologyAxioms, datafactory, ontology_n);
			}

			System.out.println("\n==> Step 4 is done");

			/***********************************************************************************************************************************************/

			/** choose either "createBridgingAxiomsUsingOriginalAlignments" method, or
			 * "createBridgingAxiomsUsingFilteredAlignments" method. (if you uncomment one, comment the other, and vice versa).
			 */

			System.out.println("\n==> Step 5 is done");

			/***********************************************************************************************************************************************/
			manager.addAxioms(mergedOntology2, mergedOntologyAxioms);
			manager.saveOntology(mergedOntology2, IRI.create(new File("C:\\Users\\RB275872\\Desktop\\NewTestMergingWithRefactoring\\mergedResult222.owl")));
		  
		  
	}
}
