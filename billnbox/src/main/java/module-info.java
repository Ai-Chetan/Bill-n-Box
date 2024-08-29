module com.example.billnbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.billnbox to javafx.fxml;
    exports com.example.billnbox;
}