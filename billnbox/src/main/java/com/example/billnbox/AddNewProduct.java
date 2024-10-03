package com.example.billnbox;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AddNewProduct {

    @FXML
    private TableView<Product> tableView;

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
    private TextField productName;

    @FXML
    private TextField category;

    @FXML
    private TextField quantity;

    @FXML
    private TextField price;

    @FXML
    private DatePicker mfgDate;

    @FXML
    private DatePicker expDate;

    @FXML
    private TextField lowQuantityAlert;

    @FXML
    private Button addBtn;

    @FXML
    private Button addToInventoryBtn;

    @FXML
    private Label statusLabel;

    private final ObservableList<Product> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Initialize table columns
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        mfgDateColumn.setCellValueFactory(new PropertyValueFactory<>("mfgDate"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        lowQuantityAlertColumn.setCellValueFactory(new PropertyValueFactory<>("lowQuantityAlert"));

        // Set table data
        tableView.setItems(data);

        // Set initial state of buttons
        updateAddButtonState();
        updateAddToInventoryButtonState();

        // Add listeners to text fields
        productName.textProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        category.textProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        quantity.textProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        price.textProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        mfgDate.valueProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        expDate.valueProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());
        lowQuantityAlert.textProperty().addListener((observable, oldValue, newValue) -> updateAddButtonState());

        // Add listener to the tableView's items property using ListChangeListener
        data.addListener((ListChangeListener.Change<? extends Product> change) -> updateAddToInventoryButtonState());

        // Add listener for table selection
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });
    }

    public static void addProductsToInventory(ObservableList<Product> products) throws SQLException {
        String insertSql = "INSERT INTO Product (ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert, OwnerID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            for (Product product : products) {
                insertStmt.setString(1, product.getProductName());
                insertStmt.setString(2, product.getCategory());
                insertStmt.setInt(3, product.getQuantity());
                insertStmt.setDouble(4, product.getPrice());
                insertStmt.setDate(5, Date.valueOf(product.getMfgDate()));
                insertStmt.setDate(6, Date.valueOf(product.getExpDate()));
                insertStmt.setInt(7, product.getLowQuantityAlert());
                insertStmt.setInt(8, SessionManager.getOwnerID());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    // Method to populate fields when a row in the table is selected
    private void populateFields(Product product) {
        productName.setText(product.getProductName());
        category.setText(product.getCategory());
        quantity.setText(String.valueOf(product.getQuantity()));
        price.setText(String.valueOf(product.getPrice()));
        mfgDate.setValue(product.getMfgDate());  // Set LocalDate to DatePicker
        expDate.setValue(product.getExpDate());  // Set LocalDate to DatePicker
        lowQuantityAlert.setText(String.valueOf(product.getLowQuantityAlert()));
    }

    private void updateAddButtonState() {
        // Check if any text field is empty
        boolean anyEmpty = productName.getText().trim().isEmpty() ||
                category.getText().trim().isEmpty() ||
                quantity.getText().trim().isEmpty() ||
                price.getText().trim().isEmpty() ||
                expDate.getValue() == null ||
                mfgDate.getValue() == null ||
                lowQuantityAlert.getText().trim().isEmpty();

        // Disable the Add button if any field is empty
        addBtn.setDisable(anyEmpty);
    }

    private void updateAddToInventoryButtonState() {
        // Disable the Add to Inventory button if the table is empty
        addToInventoryBtn.setDisable(data.isEmpty());
    }

    @FXML
    private void handleAddButton() {
        try {
            // Check if an item is selected for editing
            Product selectedProduct = tableView.getSelectionModel().getSelectedItem();

            String name = productName.getText();
            String cat = category.getText();
            int qty = Integer.parseInt(quantity.getText());
            double prc = Double.parseDouble(price.getText());
            LocalDate mfg = mfgDate.getValue();  // Get LocalDate from DatePicker
            LocalDate exp = expDate.getValue();  // Get LocalDate from DatePicker
            int alert = Integer.parseInt(lowQuantityAlert.getText());

            if (selectedProduct != null) {
                // Update existing product
                selectedProduct.setProductName(name);
                selectedProduct.setCategory(cat);
                selectedProduct.setQuantity(qty);
                selectedProduct.setPrice(prc);
                selectedProduct.setMfgDate(mfg);  // Set LocalDate
                selectedProduct.setExpDate(exp);  // Set LocalDate
                selectedProduct.setLowQuantityAlert(alert);

                // Refresh the table view
                tableView.refresh();
                statusLabel.setText("Product updated successfully!");
            } else {
                // Add new product
                Product newProduct = new Product(name, cat, qty, prc, mfg, exp, alert);
                data.add(newProduct);
                statusLabel.setText("Product added successfully!");

                // Log the activity of adding a new product
                logActivity("Added new product: " + name);
            }

            // Clear text fields
            clearFields();

            // Update button states
            updateAddToInventoryButtonState();

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input! Please enter correct values.");
        }
    }

    // Method to clear the text fields after adding or updating a product
    private void clearFields() {
        productName.clear();
        category.clear();
        quantity.clear();
        price.clear();
        mfgDate.setValue(null);
        expDate.setValue(null);
        lowQuantityAlert.clear();

        // Clear the selection
        tableView.getSelectionModel().clearSelection();
    }

    // Define Product class (include setters)
    public static class Product {
        private String productName;
        private String category;
        private int quantity;
        private double price;
        private LocalDate mfgDate;  // Changed from String to LocalDate
        private LocalDate expDate;  // Changed from String to LocalDate
        private int lowQuantityAlert;

        public Product(String productName, String category, int quantity, double price, LocalDate mfgDate, LocalDate expDate, int lowQuantityAlert) {
            this.productName = productName;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
            this.mfgDate = mfgDate;  // Use LocalDate directly
            this.expDate = expDate;  // Use LocalDate directly
            this.lowQuantityAlert = lowQuantityAlert;
        }

        // Getters and Setters for each field
        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public LocalDate getMfgDate() {  // Changed to LocalDate
            return mfgDate;
        }

        public void setMfgDate(LocalDate mfgDate) {  // Changed to LocalDate
            this.mfgDate = mfgDate;
        }

        public LocalDate getExpDate() {  // Changed to LocalDate
            return expDate;
        }

        public void setExpDate(LocalDate expDate) {  // Changed to LocalDate
            this.expDate = expDate;
        }

        public int getLowQuantityAlert() {
            return lowQuantityAlert;
        }

        public void setLowQuantityAlert(int lowQuantityAlert) {
            this.lowQuantityAlert = lowQuantityAlert;
        }
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

    @FXML
    private void handleCancelButton(ActionEvent event) {
        navigateToPage(event, "10-inventory.fxml");
    }

    @FXML
    private void handleaddToInventory(ActionEvent event) {
        try {
            addProductsToInventory(tableView.getItems());
            statusLabel.setText("Products added to inventory successfully!");

            // Optionally, navigate to another page after successful insertion
            navigateToPage(event, "10-inventory.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error while adding products to inventory.");
        }
    }

    private void logActivity(String activity) {
        String insertLogSql = "INSERT INTO logs (date, time, User, activity, OwnerID) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertLogSql)) {

            // Set current date and time
            LocalDate today = LocalDate.now();
            Time time = Time.valueOf(LocalTime.now());
            String username = SessionManager.getInstance().getUsername(); // Assuming you have a way to get the current username
            int ownerID = SessionManager.getOwnerID();

            pstmt.setDate(1, Date.valueOf(today));
            pstmt.setTime(2, time);
            pstmt.setString(3, username);
            pstmt.setString(4, activity);
            pstmt.setInt(5, ownerID);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
