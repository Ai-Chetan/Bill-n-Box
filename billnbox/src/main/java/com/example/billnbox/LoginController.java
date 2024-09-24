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
    private RadioButton loginradiobtn1; // Owner radio button

    @FXML
    private RadioButton loginradiobtn2; // Employee radio button

    @FXML
    private AnchorPane registerpane;

    @FXML
    private ToggleGroup LoginAsOwnerOrEmployee;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginErrorLabel;

    private String username;

    public static boolean isOwner;
    public static boolean getIsOwner() {
        return isOwner;
    }
    public static void setIsOwner(boolean isOwner) {
        LoginController.isOwner = isOwner;
    }

    public void initialize() {
        // Initially hide the admin pane
        registerpane.setVisible(false);
        loginErrorLabel.setVisible(false);

        // Add a listener to detect changes in the radio buttons
        LoginAsOwnerOrEmployee.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (LoginAsOwnerOrEmployee.getSelectedToggle() != null) {
                RadioButton selectedRadioButton = (RadioButton) LoginAsOwnerOrEmployee.getSelectedToggle();

                // Change content based on selected radio button
                if (selectedRadioButton == loginradiobtn1) {
                    registerpane.setVisible(true);
                } else if (selectedRadioButton == loginradiobtn2) {
                    registerpane.setVisible(false);
                }
            }
        });
    }

    @FXML
    private void LogInButton(ActionEvent event) {

        if (loginradiobtn1.isSelected()) {
            isOwner = true;
        } else {
            isOwner = false;
        }

        // Get the username and password from the fields
        username = usernameField.getText();
        String password = passwordField.getText();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Username and Password Fields cannot be Empty.");
            loginErrorLabel.setVisible(true);
            return;
        }

        // Determine if the user is logging in as Owner or Employee
        RadioButton selectedRadioButton = (RadioButton) LoginAsOwnerOrEmployee.getSelectedToggle();
        boolean isValidLogin = false;

        if (selectedRadioButton != null) {
            if (selectedRadioButton == loginradiobtn1) {
                // Owner login
                isValidLogin = verifyLogin(username, password, "Owner");
            } else if (selectedRadioButton == loginradiobtn2) {
                // Employee login
                isValidLogin = verifyLogin(username, password, "Employee");
            }
        } else {
            // No radio button selected
            loginErrorLabel.setText("Please select whether you are logging in as Owner or Employee.");
            loginErrorLabel.setVisible(true);
            return;
        }

        if (isValidLogin) {
            // Set username in SessionManager
            SessionManager.getInstance().setUsername(username);
            try {
                // Load the dashboard FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("8-dashboard.fxml"));
                Parent root = loader.load();

                Controller controller = loader.getController();
                controller.initializeDashboard();

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

    // Method to verify username and password from the respective table in the database
    private boolean verifyLogin(String username, String password, String userType) {
        String sql;
        if (userType.equals("Owner")) {
            sql = "SELECT * FROM Owner WHERE Username = ? AND Password = ?";
        } else {
            sql = "SELECT * FROM Employee WHERE Username = ? AND Password = ?";
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet resultSet = pstmt.executeQuery();

            // Check if user exists in the respective table
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