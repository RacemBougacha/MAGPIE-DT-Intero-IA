package service;

import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.FileFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class PlantUMLService {
    
    public BufferedImage generateDiagram(String plantUmlCode) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SourceStringReader reader = new SourceStringReader(plantUmlCode);
            reader.outputImage(outputStream, new FileFormatOption(FileFormat.PNG));
            return ImageIO.read(new java.io.ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du diagramme UML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean saveDiagramImage(BufferedImage image, Path filePath) {
        try {
            String fileName = filePath.toString().toLowerCase();
            String format = "png";
            
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                format = "jpg";
            } else if (fileName.endsWith(".gif")) {
                format = "gif";
            } else if (fileName.endsWith(".bmp")) {
                format = "bmp";
            }
            
            return ImageIO.write(image, format, filePath.toFile());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de l'image: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public String validateAndCorrectUMLCode(String plantUmlCode, boolean isStateDiagram) {
        if (!plantUmlCode.trim().startsWith("@startuml")) {
            plantUmlCode = "@startuml\n" + plantUmlCode;
        }
        if (!plantUmlCode.trim().endsWith("@enduml")) {
            plantUmlCode = plantUmlCode + "\n@enduml";
        }
        
        if (isStateDiagram) {
            plantUmlCode = plantUmlCode.replaceAll("\\[\\*\\]\\s+-\\[[0-9]+\\]->", "[*] -->");
            plantUmlCode = plantUmlCode.replaceAll("state\\s+([\\w]+)\\s+->\\s+state", "state $1 --> state");
            plantUmlCode = plantUmlCode.replaceAll("state\\s+([\\w]+)\\s+-([^>]*)>\\s+state", "state $1 --$2> state");
            plantUmlCode = plantUmlCode.replaceAll("state\\s+([\\w]+)\\s+-->\\s+state\\s+([\\w]+)", "$1 --> $2");
            
            String[] lines = plantUmlCode.split("\n");
            StringBuilder correctedCode = new StringBuilder();
            
            java.util.Set<String> definedStates = new java.util.HashSet<>();
            
            for (String line : lines) {
                correctedCode.append(line).append("\n");
                
                if (line.contains("-->") && !line.contains("[*]")) {
                    String[] parts = line.split("-->");
                    if (parts.length >= 2) {
                        String sourceState = parts[0].trim().split("\\s+")[0];
                        String targetState = parts[1].trim().split("\\s+|:")[0];
                        
                        if (!definedStates.contains(sourceState) && !sourceState.equals("[*]")) {
                            definedStates.add(sourceState);
                            correctedCode.append("state ").append(sourceState).append(" {\n}\n");
                        }
                        
                        if (!definedStates.contains(targetState) && !targetState.equals("[*]")) {
                            definedStates.add(targetState);
                            correctedCode.append("state ").append(targetState).append(" {\n}\n");
                        }
                    }
                }
            }
            
            return correctedCode.toString();
        }
        
        return plantUmlCode;
    }
}