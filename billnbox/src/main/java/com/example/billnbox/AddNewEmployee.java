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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddNewEmployee {

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
            emptyFields.setVisible(true);
            emptyFields.setText("Fields cannot be Empty");
            return;
        }

        // Mobile number constraint - must be exactly 10 digits
        if (!PhoneNo.matches("\\d{10}")) {
            emptyFields.setVisible(true);
            emptyFields.setText("Mobile number must be exactly 10 digits");
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
            emptyFields.setVisible(true);
            emptyFields.setText("Fields cannot be empty");
            return;
        }

        // Add constraint for password length
        if (EmpPassword.getText().length() < 8) {
            emptyFields.setVisible(true);
            emptyFields.setText("Password must be at least 8 characters long");
            return;
        }

        try {
            employeeName.setVisible(false);
            DatabaseConnection.insertEmployee(EmployeeName, EmailID, PhoneNo, Username, Password);
            navigateToPage(event, "12a-employees.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        progBar2.setProgress(1.0);
    }


    public static class DatabaseConnection {

        // Method to insert employee data into the database
        public static boolean insertEmployee(String name, String email, String number, String username, String password) {
            String query = "INSERT INTO Employee (Name, EmailID, PhoneNo, Username, Password, OwnerID) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                int ownerID = SessionManager.getOwnerID(); // Get the current owner ID
                String ownerUsername = SessionManager.getInstance().getUsername(); // Get the owner username from the session

                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, number);
                preparedStatement.setString(4, username);
                preparedStatement.setString(5, password);
                preparedStatement.setInt(6, ownerID); // Insert OwnerID to associate the employee with the shop

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;

            } catch (SQLException e) {
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