package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import java.io.IOException;

public class Controller {

    private final String username = SessionManager.getInstance().getUsername();

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    public void initialize() {
        // This method will be triggered for every scene where the controller is used.
        // Initialize combo box items here if needed across multiple scenes.
    }

    @FXML
    private void initializeDashboard() {
        // Creating data series for the BarChart
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("2023");

        // Add data to the series
        series1.getData().add(new XYChart.Data<>("January", 200));
        series1.getData().add(new XYChart.Data<>("February", 300));
        series1.getData().add(new XYChart.Data<>("March", 150));
        series1.getData().add(new XYChart.Data<>("April", 400));
        series1.getData().add(new XYChart.Data<>("May", 200));
        series1.getData().add(new XYChart.Data<>("June", 300));
        series1.getData().add(new XYChart.Data<>("July", 150));
        series1.getData().add(new XYChart.Data<>("August", 400));
        series1.getData().add(new XYChart.Data<>("September", 200));
        series1.getData().add(new XYChart.Data<>("October", 300));
        series1.getData().add(new XYChart.Data<>("November", 150));
        series1.getData().add(new XYChart.Data<>("December", 500));

        // Add the series to the BarChart
        if (barChart != null) {
            barChart.getData().add(series1);
        }
    }

    @FXML
    private void LogInButton(ActionEvent event) {
        boolean isValidLogin = true; // Add actual login logic
        if (isValidLogin) {
            loadScene(event, "8-dashboard.fxml");
        } else {
            System.out.println("Login failed. Please check your credentials.");
        }
    }

    @FXML
    private void forgotPasswordSubmit(ActionEvent event) {
        loadScene(event, "7-forgot-password-2.fxml");
    }

    @FXML
    private void profileButton(ActionEvent event) {
        loadScene(event, "9-profile.fxml");
    }

    @FXML
    private void inventoryButton(ActionEvent event) {
        loadScene(event, "10-inventory.fxml");
    }

    @FXML
    private void billButton(ActionEvent event) {
        loadScene(event, "11-bill.fxml");
    }

    @FXML
    private void employeesButton(ActionEvent event) {
        loadScene(event, "12a-employees.fxml");
    }

    @FXML
    private void logButton(ActionEvent event) {
        loadScene(event, "13-log.fxml");
    }

    @FXML
    private void changepassButton(ActionEvent event) {
        loadScene(event, "14-change-password.fxml");
    }

    @FXML
    private void AddNewEmployee(ActionEvent event) {
        loadScene(event, "12b-employees.fxml");
    }

    @FXML
    private void changePasswordBtn(ActionEvent event) {
        loadScene(event, "15-change-password-successful.fxml");
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        loadScene(event, "1-login-page.fxml");
    }

    @FXML
    private void confirmLogout(ActionEvent event) {
        loadScene(event, "16-logout-confirmation.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // If loading the dashboard, also initialize BarChart data
            if (fxmlFileName.equals("8-dashboard.fxml")) {
                Controller controller = loader.getController();
                controller.initializeDashboard();  // Call method to initialize BarChart
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
