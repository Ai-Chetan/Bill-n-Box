module com.example.billnbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires transitive itextpdf;
    requires java.compiler;


    opens com.example.billnbox to javafx.fxml;
    exports com.example.billnbox;
}