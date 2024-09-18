package com.example.billnbox;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddNewEmployee2 {

    @FXML
    private TextField Username;

    @FXML
    private PasswordField Password;

    private String name;
    private String email;
    private String phoneNumber;

    // Set data passed from Page 1
    public void setPage1Data(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Handle Submit Button Action (Submit data and move to Manage Employees)
    @FXML
    private void handleAddButton() {
        String username = this.Username.getText();
        String Password = this.Password.getText();

        // Assuming you have a method to insert the employee into the database
        boolean isInserted = DatabaseConnection.insertEmployee(name, email, phoneNumber, username,Password);

        if (isInserted) {
            try {
                Stage stage = (Stage)Username.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("12a-employees.fxml")); // Update the path if necessary
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleCancelButton() {
        try {
            // Get the current window (stage)
            Stage stage = (Stage) Username.getScene().getWindow();

            // Load the Dashboard.fxml file
            Parent root = FXMLLoader.load(getClass().getResource("12b-employees.fxml")); // Update the path if necessary

            // Set the Dashboard scene
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class DatabaseConnection {

        /*private static final String URL = "jdbc:mysql://localhost:3306/your_database_name";
        private static final String USER = "your_username";
        private static final String PASSWORD = "your_password";
        */

        // Establish connection to the database
        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/BillNBoxDB", "root", "22yash02raj2006");
        }

        // Method to insert employee data into the database
        public static boolean insertEmployee(String name, String email, String number, String username, String password) {
            String query = "INSERT INTO employees (name, email, number, username, password) VALUES (?, ?, ?, ?, ?)";

            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, number);
                preparedStatement.setString(4, username);
                preparedStatement.setString(5, password);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}


