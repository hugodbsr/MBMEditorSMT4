module com.example.mbmeditorsmt4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.fxmisc.richtext;
    requires okhttp3;
    requires java.net.http;


    opens com.example.mbmeditorsmt4 to javafx.fxml;
    exports com.example.mbmeditorsmt4;
}