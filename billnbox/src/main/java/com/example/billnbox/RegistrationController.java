package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

public class RegistrationController {

    private String Name;
    private String EmailID;
    private String MobileNumber;
    private String ShopName;
    private String ShopAddress;
    private String Username;
    private String Password;
    private String ConfirmPassword;

    @FXML
    private TextField nameField, emailField, mobnoField, shopnameField, usernameField, passwordField, confirmpasswordField;

    @FXML
    private TextArea shopaddressField;

    @FXML
    private Label emptyFields;

    // Database connection details
    public static class DatabaseConnector {

        private static final String DB_URL = "jdbc:mysql://localhost:3307/BillNBoxDB";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "root!123";

        public static void storeUser(String username, String password, String name, String email, String mobno, String shopname, String shopaddress) {
            String insertSQL = "INSERT INTO Owner (Username, Password, Name, EmailID, PhoneNo, ShopName, ShopAddress) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
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

    // Move to Page 2 (Shop Details)
    @FXML
    private void RegistrationNext1(ActionEvent event) {
        Name = nameField.getText();
        EmailID = emailField.getText();
        MobileNumber = mobnoField.getText();

        // Pass data to next controller
        if (validateFields(Name, EmailID, MobileNumber)) {
            navigateToPageWithData(event, "3-registration-page-2.fxml");
        }
    }

    // Move to Page 3 (User Credentials)
    @FXML
    private void RegistrationNext2(ActionEvent event) {
        ShopName = shopnameField.getText();
        ShopAddress = shopaddressField.getText();

        if (validateFields(ShopName, ShopAddress)) {
            navigateToPageWithData(event, "4-registration-page-3.fxml");
        }
    }

    // Navigate to previous pages
    @FXML
    private void RegistrationBack2(ActionEvent event) {
        navigateToPageWithData(event, "2-registration-page-1.fxml");
    }

    @FXML
    private void RegistrationBack3(ActionEvent event) {
        navigateToPageWithData(event, "3-registration-page-2.fxml");
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        navigateToPage(event, "1-login-page.fxml");
    }

    @FXML
    private void gotoDashboard(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }

    // Final registration process
    @FXML
    private void handleRegister(ActionEvent event) {
        Username = usernameField.getText();
        Password = passwordField.getText();
        ConfirmPassword = confirmpasswordField.getText();

        if (!Password.equals(ConfirmPassword)) {
            showError("Confirm Password does not match");
        } else if (Username.isEmpty() || Password.isEmpty()) {
            showError("Username and Password Fields cannot be Empty");
        } else {
            DatabaseConnector.storeUser(Username, Password, Name, EmailID, MobileNumber, ShopName, ShopAddress);
            navigateToPage(event, "5-registration-successful.fxml");
        }
    }

    // Validation for empty fields
    private boolean validateFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                showError("All fields must be filled");
                return false;
            }
        }
        return true;
    }

    // Show error message in label
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);
        }
    }

    // Navigation method with data passing
    private void navigateToPageWithData(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + fxmlFile));
            Parent root = loader.load();

            // Get the next controller and pass the current data
            RegistrationController nextController = loader.getController();
            nextController.setData(Name, EmailID, MobileNumber, ShopName, ShopAddress);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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

    // Setter method for passing data to next page
    public void setData(String name, String email, String mobileNumber, String shopName, String shopAddress) {
        this.Name = name;
        this.EmailID = email;
        this.MobileNumber = mobileNumber;
        this.ShopName = shopName;
        this.ShopAddress = shopAddress;
    }
}
