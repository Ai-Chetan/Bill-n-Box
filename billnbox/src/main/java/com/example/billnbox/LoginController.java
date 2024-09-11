package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private RadioButton loginradiobtn1;

    @FXML
    private RadioButton loginradiobtn2;

    @FXML
    private AnchorPane adminpane;

    @FXML
    private ToggleGroup LoginAsAdminOrEmployee;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginErrorLabel;

    // Database credentials
    private String url = "jdbc:mysql://localhost:3307/BillNBoxDB";
    private String user = "root";
    private String dbPassword = "rootroot";

    public void initialize() {
        // Initially hide the admin pane
        adminpane.setVisible(false);
        loginErrorLabel.setVisible(false);

        // Add a listener to detect changes in the radio buttons
        LoginAsAdminOrEmployee.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (LoginAsAdminOrEmployee.getSelectedToggle() != null) {
                RadioButton selectedRadioButton = (RadioButton) LoginAsAdminOrEmployee.getSelectedToggle();
                String selectedText = selectedRadioButton.getText();

                // Change content based on selected radio button
                if (selectedRadioButton == loginradiobtn1) {
                    adminpane.setVisible(true);
                } else if (selectedRadioButton == loginradiobtn2) {
                    adminpane.setVisible(false);
                }
            }
        });
    }

    @FXML
    private void LogInButton(ActionEvent event) {
        // Get the username and password from the fields
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Fields cannot be empty.");
            loginErrorLabel.setVisible(true);
            return;
        }

        // Perform login validation by checking with the database
        boolean isValidLogin = verifyLogin(username, password);

        if (isValidLogin) {
            try {
                // Load the dashboard FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("8-dashboard.fxml"));
                Parent root = loader.load();

                // Get the current stage
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

                // Set the scene to the dashboard
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Show error message for invalid login
            loginErrorLabel.setText("Invalid credentials, please try again.");
            loginErrorLabel.setVisible(true);
        }
    }

    // Method to verify username and password from the database
    private boolean verifyLogin(String username, String password) {
        String sql = "SELECT * FROM RegisteredUsers WHERE Username = ? AND Password = ?";
        try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet resultSet = pstmt.executeQuery();

            // Check if user exists
            if (resultSet.next()) {
                // Username and password are correct
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If no match found, return false
        return false;
    }

    @FXML
    public void ForgotPasswordText(javafx.scene.input.MouseEvent mouseEvent) {
        try {
            // Load the forgot password FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("6-forgot-password-1.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((javafx.scene.Node) mouseEvent.getSource()).getScene().getWindow();

            // Set the scene to the forgot password page
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ResisterButton(ActionEvent event) {
        try {
            // Load the registration page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("2-registration-page-1.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Set the scene to the registration page
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
