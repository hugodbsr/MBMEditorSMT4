package interfaceEditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SearchController {

    @FXML
    private ChoiceBox<String> TagType;

    @FXML
    private TextField WordSearch;

    @FXML
    private Button LaunchSearch;

    private MainController mainController;

    @FXML
    protected void initialize() {
        TagType.getItems().addAll("Source", "Target", "All");
        LaunchSearch.setOnAction(this::handleSearchAction);
    }

    private void handleSearchAction(ActionEvent event) {
        if (TagType.getValue() != null && !WordSearch.getText().isEmpty()) {
            mainController.setErrorLabel("");
            mainController.setTextSearch(WordSearch.getText());
            mainController.setTagSearch(TagType.getValue());
            mainController.LoadFolder();
            closeWindow();
        } else {
            mainController.setErrorLabel("Please select a tag & text.");
        }
    }

    private void closeWindow() {
        ((Stage) LaunchSearch.getScene().getWindow()).close();
    }

    public void setTagType(String tagType) {
        TagType.setValue(tagType);
    }

    public void setWordSearch(String wordSearch) {
        WordSearch.setText(wordSearch);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

}
