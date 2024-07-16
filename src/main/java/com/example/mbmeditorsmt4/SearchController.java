package com.example.mbmeditorsmt4;

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
        LaunchSearch.setOnAction((ActionEvent event) -> {
            if(TagType.getValue() != null && WordSearch.getLength() != 0) {
                mainController.setErrorLabel("");
                mainController.setTextSearch(WordSearch.getText());
                mainController.setTagSearch(TagType.getValue());
                mainController.LoadFolder();
                ((Stage)LaunchSearch.getScene().getWindow()).close();
            }
            else{
                mainController.setErrorLabel("Please select a tag & text.");
            }
        });
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
