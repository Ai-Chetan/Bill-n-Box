package com.example.billnbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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
    }

    // Method to load employee data from the database
    private void loadEmployeeData() {
        try {
            ResultSet rs = DatabaseConnection.getEmployeeData(); // Assume this method gets data from DB
            int srNo = 1;

            while (rs.next()) {
                employeeList.add(new Employee(
                        srNo++,
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }

            // Set the employee list to the table
            employeeTable.setItems(employeeList);

        } catch (SQLException e) {
            e.printStackTrace();
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
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class Employee {
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
    public class DatabaseConnection {

        public static ResultSet getEmployeeData() {
            ResultSet resultSet = null;

            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/employeesdb", "root", "22yash02raj2006");
                Statement stmt = conn.createStatement();
                String query = "SELECT name, username, password, phone, email FROM Employee";
                resultSet = stmt.executeQuery(query);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultSet;
        }
    }
}