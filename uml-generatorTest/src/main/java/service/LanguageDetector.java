package service;

/**
 * Service pour détecter la langue d'un texte
 */
public class LanguageDetector {
    // Mots communs en anglais
    private static final String[] ENGLISH_COMMON_WORDS = {
            "the", "and", "is", "in", "to", "a", "of", "for", "with", "that", 
            "this", "on", "as", "are", "by", "an", "be", "it", "or", "at",
            "class", "interface", "extends", "implements", "private", "public",
            "static", "has", "contains", "diagram", "create", "generate", "show"
    };
    
    // Mots communs en français
    private static final String[] FRENCH_COMMON_WORDS = {
            "le", "la", "les", "un", "une", "des", "et", "est", "dans", "pour",
            "avec", "que", "qui", "sur", "ce", "cette", "sont", "par", "ou", "au",
            "classe", "interface", "étend", "implémente", "privé", "public",
            "statique", "possède", "contient", "diagramme", "créer", "générer", "afficher"
    };
    
    /**
     * Détecte la langue d'un texte
     * @param text Le texte à analyser
     * @return "en" pour l'anglais, "fr" pour le français
     */
    public static String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en"; // Par défaut en anglais si aucun texte
        }
        
        // Convertir le texte en minuscules
        String lowerText = text.toLowerCase();
        
        // Compter les occurrences de mots communs
        int englishCount = countWordOccurrences(lowerText, ENGLISH_COMMON_WORDS);
        int frenchCount = countWordOccurrences(lowerText, FRENCH_COMMON_WORDS);
        
        // Si on détecte plus de mots anglais, on considère que c'est de l'anglais
        return englishCount > frenchCount ? "en" : "fr";
    }
    
    /**
     * Compte les occurrences de mots dans un texte
     * @param text Le texte à analyser
     * @param words Les mots à compter
     * @return Le nombre total d'occurrences
     */
    private static int countWordOccurrences(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            // Recherche le mot avec des délimiteurs de mots
            String pattern = "\\b" + word + "\\b";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            
            while (m.find()) {
                count++;
            }
        }
        return count;
    }
}