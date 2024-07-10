package com.example.mbmeditorsmt4;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.fxmisc.richtext.InlineCssTextArea;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HelloController {
    @FXML
    private Button redButton;

    @FXML
    private Button blackButton;

    @FXML
    private Button blueButton;

    @FXML
    private Button SourceButton;

    @FXML
    private Button OriginButton;

    @FXML
    private TreeView<String> FolderView;

    @FXML
    private Button FolderButton;

    @FXML
    private InlineCssTextArea TextEntry;

    @FXML
    private TextArea XMLText;

    @FXML
    private Label ErrorLabel;

    @FXML
    private ListView<String> IDView;

    @FXML
    private MenuItem MenuSelectFolder;

    @FXML
    private TextField NameEntry;

    @FXML
    private Button PreviousText;

    @FXML
    private Button NextText;

    @FXML
    private Button SaveIDButton;

    @FXML
    private Button ResetID;

    DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedDirectory;
    private final HashMap<String, String> fileLocations = new HashMap<>();
    private int actualTextDisplay = 0;
    private Format actualFormat;
    private String toDisplay = "source";
    private String actualID;

    @FXML
    protected void initialize() {
        redButton.setOnAction(event -> changeTextColor("£", "red"));
        blackButton.setOnAction(event -> changeTextColor("§", "black"));
        blueButton.setOnAction(event -> changeTextColor("µ", "blue"));
        SourceButton.setOnAction(event -> displaySource());
        OriginButton.setOnAction(event -> displayTarget());
        SaveIDButton.setOnAction(event -> SaveActualID());
        ResetID.setOnAction(event -> displayForId(actualID, toDisplay));

        PreviousText.setOnAction(event -> ChangeDisplayedText("PREVIOUS"));
        NextText.setOnAction(event -> ChangeDisplayedText("NEXT"));

        directoryChooser = new DirectoryChooser();

        FolderButton.setOnAction(event -> SelectAndLoadFolder());
        MenuSelectFolder.setOnAction(event -> SelectAndLoadFolder());

        TextEntry.setOnKeyTyped(event -> updateTextEntry());

        NameEntry.setOnKeyTyped(e -> {
            if(!actualFormat.getSpeakerName().equals(XMLText.getText())){
                actualFormat.setSpeakerName(NameEntry.getText());
                actualFormat.convertToCode();
                XMLText.setText(actualFormat.getCodeTextFormat());
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
                actualID = newValue.substring(2);
                displayForId(actualID, toDisplay);
            }
        });
    }

    private void SaveActualID() {
        if (actualID == null || actualID.isEmpty()) {
            ErrorLabel.setText("No ID selected to save");
            return;
        }

        String textContent = actualFormat.getCodeTextFormat();
        String filePath = buildFilePath(FolderView.getSelectionModel().getSelectedItem());

        if (!filePath.toLowerCase().endsWith(".xml")) {
            ErrorLabel.setText("No valid XML file selected");
            return;
        }

        File xmlFile = new File(filePath);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList entryList = doc.getElementsByTagName("entry");
            boolean idFound = false;

            for (int i = 0; i < entryList.getLength(); i++) {
                Node node = entryList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (actualID.equals(element.getAttribute("id"))) {
                        NodeList sourceList = element.getElementsByTagName(toDisplay);
                        if (sourceList.getLength() > 0) {
                            sourceList.item(0).setTextContent(textContent);
                        } else {
                            Element newElement = doc.createElement(toDisplay);
                            newElement.setTextContent(textContent);
                            element.appendChild(newElement);
                        }
                        idFound = true;
                        break;
                    }
                }
            }

            if (idFound) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);
                ErrorLabel.setText("ID saved successfully!");
            } else {
                ErrorLabel.setText("ID not found in the XML file");
            }

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            ErrorLabel.setText("Error saving ID: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ChangeButtonEnable(){
        if(actualFormat == null || actualFormat.getCorrectTextFormat().isEmpty()){
            NextText.setDisable(true);
            NextText.setDisable(false);
        }
        PreviousText.setDisable(actualTextDisplay == 0);
        if(actualTextDisplay < actualFormat.getCorrectTextFormat().size()){
            NextText.setDisable(false);
        }
        if(actualTextDisplay == actualFormat.getCorrectTextFormat().size()-1){
            NextText.setDisable(true);
        }
    }

    private void updateTextEntry() {
        if (TextEntry.getCurrentParagraph() > 2) {
            TextEntry.deletePreviousChar();
            ErrorLabel.setText("Cannot create a new line");
        } else {
            ErrorLabel.setText("");
        }

        ArrayList<String> format = actualFormat.getCorrectTextFormat();
        String actualTextFormat = format.get(actualTextDisplay);
        String currenTextEntry = TextEntry.getText();

        if (!currenTextEntry.equals(actualTextFormat)) {
            format.set(actualTextDisplay, currenTextEntry);

            actualFormat.setCorrectTextFormat(format);
            actualFormat.convertToCode();
            XMLText.setText(actualFormat.getCodeTextFormat());
            applyColor();
        }
    }

    private void ChangeDisplayedText(String direction){
        if(!actualFormat.getCorrectTextFormat().isEmpty()){
            if(direction.equals("NEXT") && actualTextDisplay < actualFormat.getCorrectTextFormat().size()-1){
                actualTextDisplay++;
                TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay));
                NameEntry.setText(actualFormat.getSpeakerName());
            }
            if(direction.equals("PREVIOUS") && actualTextDisplay > 0){
                actualTextDisplay--;
                TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay));
                NameEntry.setText(actualFormat.getSpeakerName());
            }
            applyColor();
            ChangeButtonEnable();
        }
    }

    private void displayForId(String id, String toDisplay) {
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
                                    NodeList targetList = element.getElementsByTagName("target");
                                    if (sourceList.getLength() > 0) {
                                        actualTextDisplay = 0;
                                        Node sourceNode = sourceList.item(0);
                                        Node targetNode = targetList.item(0);
                                        actualFormat = new Format(sourceNode.getTextContent(), targetNode.getTextContent());
                                        if(toDisplay.equals("source")){
                                            XMLText.setText(sourceNode.getTextContent());
                                            TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(0));
                                            TextEntry.setDisable(false);
                                        }
                                        else{
                                            XMLText.setText(targetNode.getTextContent());
                                            TextEntry.replaceText(actualFormat.getORIGINALcorrectTextFormat().get(0));
                                            TextEntry.setDisable(true);
                                        }

                                        if(actualFormat.getSpeakerName() == null){
                                            NameEntry.setDisable(true);
                                            NameEntry.clear();
                                        }
                                        else{
                                            NameEntry.setDisable(false);
                                            NameEntry.setText(actualFormat.getSpeakerName());
                                        }
                                        ChangeButtonEnable();
                                        applyColor();
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
        directoryChooser.setInitialDirectory(selectedDirectory);
        File temporaryDirectory = directoryChooser.showDialog(FolderButton.getScene().getWindow());
        if(temporaryDirectory != null && temporaryDirectory != selectedDirectory) {
            selectedDirectory = temporaryDirectory;
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
            return null;
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

    private void applyColor() {
        String text = TextEntry.getText();
        boolean isRED = false;
        if(text.contains("£") || text.contains("µ")){
            for (int i = 0; i < text.length(); i++) {
                isRED = text.charAt(i) == '£' || (text.charAt(i) != 'µ' && isRED);
                if(text.charAt(i) == '£' || text.charAt(i) == 'µ') {
                    int start = i;
                    TextEntry.replaceText(i, i+1, "");
                    text = TextEntry.getText();
                    while (i < text.length() && text.charAt(i) != '§') {
                        i++;
                    }
                    int end = i;
                    TextEntry.replaceText(i, i+1, "");
                    text = TextEntry.getText();
                    if(isRED){
                        TextEntry.setStyle(start, end,"-fx-fill: red");
                    }
                    else{
                        TextEntry.setStyle(start, end,"-fx-fill: blue");
                    }
                }
            }
        }
        else{
            TextEntry.setStyle(0, text.length(), "-fx-fill: black");
        }
    }

    private void changeTextColor(String colorSymbol, String colorName) {
        String actual = actualFormat.getCorrectTextFormat().get(actualTextDisplay);
        int selectionStart = TextEntry.getSelection().getStart();
        int selectionEnd = TextEntry.getSelection().getEnd();

        String debut = actual.substring(0, adjustPosition(actual, selectionStart));
        String milieu = actual.substring(adjustPosition(actual, selectionStart), adjustPosition(actual, selectionEnd));
        milieu = milieu.replace("£", "");
        milieu = milieu.replace("§", "");
        milieu = milieu.replace("µ", "");
        String fin = actual.substring(adjustPosition(actual, selectionEnd));
        if(!fin.isEmpty()){
            if (fin.charAt(0) == '£' || fin.charAt(0) == '§' || fin.charAt(0) == 'µ') {
                fin = fin.substring(1);
            }
        }

        if(colorSymbol.equals("§")){
            actualFormat.getCorrectTextFormat().set(actualTextDisplay, debut + milieu + fin);
        }
        else{
            actualFormat.getCorrectTextFormat().set(actualTextDisplay, debut + colorSymbol + milieu + "§" + fin);
        }
        actualFormat.convertToCode();
        XMLText.setText(actualFormat.getCodeTextFormat());
        TextEntry.setStyle(selectionStart, selectionEnd, "-fx-fill: " + colorName);
    }

    private int adjustPosition(String text, int position){
        int cpt = 0;
        for(int i = 0; i < position; i++){
            if(text.charAt(i) == '£' || text.charAt(i) == '§' || text.charAt(i) == 'µ'){
                cpt++;
            }
        }
        return position + cpt;
    }

    private void displaySource() {
        toDisplay = "source";
        TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay).replaceAll("[§µ£]", ""));
        XMLText.setText(actualFormat.getCodeTextFormat());
        TextEntry.setDisable(false);
    }

    private void displayTarget() {
        toDisplay = "target";
        TextEntry.replaceText(actualFormat.getORIGINALcorrectTextFormat().get(actualTextDisplay).replaceAll("[§µ£]", ""));
        XMLText.setText(actualFormat.getORIGINALcodeTextFormat());
        TextEntry.setDisable(true);
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
