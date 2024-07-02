package com.example.mbmeditorsmt4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MBMApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MBMApplication.class.getResource("Mbm-Interface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 890, 518);
        stage.setTitle("Mbm Editor for Shin Megami Tensei IV");
        stage.setScene(scene);
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}