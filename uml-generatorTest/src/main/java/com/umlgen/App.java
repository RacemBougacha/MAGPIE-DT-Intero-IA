package com.umlgen;

import com.formdev.flatlaf.FlatLightLaf;
import view.UMLGeneratorUI;

import Genmodel.UMLGenerator;

import service.PlantUMLService;

import service.LLMService;
import service.PapyrusService;
import controller.UMLGeneratorController;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Installation du Look and Feel FlatLaf
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Échec de l'initialisation du thème FlatLaf: " + e);
        }
        
        // Initialisation des services
        LLMService llmService = new LLMService();
        PlantUMLService plantUMLService = new PlantUMLService();
        PapyrusService papyrusService = new PapyrusService();
        
        // Initialisation du modèle
        UMLGenerator umlGenerator = new UMLGenerator(llmService, plantUMLService, papyrusService);
        
        // Création de l'interface et du contrôleur
        SwingUtilities.invokeLater(() -> {
            UMLGeneratorUI ui = new UMLGeneratorUI();
            UMLGeneratorController controller = new UMLGeneratorController(umlGenerator, ui);
            ui.setController(controller);
            ui.display();
        });
    }
}