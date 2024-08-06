module interfaceEditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.fxmisc.richtext;
    requires okhttp3;
    requires java.net.http;
    requires java.desktop;


    opens interfaceEditor to javafx.fxml;
    exports interfaceEditor;
}