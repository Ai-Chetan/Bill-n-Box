package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePasswordController {

    private final String username = SessionManager.getInstance().getUsername();

    @FXML
    private PasswordField oldPassword, newPassword, newPasswordConfirmation;

    @FXML
    private Label emptyFields; // Label to show error messages

    @FXML
    public void initialize() {
        if (emptyFields != null) {
            emptyFields.setVisible(false);  // Hide the emptyFields label initially
        }
    }

    // Action method for changing the password
    @FXML
    private void changePassword(ActionEvent event) {
        // Get the values from the text fields
        String oldPwd = oldPassword.getText();
        String newPwd = newPassword.getText();
        String confirmNewPwd = newPasswordConfirmation.getText();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmNewPwd.isEmpty()) {
            showError("All fields must be filled.");
            return;
        }

        if (!newPwd.equals(confirmNewPwd)) {
            showError("New password and confirmation do not match.");
            return;
        }

        System.out.println(username);

        // Validate the old password from the database
        String sql = "SELECT Password FROM Owner WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("Password");

                if (!dbPassword.equals(oldPwd)) {
                    showError("Old password is incorrect.");
                    return;
                }

                // Update the password if old password matches
                updatePassword(event, newPwd); // Pass the event
            } else {
                showError("User not found.");
            }

        } catch (SQLException e) {
            showError("Error accessing database: " + e.getMessage());
        }
    }

    // Method to update the password in the database
    private void updatePassword(ActionEvent event, String newPassword) {
        String updateSQL = "UPDATE Owner SET Password = ? WHERE Username = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                navigateToPage(event, "15-change-password-successful.fxml");
            } else {
                showError("Password update failed.");
            }

        } catch (SQLException e) {
            showError("Error updating password: " + e.getMessage());
        }
    }

    // Show error message in label
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);
        }
    }

    // Navigate to a specific page
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

    // Action method to go back to the dashboard
    @FXML
    private void LogInButton(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }
}
