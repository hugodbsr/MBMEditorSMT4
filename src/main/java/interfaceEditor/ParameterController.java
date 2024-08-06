package interfaceEditor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ParameterController {

    @FXML
    private TextField PathDirectory;

    @FXML
    private Button OpenDirectory;

    @FXML
    private Button ApplySettings;

    private MainController mainController;

    @FXML
    private void initialize() {
        OpenDirectory.setOnAction(event -> handleOpenDirectory());

        ApplySettings.setOnAction(event -> saveSettings());
    }

    @FXML
    private void handleOpenDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(mainController.getSelectedDirectory());
        Stage stage = (Stage) OpenDirectory.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            PathDirectory.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void saveSettings() {
        String directoryPath = PathDirectory.getText();

        Properties properties = new Properties();

        properties.setProperty("DirectoryPath", directoryPath);

        File iniFile = new File("settings.ini");

        try (FileOutputStream fileOut = new FileOutputStream(iniFile)) {
            properties.store(fileOut, "Application Settings");
            mainController.setErrorLabel("Application Settings saved");
            mainController.setSelectedDirectory(directoryPath);
            ((Stage)ApplySettings.getScene().getWindow()).close();
            mainController.LoadFolder();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setPathDirectory(String pathDirectory) {
        PathDirectory.setText(pathDirectory);
    }
}
