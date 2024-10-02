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
    private boolean isOwner = LoginController.getIsOwner();
    private boolean isEditing = false; // Track if the profile is in edit mode
    private String Name, EmailID, PhoneNo, ShopName, ShopAddress;
    private int ownerId;
    @FXML
    private TextField nameField, emailField, mobnoField, shopnameField, filePath;
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
        makeFieldsNonEditable(); // Make all fields non-editable initially
    }

    private void loadUserProfile() {
        if (username == null || username.isEmpty()) {
            showError("No user is logged in.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            if (isOwner) {
                loadOwnerDetails(conn); // Load owner details if they are an owner
            } else {
                loadEmployeeDetails(conn); // Load employee details
            }
        } catch (SQLException e) {
            showError("Error loading profile: " + e.getMessage());
        }
        filePath.setText(RegistrationController.FilePath);
    }

    private void loadOwnerDetails(Connection conn) throws SQLException {
        String ownerSql = "SELECT Name, EmailID, PhoneNo, ShopName, ShopAddress FROM Owner WHERE Username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(ownerSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("Name"));
                emailField.setText(rs.getString("EmailID"));
                mobnoField.setText(rs.getString("PhoneNo"));
                shopnameField.setText(rs.getString("ShopName"));
                shopaddressField.setText(rs.getString("ShopAddress"));
            } else {
                showError("Owner profile not found.");
            }
        }
    }

    private void loadEmployeeDetails(Connection conn) throws SQLException {
        String employeeSql = "SELECT Name, EmailID, PhoneNo, OwnerID FROM Employee WHERE Username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(employeeSql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("Name"));
                emailField.setText(rs.getString("EmailID"));
                mobnoField.setText(rs.getString("PhoneNo"));
                ownerId = rs.getInt("OwnerID");

                if (ownerId > 0) {
                    loadShopDetails(conn); // Load shop details only if OwnerID is valid
                } else {
                    // If there's no valid OwnerID, clear shop fields or set a message indicating no owner/shop
                    shopnameField.setText("No associated shop");
                    shopaddressField.setText("No associated address");
                }
            } else {
                showError("Employee profile not found.");
            }
        }
    }

    private void loadShopDetails(Connection conn) throws SQLException {
        String shopDetailsSql = "SELECT ShopName, ShopAddress FROM Owner WHERE OwnerID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(shopDetailsSql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                shopnameField.setText(rs.getString("ShopName"));
                shopaddressField.setText(rs.getString("ShopAddress"));
            } else {
                // If the shop details are not found, display a meaningful message
                shopnameField.setText("Shop details not found");
                shopaddressField.setText("Shop details not found");
            }
        }
    }


    // Method to toggle between edit and save modes
    @FXML
    private void updateProfile(ActionEvent event) {
        if (isEditing) {
            // Validate inputs before saving
            if (isValidInput()) {
                // Save changes to the database
                RegistrationController.FilePath = filePath.getText();
                saveProfile();

                // Only make fields non-editable and change the button text if the save is successful
                makeFieldsNonEditable();
                profilebtn.setText("Edit Profile"); // Change button text back to "Edit Profile"
                isEditing = false; // Switch back to non-editing mode
            }
        } else {
            // Enable fields for editing
            if (isOwner) {
                makeOwnerFieldsEditable();
            } else {
                makeEmployeeFieldsEditable();
            }
            profilebtn.setText("Save Changes"); // Change button text to "Save Changes"
            isEditing = true; // Switch to editing mode
        }
    }

    // Validation method to ensure mobile number is correct and other fields are filled
    private boolean isValidInput() {
        String mobno = mobnoField.getText();

        // Check if the mobile number is valid (10 digits)
        if (mobno.length() != 10 || !mobno.matches("\\d+")) {
            showError("Mobile number must be exactly 10 digits.");
            return false;
        }

        // Check if any required fields are empty
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || mobno.isEmpty() ||
                (isOwner && (shopnameField.getText().isEmpty() || shopaddressField.getText().isEmpty()))) {
            showError("All fields must be filled out.");
            return false;
        }

        // If all validations pass, return true
        return true;
    }

    // Method to make all fields non-editable
    private void makeFieldsNonEditable() {
        nameField.setEditable(false);
        emailField.setEditable(false);
        mobnoField.setEditable(false);
        shopnameField.setEditable(false); // Shop name is non-editable by default
        shopaddressField.setEditable(false); // Shop address is non-editable by default
    }

    // Method to make only employee fields editable
    private void makeEmployeeFieldsEditable() {
        nameField.setEditable(true);
        emailField.setEditable(true);
        mobnoField.setEditable(true);
        // Employee cannot edit shop details
        shopnameField.setEditable(false);
        shopaddressField.setEditable(false);
    }

    // Method to make owner fields editable
    private void makeOwnerFieldsEditable() {
        nameField.setEditable(true);
        emailField.setEditable(true);
        mobnoField.setEditable(true);
        shopnameField.setEditable(true);
        shopaddressField.setEditable(true);
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

        // Validate mobile number length (should be exactly 10 digits)
        if (mobno.length() != 10 || !mobno.matches("\\d+")) {
            showError("Mobile number must be exactly 10 digits.");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || mobno.isEmpty() || shopname.isEmpty() || shopaddress.isEmpty()) {
            showError("All fields must be filled out.");
            return;
        }

        String updateOwnerSQL = isOwner ? "UPDATE Owner SET ShopName = ?, ShopAddress = ?, Name = ?, EmailID = ?, PhoneNo = ? WHERE Username = ?" : null;
        // Update employee details
        String updateEmployeeSQL = "UPDATE Employee SET Name = ?, EmailID = ?, PhoneNo = ? WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {

            // Update owner details if the user is the owner
            if (isOwner) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateOwnerSQL)) {
                    pstmt.setString(1, shopname);
                    pstmt.setString(2, shopaddress);
                    pstmt.setString(3, name);
                    pstmt.setString(4, email);
                    pstmt.setString(5, mobno);
                    pstmt.setString(6, username);
                    pstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(updateEmployeeSQL)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, mobno);
                    pstmt.setString(4, username);
                    pstmt.executeUpdate();
                }
            }
            showError("Profile updated successfully.");
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
