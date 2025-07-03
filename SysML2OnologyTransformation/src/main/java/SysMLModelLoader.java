import java.io.File;
import java.util.Map;

import java.util.Map;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;
import org.eclipse.uml2.uml.internal.impl.ProfileApplicationImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.fasterxml.jackson.annotation.PropertyAccessor;

import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.URIConverter;



public class SysMLModelLoader {

    public static Model loadSysMLModel(String filePath) {
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    	ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		Map uriMap = resourceSet.getURIConverter().getURIMap();
		URI uri = URI.createURI("jar:file:/C:/Users/RB275872/Desktop/eclipse-java-2023-09-R-win32-x86_64/eclipse/plugins/org.eclipse.uml2.uml.resources_<version>.jar!/"); // for example
		uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), uri.appendSegment("libraries").appendSegment(""));
		uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), uri.appendSegment("metamodels").appendSegment(""));
		uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), uri.appendSegment("profiles").appendSegment(""));
		URI fileURI = URI.createFileURI(new File(filePath).getAbsolutePath());
		Resource inResource=resourceSet.getResource(fileURI, true);
    	
        
        URI uriUMLFile = URI.createFileURI(new File(filePath).getAbsolutePath());
        Resource resource = resourceSet.getResource(uriUMLFile, true);

        // Return the root model element (SysML model)
        for (EObject eObject : resource.getContents()) {
            if (eObject instanceof Model) {
                return (Model) eObject;
            }
        }
        throw new RuntimeException("Aucun Model trouvé dans le fichier");
    
        //return null;
    }
    
    /*public static List<Package> extractSysMLPackage(Package umlModel) {
    	 List<Package> packageList = new ArrayList<>();
         // Parcourt tous les éléments de type package empaquetés dans le package
         for (PackageableElement element : umlModel.getPackagedElements()) {               
             // Si l'élément est une package, on l'ajoute à la liste
             if (element instanceof Package) {
            			//System.out.println(element);
            			packageList.add((Package) element);
             }
             // Si l'élément est un sous-package, on appelle extractClasses de manière récursive
             else if (element instanceof Package) {
            	 packageList.addAll(extractSysMLPackage((Package) element));
             }
         }
         return packageList;
    }*/
    public static List<Package> extractSysMLPackage(Package umlModel) {
        List<Package> packageList = new ArrayList<>();

        // Parcours des éléments du package pour trouver des sous-packages
        for (PackageableElement element : umlModel.getPackagedElements()) {               
            if (element instanceof Package) {
                Package subPackage = (Package) element;

                // Ajouter le package courant
                packageList.add(subPackage);
                System.out.println("Package trouvé : " + subPackage.getName());

                // Récursivité : rechercher les sous-packages
                packageList.addAll(extractSysMLPackage(subPackage));
            }
        }
        return packageList;
    }
    
    public static List<Class> extractSysMLBlocks(Package umlModel) {
   	 List<Class> classList = new ArrayList<>();
        // Parcourt tous les éléments de type package empaquetés dans le package
        for (PackageableElement element : umlModel.getPackagedElements()) {               
            // Si l'élément est une package, on l'ajoute à la liste
            if (element instanceof Class && !(element instanceof Interaction)) {
           			//System.out.println(element);
           			classList.add((Class) element);
            }
            // Si l'élément est un sous-package, on appelle extractClasses de manière récursive
            else if (element instanceof Package) {
            	classList.addAll(extractSysMLBlocks((Package) element));
            }
        }
        return classList;
   }
    
    public static List<Association> extractAssociations(Package umlModel) {
    	 List<Association> associationList = new ArrayList<>();
         // Parcourt tous les éléments empaquetés dans le package
         for (Element element : umlModel.allOwnedElements()) {
        	// System.out.println(element);
             // Si l'élément est une classe, on l'ajoute à la liste
             if (element instanceof Association) { 	 
            	//	System.out.println(element);
            	 associationList.add((Association) element);
             }
             // Si l'élément est un sous-package, on appelle extractClasses de manière récursive    
         }
         return associationList;
    	
    }

        public static List<Association> extractAggregations(Package umlModel) {
            List<Association> aggregations = new ArrayList<>();

            // Itérer sur toutes les associations dans le modèle
            for (Element association : umlModel.allOwnedElements()) {

                // Vérifier si l'association est un type d'agrégation
            	if (association instanceof Association)  
                if (isAggregation((Association)association)) {
                    aggregations.add((Association)association);
                }
            }

            return aggregations;
        }

        // Vérifie si une association est une agrégation
        private static boolean isAggregation(Association association) {
            // Vérifier les propriétés de l'association
            for (Property property : association.getMemberEnds()) {
                // L'agrégation est généralement indiquée par un stéréotype spécifique
                if (property.getAggregation() == AggregationKind.COMPOSITE_LITERAL ||
                        property.getAggregation() == AggregationKind.SHARED_LITERAL ) {
                    return true;
                }
            }
            return false;
        
    }
        
        // Fonction pour extraire les associations de type composition
        public static List<Association> extractCompositions(Package umlModel) {
            List<Association> compositions = new ArrayList<>();

            // Itérer sur toutes les associations dans le modèle
            for (Element association : umlModel.allOwnedElements()) {

                // Vérifier si l'association est une composition
            	if (association instanceof Association)  
                if (isComposition((Association)association)) {
                    compositions.add((Association)association);
                }
            }

            return compositions;
        }

        // Vérifie si une association est une composition
        private static boolean isComposition(Association association) {
            // Vérifier les propriétés de l'association
            for (Property property : association.getMemberEnds()) {
                // La composition est indiquée par un type d'agrégation composite
                if (property.getAggregation() == AggregationKind.COMPOSITE_LITERAL) {
                    return true;
                }
            }
            return false;
       
    }


    // Extract all generalizations from the model
    public static List<Generalization> extractGeneralizations(Package umlModel) {
    	
    	List<Generalization> GeneralisationList = new ArrayList<>();

        // Parcourt tous les éléments empaquetés dans le package
        for (Element element : umlModel.allOwnedElements()) {
       //	 System.out.println(element);
            // Si l'élément est une classe, on l'ajoute à la liste
            if (element instanceof Generalization) {
           	 
           		System.out.println("****rrrrrrrttttttttt"+element);
           		GeneralisationList.add((Generalization) element);
            }
            // Si l'élément est un sous-package, on appelle extractClasses de manière récursive
           
            
        }
        return GeneralisationList;
   	
    }

    // Extract all dependencies from the model
    public static List<Dependency> extractDependencies(Package umlModel) {
        List<Dependency> dependencyList = new ArrayList<>();

        // Parcourt tous les éléments empaquetés dans le package
        for (Element element : umlModel.allOwnedElements()) {
       	 System.out.println(element);
            // Si l'élément est une classe, on l'ajoute à la liste
            if (element instanceof Dependency) {
           		System.out.println(element);
           	 dependencyList.add((Dependency) element);
            }
            // Si l'élément est un sous-package, on appelle extractClasses de manière récursive
           
            
        }
        return dependencyList;
    }

    // Extract all connectors from the model
    public static List<Connector> extractConnectors(Package umlModel) {
        return umlModel.getPackagedElements().stream()
                .filter(element -> element instanceof Class)  // Blocks are UML Classes
                .map(element -> (Class) element)
                .flatMap(cls -> cls.getOwnedConnectors().stream())
                .collect(Collectors.toList());
    }

    // Print extracted relationships
    public static void printRelationships(Package umlModel) {
        // Associations
        System.out.println("=== Associations ===");
        for (Association association : extractAssociations(umlModel)) {
            System.out.println("Association: " + association.getName());
        }

        // Generalizations
        System.out.println("\n=== Generalizations ===");
        for (Generalization generalization : extractGeneralizations(umlModel)) {
            System.out.println("Generalization: " + generalization.getGeneral().getName() +
                    " <- " + generalization.getSpecific().getName());
        }

   /*     // Dependencies
        System.out.println("\n=== Dependencies ===");
        for (Dependency dependency : extractDependencies(umlModel)) {
            System.out.println("Dependency: " + dependency.getClients().get(0).getName() +
                    " -> " + dependency.getSuppliers().get(0).getName());
        }

        // Connectors
        System.out.println("\n=== Connectors ===");
        for (Connector connector : extractConnectors(umlModel)) {
            System.out.println("Connector: " + connector.getName());
        }*/
    }
    

    /**
     * Récupère toutes les machines d'état du modèle SysML
     */
    public static List<StateMachine> extractStateMachines(Package sysmlModel) {
        List<StateMachine> stateMachines = new ArrayList<>();
        
        for (Element element : sysmlModel.allOwnedElements()) {
            if (element instanceof StateMachine) {
            	System.out.println("state Machine:"+((StateMachine) element).getName());
                stateMachines.add((StateMachine) element);
            }
        }

        return stateMachines;
    }
    
    /**
     * Récupère toutes les activités du modèle SysML
     */
    public static List<Activity> extractActivityDiagrams(Package sysmlModel) {
        List<Activity> activities = new ArrayList<>();

        for (Element element : sysmlModel.allOwnedElements()) {
            if (element instanceof Activity) {
            	System.out.println("activity:"+((Activity) element).getName());
                activities.add((Activity) element);
            }
        }

        return activities;
    }
    
    /**
     * Extrait toutes les interactions (diagrammes de séquence) du modèle SysML.
     */
    public static List<Interaction> extractSequenceDiagrams(Package sysmlModel) {
        List<Interaction> interactions = new ArrayList<>();

        for (Element element : sysmlModel.allOwnedElements()) {
            if (element instanceof Collaboration) {
                for (Element nestedElement : ((Collaboration) element).getOwnedElements()) {
                    if (nestedElement instanceof Interaction) {
                        interactions.add((Interaction) nestedElement);
                        System.out.println("collaboration*********:"+((Interaction) element).getName());
                    }
                }
            } else if (element instanceof Interaction) {
                interactions.add((Interaction) element);
                System.out.println("interaction*********:"+((Interaction) element).getName());
            }
        }
        return interactions;
    }
    
    public static void main (String[] args)
    {
    	Model sysmlModel= loadSysMLModel("C:\\Users\\RB275872\\Desktop\\Landing Gear System.uml");//"C:\\Users\\RB275872\\Desktop\\Versionning GENVIA\\WorkspaceGENVIA 03-02-2025\\SystemeDeProductionGenvia\\SystemeDeProductionGenvia.uml");
    	System.out.println(sysmlModel);
    	
    	List<Package> packages = extractSysMLPackage(sysmlModel);
    	List<Class> classes = extractSysMLBlocks(sysmlModel);
    	List<Association> Asso = extractAssociations(sysmlModel);
    	System.out.print("****Ass"+Asso.size());
    	List<Generalization> gener = extractGeneralizations(sysmlModel);
    	System.out.print("****Gener"+gener.toString());
    	List<Dependency> depen = extractDependencies(sysmlModel);
    	System.out.print("****Depen"+depen.toString());
    	List<StateMachine> statemachines = extractStateMachines(sysmlModel);
    	System.out.print("****statemachines"+statemachines.toString());
    	List<Activity> activities = extractActivityDiagrams(sysmlModel);
    	System.out.print("****activities"+activities.toString());
    	List<Interaction> interactions = extractSequenceDiagrams(sysmlModel);
    	System.out.println("****interactions "+interactions.toString());
    	 // Extract and print relationships
        if (sysmlModel != null) {
            printRelationships(sysmlModel);
        } else {
            System.out.println("Failed to load SysML model.");
        }
    	
    	System.out.print("****"+classes.size());
    }
}
