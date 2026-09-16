package merging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.w3c.dom.events.EventException;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.method.ClassStructAlignment;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import fr.inrialpes.exmo.align.impl.method.NameEqAlignment;
import fr.inrialpes.exmo.align.impl.method.SMOANameAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment;
import fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.ling.JWNLAlignment;
import fr.inrialpes.exmo.ontosim.string.JWNLDistances;

public class AlignMergeRefactorinWithMultiOnto {
	
	public int f = 0;
	Alignment al = null;
	public File AlignmentFunction(URI uri1, URI uri2) throws AlignmentException, FileNotFoundException, UnsupportedEncodingException {
		System.out.println("Step 1 of the process start");
		System.out.println("Alignment step between ontology 1: "+uri1+" and ontology 2: "+uri2+" start");
		Properties params = new Properties();

		/** 1. Exact name matching **/
		NameEqAlignment neq = new NameEqAlignment();
		neq.init(uri1, uri2);
		neq.align(al, params);
		al = neq;
		System.out.println("After NameEq: " + neq.nbCells());
		
		/** 2. Structural comparison **/
		ClassStructAlignment cs = new ClassStructAlignment();
		cs.init(uri1, uri2);
		cs.align(al, params);
		al = cs;
		System.out.println("After ClassStruct: " + cs.nbCells());
		
		/** 3. Edit-distance similarity **/
		EditDistNameAlignment editdist = new EditDistNameAlignment();
		editdist.init(uri1, uri2);
		editdist.align(al, params);
		al = editdist;
		System.out.println("After EditDist: " + editdist.nbCells());
		
		/** 4. Substring matching **/
		SubsDistNameAlignment subsedit = new SubsDistNameAlignment();
		subsedit.init(uri1, uri2);
		subsedit.align(al, params);
		al = subsedit;
		System.out.println("After Substring: " + subsedit.nbCells());
		
		/** 5. String-based (SMOA), seuil 0.85 — tel que décrit dans le papier **/
		AlignmentProcess ap = new StringDistAlignment();
		ap.init(uri1, uri2);
		params.setProperty("stringFunction", "smoaDistance");
		params.setProperty("noinst", "1");
		ap.align(al, params);
		ap.cut(0.85);
		al = ap;
		System.out.println("After SMOA (cut 0.85): " + al.nbCells());
		
		/** 6. WordNet — nécessite un dictionnaire local, chemin à paramétrer (voir plus bas) **/
		JWNLAlignment ap2 = new JWNLAlignment();
		ap2.init(uri1, uri2);
		params.setProperty("wndict", WORDNET_DICT_PATH); // variable, pas de chemin en dur
		params.setProperty("wnfunction", "cosynonymySimilarity");
		ap2.align(al, params);
		al = ap2;
		System.out.println("After WordNet: " + al.nbCells());
		
		/** Alignment ontology creation **/
			 
		File Align = new File("C:\\Users\\RB275872\\Desktop\\IATOOL\\alignResult.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\AlignmentResult.owl");
		PrintWriter writer = new PrintWriter ( new FileOutputStream( Align), true );
		AlignmentVisitor renderer = new  OWLAxiomsRendererVisitor(writer);
		al.render(renderer);
		writer.flush();
		writer.close();
		
		/** Alignment metadata creation **/
		File AligMesur = new File("C:\\Users\\RB275872\\Desktop\\IATOOL\\alignMesur.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\AlignmentResultMesurement"+f+".owl");
		PrintWriter writerMesur = new PrintWriter ( new FileOutputStream( AligMesur), true );
		AlignmentVisitor rendererMesur = new RDFRendererVisitor(writerMesur);
		al.render(rendererMesur);
		writerMesur.flush();
		writerMesur.close();
		f++;
		
		  System.out.println("Step 1 of the process finish");
		  System.out.println("Alignment step between ontology 1: "+uri1+" and ontology 2: "+uri2+" finish");
		  
		  return Align;
		  
	}
	
	
	public File MergeFunction(ArrayList<URI> ontologiesSet, File alignFile, OWLOntologyManager manager) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException  {
		
		
		 for(int g=0; g < ontologiesSet.size(); g++){
			 try
			 {manager.loadOntologyFromOntologyDocument(IRI.create(ontologiesSet.get(g)));}
			 catch (OWLOntologyAlreadyExistsException e) {System.out.println("***");}
			 
		 }
		 try
		 {
		 manager.loadOntologyFromOntologyDocument(alignFile);}
		 catch (OWLOntologyAlreadyExistsException e) {System.out.println("***");}
		 
		 System.out.println("Step 2 of the process start");
		 System.out.println("Merge step between ontology ontologies: "+manager.getOntologies().toString()+" start"); 

		 /** Merged Ontology creation **/
		 OWLOntologyMerger merger = new OWLOntologyMerger(manager);
		 String mergedOntologyIRI = "http://example.com/merged";
		  
		  if(manager.getOntology(IRI.create("http://example.com/merged")) != null)
		  {manager.removeOntology(manager.getOntology(IRI.create("http://example.com/merged"))); }
		
		  OWLOntology mergedOntology = merger.createMergedOntology(manager, IRI.create("http://example.com/merged")) ;
		  
		
		  File merged=new File ("C:\\Users\\RB275872\\Desktop\\IATOOL\\mergedResult.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\mergedResult.owl");
		  manager.saveOntology(mergedOntology,  new FileOutputStream(merged));
		  
		  System.out.println("Step 2 of the process finish");
		  System.out.println("Merge step between ontology ontologies: "+manager.getOntologies().toString()+" finish"); 
		  return merged;
	}
	
	public File refactorFile(ArrayList<URI> ontologiesSet, File mergedFile, OWLOntologyManager manager) throws OWLException {
		
		System.out.println("Step 3 of the process start");
		System.out.println("Refactoring step of ontologies: "+manager.getOntologies().toString()+" start"); 
		
		OWLDataFactory datafactory = manager.getOWLDataFactory();
		if(manager.getOntology(IRI.create("http://RefactorMerging")) != null)
		 {manager.removeOntology(manager.getOntology(IRI.create("http://RefactorMerging"))); }
		
		/** Refactoring ontology creation **/
		String mergedOntologyIRI2 = "http://RefactorMerging";
		OWLOntology mergedRefactOntology2 = manager.createOntology(IRI.create(mergedOntologyIRI2));
		Set<OWLAxiom> mergedOntologyAxioms = new HashSet<OWLAxiom>();
		HashMap<String, String> hash_entitiesMergedNames = new HashMap<String, String>();
		HashMap<String, HashSet<String>> hash_mergedNamesEntities = new HashMap<String, HashSet<String>>();
		HashMap<String, String> classes = new HashMap<String, String>();
		HashMap<String, String> objectProps = new HashMap<String, String>();
		HashMap<String, String> dataProps = new HashMap<String, String>();
		HashMap<String, String> instances = new HashMap<String, String>();
		ArrayList<OWLOntology> SetOfontologies = new ArrayList<OWLOntology>();
			 
		SetOfontologies.add(manager.loadOntologyFromOntologyDocument(mergedFile));
		int ontologyCounter = 0;

		for(OWLOntology ontology_n : SetOfontologies){
		ontologyCounter++;
		String ontoCounter = String.valueOf(ontologyCounter);

		/** you can enter the first method and comment the parsing of disjointClasses axioms (which are the cause of future unsatisfiabilities in the merged ontology) */
		/** classes refactoring **/
		RefactorCreation.createAxiomsOfParsedClasses(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, classes);
		/** object properties refactoring **/
		RefactorCreation.createAxiomsOfParsedObjectProperties(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, objectProps);
		/** data properties refactoring **/
		RefactorCreation.createAxiomsOfParsedDataProperties(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, dataProps);
		/** individuals refactoring **/		
		RefactorCreation.createAxiomsOfParsedIndividuals(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter, instances);
		/** Parsed SubProperty Chain Of Axioms **/
		RefactorCreation.createAxiomsOfParsedSubPropertyChainOfAxioms(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI2, ontoCounter);
				//createAxiomsOfParsedHasKeyAxioms(hash_entitiesMergedNames, datafactory, ontology_n, mergedOntologyAxioms, mergedOntologyIRI, ontoCounter);
		/**	Parsed Non Built In Annotation Properties **/
		RefactorCreation.createAxiomsOfParsedNonBuiltInAnnotationProperties(mergedOntologyAxioms, ontology_n, datafactory);
				//createAxiomsOfParsedDataTypes(mergedOntologyAxioms, ontology_n, datafactory);
		/** Parsed Anonymous Individuals **/	
		RefactorCreation.createAxiomsOfParsedAnonymousIndividuals(mergedOntologyAxioms, datafactory, ontology_n);
				
				
			}

			
		manager.addAxioms(mergedRefactOntology2, mergedOntologyAxioms);
			
		File RefactorFile = new File("C:\\Users\\RB275872\\Desktop\\IATOOL\\RefactorResult.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\RefactorResult.owl");
		manager.saveOntology(mergedRefactOntology2, IRI.create(RefactorFile));
			
		System.out.println("Step 3 of the process finish");
		System.out.println("Refactoring step of ontologies: "+manager.getOntologies().toString()+" finish"); 
		  
			return RefactorFile;
	}
	
	
	
	public static void main(String[] args) throws URISyntaxException, AlignmentException, OWLException, IOException  {
		
		URI uri1 = null;
    	URI uri2 = null;
    	URI uri3 = null;
    	URI uri4 = null;
    	URI uri5 = null;
    	URI uri6 = null;
    	URI uri7 = null;
    	URI uri8 = null;
    	URI uri9=null;
    	String method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
    	
    	/** Inputs Ontologies **/
    	String u1 = "file:///C:/Users/RB275872/Desktop/IATOOL/SysML111Ontology.owl";
    	String u2= "file:///C:/Users/RB275872/Desktop/IATOOL/SysML222Ontology.owl";
    	/*String u1 = "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-master/Event.ttl";
    	String u2= "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-master/DigitalTwin.ttl";
    	String u3= "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-master/Classifications.ttl";
    	String u4 = "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-1.0.1-alpha/BusinessService.ttl";
    	String u5= "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-1.0.1-alpha/PhysicalInfrastructure.ttl";
    	String u6= "file:///C:/Users/RB275872/Desktop/MyWorkAPI/FEDeRATED-Semantic-Model-master/LegalPerson.ttl";*/
    	
    	uri1 = new URI(u1);
    	uri2 = new URI(u2);
    	/*uri3 = new URI(u3);
    	uri4 = new URI(u4);
    	uri5 = new URI(u5);
    	uri6 = new URI(u6);*/
    
    	/** List of ontologies initialisation **/
		ArrayList<URI> ontologiesSet = new ArrayList<URI>();
		
		ontologiesSet.add(uri1);
		ontologiesSet.add(uri2);
		/*ontologiesSet.add(uri3);
		ontologiesSet.add(uri4);
		ontologiesSet.add(uri5);
		ontologiesSet.add(uri6);*/
		
		
		/** Alignment and merge without refactoring instanciation**/
		AlignMergeRefactorinWithMultiOnto amNoRefactwmo = new AlignMergeRefactorinWithMultiOnto();

		File alignFile = new File("C:\\Users\\RB275872\\Desktop\\IATOOL\\AlignmentResult.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\AlignmentResult.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		int size = ontologiesSet.size();
		
		for(int g=0; g < size; g++){
			try {
				
			//manager.getOntologies().forEach(e ->{manager.removeOntology(e);});
				
			//Alignment 
			alignFile = amNoRefactwmo.AlignmentFunction(ontologiesSet.get(g), ontologiesSet.get(g+1));
			
			//Merge
			File mergedFile = amNoRefactwmo.MergeFunction(ontologiesSet, alignFile, manager);
			manager.removeOntology(manager.getOntology(IRI.create("http://example.com/merged")));
			ontologiesSet.add(mergedFile.toURI());
			}catch (IndexOutOfBoundsException e)
			{
				break;
			}
		
		
		/** Alignment and merge with refactoring instanciation**/
	/*	AlignMergeRefactorinWithMultiOnto amRefactwmo = new AlignMergeRefactorinWithMultiOnto();

		File alignFile = new File("C:\\Users\\RB275872\\Desktop\\IATOOL\\AlignmentResult.owl");//"C:\\Users\\RB275872\\Desktop\\Federated Ontology Alignment\\FederatedOwl\\AlignmentResult.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		int size = ontologiesSet.size();
		
		for(int g=0; g < size; g++){
			try {
			//Alignment 
			alignFile = amRefactwmo.AlignmentFunction(ontologiesSet.get(g), ontologiesSet.get(g+1));
			
			//Merge
			File mergedFile = amRefactwmo.MergeFunction(ontologiesSet, alignFile, manager);
			manager.removeOntology(manager.getOntology(IRI.create("http://example.com/merged")));
			
			//Refactoring
			File refactorFile = amRefactwmo.refactorFile(ontologiesSet, mergedFile, manager);
			manager.removeOntology(manager.getOntology(IRI.create("http://RefactorMerging")));
			ontologiesSet.add(refactorFile.toURI());
			}catch (IndexOutOfBoundsException e)
			{
				break;
			}*/
		}
	
	}
	
		
	

}
