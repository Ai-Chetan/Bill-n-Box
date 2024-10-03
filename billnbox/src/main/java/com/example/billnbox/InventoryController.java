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
import java.sql.*;
import java.util.ArrayList;

public class InventoryController {

    private ArrayList<Product> editedProducts = new ArrayList<>();

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
        productNameColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String oldValue = product.getProductName();
            product.setProductName(event.getNewValue());
            logInventoryChange("ProductName", oldValue, event.getNewValue(), oldValue);  // Use oldValue as the product name before editing
        });

        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String oldValue = product.getCategory();
            product.setCategory(event.getNewValue());
            logInventoryChange("Category", oldValue, event.getNewValue(), product.getProductName());
        });

        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Integer oldValue = product.getQuantity();
            product.setQuantity(event.getNewValue());
            logInventoryChange("Quantity", oldValue.toString(), event.getNewValue().toString(), product.getProductName());
        });

        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Double oldValue = product.getPrice();
            product.setPrice(event.getNewValue());
            logInventoryChange("Price", oldValue.toString(), event.getNewValue().toString(), product.getProductName());
        });

        mfgDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        mfgDateColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String oldValue = product.getMfgDate();
            product.setMfgDate(event.getNewValue());
            logInventoryChange("MfgDate", oldValue, event.getNewValue(), product.getProductName());
        });

        expDateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expDateColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String oldValue = product.getExpDate();
            product.setExpDate(event.getNewValue());
            logInventoryChange("ExpDate", oldValue, event.getNewValue(), product.getProductName());
        });

        lowQuantityAlertColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        lowQuantityAlertColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Integer oldValue = product.getLowQuantityAlert();
            product.setLowQuantityAlert(event.getNewValue());
            logInventoryChange("LowQuantityAlert", oldValue.toString(), event.getNewValue().toString(), product.getProductName());
        });
    }

    private void makeTableNonEditable() {
        tableView.setEditable(false); // Disable table editing
    }

    private void loadProductData() {
        // Clear existing items in the TableView to prevent duplicates
        tableView.getItems().clear();

        ObservableList<Product> productList = FXCollections.observableArrayList();

        // Use the OwnerID from the session manager
        String sql = "SELECT SrNo, ProductName, Category, Quantity, Price, MfgDate, ExpDate, LowQuantityAlert FROM Product WHERE OwnerID=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the OwnerID parameter
            pstmt.setInt(1, SessionManager.getInstance().getOwnerID());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                            rs.getInt("SrNo"), // Use actual SrNo from database
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
        String updateSql = "UPDATE Product SET ProductName = ?, Category = ?, Quantity = ?, Price = ?, MfgDate = ?, ExpDate = ?, LowQuantityAlert = ? WHERE SrNo = ? AND OwnerID = ?";
        String deleteSql = "DELETE FROM Product WHERE SrNo = ? AND OwnerID = ?";  // SQL to delete products

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            int ownerId = SessionManager.getOwnerID(); // Get the owner ID from the session manager

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
                updateStmt.setInt(9, ownerId); // Set the owner ID for the update statement
                updateStmt.addBatch();
            }
            updateStmt.executeBatch();

            // Delete marked products
            for (Product deletedProduct : deletedProducts) {
                deleteStmt.setInt(1, deletedProduct.getSrNo());
                deleteStmt.setInt(2, ownerId); // Set the owner ID for the delete statement
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
            // Mark the product for deletion
            deletedProducts.add(selectedProduct);

            // Remove product from TableView
            tableView.getItems().remove(selectedProduct);

            showError("Product marked for deletion.");
        }
    }

    private void deleteProductFromDatabase(Product product) {
        String deleteSql = "DELETE FROM Product WHERE SrNo = ? AND OwnerID = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Disable foreign key checks
            Statement stmt = conn.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Delete product from database
            PreparedStatement pstmt = conn.prepareStatement(deleteSql);
            pstmt.setInt(1, product.getSrNo());
            pstmt.setInt(2, getOwnerID());  // Use OwnerID from SessionManager
            pstmt.executeUpdate();

            // Re-enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            e.printStackTrace();
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


    private void deleteMarkedProducts() {
        String deleteSql = "DELETE FROM Product WHERE SrNo = ? AND OwnerID = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Disable foreign key checks
            Statement stmt = conn.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Delete marked products from database
            PreparedStatement pstmt = conn.prepareStatement(deleteSql);
            for (Product product : deletedProducts) {
                pstmt.setInt(1, product.getSrNo());
                pstmt.setInt(2, getOwnerID());  // Use OwnerID from SessionManager
                pstmt.executeUpdate();
            }

            // Re-enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Clear the products marked for deletion
        deletedProducts.clear();
    }

    @FXML
    private void saveInventoryChanges(ActionEvent event) {
        if (isEditing) {
            // Delete marked products from database
            deleteMarkedProducts();

            // Save other changes to the database
            saveProductData();

            // Log entry for inventory edit
            insertLog("Inventory edited by " + getCurrentUser());

            // Disable editing mode and refresh the product table
            isEditing = false;
            makeTableNonEditable();
            loadProductData();
        }
    }

    // Update product information in the database
    private void updateProductInDatabase(Product product) {
        String updateQuery = "UPDATE Product SET ProductName = ?, Category = ?, Quantity = ?, Price = ?, MfgDate = ?, ExpDate = ? WHERE SrNo = ? AND OwnerID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory());
            stmt.setInt(3, product.getQuantity());
            stmt.setDouble(4, product.getPrice());
            stmt.setDate(5, Date.valueOf(product.getMfgDate()));
            stmt.setDate(6, Date.valueOf(product.getExpDate()));
            stmt.setInt(7, product.getSrNo());
            stmt.setInt(8, getOwnerID());  // Use OwnerID from SessionManager

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get the current logged-in user from SessionManager
    private String getCurrentUser() {
        return SessionManager.getInstance().getUsername();  // Fetch current user from SessionManager
    }
    // Get the current OwnerID from the SessionManager
    private int getOwnerID() {
        return SessionManager.getInstance().getOwnerID();  // Fetch OwnerID from SessionManager
    }
    private void logInventoryChange(String columnName, String oldValue, String newValue, String productName) {
        String activity = "Edited " + columnName + " from '" + oldValue + "' to '" + newValue + "' for Product: " + productName;
        insertLog(activity);
    }

    // Insert a log entry into the logs table
    private void insertLog(String activity) {
        String logQuery = "INSERT INTO logs (date, time, User, activity,OwnerID) VALUES (CURDATE(), CURTIME(), ?, ?,?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(logQuery)) {

            stmt.setString(1, getCurrentUser());  // User performing the action
            stmt.setString(2, activity);          // Activity description
            stmt.setInt(3, SessionManager.getInstance().getOwnerID());  // OwnerID from SessionManager

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
