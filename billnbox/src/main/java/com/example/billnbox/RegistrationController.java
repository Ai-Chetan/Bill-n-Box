package com.example.billnbox;

import  javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static com.example.billnbox.SessionManager.username;


public class RegistrationController {

    public static boolean isOwner = true;

    private String Name;
    private String EmailID;
    private String MobileNumber;
    private String ShopName;
    private String ShopAddress;
    private String FilePath;
    private String Username;
    private String Password;
    private String ConfirmPassword;

    @FXML
    private TextField nameField, emailField, mobnoField, shopnameField, usernameField, visiblePasswordField, visiblePasswordField1, filePathField;
    @FXML
    private TextField passwordField, confirmpasswordField;
    @FXML
    private CheckBox showpasswordCheckbox, showpasswordCheckbox1; // CheckBox for toggling visibility
    @FXML
    private TextArea shopaddressField;
    @FXML
    private Label emptyFields;

    // Database connection details
    public static class DatabaseConnector {

        public static void storeUser(String username, String password, String name, String email, String mobno, String shopname, String shopaddress, String filepath) {
            String insertSQL = "INSERT INTO Owner (Username, Password, Name, EmailID, PhoneNo, ShopName, ShopAddress, FilePath) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, email);
                pstmt.setString(5, mobno);
                pstmt.setString(6, shopname);
                pstmt.setString(7, shopaddress);
                pstmt.setString(8, filepath);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User registered successfully.");
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newOwnerID = generatedKeys.getInt(1);

                            // Set the new OwnerID in SessionManager
                            SessionManager.getInstance().setOwnerID(newOwnerID);

                            System.out.println("New OwnerID set in SessionManager: " + newOwnerID);
                        }
                    }
                } else {
                    System.out.println("Registration failed.");
                }
            } catch (SQLException e) {
                System.out.println("Database connection failed: " + e.getMessage());
            }
        }
    }

    // Initialize controller
    @FXML
    public void initialize() {
        LoginController.setIsOwner(isOwner);
        if (emptyFields != null) {
            emptyFields.setVisible(false);  // Hide the emptyFields label initially
        }
        if (visiblePasswordField != null) {
            visiblePasswordField.setVisible(false); // Initially hidden since password is masked
            visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());
            visiblePasswordField1.setVisible(false); // Initially hidden since password is masked
            visiblePasswordField1.managedProperty().bind(visiblePasswordField1.visibleProperty());
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        if (showpasswordCheckbox.isSelected()) {
            // Show the plain text password and hide the PasswordField
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            // Hide the plain text password and show the PasswordField
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            visiblePasswordField.setVisible(false);
        }
    }

    @FXML
    private void togglePasswordVisibility1(ActionEvent event) {
        if (showpasswordCheckbox1.isSelected()) {
            // Show the plain text password and hide the PasswordField
            visiblePasswordField1.setText(confirmpasswordField.getText());
            visiblePasswordField1.setVisible(true);
            confirmpasswordField.setVisible(false);
        } else {
            // Hide the plain text password and show the PasswordField
            confirmpasswordField.setText(visiblePasswordField1.getText());
            confirmpasswordField.setVisible(true);
            visiblePasswordField1.setVisible(false);
        }
    }


    // Move to Page 2 (Shop Details)
    @FXML
    private void RegistrationNext1(ActionEvent event) {
        Name = nameField.getText();
        EmailID = emailField.getText();
        MobileNumber = mobnoField.getText();

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if (Name.isEmpty() || EmailID.isEmpty() || MobileNumber.isEmpty()) {
            showError("Fields cannot be Empty");
        } else if (!MobileNumber.matches("\\d{10}")) {
            showError("Mobile number must consist of exactly 10 digits");
        } else if (!EmailID.matches(emailPattern)) {
            showError("Invalid Email-Id.");
        } else {
            // Pass data to next controller
            if (validateFields(Name, EmailID, MobileNumber)) {
                navigateToPageWithData(event, "3-registration-page-2.fxml");
            }
        }
    }

    // Move to Page 3 (User Credentials)
    @FXML
    private void RegistrationNext2(ActionEvent event) {
        ShopName = shopnameField.getText();
        ShopAddress = shopaddressField.getText();
        FilePath = filePathField.getText();

        if (ShopName.isEmpty() || ShopAddress.isEmpty()) {
            showError("Fields cannot be Empty");
        }

        if (validateFields(ShopName, ShopAddress, FilePath)) {
            navigateToPageWithData(event, "4-registration-page-3.fxml");
        }
    }

    // Navigate to previous pages
    @FXML
    private void RegistrationBack2(ActionEvent event) {
        navigateToPageWithData(event, "2-registration-page-1.fxml");
    }

    @FXML
    private void RegistrationBack3(ActionEvent event) {
        navigateToPageWithData(event, "3-registration-page-2.fxml");
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        navigateToPage(event, "1-login-page.fxml");
    }

    @FXML
    private void gotoDashboard(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }

    // Final registration process
    @FXML
    private void handleRegister(ActionEvent event) {
        Username = usernameField.getText();
        Password = passwordField.getText();
        ConfirmPassword = confirmpasswordField.getText();

        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$";

        if (!Password.equals(ConfirmPassword)) {
            showError("Confirm Password does not match");
        } else if (Password.length() < 8) {
            showError("Password must be at least 8 characters long");
        } else if (!Password.matches(passwordPattern)) {
            showError("Password must contain at least one uppercase letter, one digit, and one special symbol");
        } else if (Username.isEmpty() || Password.isEmpty()) {
            showError("Username and Password Fields cannot be Empty");
        } else if (isUsernameTaken(Username)) {  // Check if the username is already taken
            showError("Username already exists. Please choose another.");
        } else {
            // Set username in SessionManager
            SessionManager.getInstance().setUsername(Username);
            DatabaseConnector.storeUser(Username, Password, Name, EmailID, MobileNumber, ShopName, ShopAddress, FilePath);
            navigateToPage(event, "5-registration-successful.fxml");
        }
    }

    // Check if the username already exists in the database
    private boolean isUsernameTaken(String username) {
        String query = "SELECT * FROM Owner WHERE Username = ?";
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

    // Validation for empty fields
    private boolean validateFields(String... fields) {
        for (String field : fields) {
            if (field != FilePath && (field == null || field.trim().isEmpty())) {
                showError("All fields must be filled");
                return false;
            }
        }
        return true;
    }

    // Show error message in label
    private void showError(String message) {
        if (emptyFields != null) {
            emptyFields.setVisible(true);
            emptyFields.setText(message);

            // Hide the label after 3 seconds
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.millis(3000),
                    event -> emptyFields.setVisible(false)
            ));
            timeline.setCycleCount(1);
            timeline.play();
        }
    }

    // Navigation method with data passing
    private void navigateToPageWithData(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + fxmlFile));
            Parent root = loader.load();

            // Get the next controller and pass the current data
            RegistrationController nextController = loader.getController();
            nextController.setData(Name, EmailID, MobileNumber, ShopName, ShopAddress, FilePath, Username);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Simple navigation without data passing
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

    // Setter method for passing data to next page
    // Setter method for passing data to the next page
    public void setData(String name, String email, String mobileNumber, String shopName, String shopAddress, String filePath, String username) {
        this.Name = name;
        this.EmailID = email;
        this.MobileNumber = mobileNumber;
        this.ShopName = shopName;
        this.ShopAddress = shopAddress;
        this.FilePath = filePath;
        this.Username = username;

        // Update the fields with the passed data
        if (nameField != null) {
            nameField.setText(name);
        }
        if (emailField != null) {
            emailField.setText(email);
        }
        if (mobnoField != null) {
            mobnoField.setText(mobileNumber);
        }
        if (shopnameField != null) {
            shopnameField.setText(shopName);
        }
        if (shopaddressField != null) {
            shopaddressField.setText(shopAddress);
        }
        if (filePathField != null) {
            filePathField.setText(filePath);
        }
        if (usernameField != null) {
            usernameField.setText(username);
        }
    }
}