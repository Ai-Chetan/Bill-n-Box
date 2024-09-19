package com.example.billnbox;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;

public class EmployeeController {

    @FXML
    private TableView<Employee> employeeTable;

    @FXML
    private TableColumn<Employee, Integer> srNoColumn;

    @FXML
    private TableColumn<Employee, String> nameColumn;

    @FXML
    private TableColumn<Employee, String> usernameColumn;

    @FXML
    private TableColumn<Employee, String> passwordColumn;

    @FXML
    private TableColumn<Employee, String> phoneColumn;

    @FXML
    private TableColumn<Employee, String> emailColumn;

    @FXML
    private Button deleteButton;  // Add reference to the delete button

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    // Initialize the TableView and load employee data
    @FXML
    public void initialize() {
        // Set cell value factories for each column
        srNoColumn.setCellValueFactory(new PropertyValueFactory<>("srNo"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load employee data from the database
        loadEmployeeData();

        // Add a listener to enable/disable the delete button based on selection
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null);
        });
    }

    // Method to load employee data from the database
    private void loadEmployeeData() {
        ResultSet rs = null;
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
            stmt = conn.createStatement();
            String query = "SELECT EmpID, Username, Password, Name, EmailID, PhoneNo FROM Employee";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getInt("EmpID"),
                        rs.getString("Name"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("PhoneNo"),
                        rs.getString("EmailID")
                ));
            }

            // Set the employee list to the table
            employeeTable.setItems(employeeList);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Handle Back Button (Navigate to Dashboard)
    @FXML
    private void handleBackButton() {
        try {
            Stage stage = (Stage) employeeTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("8-Dashboard.fxml")); // Update path if necessary
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle Add New Employee Button (Navigate to Page 1)
    @FXML
    private void handleAddNewEmpButton() {
        try {
            Stage stage = (Stage) employeeTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("12b-employees.fxml")); // Update path if necessary
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle Delete Button
    @FXML
    private void handleDeleteButton() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            return;
        }

        // Show confirmation dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this employee?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            deleteEmployee(selectedEmployee);
        }
    }

    // Method to delete employee from the database
    private void deleteEmployee(Employee employee) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
            String query = "DELETE FROM Employee WHERE EmpID = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, employee.getSrNo());
            pstmt.executeUpdate();

            // Remove employee from the TableView
            employeeList.remove(employee);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Employee class
    public static class Employee {
        private int srNo;
        private String name;
        private String username;
        private String password;
        private String phone;
        private String email;

        public Employee(int srNo, String name, String username, String password, String phone, String email) {
            this.srNo = srNo;
            this.name = name;
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.email = email;
        }

        public int getSrNo() {
            return srNo;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }
    }
}