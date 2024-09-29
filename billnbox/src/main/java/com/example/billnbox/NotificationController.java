package com.example.billnbox;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class NotificationController {

    private final int ownerId; // Store the owner's ID

    public NotificationController(int ownerId) {
        this.ownerId = ownerId; // Initialize with the owner ID
    }

    public void initialize() {}

    // Start the notification service
    public void StartNotification() {
        Task<Void> notificationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    checkForNotifications();  // Check for notifications every day
                    Thread.sleep(24 * 60 * 60 * 1000);  // 24 hours
                }
            }
        };
        Thread notificationThread = new Thread(notificationTask);
        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    // Check for products with low stock or nearing expiry
    private void checkForNotifications() {
        String sql = "SELECT ProductName, ExpDate, Quantity, LowQuantityAlert FROM Product " +
                "WHERE (ExpDate <= CURDATE() + INTERVAL 7 DAY OR Quantity <= LowQuantityAlert) AND OwnerId = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ownerId); // Set the owner ID parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                // Use a Set to track products that have already been notified
                Set<String> notifiedProducts = new HashSet<>();

                while (rs.next()) {
                    String productName = rs.getString("ProductName");
                    LocalDate expDate = rs.getDate("ExpDate").toLocalDate();
                    int quantity = rs.getInt("Quantity");
                    int lowQuantityAlert = rs.getInt("LowQuantityAlert");

                    // Notify for expiry (if within 7 days)
                    if (expDate.isBefore(LocalDate.now().plusDays(7)) && !notifiedProducts.contains(productName + "Expiry")) {
                        notifyUser("Expiry Alert", productName + " is expiring on " + expDate);
                        notifiedProducts.add(productName + "Expiry");  // Mark as notified for expiry
                    }

                    // Notify for low stock
                    if (quantity <= lowQuantityAlert && !notifiedProducts.contains(productName + "LowStock")) {
                        notifyUser("Low Stock Alert", productName + " is below the minimum specified value. Only " + quantity + " left.");
                        notifiedProducts.add(productName + "LowStock");  // Mark as notified for low stock
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Notify the user with a custom message
    private void notifyUser(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);  // Warning alert for notifications
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void LogInButton(ActionEvent event) {
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
}
