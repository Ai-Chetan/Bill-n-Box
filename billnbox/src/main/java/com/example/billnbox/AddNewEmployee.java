package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class AddNewEmployee {

    // Fields remain the same
    private String EmployeeName, PhoneNo, EmailID, Username, Password;

    @FXML
    private Label EmpUsernameTag, EmpPasswordTag, employeeNameTag, emailIdTag, phoneNoTag, emptyFields;

    @FXML
    private TextField employeeName, phoneNo, emailId, EmpUsername;

    @FXML
    private PasswordField EmpPassword;

    @FXML
    private Button cancelButton, backButton, nextButton, addEmployeeButton;

    @FXML
    private ProgressBar progBar1, progBar2;

    @FXML
    public void initialize() {
        EmpUsernameTag.setVisible(false);
        EmpPasswordTag.setVisible(false);
        EmpUsername.setVisible(false);
        EmpPassword.setVisible(false);
        backButton.setVisible(false);
        addEmployeeButton.setVisible(false);
        emptyFields.setVisible(false);
    }

    @FXML
    private void handlecancelButton(ActionEvent event) {
        navigateToPage(event, "12a-employees.fxml");
    }

    @FXML
    private void handlenextButton(ActionEvent event) {
        EmployeeName = employeeName.getText();
        EmailID = emailId.getText();
        PhoneNo = phoneNo.getText();

        if (EmployeeName.isEmpty() || EmailID.isEmpty() || PhoneNo.isEmpty()) {
            showError("Fields cannot be Empty");
            return;
        }

        // Mobile number constraint - must be exactly 10 digits
        if (!PhoneNo.matches("\\d{10}")) {
            showError("Mobile number must be exactly 10 digits");
            return;
        }

        // Hide the first section and display the next section
        employeeName.setVisible(false);
        emailId.setVisible(false);
        phoneNo.setVisible(false);
        employeeNameTag.setVisible(false);
        emailIdTag.setVisible(false);
        phoneNoTag.setVisible(false);
        emptyFields.setVisible(false);

        cancelButton.setVisible(false);
        nextButton.setVisible(false);
        backButton.setVisible(true);
        addEmployeeButton.setVisible(true);

        EmpUsernameTag.setVisible(true);
        EmpPasswordTag.setVisible(true);
        EmpUsername.setVisible(true);
        EmpPassword.setVisible(true);
        backButton.setVisible(true);
        addEmployeeButton.setVisible(true);

        progBar1.setProgress(1.0);
        progBar2.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    @FXML
    private void handlebackButton(ActionEvent event) {
        employeeName.setVisible(true);
        emailId.setVisible(true);
        phoneNo.setVisible(true);
        employeeNameTag.setVisible(true);
        emailIdTag.setVisible(true);
        phoneNoTag.setVisible(true);

        cancelButton.setVisible(true);
        nextButton.setVisible(true);
        backButton.setVisible(false);
        addEmployeeButton.setVisible(false);

        EmpUsernameTag.setVisible(false);
        EmpPasswordTag.setVisible(false);
        EmpUsername.setVisible(false);
        EmpPassword.setVisible(false);
        backButton.setVisible(false);
        addEmployeeButton.setVisible(false);

        employeeName.setText(EmployeeName);
        emailId.setText(EmailID);
        phoneNo.setText(PhoneNo);
    }

    @FXML
    private void handleaddEmployeeButton(ActionEvent event) {

        // Check if fields are empty
        if (EmpUsername.getText().isEmpty() || EmpPassword.getText().isEmpty()) {
            showError("Fields cannot be empty");
            return;
        }

        // Add constraint for password length
        if (EmpPassword.getText().length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }
        if(isUsernameTaken(EmpUsername.getText())){
            showError("Username already exists. Please choose another.");
            return;
        }

        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$";
        if (!EmpPassword.getText().matches(passwordPattern)) {
            showError("Password must contain at least one uppercase letter, one digit, and one special symbol");
            return;
        }

        // Set the values for Username and Password
        Username = EmpUsername.getText();
        Password = EmpPassword.getText();

        try {
            // Make the fields invisible during the process
            employeeName.setVisible(false);
            emailId.setVisible(false);
            phoneNo.setVisible(false);
            EmpUsername.setVisible(false);
            EmpPassword.setVisible(false);

            // Insert employee into the database
            boolean isInserted = DatabaseConnection.insertEmployee(EmployeeName, EmailID, PhoneNo, Username, Password);

            // If insertion is successful, navigate to the next page
            if (isInserted) {
                navigateToPage(event, "12a-employees.fxml");
            } else {
                showError("Failed to add employee.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred while adding the employee.");
        }

        progBar2.setProgress(1.0);  // Set progress to full when done
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT * FROM Employee WHERE Username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();  // If a record is found, the username exists
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return false;
    }
    // Method to show error messages with a timer
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);

            // Automatically hide the message after 3 seconds (3000 ms)
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.millis(3000), // 3-second delay
                    event -> emptyFields.setVisible(false) // Hide the label after the delay
            ));
            timeline.setCycleCount(1); // Ensure it only runs once
            timeline.play();
        }
    }

    // Database connection class remains the same
    public static class DatabaseConnection {

        public static boolean insertEmployee(String name, String email, String number, String username, String password) {
            String query = "INSERT INTO Employee (Name, EmailID, PhoneNo, Username, Password, OwnerID) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                int ownerID = SessionManager.getOwnerID(); // Get the current owner ID
                String ownerUsername = SessionManager.getInstance().getUsername(); // Get the owner username from the session

                // Ensure username is not null or empty
                if (username == null || username.trim().isEmpty()) {
                    System.out.println("Error: Username cannot be null or empty.");
                    return false;
                }

                // Set values in the prepared statement
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, number);
                preparedStatement.setString(4, username);
                preparedStatement.setString(5, password);
                preparedStatement.setInt(6, ownerID); // Insert OwnerID to associate the employee with the shop

                // Execute the update
                int rowsAffected = preparedStatement.executeUpdate();
                System.out.println("Rows affected: " + rowsAffected); // For debugging
                return rowsAffected > 0;

            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Integrity constraint violation: " + e.getMessage());
                e.printStackTrace();
                return false;
            } catch (SQLException e) {
                System.out.println("SQL error: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

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
