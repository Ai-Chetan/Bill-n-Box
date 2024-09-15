package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {

    private final String username = SessionManager.getInstance().getUsername(); // Variable to store the username
    private boolean isEditing = false; // Track if the profile is in edit mode

    @FXML
    private TextField nameField, emailField, mobnoField, shopnameField;

    @FXML
    private TextArea shopaddressField;

    @FXML
    private Label emptyFields;

    @FXML
    private Button profilebtn; // Button for edit/save profile

    @FXML
    public void initialize() {
        if (emptyFields != null) {
            emptyFields.setVisible(false);  // Hide the emptyFields label initially
        }
        if (profilebtn != null) {
            profilebtn.setText("Edit Profile");  // Initial button text
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        if (username == null || username.isEmpty()) {
            showError("No user is logged in.");
            return;
        }

        String sql = "SELECT Name, EmailID, PhoneNo, ShopName, ShopAddress FROM Owner WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("Name"));
                emailField.setText(rs.getString("EmailID"));
                mobnoField.setText(rs.getString("PhoneNo"));
                shopnameField.setText(rs.getString("ShopName"));
                shopaddressField.setText(rs.getString("ShopAddress"));
            } else {
                showError("User profile not found.");
            }

        } catch (SQLException e) {
            showError("Error loading profile: " + e.getMessage());
        }

        makeFieldsNonEditable(); // Make fields non-editable initially
    }

    // Method to make all fields non-editable
    private void makeFieldsNonEditable() {
        nameField.setEditable(false);
        emailField.setEditable(false);
        mobnoField.setEditable(false);
        shopnameField.setEditable(false);
        shopaddressField.setEditable(false);
    }

    // Method to make all fields editable
    private void makeFieldsEditable() {
        nameField.setEditable(true);
        emailField.setEditable(true);
        mobnoField.setEditable(true);
        shopnameField.setEditable(true);
        shopaddressField.setEditable(true);
    }

    // Method to toggle between edit and save modes
    @FXML
    private void updateProfile(ActionEvent event) {
        if (isEditing) {
            // Save changes to the database
            saveProfile();
            makeFieldsNonEditable();
            profilebtn.setText("Edit Profile"); // Change button text back to "Edit Profile"
            isEditing = false; // Switch back to non-editing mode
        } else {
            // Enable fields for editing
            makeFieldsEditable();
            profilebtn.setText("Save Changes"); // Change button text to "Save Changes"
            isEditing = true; // Switch to editing mode
        }
    }

    // Save profile information to the database
    private void saveProfile() {
        if (username == null || username.isEmpty()) {
            showError("No user is logged in.");
            return;
        }

        String name = nameField.getText();
        String email = emailField.getText();
        String mobno = mobnoField.getText();
        String shopname = shopnameField.getText();
        String shopaddress = shopaddressField.getText();

        if (name.isEmpty() || email.isEmpty() || mobno.isEmpty() || shopname.isEmpty() || shopaddress.isEmpty()) {
            showError("All fields must be filled out.");
            return;
        }

        String updateSQL = "UPDATE Owner SET Name = ?, EmailID = ?, PhoneNo = ?, ShopName = ?, ShopAddress = ? WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, mobno);
            pstmt.setString(4, shopname);
            pstmt.setString(5, shopaddress);
            pstmt.setString(6, username);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showError("Profile updated successfully.");
            } else {
                showError("Profile update failed.");
            }

        } catch (SQLException e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    // Show error message in label
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);
        }
    }

    @FXML
    private void LogInButton(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
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
