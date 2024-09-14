package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileController {

    private String Name;
    private String EmailID;
    private String MobileNumber;
    private String ShopName;
    private String ShopAddress;

    @FXML
    private TextField nameField, emailField, mobnoField, shopnameField;

    @FXML
    private TextArea shopaddressField;

    @FXML
    private Label emptyFields;

    // Database connection details
    public static class DatabaseConnector {

        public static void storeUser(String username, String password, String name, String email, String mobno, String shopname, String shopaddress) {
            String insertSQL = "INSERT INTO Owner (Username, Password, Name, EmailID, PhoneNo, ShopName, ShopAddress) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, email);
                pstmt.setString(5, mobno);
                pstmt.setString(6, shopname);
                pstmt.setString(7, shopaddress);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User registered successfully.");
                } else {
                    System.out.println("Registration failed.");
                }
            } catch (SQLException e) {
                System.out.println("Database connection failed: " + e.getMessage());
            }
        }
    }

    // Initialize controller
    @FXML
    public void initialize() {
        if (emptyFields != null) {
            emptyFields.setVisible(false);  // Hide the emptyFields label initially
        }
    }

    @FXML
    private void LogInButton(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }

    // Show error message in label
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);
        }
    }

    // Simple navigation without data passing
    private void navigateToPage(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + fxmlFile));
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
