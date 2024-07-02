module com.example.mbmeditorsmt4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mbmeditorsmt4 to javafx.fxml;
    exports com.example.mbmeditorsmt4;
}