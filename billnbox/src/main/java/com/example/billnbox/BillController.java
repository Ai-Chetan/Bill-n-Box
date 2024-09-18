package com.example.billnbox;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BillController {

    @FXML
    private TableView<Product> tableView;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Double> totalPriceColumn;
    @FXML
    private TextField productName, quantity, price, grandPrice;
    @FXML
    private Button addToBillBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private ListView<Product> suggestionList;

    private ObservableList<Product> productList;
    private Product selectedProduct;

    @FXML
    public void initialize() {
        productList = FXCollections.observableArrayList();
        tableView.setItems(productList);

        // Create a placeholder label
        Label placeholder = new Label("No Product Found");
        suggestionList.setPlaceholder(placeholder);

        // Disable table cell editing
        tableView.setEditable(false);

        // Bind columns to Product properties
        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        totalPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getQuantity() * cellData.getValue().getPrice()).asObject());

        suggestionList.setVisible(false);
        deleteBtn.setDisable(true);

        // Event handler for text field when user types
        productName.setOnKeyReleased(event -> {
            String text = productName.getText();
            if (text.length() > 0) {
                List<Product> suggestions = getSuggestions(text);
                updateSuggestionList(suggestions);
                suggestionList.setVisible(true);
            } else {
                suggestionList.setVisible(false);
            }
        });

        // Event when a suggestion is clicked
        suggestionList.setOnMouseClicked(event -> {
            Product selectedProduct = suggestionList.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                productName.setText(selectedProduct.getName());
                price.setText(String.valueOf(selectedProduct.getPrice()));
                suggestionList.setVisible(false);
            }
        });

        // Event handler for when a row is selected in the TableView
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProduct = newSelection;
                productName.setText(newSelection.getName());
                quantity.setText(String.valueOf(newSelection.getQuantity()));
                price.setText(String.valueOf(newSelection.getPrice()));
                addToBillBtn.setText("Edit Entry");
                deleteBtn.setDisable(false);
            } else {
                addToBillBtn.setText("Add to Bill");
                deleteBtn.setDisable(true);
            }
        });
    }

    private void calculateGrandTotal() {
        double grandTotal = 0.0;
        for (Product product : productList) {
            grandTotal += product.getQuantity() * product.getPrice();
        }
        grandPrice.setText(String.format("%.2f", grandTotal));
    }

    public class Product {
        private SimpleStringProperty name;
        private SimpleDoubleProperty price;
        private SimpleIntegerProperty quantity;

        public Product(String name, double price, int quantity) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
        }

        public String getName() {
            return name.get();
        }

        public double getPrice() {
            return price.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public void setPrice(double price) {
            this.price.set(price);
        }

        public void setQuantity(int quantity) {
            this.quantity.set(quantity);
        }

        public SimpleStringProperty productNameProperty() {
            return name;
        }

        public SimpleDoubleProperty priceProperty() {
            return price;
        }

        public SimpleIntegerProperty quantityProperty() {
            return quantity;
        }

        @Override
        public String toString() {
            return name.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Product product = (Product) o;
            return Double.compare(product.price.get(), price.get()) == 0 &&
                    quantity.get() == product.quantity.get() &&
                    name.get().equals(product.name.get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(name.get(), price.get(), quantity.get());
        }
    }

    private List<Product> getSuggestions(String input) {
        List<Product> suggestions = new ArrayList<>();
        String query = "SELECT ProductName, Price FROM Product WHERE ProductName LIKE ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + input + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String productName = rs.getString("ProductName");
                double price = rs.getDouble("Price");
                suggestions.add(new Product(productName, price, 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    private void updateSuggestionList(List<Product> suggestions) {
        ObservableList<Product> items = FXCollections.observableArrayList(suggestions);
        suggestionList.setItems(items);
    }

    @FXML
    private void handleAddToBillButton(ActionEvent event) {
        String productNameText = productName.getText();
        double productPrice = Double.parseDouble(price.getText());
        int productQuantity = Integer.parseInt(quantity.getText());

        if (selectedProduct == null) {
            Product product = new Product(productNameText, productPrice, productQuantity);
            productList.add(product);
        } else {
            selectedProduct.setName(productNameText);
            selectedProduct.setPrice(productPrice);
            selectedProduct.setQuantity(productQuantity);

            // Ensure TableView updates the correct item
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                tableView.getItems().set(selectedIndex, selectedProduct);
            }

            addToBillBtn.setText("Add to Bill");
        }

        clearForm();
        calculateGrandTotal();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (selectedProduct != null) {
            int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                productList.remove(selectedIndex);
                tableView.getSelectionModel().clearSelection();
            }
            clearForm();
            calculateGrandTotal();
        }
    }

    private void clearForm() {
        productName.clear();
        price.clear();
        quantity.clear();
        selectedProduct = null;
        addToBillBtn.setText("Add to Bill");
        deleteBtn.setDisable(true);
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
        navigateToPage(event, "8-dashboard.fxml");
    }

    @FXML
    private void handleAddToInventory(ActionEvent event) {
        navigateToPage(event, "8-dashboard.fxml");
    }
}
