package com.example.billnbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryController {

    public static class Product {
        private final Integer srNo;
        private String productName;
        private String category;
        private Integer quantity;
        private Double price;
        private String mfgDate;
        private String expDate;
        private Integer lowQuantityAlert;

        public Product(Integer srNo, String productName, String category, Integer quantity, Double price, String mfgDate, String expDate, Integer lowQuantityAlert) {
            this.srNo = srNo;
            this.productName = productName;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.mfgDate = mfgDate;
            this.expDate = expDate;
            this.lowQuantityAlert = lowQuantityAlert;
        }

        // Getters and Setters for all fields
        public Integer getSrNo() { return srNo; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getMfgDate() { return mfgDate; }
        public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }
        public String getExpDate() { return expDate; }
        public void setExpDate(String expDate) { this.expDate = expDate; }
        public Integer getLowQuantityAlert() { return lowQuantityAlert; }
        public void setLowQuantityAlert(Integer lowQuantityAlert) { this.lowQuantityAlert = lowQuantityAlert; }
    }

    @FXML
    private TableView<Product> tableView;

    @FXML
    private TableColumn<Product, Integer> srNoColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;

    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableColumn<Product, Integer> quantityColumn;

    @FXML
    private TableColumn<Product, Double> priceColumn;

    @FXML
    private TableColumn<Product, String> mfgDateColumn;

    @FXML
    private TableColumn<Product, String> expDateColumn;

    @FXML
    private TableColumn<Product, Integer> lowQuantityAlertColumn;

    @FXML
    private Button inventoryBtn, deleteButton, backToDashboard, addNewProductButton, discardButton;

    @FXML
    private Label statusLabel;

    private ObservableList<Product> deletedProducts = FXCollections.observableArrayList();

    private boolean isEditing = false;
    private boolean isOwner; // To determine if the user is an owner

    @FXML
    public void initialize() {
        // Assume we have a method to get isOwner from LoginController
        isOwner = LoginController.getIsOwner(); // Adjust this based on how you access this value

        // Set up table columns
        srNoColumn.setCellValueFactory(new PropertyValueFactory<>("srNo"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        mfgDateColumn.setCellValueFactory(new PropertyValueFactory<>("mfgDate"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        lowQuantityAlertColumn.setCellValueFactory(new PropertyValueFactory<>("lowQuantityAlert"));

        // Initially, make the table non-editable
        makeTableNonEditable();

        // Load product data from the database
        loadProductData();

        // Disable the delete button initially
        deleteButton.setDisable(true);
        deleteButton.setVisible(false);
        discardButton.setVisible(false);
        addNewProductButton.setVisible(false);

        // Add listener to enable/disable the delete button when a row is selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null); // Disable button if no row is selected
        });

        // Restrict editing and deleting if the user is not the owner
        if (!isOwner) {
            makeTableNonEditable();
            deleteButton.setVisible(false);
            addNewProductButton.setVisible(false);
            inventoryBtn.setVisible(false); // Disable the edit button as well
        }
    }

    private void makeTableEditable() {
        tableView.setEditable(true);

        productNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        productNameColumn.setOnEditCommit(event -> event.getRowValue().setProductName(event.getNewValue()));

        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryColumn.setOnEditCommit(event -> event.getRowValue().setCategory(event.getNewValue()));

        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setOnEditCommit(event -> event.getRowValue().setQuantity(event.getNewValue()));

        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceColumn.setOnEditCommit(event -> event.getRowValue().setPrice(event.getNewValue()));

        mfgDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mfgDateColumn.setOnEditCommit(event -> event.getRowValue().setMfgDate(event.getNewValue()));

        expDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expDateColumn.setOnEditCommit(event -> event.getRowValue().setExpDate(event.getNewValue()));

        lowQuantityAlertColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        lowQuantityAlertColumn.setOnEditCommit(event -> event.getRowValue().setLowQuantityAlert(event.getNewValue()));
    }

    private void makeTableNonEditable() {
        tableView.setEditable(false); // Disable table editing
    }

    private void loadProductData() {
        // Clear existing items in the TableView to prevent duplicates
        tableView.getItems().clear();

        ObservableList<Product> productList = FXCollections.observableArrayList();

        String sql = "SELECT SrNo, ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert FROM Product";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("SrNo"),
                        rs.getString("ProductName"),
                        rs.getString("Category"),
                        rs.getInt("Quantity"),
                        rs.getDouble("Price"),
                        rs.getString("MfgDate"),
                        rs.getString("ExpDate"),
                        rs.getInt("LowQuantityAlert")
                );
                productList.add(product);
            }

            tableView.setItems(productList);

        } catch (SQLException e) {
            showError("Error loading product data: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setVisible(true);
            statusLabel.setText(message);

            // Automatically hide the label after 3 seconds (3000 milliseconds)
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> statusLabel.setVisible(false));
            pause.play();
        }
    }

    private void saveProductData() {
        ObservableList<Product> products = tableView.getItems();
        String updateSql = "UPDATE Product SET ProductName = ?, Category = ?, Quantity = ?, Price = ?, MfgDate = ?, ExpDate = ?, LowQuantityAlert = ? WHERE SrNo = ?";
        String deleteSql = "DELETE FROM Product WHERE SrNo = ?";  // SQL to delete products

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            // Update existing products
            for (Product product : products) {
                updateStmt.setString(1, product.getProductName());
                updateStmt.setString(2, product.getCategory());
                updateStmt.setInt(3, product.getQuantity());
                updateStmt.setDouble(4, product.getPrice());
                updateStmt.setString(5, product.getMfgDate());
                updateStmt.setString(6, product.getExpDate());
                updateStmt.setInt(7, product.getLowQuantityAlert());
                updateStmt.setInt(8, product.getSrNo());
                updateStmt.addBatch();
            }
            updateStmt.executeBatch();

            // Delete marked products
            for (Product deletedProduct : deletedProducts) {
                deleteStmt.setInt(1, deletedProduct.getSrNo());
                deleteStmt.addBatch();
            }
            deleteStmt.executeBatch();

            // Clear deleted products list after saving
            deletedProducts.clear();

            showError("Product data saved successfully.");
        } catch (SQLException e) {
            showError("Error saving product data: " + e.getMessage());
        }
    }

    @FXML
    private void toggleBtn(ActionEvent actionEvent) {
        if (isEditing) {
            // Save the changes to the database
            saveProductData();
            // Hide the delete and add new product buttons
            deleteButton.setVisible(false);
            addNewProductButton.setVisible(false);
            discardButton.setVisible(false);
            // Disable table editing
            makeTableNonEditable();
            // Change button text back to "Edit Inventory"
            inventoryBtn.setText("Edit Inventory");
            backToDashboard.setVisible(true);
        } else {
            // Enable table editing if the user is the owner
            if (isOwner) {
                makeTableEditable();
                // Hide status label
                statusLabel.setVisible(false);
                // Show the delete and add new product buttons
                deleteButton.setVisible(true);
                addNewProductButton.setVisible(true);
                discardButton.setVisible(true);
                backToDashboard.setVisible(false);
                // Change button text to "Save Changes"
                inventoryBtn.setText("Save Changes");
            }
        }

        // Toggle the editing state
        isEditing = !isEditing;
    }

    @FXML
    private void deleteSelectedProduct(ActionEvent event) {
        if (!isOwner) return; // Restrict delete action if not an owner

        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();

        if (selectedProduct != null) {
            // Remove product from TableView but not the database yet
            tableView.getItems().remove(selectedProduct);
            deletedProducts.add(selectedProduct);  // Track the deleted product

            showError("Product marked for deletion.");
        }
    }

    @FXML
    private void BackToDashboard(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }

    @FXML
    private void addNewProduct(ActionEvent event) {
        navigateToPage(event, "10a-add-new-product.fxml");
    }

    @FXML
    private void discardButtonAction(ActionEvent event) {
        // Clear the current table data
        tableView.getItems().clear();

        // Reload the original data from the database
        loadProductData();

        // Toggle back to viewing mode (non-editable)
        isEditing = false;
        makeTableNonEditable();

        // Hide discard and add new product buttons
        discardButton.setVisible(false);
        addNewProductButton.setVisible(false);
        deleteButton.setVisible(false);

        // Show the BackToDashboard button again
        backToDashboard.setVisible(true);

        // Change the inventory button text back to "Edit Inventory"
        inventoryBtn.setText("Edit Inventory");

        // Hide the status label, if any
        statusLabel.setVisible(false);
    }

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
}
