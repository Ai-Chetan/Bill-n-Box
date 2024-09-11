package com.example.billnbox;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationController {

    @FXML
    private TextField RegisterUsername;

    @FXML
    private PasswordField RegisterPassword;

    @FXML
    private Label emptyFields;

    @FXML
    private Button Registering;

    @FXML
    public void initialize() {
        emptyFields.setVisible(false);
    }

    @FXML
    private void Register(ActionEvent event) {
        // Check if the fields are empty
        if (RegisterUsername.getText().isEmpty() || RegisterPassword.getText().isEmpty()) {
            emptyFields.setText("Username and Password cannot be empty.");
            emptyFields.setVisible(true);
            return;
        }

        // Insert into the database
        if (insertUser(RegisterUsername.getText(), RegisterPassword.getText())) {
            // Navigate to the success page if insertion is successful
            navigateToPage(event, "5-registration-successful.fxml");
        } else {
            emptyFields.setText("Registration failed. Please try again.");
            emptyFields.setVisible(true);
        }
    }

    private boolean insertUser(String username, String password) {
        String url = "jdbc:mysql://localhost:3307/BillNBoxDB";
        String user = "root";
        String dbPassword = "rootroot";
        String sql = "INSERT INTO RegisteredUsers (Username, Password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;  // Insertion was successful

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Insertion failed
        }
    }

    @FXML
    private void RegistrationBack3(ActionEvent event) {
        // Navigate back to the previous page
        navigateToPage(event, "3-registration-page-2.fxml");
    }

    private void navigateToPage(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
