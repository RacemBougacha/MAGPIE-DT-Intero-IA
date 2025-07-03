package service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Gestionnaire de langues pour l'interface utilisateur
 */
public class LanguageManager {
    // Instance unique (pattern Singleton)
    private static LanguageManager instance;
    
    // Langue actuelle
    private String currentLanguage = "fr"; // Français par défaut
    
    // Dictionnaire de traductions
    private Map<String, Map<String, String>> translations;
    
    /**
     * Constructeur privé (pattern Singleton)
     */
    private LanguageManager() {
        // Initialisation des traductions
        initializeTranslations();
    }
    
    /**
     * Obtient l'instance unique du gestionnaire
     * @return L'instance du LanguageManager
     */
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }
    
    /**
     * Initialise les traductions
     */
    private void initializeTranslations() {
        translations = new HashMap<>();
        
        // Traductions en français
        Map<String, String> frTranslations = new HashMap<>();
        frTranslations.put("app.title", "UML Generator - IA pour la génération de diagrammes UML avec Ollama");
        frTranslations.put("menu.file", "Fichier");
        frTranslations.put("menu.connect", "Connecter à Ollama...");
        frTranslations.put("menu.exit", "Quitter");
        frTranslations.put("panel.description", "Description textuelle");
        frTranslations.put("panel.code", "Code PlantUML");
        frTranslations.put("panel.diagram", "Diagramme UML");
        frTranslations.put("button.generate", "Générer le diagramme UML");
        frTranslations.put("button.connect", "Connecter à un modèle Ollama");
        frTranslations.put("button.update", "Mettre à jour le diagramme");
        frTranslations.put("button.saveCode", "Enregistrer le code");
        frTranslations.put("button.saveImage", "Enregistrer l'image");
        frTranslations.put("status.ready", "Prêt. Veuillez charger un modèle LLM.");
        frTranslations.put("status.generating", "Génération en cours...");
        frTranslations.put("status.complete", "Génération terminée avec succès.");
        frTranslations.put("status.failed", "Échec de la génération.");
        frTranslations.put("status.modelLoaded", "Modèle chargé avec succès: ");
        frTranslations.put("error.noDescription", "Veuillez entrer une description textuelle.");
        frTranslations.put("error.noCode", "Le code PlantUML est vide.");
        frTranslations.put("error.noModel", "Le modèle LLM n'est pas chargé. Veuillez charger un modèle d'abord.");
        frTranslations.put("error.noCodeToSave", "Aucun code UML à enregistrer.");
        frTranslations.put("error.noImageToSave", "Aucun diagramme UML à enregistrer.");
        frTranslations.put("dialog.selectModel", "Sélectionnez un modèle Ollama à utiliser:\n(Assurez-vous que Ollama est en cours d'exécution sur votre machine)");
        frTranslations.put("dialog.customModel", "Entrez le nom du modèle Ollama:");
        frTranslations.put("dialog.serverUrl", "URL du serveur Ollama (laisser par défaut pour localhost):");
        frTranslations.put("dialog.saveCode", "Enregistrer le code UML");
        frTranslations.put("dialog.saveImage", "Enregistrer l'image du diagramme");
        frTranslations.put("file.puml", "Fichiers PlantUML (*.puml)");
        frTranslations.put("file.png", "Images PNG (*.png)");
        frTranslations.put("file.jpg", "Images JPEG (*.jpg, *.jpeg)");
        frTranslations.put("language.switch", "Changer la langue");
        frTranslations.put("language.english", "English");
        frTranslations.put("language.french", "Français");
        frTranslations.put("diagram.empty", "Pas de diagramme généré");
        
        // Traductions en anglais
        Map<String, String> enTranslations = new HashMap<>();
        enTranslations.put("app.title", "UML Generator - AI for UML diagram generation with Ollama");
        enTranslations.put("menu.file", "File");
        enTranslations.put("menu.connect", "Connect to Ollama...");
        enTranslations.put("menu.exit", "Exit");
        enTranslations.put("panel.description", "Text Description");
        enTranslations.put("panel.code", "PlantUML Code");
        enTranslations.put("panel.diagram", "UML Diagram");
        enTranslations.put("button.generate", "Generate UML Diagram");
        enTranslations.put("button.connect", "Connect to Ollama Model");
        enTranslations.put("button.update", "Update Diagram");
        enTranslations.put("button.saveCode", "Save Code");
        enTranslations.put("button.saveImage", "Save Image");
        enTranslations.put("status.ready", "Ready. Please load an LLM model.");
        enTranslations.put("status.generating", "Generating...");
        enTranslations.put("status.complete", "Generation completed successfully.");
        enTranslations.put("status.failed", "Generation failed.");
        enTranslations.put("status.modelLoaded", "Model loaded successfully: ");
        enTranslations.put("error.noDescription", "Please enter a text description.");
        enTranslations.put("error.noCode", "PlantUML code is empty.");
        enTranslations.put("error.noModel", "LLM model is not loaded. Please load a model first.");
        enTranslations.put("error.noCodeToSave", "No UML code to save.");
        enTranslations.put("error.noImageToSave", "No UML diagram to save.");
        enTranslations.put("dialog.selectModel", "Select an Ollama model to use:\n(Make sure Ollama is running on your machine)");
        enTranslations.put("dialog.customModel", "Enter Ollama model name:");
        enTranslations.put("dialog.serverUrl", "Ollama server URL (leave default for localhost):");
        enTranslations.put("dialog.saveCode", "Save UML Code");
        enTranslations.put("dialog.saveImage", "Save Diagram Image");
        enTranslations.put("file.puml", "PlantUML Files (*.puml)");
        enTranslations.put("file.png", "PNG Images (*.png)");
        enTranslations.put("file.jpg", "JPEG Images (*.jpg, *.jpeg)");
        enTranslations.put("language.switch", "Change Language");
        enTranslations.put("language.english", "English");
        enTranslations.put("language.french", "Français");
        enTranslations.put("diagram.empty", "No diagram generated");
        
        // Ajout des traductions au dictionnaire
        translations.put("fr", frTranslations);
        translations.put("en", enTranslations);
    }
    
    /**
     * Définit la langue à utiliser
     * @param language Code de langue ("fr" ou "en")
     */
    public void setLanguage(String language) {
        if (translations.containsKey(language)) {
            currentLanguage = language;
        }
    }
    
    /**
     * Obtient la langue actuelle
     * @return Le code de langue actuel
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Traduit une clé dans la langue actuelle
     * @param key La clé à traduire
     * @return La traduction ou la clé elle-même si non trouvée
     */
    public String translate(String key) {
        Map<String, String> langMap = translations.get(currentLanguage);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        return key;
    }
}