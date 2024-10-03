package com.example.billnbox;

import com.example.billnbox.InventoryController.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // Method to load notifications for expiry and low quantity products
    public void loadNotifications() {
        String expiryQuery = "SELECT * FROM Product WHERE ExpDate <= NOW() + INTERVAL 7 DAY AND OwnerID = ?";
        String lowQuantityQuery = "SELECT * FROM Product WHERE Quantity <= LowQuantityAlert AND OwnerID = ?";

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement expiryStmt = connection.prepareStatement(expiryQuery);
             PreparedStatement lowQuantityStmt = connection.prepareStatement(lowQuantityQuery)) {

            String ownerID = String.valueOf(SessionManager.getInstance().getOwnerID());
            expiryStmt.setString(1, ownerID);
            lowQuantityStmt.setString(1, ownerID);

            ResultSet expiryResults = expiryStmt.executeQuery();
            while (expiryResults.next()) {
                Product product = extractProductFromResultSet(expiryResults);
                Pane notificationPane = new Pane();
                notificationPane.setStyle("-fx-background-color: #e4c8aa; -fx-border-color: black; -fx-background-radius: 15; -fx-border-radius: 15;");
                notificationPane.setPrefHeight(71);
                notificationPane.setPrefWidth(676);

                Text notificationText = new Text("Product " + product.getProductName() + " is nearing expiry date.");
                notificationText.setWrappingWidth(563);
                notificationText.setLayoutX(14);
                notificationText.setLayoutY(31);
                notificationText.setFont(Font.font("Segoe UI", 16));

                notificationPane.setPadding(new Insets(10));
                notificationPanel.getChildren().add(notificationPane);
                Separator separator = new Separator();
                separator.setOrientation(Orientation.HORIZONTAL);
                separator.setPrefWidth(676);
                notificationPanel.getChildren().add(separator); // Add a separator for spacing
            }

            ResultSet lowQuantityResults = lowQuantityStmt.executeQuery();
            while (lowQuantityResults.next()) {
                Product product = extractProductFromResultSet(lowQuantityResults);
                Pane notificationPane = new Pane();
                notificationPane.setStyle("-fx-background-color: #e4c8aa; -fx-border-color: black; -fx-background-radius: 15; -fx-border-radius: 15;");
                notificationPane.setPrefHeight(71);
                notificationPane.setPrefWidth(676);

                Text notificationText = new Text("Product " + product.getProductName() + " has low quantity.");
                notificationText.setWrappingWidth(563);
                notificationText.setLayoutX(14);
                notificationText.setLayoutY(31);
                notificationText.setFont(Font.font("Segoe UI", 16));

                notificationPane.setPadding(new Insets(10));
                notificationPanel.getChildren().add(notificationPane);
                Separator separator = new Separator();
                separator.setOrientation(Orientation.HORIZONTAL);
                separator.setPrefWidth(676);
                notificationPanel.getChildren().add(separator); // Add a separator for spacing
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}