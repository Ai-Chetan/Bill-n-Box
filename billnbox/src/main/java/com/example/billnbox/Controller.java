package com.example.billnbox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button; // Import Button class

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Controller {

    private final String username = SessionManager.getInstance().getUsername();
    private final String shopname = SessionManager.getInstance().getShopname();
    private final String shopaddress = SessionManager.getInstance().getShopaddress();

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private Button logButton; // Reference to the Log button
    @FXML
    private Button employeesButton; // Reference to the Employees button

    // Reference to NotificationController
    private NotificationController notificationController;

    private boolean isOwner = LoginController.getIsOwner(); // To determine if the user is an owner

    @FXML
    public void initialize() {
        // Set visibility of buttons based on ownership
        if (logButton != null) {
            logButton.setVisible(isOwner);
            employeesButton.setVisible(isOwner);
        }

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

    // Initialize Dashboard method where notification system is started
    @FXML
    public void initializeDashboard() {
        notificationController = new NotificationController();
        notificationController.StartNotification();  // Start notifications on dashboard load
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
    private void logButton(ActionEvent event) {
        loadScene(event, "13-log.fxml");
    }

    @FXML
    private void employeesButton(ActionEvent event) {
        loadScene(event, "12a-employees.fxml");
    }

    @FXML
    private void changepassButton(ActionEvent event) {
        loadScene(event, "14-change-password.fxml");
    }

    @FXML
    private void confirmLogout(ActionEvent event) {

        String currentUser = SessionManager.getUsername();
        logActivity(currentUser, "Logged out");

        loadScene(event, "16-logout-confirmation.fxml");
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        loadScene(event, "1-login-page.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // If loading the dashboard, also initialize BarChart data and notifications
            if (fxmlFileName.equals("8-dashboard.fxml")) {
                Controller controller = loader.getController();
                controller.initializeDashboard();  // Initialize the dashboard and notifications
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void logActivity(String username, String activity) {
        String sql = "INSERT INTO logs (date, time, User, activity,OwnerID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
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




    public void LogInButton(ActionEvent event) {
        loadScene(event, "8-dashboard.fxml");
    }
}
