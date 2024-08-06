package interfaceEditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.stage.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.fxmisc.richtext.InlineCssTextArea;
import javax.xml.parsers.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.util.*;

import static interfaceEditor.TextUtils.*;
import static interfaceEditor.FileUtils.*;

public class MainController{
    @FXML
    private MenuItem redButton;

    @FXML
    private MenuItem blackButton;

    @FXML
    private MenuItem blueButton;

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

    @FXML
    private ImageView BGImage;

    @FXML
    private Button BG1;

    @FXML
    private Button BG2;

    @FXML
    private Button BG3;

    @FXML
    private Button BG4;

    @FXML
    private Button BG5;

    @FXML
    private Button BG6;

    @FXML
    private MenuItem Search1;

    @FXML
    private MenuItem Search2;

    @FXML
    private ToolBar buttonToolBar;

    @FXML
    private Button CopyID;

    @FXML
    private MenuItem CopyWholeSource;

    @FXML
    private MenuItem CopyWholeTarget;

    @FXML
    private MenuItem ParameterButton;

    @FXML
    private MenuItem ApplyID;

    @FXML
    private MenuItem ApplyWhole;

    DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedDirectory;
    private final HashMap<String, String> fileLocations = new HashMap<>();
    private int actualTextDisplay = 0;
    private Format actualFormat;
    private String toDisplay = "source";
    private String actualID;
    private int BGLn = 2;

    private String textSearch;
    private String tagSearch;

    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();

    @FXML
    private void initialize() {
        CopyWholeSource.setOnAction(e -> CopyWholeID("source"));
        CopyWholeTarget.setOnAction(e -> CopyWholeID("target"));

        ApplyID.setOnAction(e -> handleApplyID());
        ApplyWhole.setOnAction(e -> handleApplyWhole());
        CopyID.setOnAction(e -> handleCopyID());

        Search1.setOnAction(event -> openNewWindow("Search-Interface"));
        Search2.setOnAction(event -> {
            textSearch = "";
            tagSearch = "";
            LoadFolder();
        });

        ParameterButton.setOnAction(event -> openNewWindow("Parameter-Interface"));

        redButton.setOnAction(event -> changeTextColor("£", "red"));
        blackButton.setOnAction(event -> changeTextColor("§", "black"));
        blueButton.setOnAction(event -> changeTextColor("µ", "blue"));

        SourceButton.setOnAction(event -> displaySource());
        OriginButton.setOnAction(event -> displayTarget());
        SaveIDButton.setOnAction(event -> SaveActualID());
        ResetID.setOnAction(event -> {
            actualFormat = new Format(actualFormat.getORIGINALcodeTextFormat(), actualFormat.getORIGINALcodeTextFormat());
            XMLText.setText(actualFormat.getORIGINALcorrectTextFormat().get(0));
            TextEntry.replaceText(actualFormat.getORIGINALcorrectTextFormat().get(0));
        });

        BG1.setOnAction(event -> changeBackgroundIMG("Dialogue", 143.0, 551.0, 1.0, 121.0, 85.0, 467.0, 31.0, 150.0));
        BG2.setOnAction(event -> changeBackgroundIMG("Explanation", 218.0, 563.0, 3.0, 143.0, 85.0, 467.0, 31.0, 150.0));
        BG3.setOnAction(event -> changeBackgroundIMG("Quest", 218.0, 563.0, 4.0, 23.0, 250.0, 515.0, 30.0, 27.0));
        BG4.setOnAction(event -> changeBackgroundIMG("Tutorial", 218.0, 563.0, 3.0, 38.0, 120.0, 473.0, 33.0, 80.0));
        BG5.setOnAction(event -> changeBackgroundIMG("Choice", 218.0, 563.0, 3.0, 24.0, 133.0, 517.0, 60.0, 100.0));
        BG6.setOnAction(event -> changeBackgroundIMG("", 0, 0, 0, 0, 230, 550, 0, 0));

        PreviousText.setOnAction(event -> ChangeDisplayedText("PREVIOUS"));
        NextText.setOnAction(event -> ChangeDisplayedText("NEXT"));

        FolderButton.setOnAction(event -> SelectAndLoadFolder());
        MenuSelectFolder.setOnAction(event -> SelectAndLoadFolder());

        TextEntry.setOnKeyTyped(event -> updateTextEntry());
        NameEntry.setOnKeyTyped(e -> handleNameEntry());

        configureTreeViewListeners();
        loadProperties();

        configureContextMenu();
    }

    private void configureContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openDirectoryMenuItem = new MenuItem("Open Directory");
        contextMenu.getItems().add(openDirectoryMenuItem);
        openDirectoryMenuItem.setOnAction(event -> openFileDirectory());

        FolderView.setOnContextMenuRequested(event -> {
            if (FolderView.getSelectionModel().getSelectedItem() != null) {
                contextMenu.show(FolderView, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });
    }

    private void configureTreeViewListeners() {
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

    private void handleApplyID() {
        if (actualID == null || actualID.isEmpty()) {
            ErrorLabel.setText("No ID selected to apply.");
            return;
        }
        SaveActualID();
        copySourceToTarget(actualID);
        displayForId(actualID, toDisplay);
    }

    private void handleApplyWhole() {
        if (selectedDirectory == null) {
            ErrorLabel.setText("No directory selected.");
            return;
        }
        String filePath = buildFilePath(FolderView.getSelectionModel().getSelectedItem());
        if (!filePath.toLowerCase().endsWith(".xml")) {
            ErrorLabel.setText("Selected file is not an XML file.");
            return;
        }
        File selectedFile = new File(filePath);
        String relativePath = fileLocations.get(selectedFile.getName());
        if (relativePath == null) {
            ErrorLabel.setText("Relative path not found for the selected file.");
            return;
        }
        File xmlFile = new File(selectedDirectory, relativePath);
        try {
            Document document = parseXmlFile(xmlFile);
            NodeList entryList = document.getElementsByTagName("entry");
            if (actualID != null) {
                SaveActualID();
            }
            processEntries(entryList);
            displayForId(actualID, toDisplay);
        } catch (Exception e) {
            handleError("Error processing XML file", e);
        }
    }

    private Document parseXmlFile(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        document.getDocumentElement().normalize();
        return document;
    }

    private void processEntries(NodeList entryList) {
        for (int i = 0; i < entryList.getLength(); i++) {
            Node node = entryList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.hasAttribute("id")) {
                    String id = element.getAttribute("id");
                    System.out.println(id);
                    copySourceToTarget(id);
                }
            }
        }
    }

    private void handleCopyID() {
        if (actualFormat == null) {
            ErrorLabel.setText("No format available to copy.");
            return;
        }
        content.putString(actualFormat.toStringCorrect());
        clipboard.setContent(content);
        ErrorLabel.setText("Text has been copied to Clipboard.");
    }

    private void handleNameEntry() {
        if (actualFormat != null && !actualFormat.getSpeakerName().equals(XMLText.getText())) {
            actualFormat.setSpeakerName(NameEntry.getText());
            actualFormat.convertToCode();
            XMLText.setText(actualFormat.getCodeTextFormat());
        }
    }


    private void openFileDirectory() {
        TreeItem<String> selectedItem = FolderView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String filePath = buildFilePath(selectedItem);
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IOException e) {
                    ErrorLabel.setText("Error when try to open directory : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                ErrorLabel.setText("Selected file does not exist.");
            }
        } else {
            ErrorLabel.setText("No file selected.");
        }
    }

    private void handleError(String message, Exception e) {
        ErrorLabel.setText(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public void CopyWholeID(String tag) {
        String filePath = buildFilePath(FolderView.getSelectionModel().getSelectedItem());
        File selectedFile = new File(filePath);
        String relativePath = fileLocations.get(selectedFile.getName());
        if (relativePath == null) {
            ErrorLabel.setText("Relative path not found for the selected file.");
            return;
        }
        File xmlFile = new File(selectedDirectory, relativePath);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            document.getDocumentElement().normalize();
            NodeList entryList = document.getElementsByTagName("entry");

            String renvoie = "";

            for (int i = 0; i < entryList.getLength(); i++) {
                Node node = entryList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList sourceList = element.getElementsByTagName("source");
                    NodeList targetList = element.getElementsByTagName("target");
                    if (sourceList.getLength() > 0) {
                        actualTextDisplay = 0;
                        Node sourceNode = sourceList.item(0);
                        Node targetNode = targetList.item(0);
                        Format format = new Format(sourceNode.getTextContent(), targetNode.getTextContent());
                        if (tag.equals("source")) {
                            renvoie += format.toStringCorrect();
                        }
                        if (tag.equals("target")) {
                            renvoie += format.toStringCorrectORIGINAL();
                        }
                    }
                }
            }

            content.putString(renvoie);
            clipboard.setContent(content);
            ErrorLabel.setText("Text has been copied to Clipboard.");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            ErrorLabel.setText("Error processing XML file: " + e.getMessage());
        }
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try {
            File iniFile = new File("settings.ini");
            FileInputStream inputStream = new FileInputStream(iniFile);
            properties.load(inputStream);
            inputStream.close();

            String directoryPath = properties.getProperty("DirectoryPath");
            if (directoryPath != null && !directoryPath.isEmpty()) {
                selectedDirectory = new File(directoryPath);
                directoryChooser.setInitialDirectory(selectedDirectory);
                LoadFolder();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openNewWindow(String windowName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(windowName + ".fxml"));

            Parent root = fxmlLoader.load();

            Stage stage = new Stage();

            if(windowName.equals("Search-Interface")){
                SearchController secondController = fxmlLoader.getController();
                secondController.setMainController(this);
                secondController.setTagType(tagSearch);
                secondController.setWordSearch(textSearch);

                stage.setTitle("Search Text");
            }
            else if(windowName.equals("Parameter-Interface")){
                ParameterController secondController = fxmlLoader.getController();
                secondController.setMainController(this);
                secondController.setPathDirectory(selectedDirectory.getAbsolutePath());
                stage.setTitle("Settings");
            }


            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeBackgroundIMG(String fileName, double fitHeight, double fitWidth, double layoutX, double layoutY, double TprefHeight, double TprefWidth, double TlayoutX, double TlayoutY) {
        if(!fileName.isEmpty()){
            String imagePath = Objects.requireNonNull(getClass().getResource("/interfaceEditor/" + fileName + ".png")).toExternalForm();
            Image image = new Image(imagePath);
            BGImage.setImage(image);
        }
        else{
            BGImage.imageProperty().set(null);
            BGLn = 20;
        }

        BGImage.setFitHeight(fitHeight);
        BGImage.setFitWidth(fitWidth);
        BGImage.setLayoutX(layoutX);
        BGImage.setLayoutY(layoutY);

        TextEntry.setPrefHeight(TprefHeight);
        TextEntry.setPrefWidth(TprefWidth);
        TextEntry.setLayoutX(TlayoutX);
        TextEntry.setLayoutY(TlayoutY);

        if(fileName.equals("Dialogue")){
            TextEntry.setStyle("-fx-font-size: 21px;");
            NameEntry.setDisable(false);
            if(actualFormat!=null){
                if(actualFormat.getSpeakerName()!=null){
                    NameEntry.setText(actualFormat.getSpeakerName());
                }
            }
            NameEntry.setOpacity(1);
            BGLn = 2;
        }
        else{
            NameEntry.setDisable(true);
            NameEntry.setOpacity(0);
        }
        if(fileName.equals("Explanation")){
            TextEntry.setStyle("-fx-font-size: 21px;");
            BGLn = 2;
        }
        if(fileName.equals("Quest")){
            TextEntry.setStyle("-fx-font-size: 19px");
            BGLn = 5;
        }
        if(fileName.equals("Tutorial")){
            TextEntry.setStyle("-fx-font-size: 21px;");
            BGLn = 3;
        }
        if(fileName.equals("Choice")){
            TextEntry.setStyle("-fx-font-size: 21px;");
            BGLn = 2;
        }
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
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);
                ErrorLabel.setText("ID saved successfully!");
            } else {
                ErrorLabel.setText("ID not found in the XML file");
            }

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            ErrorLabel.setText("Error saving ID: " + e.getMessage());
        }
    }

    private void ChangeButtonEnable() {
        if (actualFormat == null || actualFormat.getCorrectTextFormat().isEmpty()) {
            NextText.setDisable(true);
            PreviousText.setDisable(true);
            return;
        }
        PreviousText.setDisable(actualTextDisplay == 0);
        NextText.setDisable(actualTextDisplay >= actualFormat.getCorrectTextFormat().size() - 1);
    }

    private void updateTextEntry() {
        if (actualFormat == null) {
            ErrorLabel.setText("No format available to update.");
            return;
        }

        if (TextEntry.getCurrentParagraph() > BGLn) {
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
            applyColor(TextEntry);
        }
    }

    private void ChangeDisplayedText(String direction) {
        if (actualFormat == null || actualFormat.getCorrectTextFormat().isEmpty()) {
            ErrorLabel.setText("No text format available to display.");
            return;
        }

        if (direction.equals("NEXT") && actualTextDisplay < actualFormat.getCorrectTextFormat().size() - 1) {
            actualTextDisplay++;
            TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay));
            NameEntry.setText(actualFormat.getSpeakerName());
        } else if (direction.equals("PREVIOUS") && actualTextDisplay > 0) {
            actualTextDisplay--;
            TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay));
            NameEntry.setText(actualFormat.getSpeakerName());
        }
        applyColor(TextEntry);
        ChangeButtonEnable();
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
                        Document document = parseXmlFile(xmlFile);
                        NodeList entryList = document.getElementsByTagName("entry");
                        Element element = findElementById(entryList, id);

                        if (element != null) {
                            NodeList sourceList = element.getElementsByTagName("source");
                            NodeList targetList = element.getElementsByTagName("target");
                            if (sourceList.getLength() > 0 && targetList.getLength() > 0) {
                                actualTextDisplay = 0;
                                Node sourceNode = sourceList.item(0);
                                Node targetNode = targetList.item(0);
                                actualFormat = new Format(sourceNode.getTextContent(), targetNode.getTextContent());

                                if (toDisplay.equals("source")) {
                                    XMLText.setText(sourceNode.getTextContent());
                                    TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(0));
                                } else {
                                    XMLText.setText(targetNode.getTextContent());
                                    TextEntry.replaceText(actualFormat.getORIGINALcorrectTextFormat().get(0));
                                    TextEntry.setDisable(true);
                                    buttonToolBar.setDisable(true);
                                }

                                if (actualFormat.getSpeakerName() == null) {
                                    NameEntry.setDisable(true);
                                    buttonToolBar.setDisable(true);
                                    NameEntry.clear();
                                } else {
                                    NameEntry.setDisable(false);
                                    buttonToolBar.setDisable(false);
                                    TextEntry.setDisable(false);
                                    NameEntry.setText(actualFormat.getSpeakerName());
                                }
                                ChangeButtonEnable();
                                applyColor(TextEntry);
                            } else {
                                ErrorLabel.setText("Source or target not found in the XML file");
                            }
                        } else {
                            ErrorLabel.setText("ID not found in the XML file");
                        }
                    } catch (Exception e) {
                        handleError("Error displaying ID", e);
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
                    Document document = parseXmlFile(xmlFile);
                    NodeList entryList = document.getElementsByTagName("entry");

                    for (int i = 0; i < entryList.getLength(); i++) {
                        Node node = entryList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;
                            if (element.hasAttribute("id")) {
                                if (tagSearch != null && textSearch != null) {
                                    if (textSearch.isEmpty()) {
                                        IDView.getItems().add("id" + element.getAttribute("id"));
                                    } else {
                                        if (tagSearch.equals("All") || tagSearch.equals("Source")) {
                                            NodeList sourceList = element.getElementsByTagName("source");
                                            for (int j = 0; j < sourceList.getLength(); j++) {
                                                if (sourceList.getLength() > j && sourceList.item(j).getTextContent().contains(textSearch)) {
                                                    IDView.getItems().add("id" + element.getAttribute("id"));
                                                }
                                            }
                                        }
                                        if (tagSearch.equals("All") || tagSearch.equals("Target")) {
                                            NodeList targetList = element.getElementsByTagName("target");
                                            for (int j = 0; j < targetList.getLength(); j++) {
                                                if (targetList.getLength() > j && targetList.item(j).getTextContent().contains(textSearch)) {
                                                    IDView.getItems().add("id" + element.getAttribute("id"));
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    IDView.getItems().add("id" + element.getAttribute("id"));
                                }
                            }
                        }
                    }
                    if (IDView.getItems().isEmpty()) {
                        ErrorLabel.setText("No ID in this file");
                    } else {
                        ErrorLabel.setText("");
                    }
                } catch (Exception e) {
                    handleError("Error loading IDs", e);
                }
            }
        }
    }

    private void SelectAndLoadFolder() {
        directoryChooser.setInitialDirectory(selectedDirectory);
        File temporaryDirectory = directoryChooser.showDialog(FolderButton.getScene().getWindow());
        if(temporaryDirectory != null && temporaryDirectory != selectedDirectory) {
            selectedDirectory = temporaryDirectory;
            LoadFolder();
        }
    }

    public void LoadFolder(){
        IDView.getItems().clear();
        TextEntry.clear();
        XMLText.setText("");
        TreeItem<String> rootItem = new TreeItem<>(selectedDirectory.getAbsolutePath());
        rootItem.setExpanded(true);
        try {
            populateTreeView(selectedDirectory, rootItem);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        FolderView.setRoot(rootItem);
        FolderButton.setDisable(true);
        FolderButton.setVisible(false);
    }

    private void populateTreeView(File directory, TreeItem<String> parentItem) throws ParserConfigurationException, IOException, SAXException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (containsXmlFiles(file)) {
                        TreeItem<String> newItem = new TreeItem<>(file.getName());
                        populateTreeView(file, newItem);
                        if (!newItem.getChildren().isEmpty()) {
                            parentItem.getChildren().add(newItem);
                        }
                    }
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                    if (fileMatchesSearch(file)) {
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
    }

    private boolean fileMatchesSearch(File file) {
        if (tagSearch == null && textSearch == null) {
            return true;
        }
        if (textSearch.isEmpty()) {
            return true;
        }
        try {
            Document document = parseXmlFile(file);
            NodeList entryList = document.getElementsByTagName("entry");

            for (int i = 0; i < entryList.getLength(); i++) {
                Node node = entryList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (tagSearch.equals("All") || tagSearch.equals("Source")) {
                        NodeList sourceList = element.getElementsByTagName("source");
                        if (containsText(sourceList, textSearch)) {
                            return true;
                        }
                    }
                    if (tagSearch.equals("All") || tagSearch.equals("Target")) {
                        NodeList targetList = element.getElementsByTagName("target");
                        if (containsText(targetList, textSearch)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean containsText(NodeList nodeList, String text) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getTextContent().contains(text)) {
                return true;
            }
        }
        return false;
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

    private void displaySource() {
        toDisplay = "source";
        TextEntry.replaceText(actualFormat.getCorrectTextFormat().get(actualTextDisplay));
        XMLText.setText(actualFormat.getCodeTextFormat());
        applyColor(TextEntry);
        TextEntry.setDisable(false);
    }

    private void displayTarget() {
        toDisplay = "target";
        TextEntry.replaceText(actualFormat.getORIGINALcorrectTextFormat().get(actualTextDisplay));
        XMLText.setText(actualFormat.getORIGINALcodeTextFormat());
        applyColor(TextEntry);
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

    private void copySourceToTarget(String id) {
        if (selectedDirectory != null) {
            String filePath = buildFilePath(FolderView.getSelectionModel().getSelectedItem());
            if (filePath.toLowerCase().endsWith(".xml")) {
                File selectedFile = new File(filePath);
                String relativePath = fileLocations.get(selectedFile.getName());
                if (relativePath != null) {
                    File xmlFile = new File(selectedDirectory, relativePath);
                    try {
                        Document document = parseXmlFile(xmlFile);
                        NodeList entryList = document.getElementsByTagName("entry");
                        Element element = findElementById(entryList, id);

                        if (element != null) {
                            NodeList sourceList = element.getElementsByTagName("source");
                            NodeList targetList = element.getElementsByTagName("target");
                            if (targetList.getLength() > 0) {
                                Node targetNode = targetList.item(0);
                                if (id.equals(actualID)) {
                                    targetNode.setTextContent(actualFormat.getCodeTextFormat());
                                } else {
                                    targetNode.setTextContent(sourceList.item(0).getTextContent());
                                }

                                FileUtils.saveDocument(document, xmlFile);
                            } else {
                                ErrorLabel.setText("Target not found in the XML file");
                            }
                        } else {
                            ErrorLabel.setText("ID not found in the XML file");
                        }
                    } catch (Exception e) {
                        handleError("Error copying source to target", e);
                    }
                }
            }
        }
    }

    public void setErrorLabel(String errorLabel) {
        ErrorLabel.setText(errorLabel);
    }

    public void setTextSearch(String textSearch) {
        this.textSearch = textSearch;
    }

    public void setTagSearch(String tagSearch) {
        this.tagSearch = tagSearch;
    }

    public File getSelectedDirectory() {
        return selectedDirectory;
    }

    public void setSelectedDirectory(String selectedDirectory) {
        this.selectedDirectory = new File(selectedDirectory);
    }
}
