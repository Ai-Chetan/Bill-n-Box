package com.example.billnbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // Import Button
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

import com.example.billnbox.InventoryController.Product;
import javafx.stage.Stage;

public class NotificationController2 {

    @FXML
    private TableView<Product> expiryProducts;
    @FXML
    private TableView<Product> lowQuantityProducts;

    @FXML
    private TableColumn<Product, String> expiryProductNameColumn;
    @FXML
    private TableColumn<Product, String> expiryProductCategoryColumn;
    @FXML
    private TableColumn<Product, Integer> expiryProductQuantityColumn;
    @FXML
    private TableColumn<Product, String> expiryProductExpDateColumn;

    @FXML
    private TableColumn<Product, String> lowQuantityProductNameColumn;
    @FXML
    private TableColumn<Product, String> lowQuantityProductCategoryColumn;
    @FXML
    private TableColumn<Product, Integer> lowQuantityProductQuantityColumn;
    @FXML
    private TableColumn<Product, Double> lowQuantityProductPriceColumn;

    @FXML
    private Button backButton;
    @FXML
    private Button removeAlertButton;
    @FXML
    private Button removeAllButton;

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

    // Method to initialize the table columns and load notifications
    @FXML
    private void initialize() {
        initializeColumns();
        loadNotifications();
    }

    // Helper method to initialize the columns for the TableView
    private void initializeColumns() {
        expiryProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        expiryProductCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        expiryProductQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expiryProductExpDateColumn.setCellValueFactory(new PropertyValueFactory<>("expDate"));

        lowQuantityProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        lowQuantityProductCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        lowQuantityProductQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        lowQuantityProductPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
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

    @FXML
    private void handleRemoveAll(ActionEvent event) {
        // Only clear the TableView (display) items, without affecting the database
        expiryProducts.getItems().clear();
        lowQuantityProducts.getItems().clear();
    }

    @FXML
    private void handleRemoveAlert(ActionEvent event) {
        if (selectedProduct != null) {
            // Remove the selected product from the display without affecting the database
            expiryProducts.getItems().remove(selectedProduct);
            lowQuantityProducts.getItems().remove(selectedProduct);
        } else {
            showErrorAlert("No Selection", "No notification selected for removal.");
        }
    }

    // Method to load notifications for expiry and low quantity products
    public void loadNotifications() {
        ObservableList<Product> expiryList = FXCollections.observableArrayList();
        ObservableList<Product> lowQuantityList = FXCollections.observableArrayList();

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
                expiryList.add(product);
            }
            expiryProducts.setItems(expiryList);

            ResultSet lowQuantityResults = lowQuantityStmt.executeQuery();
            while (lowQuantityResults.next()) {
                Product product = extractProductFromResultSet(lowQuantityResults);
                lowQuantityList.add(product);
            }
            lowQuantityProducts.setItems(lowQuantityList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Handle double-click to select a product for removal
    @FXML
    private void handleTableClick(javafx.scene.input.MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {  // Use mouseEvent instead of event

            selectedProduct = expiryProducts.getSelectionModel().getSelectedItem();
            if (selectedProduct == null) {
                selectedProduct = lowQuantityProducts.getSelectionModel().getSelectedItem();
            }
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}


