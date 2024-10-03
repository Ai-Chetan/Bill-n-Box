package com.example.billnbox;

import com.example.billnbox.InventoryController.Product;
import javafx.scene.text.FontWeight;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;

public class NotificationControllerNew {
    @FXML
    private Button backButton;
    @FXML
    private Text notificationText;
    @FXML
    private VBox notificationPanel;
    @FXML
    private ScrollPane scrollPane; // Add this line

    private Product selectedProduct; // To store the selected product

    // Helper method to extract product information from the ResultSet
    private Product extractProductFromResultSet(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("SrNo"),
                resultSet.getString("ProductName"),
                resultSet.getString("Category"),
                resultSet.getInt("Quantity"),
                resultSet.getDouble("Price"),
                resultSet.getString("MfgDate"),
                resultSet.getString("ExpDate"),
                resultSet.getInt("LowQuantityAlert")
        );
    }

    // Method to initialize the notification panel and load notifications
    @FXML
    private void initialize() {
        loadNotifications();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("8-dashboard.fxml")); // Adjust path as necessary
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNotifications() {
        String expiryQuery = "SELECT * FROM Product WHERE ExpDate > NOW() AND ExpDate <= NOW() + INTERVAL 7 DAY AND OwnerID = ?";
        String expiredQuery = "SELECT * FROM Product WHERE ExpDate <= NOW() AND OwnerID = ?";
        String lowQuantityQuery = "SELECT * FROM Product WHERE Quantity <= LowQuantityAlert AND OwnerID = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement expiryStmt = connection.prepareStatement(expiryQuery);
             PreparedStatement expiredStmt = connection.prepareStatement(expiredQuery);
             PreparedStatement lowQuantityStmt = connection.prepareStatement(lowQuantityQuery)) {

            String ownerID = String.valueOf(SessionManager.getInstance().getOwnerID());
            expiryStmt.setString(1, ownerID);
            expiredStmt.setString(1, ownerID);
            lowQuantityStmt.setString(1, ownerID);

            // Handle products that are already expired
            ResultSet expiredResults = expiredStmt.executeQuery();
            while (expiredResults.next()) {
                Product product = extractProductFromResultSet(expiredResults);
                Pane notificationPane = createNotificationPane(
                        "Product " + product.getProductName() + " has expired.\nExpiry date was " + product.getExpDate() + "."
                );
                notificationPanel.getChildren().add(notificationPane);
                addSeparator();
            }

            // Handle products that are about to expire
            ResultSet expiryResults = expiryStmt.executeQuery();
            while (expiryResults.next()) {
                Product product = extractProductFromResultSet(expiryResults);
                Pane notificationPane = createNotificationPane(
                        "A stock of " + product.getProductName() + " is about to expire. Expiry date is " + product.getExpDate() + ".\nQuantity in stock is " + product.getQuantity() + "."
                );
                notificationPanel.getChildren().add(notificationPane);
                addSeparator();
            }

            // Handle products with low quantity
            ResultSet lowQuantityResults = lowQuantityStmt.executeQuery();
            while (lowQuantityResults.next()) {
                Product product = extractProductFromResultSet(lowQuantityResults);
                Pane notificationPane = createNotificationPane(
                        "Quantity of " + product.getProductName() + " is very low.\nOnly " + product.getQuantity() + " left."
                );
                notificationPanel.getChildren().add(notificationPane);
                addSeparator();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to create notification pane
    private Pane createNotificationPane(String message) {
        Pane notificationPane = new Pane();
        notificationPane.setStyle("-fx-background-color: #e4c8aa; -fx-border-color: black; -fx-background-radius: 15; -fx-border-radius: 15;");
        notificationPane.setPrefHeight(71);
        notificationPane.setPrefWidth(676);

        Text notificationText = new Text(message);
        notificationText.setWrappingWidth(563);

        // Set the font to bold using FontWeight
        notificationText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        // Instead of absolute positioning, we can add the text directly to the pane
        VBox vBox = new VBox(notificationText);
        vBox.setPadding(new Insets(10));
        notificationPane.getChildren().add(vBox);

        return notificationPane;
    }

    // Helper method to add a separator for spacing
    private void addSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        separator.setPrefWidth(676);
        notificationPanel.getChildren().add(separator);
    }
}
