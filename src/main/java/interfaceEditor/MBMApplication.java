package interfaceEditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MBMApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MBMApplication.class.getResource("Mbm-Interface.fxml"));
        AnchorPane rootLayout = loader.load();
        Scene scene = new Scene(rootLayout);

        scene.getStylesheets().add(Objects.requireNonNull(MBMApplication.class.getResource("style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("MBM Editor SMT4");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/interfaceEditor/SMTIV.png")).toExternalForm()));
        primaryStage.resizableProperty().setValue(false);
        primaryStage.show();
    }

    public static void main(String[] args){
        Application.launch(args);
    }
}
