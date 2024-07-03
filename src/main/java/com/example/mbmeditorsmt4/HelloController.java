package com.example.mbmeditorsmt4;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextArea textArea;

    @FXML
    private Button redButton;

    @FXML
    private Button blackButton;

    @FXML
    private Button blueButton;

    @FXML
    private Button afficherTexteTraduitButton;

    @FXML
    private Button afficherTexteDorigineButton;

    @FXML
    private TreeView<String> FolderView;

    @FXML
    private Button FolderButton;

    @FXML
    private TextArea TextEntry;

    @FXML
    private TextArea XMLText;

    @FXML
    private Label ErrorLabel;

    @FXML
    private ListView<String> IDView;

    @FXML
    private MenuItem MenuSelectFolder;

    DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedDirectory;
    private HashMap<String, String> fileLocations = new HashMap<>();

    @FXML
    protected void initialize() {
        redButton.setOnAction(event -> changeTextColor(Color.RED));
        blackButton.setOnAction(event -> changeTextColor(Color.BLACK));
        blueButton.setOnAction(event -> changeTextColor(Color.BLUE));
        afficherTexteTraduitButton.setOnAction(event -> displayTranslatedText());
        afficherTexteDorigineButton.setOnAction(event -> displayOriginalText());

        directoryChooser = new DirectoryChooser();

        FolderButton.setOnAction(event -> SelectAndLoadFolder());
        MenuSelectFolder.setOnAction(event -> SelectAndLoadFolder());

        TextEntry.textProperty().addListener((observable, oldValue, newValue) -> {
            int lineCount = newValue.split("\n", -1).length - 1;
            if (lineCount > 2) {
                TextEntry.setText(oldValue);
                ErrorLabel.setText("Cannot create a new line");
            } else {
                ErrorLabel.setText("");
            }
        });

        FolderView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String filePath = buildFilePath(newValue);
                if (filePath.toLowerCase().endsWith(".xml")) {
                    File selectedFile = new File(filePath);
                    displayIdsFromXml(selectedFile);
                }
            }
        });

        IDView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedId = newValue.substring(2); // Pour enlever "id" du début
                displaySourceForId(selectedId);
            }
        });
    }

    private void displaySourceForId(String id) {
        if (selectedDirectory != null) {
            String filePath = buildFilePath(FolderView.getSelectionModel().getSelectedItem());
            if (filePath.toLowerCase().endsWith(".xml")) {
                File selectedFile = new File(filePath);
                String relativePath = fileLocations.get(selectedFile.getName());
                if (relativePath != null) {
                    File xmlFile = new File(selectedDirectory, relativePath);
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(xmlFile);

                        document.getDocumentElement().normalize();
                        NodeList entryList = document.getElementsByTagName("entry");

                        for (int i = 0; i < entryList.getLength(); i++) {
                            Node node = entryList.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                if (element.hasAttribute("id") && element.getAttribute("id").equals(id)) {
                                    NodeList sourceList = element.getElementsByTagName("source");
                                    if (sourceList.getLength() > 0) {
                                        Node sourceNode = sourceList.item(0);
                                        XMLText.setText(sourceNode.getTextContent());
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void displayIdsFromXml(File file) {
        if (selectedDirectory != null) {
            IDView.getItems().clear();
            String relativePath = fileLocations.get(file.getName());
            if (relativePath != null) {
                File xmlFile = new File(selectedDirectory, relativePath);
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(xmlFile);

                    document.getDocumentElement().normalize();
                    NodeList entryList = document.getElementsByTagName("entry");

                    for (int i = 0; i < entryList.getLength(); i++) {
                        Node node = entryList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;
                            if (element.hasAttribute("id")) {
                                IDView.getItems().add("id" + element.getAttribute("id"));
                            }
                        }
                    }
                    if(IDView.getItems().isEmpty()) {
                        ErrorLabel.setText("No ID in this file");
                    }
                    else{
                        ErrorLabel.setText("");
                    }
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void SelectAndLoadFolder() {
        selectedDirectory = directoryChooser.showDialog(FolderButton.getScene().getWindow());
        if (selectedDirectory != null) {
            TreeItem<String> rootItem = new TreeItem<>(selectedDirectory.getAbsolutePath());
            rootItem.setExpanded(true);
            populateTreeView(selectedDirectory, rootItem);
            FolderView.setRoot(rootItem);
            FolderButton.setDisable(true);
            FolderButton.setVisible(false);
        }
    }

    private void populateTreeView(File directory, TreeItem<String> parentItem) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (containsXmlFiles(file)) {
                        TreeItem<String> newItem = new TreeItem<>(file.getName());
                        parentItem.getChildren().add(newItem);
                        populateTreeView(file, newItem);
                    }
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                    String relativePath = getRelativePath(file, selectedDirectory);
                    if (relativePath != null) {
                        fileLocations.put(file.getName(), relativePath);
                        TreeItem<String> newItem = new TreeItem<>(file.getName());
                        parentItem.getChildren().add(newItem);
                    } else {
                        System.out.println("Failed to get relative path for file: " + file.getName());
                    }
                }
            }
        }
    }

    private String getRelativePath(File file, File baseDirectory) {
        String filePath = file.getAbsolutePath();
        String basePath = baseDirectory.getAbsolutePath();

        if (filePath.startsWith(basePath)) {
            String relativePath = filePath.substring(basePath.length());
            if (relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(1);
            }
            return relativePath;
        } else {
            return null; // Handle error case
        }
    }

    private boolean containsXmlFiles(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (containsXmlFiles(file)) {
                        return true;
                    }
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void changeTextColor(Color color) {
        textArea.setStyle("-fx-text-fill: " + toRgbString(color) + ";");
    }

    private void displayTranslatedText() {
        textArea.setText("Translated text");
    }

    private void displayOriginalText() {
        textArea.setText("Original text");
    }

    private String toRgbString(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return String.format("rgb(%d, %d, %d)", red, green, blue);
    }

    private String buildFilePath(TreeItem<String> item) {
        StringBuilder filePathBuilder = new StringBuilder();
        TreeItem<String> currentItem = item;
        while (currentItem != null) {
            filePathBuilder.insert(0, currentItem.getValue());
            currentItem = currentItem.getParent();
            if (currentItem != null) {
                filePathBuilder.insert(0, File.separator);
            }
        }
        return filePathBuilder.toString();
    }
}
