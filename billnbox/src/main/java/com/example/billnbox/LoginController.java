package com.example.billnbox;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField; // TextField to show plain text password

    @FXML
    private ImageView eyeIcon; // CheckBox for toggling visibility

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
        loginErrorLabel.setVisible(false);
        // Ensure only one password field is visible at a time
        visiblePasswordField.setVisible(false); // Initially hidden since password is masked
        visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());
        passwordField.managedProperty().bind(passwordField.visibleProperty());

        // Keep password fields in sync
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

    }

    @FXML
    private void showPassword(MouseEvent event) {
            // Show the plain text password and hide the PasswordField
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
    }

    @FXML
    private void hidePassword(MouseEvent event) {
            // Hide the plain text password and show the PasswordField
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            visiblePasswordField.setVisible(false);
    }

    @FXML
    private void LogInButton(ActionEvent event) {

        // Get the username and password from the fields
        username = usernameField.getText();
        String password = passwordField.getText();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and Password Fields cannot be Empty.");
            return;
        }

        boolean isValidLogin = false;

        // User login
        isValidLogin = verifyLogin(username, password);

        if (isValidLogin) {
            // Set username in SessionManager
            SessionManager.getInstance().setUsername(username);

            // Log the login activity
            String userRole = isOwner ? "Owner" : "Employee"; // Determine if it's an owner or employee
            logActivity(username, userRole + " logged in");

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
            showError("Invalid credentials, please try again.");
        }
    }

    // Method to verify username and password from the respective table in the database
    private boolean verifyLogin(String username, String password) {
        String sql;

        // Reset isOwner to false initially
        isOwner = false; // Assume not an owner until verified

        // First check Employee table
        sql = "SELECT * FROM Employee WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet resultSet = pstmt.executeQuery();

            // Check if user exists in Employee table
            if (resultSet.next()) {
                // User is an employee, store OwnerID if applicable
                int ownerID = resultSet.getInt("OwnerID");
                SessionManager.getInstance().setOwnerID(ownerID);
                return true; // Successful login as Employee
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If not found in Employee table, check Owner table
        sql = "SELECT * FROM Owner WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet resultSet = pstmt.executeQuery();

            // Check if user exists in Owner table
            if (resultSet.next()) {
                // User is an owner, store OwnerID and set isOwner flag
                int ownerID = resultSet.getInt("OwnerID");
                SessionManager.getInstance().setOwnerID(ownerID);
                isOwner = true; // Set isOwner to true
                return true; // Successful login as Owner
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

    private void logActivity(String username, String activity) {
        String sql = "INSERT INTO logs (date, time, User, activity,OwnerID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setTime(2, java.sql.Time.valueOf(java.time.LocalTime.now()));
            pstmt.setString(3, username);
            pstmt.setString(4, activity);
            pstmt.setInt(5, SessionManager.getInstance().getOwnerID());  // OwnerID from SessionManager

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to display the error message and automatically hide it after 3 seconds
    private void showError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);

        // Create a PauseTransition to hide the label after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> loginErrorLabel.setVisible(false));
        pause.play();
    }
}