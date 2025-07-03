package service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class LLMService {
    private String modelName;
    private String ollamaEndpoint = "http://localhost:11434";
    private boolean modelLoaded = false;
    
    public LLMService() {
        // Initialisation du service LLM
    }
    
    /**
     * Configure l'endpoint pour l'API Ollama
     * @param endpoint L'URL de l'endpoint Ollama
     */
    public void setOllamaEndpoint(String endpoint) {
        this.ollamaEndpoint = endpoint;
    }
    
    /**
     * Charge un modèle Ollama spécifié
     * @param modelName Le nom du modèle à utiliser
     * @return true si le modèle est disponible, false sinon
     */
    public boolean loadModel(String modelName) {
        this.modelName = modelName;
        
        try {
            URL url = new URL(ollamaEndpoint + "/api/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire la réponse pour vérifier que le modèle est disponible
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String response = br.lines().collect(Collectors.joining());
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray models = jsonResponse.getJSONArray("models");
                    
                    for (int i = 0; i < models.length(); i++) {
                        JSONObject model = models.getJSONObject(i);
                        if (model.getString("name").startsWith(modelName)) {
                            this.modelLoaded = true;
                            return true;
                        }
                    }
                }
            }
            System.err.println("Modèle non trouvé ou erreur de connexion.");
            return false;
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion à Ollama: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si le modèle LLM est chargé
     * @return true si le modèle est chargé, false sinon
     */
    public boolean isModelLoaded() {
        return modelLoaded;
    }
    
    /**
     * Génère du texte à partir d'un prompt en utilisant l'API Ollama
     * @param prompt Le prompt pour le LLM
     * @return Le texte généré
     */
    public String generateText(String prompt) {
        if (!modelLoaded || modelName == null) {
            return "Erreur: Le modèle LLM n'est pas chargé.";
        }
        
        try {
            URL url = new URL(ollamaEndpoint + "/api/generate");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }
            
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("Erreur HTTP: " + status);
                return "Erreur lors de la génération du texte.";
            }
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = br.lines().collect(Collectors.joining());
                JSONObject jsonResponse = new JSONObject(response);
                String generatedText = jsonResponse.getString("response");
                
                System.out.println("Réponse brute du LLM: " + generatedText); // Debug
                return extractPlantUMLCode(generatedText);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de texte: " + e.getMessage());
            return "Erreur: " + e.getMessage();
        }
    }
    
    /**
     * Extrait le code PlantUML d'une réponse complète
     * @param response La réponse complète du LLM
     * @return Le code PlantUML extrait
     */
    private String extractPlantUMLCode(String response) {
        // Rechercher les balises @startuml et @enduml
        int startIdx = response.indexOf("@startuml");
        int endIdx = response.lastIndexOf("@enduml") + "@enduml".length();
        
        if (startIdx != -1 && endIdx != -1) {
            return response.substring(startIdx, endIdx);
        } else {
            // Si les balises ne sont pas trouvées, renvoyer la réponse complète
            return response;
        }
    }
    
    /**
     * Ferme les ressources du modèle
     */
    public void close() {
        modelLoaded = false;
        modelName = null;
    }
}