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
    private Button inventoryBtn;

    @FXML
    private Label statusLabel;

    private boolean isEditing = false;

    @FXML
    public void initialize() {
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
        }
    }

    private void saveProductData() {
        ObservableList<Product> products = tableView.getItems();

        String sql = "UPDATE Product SET ProductName = ?, Category = ?, Quantity = ?, Price = ?, MfgDate = ?, ExpDate = ?, LowQuantityAlert = ? WHERE SrNo = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Product product : products) {
                pstmt.setString(1, product.getProductName());
                pstmt.setString(2, product.getCategory());
                pstmt.setInt(3, product.getQuantity());
                pstmt.setDouble(4, product.getPrice());
                pstmt.setString(5, product.getMfgDate());
                pstmt.setString(6, product.getExpDate());
                pstmt.setInt(7, product.getLowQuantityAlert());
                pstmt.setInt(8, product.getSrNo());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
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

            // Disable table editing
            makeTableNonEditable();
            // Change button text back to "Edit Inventory"
            inventoryBtn.setText("Edit Inventory");
        } else {
            // Enable table editing
            makeTableEditable();
            statusLabel.setVisible(false);
            // Change button text to "Save Changes"
            inventoryBtn.setText("Save Changes");
        }

        // Toggle the editing state
        isEditing = !isEditing;
    }

    @FXML
    private void BackToDashboard(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
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
