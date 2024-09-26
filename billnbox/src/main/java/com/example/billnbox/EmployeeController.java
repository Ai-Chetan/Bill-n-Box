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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

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
    private Button editButton;

    @FXML
    private Button deleteButton;  // Add reference to the delete button

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    private boolean isEditing = false; // To track the edit state

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
            if(deleteButton!=null) {
                deleteButton.setDisable(newSelection == null);
            }
        });
    }

    // Method to load employee data from the database
    private void loadEmployeeData() {
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
            String query = "SELECT EmpID, Username, Password, Name, EmailID, PhoneNo, OwnerID FROM Employee WHERE OwnerID = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, SessionManager.getInstance().getOwnerID());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getInt("EmpID"),
                        rs.getString("Name"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("PhoneNo"),
                        rs.getString("EmailID"),
                        rs.getInt("OwnerID") // Set OwnerID
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
                if (pstmt != null) pstmt.close();
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

    // Handle Edit Button
    @FXML
    private void handleeditButton() {
        if (isEditing) {
            // Save changes to the database
            saveEmployeeData();

            // Change the button text back to "Edit"
            editButton.setText("Edit");
            isEditing = false;

            // Disable table editing
            employeeTable.setEditable(false);
        } else {
            // Enable table editing
            employeeTable.setEditable(true);

            nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            nameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));

            usernameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            usernameColumn.setOnEditCommit(event -> event.getRowValue().setUsername(event.getNewValue()));

            passwordColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            passwordColumn.setOnEditCommit(event -> event.getRowValue().setPassword(event.getNewValue()));

            phoneColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            phoneColumn.setOnEditCommit(event -> event.getRowValue().setPhone(event.getNewValue()));

            emailColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            emailColumn.setOnEditCommit(event -> event.getRowValue().setEmail(event.getNewValue()));

            // Change the button text to "Save Changes"
            editButton.setText("Save Changes");
            isEditing = true;
        }
    }

    // Save employee data to the database
    private void saveEmployeeData() {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
            String updateQuery = "UPDATE Employee SET Name = ?, Username = ?, Password = ?, PhoneNo = ?, EmailID = ? WHERE EmpID = ?";

            pstmt = conn.prepareStatement(updateQuery);

            for (Employee employee : employeeList) {
                pstmt.setString(1, employee.getName());
                pstmt.setString(2, employee.getUsername());
                pstmt.setString(3, employee.getPassword());
                pstmt.setString(4, employee.getPhone());
                pstmt.setString(5, employee.getEmail());
                pstmt.setInt(6, employee.getSrNo());

                pstmt.addBatch();
            }

            pstmt.executeBatch();

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
        private int ownerId;

        public Employee(int srNo, String name, String username, String password, String phone, String email, int ownerId) {
            this.srNo = srNo;
            this.name = name;
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.email = email;
            this.ownerId = ownerId;
        }

        public int getSrNo() {
            return srNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(int ownerId) {
            this.ownerId = ownerId;
        }
    }
}