package view;

import controller.UMLGeneratorController;
import controller.UMLGeneratorController.DiagramType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public class UMLGeneratorUI extends JFrame {
    private JTextArea descriptionTextArea;
    private JTextArea umlCodeTextArea;
    private JPanel diagramPanel;
    private JLabel statusLabel;
    private JButton generateButton;
    private JButton saveCodeButton;
    private JButton saveImageButton;
    private JButton updateDiagramButton;
    private JButton exportPapyrusButton;
    
    private JRadioButton classDiagramRadio;
    private JRadioButton stateDiagramRadio;
    private JRadioButton sequenceDiagramRadio;
    private JRadioButton activityDiagramRadio;
    private ButtonGroup diagramTypeGroup;
    
    private UMLGeneratorController controller;
    private BufferedImage currentDiagram;
    
    public UMLGeneratorUI() {
        setTitle("UML Generator - Utilisation de llama3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Description textuelle"));
        
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionTextArea);
        descScrollPane.setPreferredSize(new Dimension(0, 150));
        
        JPanel radioPanel = new JPanel(new GridLayout(2, 2));
        radioPanel.setBorder(BorderFactory.createTitledBorder("Type de diagramme"));
        classDiagramRadio = new JRadioButton("Diagramme de classe", true);
        stateDiagramRadio = new JRadioButton("Diagramme d'état", false);
        sequenceDiagramRadio = new JRadioButton("Diagramme de séquence", false);
        activityDiagramRadio = new JRadioButton("Diagramme d'activité", false);
        
        diagramTypeGroup = new ButtonGroup();
        diagramTypeGroup.add(classDiagramRadio);
        diagramTypeGroup.add(stateDiagramRadio);
        diagramTypeGroup.add(sequenceDiagramRadio);
        diagramTypeGroup.add(activityDiagramRadio);
        
        radioPanel.add(classDiagramRadio);
        radioPanel.add(stateDiagramRadio);
        radioPanel.add(sequenceDiagramRadio);
        radioPanel.add(activityDiagramRadio);
        
        generateButton = new JButton("Générer le diagramme UML");
        generateButton.addActionListener(e -> {
            String description = descriptionTextArea.getText();
            if (!description.isEmpty()) {
                DiagramType diagramType = DiagramType.CLASS;
                if (stateDiagramRadio.isSelected()) {
                    diagramType = DiagramType.STATE;
                } else if (sequenceDiagramRadio.isSelected()) {
                    diagramType = DiagramType.SEQUENCE;
                } else if (activityDiagramRadio.isSelected()) {
                    diagramType = DiagramType.ACTIVITY;
                }
                controller.generateUML(description, diagramType);
            } else {
                showError("Veuillez entrer une description textuelle.");
            }
        });
        
        topPanel.add(descScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(radioPanel, BorderLayout.CENTER);
        buttonPanel.add(generateButton, BorderLayout.SOUTH);
        
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JSplitPane centralSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplitPane.setResizeWeight(0.5);
        centralSplitPane.setDividerLocation(0.5);
        
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Code PlantUML"));
        
        umlCodeTextArea = new JTextArea();
        umlCodeTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane codeScrollPane = new JScrollPane(umlCodeTextArea);
        
        updateDiagramButton = new JButton("Mettre à jour le diagramme");
        updateDiagramButton.addActionListener(e -> {
            String code = umlCodeTextArea.getText();
            if (!code.isEmpty()) {
                controller.updateUMLCode(code);
            } else {
                showError("Le code PlantUML est vide.");
            }
        });
        
        saveCodeButton = new JButton("Enregistrer le code");
        saveCodeButton.addActionListener(e -> saveUMLCode());
        
        JPanel codeButtonPanel = new JPanel();
        codeButtonPanel.add(updateDiagramButton);
        codeButtonPanel.add(saveCodeButton);
        
        leftPanel.add(codeScrollPane, BorderLayout.CENTER);
        leftPanel.add(codeButtonPanel, BorderLayout.SOUTH);
        
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Diagramme UML"));
        
        diagramPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentDiagram != null) {
                    double scaleX = (double) getWidth() / currentDiagram.getWidth();
                    double scaleY = (double) getHeight() / currentDiagram.getHeight();
                    double scale = Math.min(scaleX, scaleY);
                    
                    int width = (int) (currentDiagram.getWidth() * scale);
                    int height = (int) (currentDiagram.getHeight() * scale);
                    
                    int x = (getWidth() - width) / 2;
                    int y = (getHeight() - height) / 2;
                    
                    g.drawImage(currentDiagram, x, y, width, height, this);
                } else {
                    g.setColor(Color.LIGHT_GRAY); 
                    g.drawString("Pas de diagramme généré", getWidth() / 2 - 80, getHeight() / 2);
                }
            }
        };
        diagramPanel.setBackground(Color.WHITE);
        
        JScrollPane diagramScrollPane = new JScrollPane(diagramPanel);
        
        JPanel imageButtonPanel = new JPanel();
        saveImageButton = new JButton("Enregistrer l'image");
        saveImageButton.addActionListener(e -> saveUMLImage());
        
        exportPapyrusButton = new JButton("Exporter vers Papyrus UML");
        exportPapyrusButton.addActionListener(e -> exportDirectToPapyrusUML());
        
        imageButtonPanel.add(saveImageButton);
        imageButtonPanel.add(exportPapyrusButton);
        
        rightPanel.add(diagramScrollPane, BorderLayout.CENTER);
        rightPanel.add(imageButtonPanel, BorderLayout.SOUTH);
        
        centralSplitPane.setLeftComponent(leftPanel);
        centralSplitPane.setRightComponent(rightPanel);
        
        statusLabel = new JLabel("Prêt. Veuillez charger un modèle LLM.");
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centralSplitPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    public void display() {
        setVisible(true);
    }
    
    public void setController(UMLGeneratorController controller) {
        this.controller = controller;
        if (controller != null) {
            SwingUtilities.invokeLater(() -> {
                controller.setOllamaEndpoint("http://localhost:11434");
                controller.loadModel("llama3");
            });
        }
    }
    
    public void updateUMLCode(String plantUmlCode) {
        SwingUtilities.invokeLater(() -> {
            umlCodeTextArea.setText(plantUmlCode);
        });
    }
    
    public void updateUMLDiagram(BufferedImage diagram) {
        this.currentDiagram = diagram;
        diagramPanel.repaint();
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    public void enableGeneration(boolean enabled) {
        generateButton.setEnabled(enabled);
        updateDiagramButton.setEnabled(enabled);
        exportPapyrusButton.setEnabled(enabled);
    }
    
    private void saveUMLCode() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le code UML");
        
        FileNameExtensionFilter pumlFilter = new FileNameExtensionFilter("Fichiers PlantUML (*.puml)", "puml");
        fileChooser.addChoosableFileFilter(pumlFilter);
        fileChooser.setFileFilter(pumlFilter);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".puml")) {
                filePath += ".puml";
            }
            
            controller.saveUMLCode(Path.of(filePath));
        }
    }
    
    private void saveUMLImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer l'image du diagramme");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images PNG (*.png)", "png");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
            }
            
            controller.saveUMLImage(Path.of(filePath));
        }
    }
    
    private void exportDirectToPapyrusUML() {
        String currentCode = umlCodeTextArea.getText();
        if (currentCode.isEmpty()) {
            showError("Aucun code PlantUML à convertir.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter vers Papyrus UML");
        
        FileNameExtensionFilter umlFilter = new FileNameExtensionFilter("Fichiers Papyrus UML (*.uml)", "uml");
        fileChooser.addChoosableFileFilter(umlFilter);
        fileChooser.setFileFilter(umlFilter);
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".uml")) {
                filePath += ".uml";
            }
            
            controller.generatePapyrusFromPlantUML(Path.of(filePath));
        }
    }
}