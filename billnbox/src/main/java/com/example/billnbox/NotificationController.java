package com.example.billnbox;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class NotificationController {

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
                "WHERE ExpDate <= CURDATE() + INTERVAL 7 DAY OR Quantity < LowQuantityAlert";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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
                if (quantity < lowQuantityAlert && !notifiedProducts.contains(productName + "LowStock")) {
                    notifyUser("Low Stock Alert", productName + " is below the minimum specified value. Only " + quantity + " left.");
                    notifiedProducts.add(productName + "LowStock");  // Mark as notified for low stock
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
}
