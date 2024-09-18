package com.example.billnbox;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

public class AddNewEmployee1 {

    @FXML
    private TextField employeeName;

    @FXML
    private TextField phoneNo;

    @FXML
    private TextField emailId;

    // Handle Next Button Action (Navigate to Page 2)
    @FXML
    private void handlenextButton() {
        try {
            // Get the current window (stage)
            Stage stage = (Stage) employeeName.getScene().getWindow();

            // Load the second page (Page2.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("12c-employees.fxml"));
            Parent root = loader.load();

            AddNewEmployee2 controller = loader.getController();
            String name = employeeName.getText();
            String phone = phoneNo.getText();
            String email = emailId.getText();
            controller.setPage1Data(name, email, phone);
            // Set the new scene to the current stage
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle Cancel Button Action (Navigate to Dashboard)
    @FXML
    private void handlecancelButton() {
        try {
            // Get the current window (stage)
            Stage stage = (Stage) employeeName.getScene().getWindow();

            // Load the Dashboard.fxml file
            Parent root = FXMLLoader.load(getClass().getResource("12a-employees.fxml")); // Update the path if necessary

            // Set the Dashboard scene
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
