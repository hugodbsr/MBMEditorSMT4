module com.example.mbmeditorsmt4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.fxmisc.richtext;
    requires okhttp3;
    requires java.net.http;


    opens interfaceEditor to javafx.fxml;
    exports interfaceEditor;
}