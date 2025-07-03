package controller;

import view.UMLGeneratorUI;
import javax.swing.*;

import Genmodel.UMLGenerator;
import service.PapyrusService;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class UMLGeneratorController {
    private final UMLGenerator model;
    private final UMLGeneratorUI view;
    
    private String currentClassDiagramCode;
    private String currentStateDiagramCode;
    private String currentSequenceDiagramCode;
    private String currentActivityDiagramCode;
    private BufferedImage currentClassDiagram;
    private BufferedImage currentStateDiagram;
    private BufferedImage currentSequenceDiagram;
    private BufferedImage currentActivityDiagram;
    
    private DiagramType currentDiagramType = DiagramType.CLASS;
    
    public enum DiagramType {
        CLASS,
        STATE,
        SEQUENCE,
        ACTIVITY
    }
    
    public UMLGeneratorController(UMLGenerator model, UMLGeneratorUI view) {
        this.model = model;
        this.view = view;
    }
    
    public void setOllamaEndpoint(String endpoint) {
        model.setOllamaEndpoint(endpoint);
    }
    
    public void generateUML(String description, DiagramType diagramType) {
        if (!model.isModelReady()) {
            view.showError("Le modèle LLM n'est pas chargé. Veuillez charger un modèle d'abord.");
            return;
        }
        
        view.setStatusMessage("Génération du diagramme en cours...");
        currentDiagramType = diagramType;
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    switch (diagramType) {
                        case CLASS:
                            currentClassDiagramCode = model.generateUMLCode(description, "class");
                            currentClassDiagram = model.generateUMLDiagram(currentClassDiagramCode);
                            break;
                        case STATE:
                            currentStateDiagramCode = model.generateUMLCode(description, "state");
                            currentStateDiagram = model.generateUMLDiagram(currentStateDiagramCode);
                            break;
                        case SEQUENCE:
                            currentSequenceDiagramCode = model.generateUMLCode(description, "sequence");
                            currentSequenceDiagram = model.generateUMLDiagram(currentSequenceDiagramCode);
                            break;
                        case ACTIVITY:
                            currentActivityDiagramCode = model.generateUMLCode(description, "activity");
                            currentActivityDiagram = model.generateUMLDiagram(currentActivityDiagramCode);
                            break;
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    switch (diagramType) {
                        case CLASS:
                            if (currentClassDiagram != null) {
                                view.updateUMLCode(currentClassDiagramCode);
                                view.updateUMLDiagram(currentClassDiagram);
                                view.setStatusMessage("Génération du diagramme de classe terminée avec succès.");
                            } else {
                                handleUMLGenerationError(new Exception("Failed to generate class diagram"), DiagramType.CLASS);
                            }
                            break;
                        case STATE:
                            if (currentStateDiagram != null) {
                                view.updateUMLCode(currentStateDiagramCode);
                                view.updateUMLDiagram(currentStateDiagram);
                                view.setStatusMessage("Génération du diagramme d'état terminée avec succès.");
                            } else {
                                handleUMLGenerationError(new Exception("Failed to generate state diagram"), DiagramType.STATE);
                            }
                            break;
                        case SEQUENCE:
                            if (currentSequenceDiagram != null) {
                                view.updateUMLCode(currentSequenceDiagramCode);
                                view.updateUMLDiagram(currentSequenceDiagram);
                                view.setStatusMessage("Génération du diagramme de séquence terminée avec succès.");
                            } else {
                                handleUMLGenerationError(new Exception("Failed to generate sequence diagram"), DiagramType.SEQUENCE);
                            }
                            break;
                        case ACTIVITY:
                            if (currentActivityDiagram != null) {
                                view.updateUMLCode(currentActivityDiagramCode);
                                view.updateUMLDiagram(currentActivityDiagram);
                                view.setStatusMessage("Génération du diagramme d'activité terminée avec succès.");
                            } else {
                                handleUMLGenerationError(new Exception("Failed to generate activity diagram"), DiagramType.ACTIVITY);
                            }
                            break;
                    }
                } catch (Exception e) {
                    handleUMLGenerationError(e, diagramType);
                }
            }
        };
        
        worker.execute();
    }
    
    public void loadModel(String modelPath) {
        view.setStatusMessage("Chargement du modèle...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return model.loadModel(modelPath);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        view.setStatusMessage("Modèle chargé avec succès");
                        view.enableGeneration(true);
                    } else {
                        view.showError("Échec du chargement du modèle.");
                        view.setStatusMessage("Échec du chargement.");
                        view.enableGeneration(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    view.showError("Erreur: " + e.getMessage());
                    view.setStatusMessage("Erreur lors du chargement.");
                    view.enableGeneration(false);
                }
            }
        };
        
        worker.execute();
    }
    
    public void saveUMLCode(Path filePath) {
        String currentCode = getCurrentUMLCode();
        
        if (currentCode == null || currentCode.isEmpty()) {
            view.showError("Aucun code PlantUML à enregistrer.");
            return;
        }
        
        boolean success = model.saveUMLFile(currentCode, filePath);
        
        if (success) {
            view.setStatusMessage("Code PlantUML enregistré: " + filePath.getFileName());
        } else {
            view.showError("Échec de l'enregistrement du code PlantUML.");
        }
    }
    
    public void saveUMLImage(Path filePath) {
        BufferedImage currentDiagram = getCurrentDiagram();
        if (currentDiagram == null) {
            view.showError("Aucun diagramme à enregistrer.");
            return;
        }
        
        boolean success = model.saveUMLImage(currentDiagram, filePath);
        
        if (success) {
            view.setStatusMessage("Image du diagramme enregistrée: " + filePath.getFileName());
        } else {
            view.showError("Échec de l'enregistrement de l'image.");
        }
    }
    
    public void updateUMLCode(String umlCode) {
        try {
            switch (currentDiagramType) {
                case CLASS:
                    currentClassDiagramCode = umlCode;
                    currentClassDiagram = model.generateUMLDiagram(umlCode);
                    view.updateUMLDiagram(currentClassDiagram);
                    break;
                case STATE:
                    currentStateDiagramCode = umlCode;
                    currentStateDiagram = model.generateUMLDiagram(umlCode);
                    view.updateUMLDiagram(currentStateDiagram);
                    break;
                case SEQUENCE:
                    currentSequenceDiagramCode = umlCode;
                    currentSequenceDiagram = model.generateUMLDiagram(umlCode);
                    view.updateUMLDiagram(currentSequenceDiagram);
                    break;
                case ACTIVITY:
                    currentActivityDiagramCode = umlCode;
                    currentActivityDiagram = model.generateUMLDiagram(umlCode);
                    view.updateUMLDiagram(currentActivityDiagram);
                    break;
            }
            view.setStatusMessage("Diagramme mis à jour avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Erreur lors de la mise à jour du diagramme: " + e.getMessage());
        }
    }
    
    public void generatePapyrusFromPlantUML(Path filePath) {
        try {
            String currentCode = getCurrentUMLCode();
            
            if (currentCode == null || currentCode.isEmpty()) {
                view.showError("Aucun code PlantUML à convertir.");
                return;
            }
            
            view.setStatusMessage("Conversion vers Papyrus en cours...");
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    String diagramTypeStr;
                    switch (currentDiagramType) {
                        case CLASS: diagramTypeStr = "class"; break;
                        case STATE: diagramTypeStr = "state"; break;
                        case SEQUENCE: diagramTypeStr = "sequence"; break;
                        case ACTIVITY: diagramTypeStr = "activity"; break;
                        default: diagramTypeStr = "class";
                    }
                    return model.exportToPapyrusUML(currentCode, filePath, diagramTypeStr);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            view.setStatusMessage("Fichier Papyrus UML généré: " + filePath.getFileName());
                        } else {
                            view.showError("Échec de la conversion vers Papyrus UML.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        view.showError("Erreur lors de la conversion: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Erreur: " + e.getMessage());
        }
    }
    
    private String getCurrentUMLCode() {
        switch (currentDiagramType) {
            case CLASS: return currentClassDiagramCode;
            case STATE: return currentStateDiagramCode;
            case SEQUENCE: return currentSequenceDiagramCode;
            case ACTIVITY: return currentActivityDiagramCode;
            default: return null;
        }
    }
    
    private BufferedImage getCurrentDiagram() {
        switch (currentDiagramType) {
            case CLASS: return currentClassDiagram;
            case STATE: return currentStateDiagram;
            case SEQUENCE: return currentSequenceDiagram;
            case ACTIVITY: return currentActivityDiagram;
            default: return null;
        }
    }
    
    private void handleUMLGenerationError(Exception e, DiagramType diagramType) {
        String diagramTypeString = getDiagramTypeString(diagramType);
        
        if (e != null) {
            e.printStackTrace();
        }
        
        if (e != null && e.getMessage() != null && e.getMessage().contains("Syntax Error")) {
            try {
                String fallbackCode;
                switch (diagramType) {
                    case CLASS:
                        fallbackCode = "@startuml\nclass Example\n@enduml";
                        currentClassDiagramCode = fallbackCode;
                        currentClassDiagram = model.generateUMLDiagram(fallbackCode);
                        break;
                    case STATE:
                        fallbackCode = "@startuml\n[*] --> Initial\nInitial --> [*]\n@enduml";
                        currentStateDiagramCode = fallbackCode;
                        currentStateDiagram = model.generateUMLDiagram(fallbackCode);
                        break;
                    case SEQUENCE:
                        fallbackCode = "@startuml\nactor User\nUser -> System: Request\nSystem --> User: Response\n@enduml";
                        currentSequenceDiagramCode = fallbackCode;
                        currentSequenceDiagram = model.generateUMLDiagram(fallbackCode);
                        break;
                    case ACTIVITY:
                        fallbackCode = "@startuml\nstart\n:Hello World;\nstop\n@enduml";
                        currentActivityDiagramCode = fallbackCode;
                        currentActivityDiagram = model.generateUMLDiagram(fallbackCode);
                        break;
                }
                
                view.updateUMLDiagram(getCurrentDiagram());
                view.showError("Erreur de syntaxe dans le diagramme " + diagramTypeString + ". Un diagramme minimal a été généré.");
                view.setStatusMessage("Récupération après erreur de syntaxe.");
            } catch (Exception ex) {
                view.showError("Échec de la récupération : " + ex.getMessage());
                view.setStatusMessage("Échec de la génération.");
            }
        } else {
            String errorMessage = e != null ? e.getMessage() : "Cause inconnue";
            view.showError("Erreur lors de la génération du diagramme " + diagramTypeString + " : " + errorMessage);
            view.setStatusMessage("Échec de la génération.");
        }
    }
    
    private String getDiagramTypeString(DiagramType diagramType) {
        switch (diagramType) {
            case CLASS: return "de classe";
            case STATE: return "d'état";
            case SEQUENCE: return "de séquence";
            case ACTIVITY: return "d'activité";
            default: return "inconnu";
        }
    }
    
    public void setCurrentDiagramType(DiagramType diagramType) {
        this.currentDiagramType = diagramType;
        String currentCode = getCurrentUMLCode();
        BufferedImage currentImage = getCurrentDiagram();
        
        if (currentCode != null) {
            view.updateUMLCode(currentCode);
        }
        
        if (currentImage != null) {
            view.updateUMLDiagram(currentImage);
        }
    }
    
    public DiagramType getCurrentDiagramType() {
        return currentDiagramType;
    }
    
    public void generateDirectPapyrusUML(String description, Path filePath, DiagramType diagramType) {
        if (!model.isModelReady()) {
            view.showError("LLM model not loaded");
            return;
        }

        view.setStatusMessage("Generating direct Papyrus UML...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    String diagramTypeStr = diagramType.toString().toLowerCase();
                    String xmiContent = model.generatePapyrusUMLCode(description, diagramTypeStr);
                    String cleanedXmi = PapyrusService.cleanAndValidateXMI(xmiContent);
                    return PapyrusService.savePapyrusUML(cleanedXmi, filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        view.setStatusMessage("Papyrus file generated successfully");
                    } else {
                        view.showError("Failed to generate Papyrus file");
                    }
                } catch (Exception e) {
                    view.showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}