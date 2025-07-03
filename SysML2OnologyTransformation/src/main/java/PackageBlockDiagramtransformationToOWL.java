import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class PackageBlockDiagramtransformationToOWL {

public static void transformPackageToOWLClass(Model model, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        

        // Stocker les OWL Classes des Packages pour la relation SubClassOf
        Map<String, OWLClass> packageMap = new HashMap<>();
    	
    	// Transform SysML Blocks to OWL Classes
        List<Package> packages = SysMLModelLoader.extractSysMLPackage(model);
        for (Package pck : packages) {  	
        	 String packageName = pck.getName().replace(" ", "");
             OWLClass packageOWL = factory.getOWLClass(IRI.create(BASE_IRI + packageName));

             OWLDeclarationAxiom classDeclaration = factory.getOWLDeclarationAxiom(packageOWL);
             manager.addAxiom(ontology, classDeclaration);

             packageMap.put(packageName, packageOWL); // Stocker pour les relations SubClassOf
         }

        

                 // Extraire les packages (folders) du modèle SysML
                 for (Package sysmlPackage : packages) {
                     // Créer une classe OWL pour le package
                     OWLClass packageClass = factory.getOWLClass(IRI.create(BASE_IRI + sysmlPackage.getName().replace(" ", "")));
                     OWLDeclarationAxiom classDeclaration = factory.getOWLDeclarationAxiom(packageClass);
                     manager.addAxiom(ontology, classDeclaration);
                     System.out.println("OWLClass créée pour le package : " + sysmlPackage.getName());


                     // Vérifier si le package a un parent et établir la relation SubClassOf
                     if (sysmlPackage.getOwner() instanceof Package) {
                         Package parentPackage = (Package) sysmlPackage.getOwner();
                         OWLClass parentClass = packageMap.get(parentPackage.getName());
                         if (parentClass != null) {
                             OWLSubClassOfAxiom subPackageAxiom = factory.getOWLSubClassOfAxiom(packageClass, parentClass);
                             manager.addAxiom(ontology, subPackageAxiom);
                             System.out.println("OWLClass " + sysmlPackage.getName() + " est une sous-classe de " + parentPackage.getName());
                         }
                     }

                    
                     // Ajouter les Blocks et les relier aux Packages
                     transformBlockToOWLClass(model, ontology, manager, factory, BASE_IRI, packageMap);
                     
                 }
         

    }
    
 public static void transformBlockToOWLClass(Model model, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI, Map<String, OWLClass> packageMap) {
	    
	    // Extraire les Blocks depuis le modèle SysML
	    List<Class> classes = SysMLModelLoader.extractSysMLBlocks(model);
	    for (Class cl : classes) {  
	        String className = cl.getName().replace(" ", "");
	        System.out.println("XXXXXXXXXXX"+className);
	        OWLClass classOWL = factory.getOWLClass(IRI.create(BASE_IRI + className));

	        OWLDeclarationAxiom classDeclaration = factory.getOWLDeclarationAxiom(classOWL);
	        manager.addAxiom(ontology, classDeclaration);

	        // Vérifier si le Block appartient à un Package
	        if (cl.getPackage() != null) {
	            String packageName = cl.getPackage().getName().replace(" ", "");
	            if (packageMap.containsKey(packageName)) {
	                OWLClass packageOWL = packageMap.get(packageName);
	                OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(classOWL, packageOWL);
	                manager.addAxiom(ontology, subClassAxiom);

	                System.out.println("Added SubClassOf: " + className + " ⊑ " + packageName);
	            }
	        }
	        
	        transformRelationshipsToOWL(model, ontology, manager, factory, BASE_IRI);
	    }
	}
   
    public static void transformRelationshipsToOWL(
            Package sysmlModel, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        
        // Transform associations (Adding Domain and Range)
        for (Association association : SysMLModelLoader.extractAssociations(sysmlModel)) {
            if (association.getMemberEnds().size() >= 2 && association.getName()!=null) {
                Property source = association.getMemberEnds().get(0);
                Property target = association.getMemberEnds().get(1);
                if (!(association == null)){
                	System.out.println("houda");
                if(!(source.getType()==null)) {
                	System.out.println("racem");
                System.out.println(source.getName());
                	if(!(target.getType() ==null)) {
                		System.out.println("aaaaaaaaa");
                createObjectProperty(association.getName(), source.getType().getName(), target.getType().getName(), ontology, manager, factory, BASE_IRI);
                	}}}
                }
            
            // 🔹 Transformation des agrégations et compositions en SubClassOf
         for (Association aggregation : SysMLModelLoader.extractAggregations(sysmlModel)) {
                if (aggregation.getMemberEnds().size() >= 2) {
                    Property whole = aggregation.getMemberEnds().get(1); // L'élément "global"
                    Property part = aggregation.getMemberEnds().get(0);  // L'élément "composant"

                    if (whole.getType() != null && part.getType() != null) {
                        System.out.println("Processing Aggregation: " + whole.getType().getName() + " ⊑ " + part.getType().getName());
                        createSubClassRelation(part.getType().getName(), whole.getType().getName(), ontology, manager, factory, BASE_IRI);
                    }
                }
            }

            for (Association composition : SysMLModelLoader.extractCompositions(sysmlModel)) {
                if (composition.getMemberEnds().size() >= 2) {
                    Property whole = composition.getMemberEnds().get(1);
                    Property part = composition.getMemberEnds().get(0);

                    if (whole.getType() != null && part.getType() != null) {
                        System.out.println("Processing Composition: " + whole.getType().getName() + " ⊑ " + part.getType().getName());
                        createSubClassRelation(part.getType().getName(), whole.getType().getName(), ontology, manager, factory, BASE_IRI);
                    }
                }
            }
        }

        // Transform generalizations
        for (Generalization generalization : SysMLModelLoader.extractGeneralizations(sysmlModel)) {
            String subClass = generalization.getSpecific().getName().replace(" ", "");
            String superClass = generalization.getGeneral().getName().replace(" ", "");
            OWLClass owlSubClass = factory.getOWLClass(IRI.create(BASE_IRI + subClass));
            OWLClass owlSuperClass = factory.getOWLClass(IRI.create(BASE_IRI + superClass));

            OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(owlSubClass, owlSuperClass);
            manager.addAxiom(ontology, subClassAxiom);
        }

        // Transform dependencies
       for (Dependency dependency : SysMLModelLoader.extractDependencies(sysmlModel)) {
            if (!dependency.getSuppliers().isEmpty()) {
                String client = dependency.getClients().get(0).getName();
                String supplier = dependency.getSuppliers().get(0).getName();
                createObjectProperty(client + "DependsOn" + supplier, client, supplier, ontology, manager, factory, BASE_IRI);
            }
        }

        // Transform connectors
        for (Connector connector : SysMLModelLoader.extractConnectors(sysmlModel)) { 
            if (connector.getEnds().size() >= 2) {
                ConnectableElement source = connector.getEnds().get(0).getRole();
                ConnectableElement target = connector.getEnds().get(1).getRole();
                createObjectProperty("Connect"+connector.getName(), source.getName(), target.getName(), ontology, manager, factory, BASE_IRI);
            }
        }
    }
    private static void createSubClassRelation(String subclass, String superclass, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
        OWLClass subclassOWL = factory.getOWLClass(IRI.create(BASE_IRI + subclass.replace(" ", "")));
        OWLClass superclassOWL = factory.getOWLClass(IRI.create(BASE_IRI + superclass.replace(" ", "")));

        OWLSubClassOfAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(subclassOWL, superclassOWL);
        manager.addAxiom(ontology, subClassAxiom);

        System.out.println("Added SubClassOf: " + subclass + " ⊑ " + superclass);
    }
    /**
     * Create an OWL Object Property with Domain and Range.
     */
    public static void createObjectProperty(
            String propertyName, String domainName, String rangeName, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory factory, String BASE_IRI) {
    	System.out.println("bbbb");
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create((BASE_IRI + propertyName).replace(" ", "")));
        System.out.println("cccc");
        OWLClass domainClass = factory.getOWLClass(IRI.create(BASE_IRI + domainName.replace(" ", "")));
        OWLClass rangeClass = factory.getOWLClass(IRI.create(BASE_IRI + rangeName.replace(" ", "")));
       
        // Define Domain and Range
        OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(objectProperty, domainClass);
        OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(objectProperty, rangeClass);

        // Add Axioms to Ontology
        manager.addAxiom(ontology, domainAxiom);
        manager.addAxiom(ontology, rangeAxiom);
        
        manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(objectProperty));

        System.out.println("Created Object Property: " + propertyName + " with Domain: " + domainName + " and Range: " + rangeName);
    }
}
