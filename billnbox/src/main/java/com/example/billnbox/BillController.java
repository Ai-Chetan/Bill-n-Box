package com.example.billnbox;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillController {

    private final String username = SessionManager.getInstance().getUsername(); // Variable to store the username
    private int billID; // Variable to store the generated Bill ID

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

    int counter = 0;

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
    private void handleGenerateBill(ActionEvent event) {
        counter++;
        String PDF_FILEPATH = "C:/Users/Kishor/IdeaProjects/billnbox/Generated PDFs/";
        String PDF_NAME = "Bill" + counter + ".pdf";
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(new File(PDF_FILEPATH + PDF_NAME)));
            document.open();

            Paragraph companyInfo = new Paragraph("Company Name\nAddress Line 1\nAddress Line 2\nContact: 99999 99999\t\tEmail: email@email.com\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            companyInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(companyInfo);

            Paragraph title = new Paragraph("Bill Receipt", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph date = new Paragraph("Bill ID: " + billID + "                                     Date: " + java.time.LocalDate.now() + "\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
            document.add(date);

            Paragraph customerInfo = new Paragraph("Customer Name: \nContact Details: \n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
            document.add(customerInfo);

            PdfPTable table = new PdfPTable(4);
            table.addCell("Product Name");
            table.addCell("Quantity");
            table.addCell("Price per Quantity");
            table.addCell("Total Price");

            for (Product product : productList) {
                table.addCell(product.getName());
                table.addCell(String.valueOf(product.getQuantity()));
                table.addCell(String.format("$%.2f", product.getPrice()));
                table.addCell(String.format("$%.2f", product.getQuantity() * product.getPrice()));

                // Update the product stock in the database
                updateProductStockInDatabase(product);
            }

            document.add(table);

            Paragraph grandTotal = new Paragraph("Grand Total: " + String.format("$%.2f", calculateTotalAmount()) + "           ",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            grandTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(grandTotal);

            Paragraph footer = new Paragraph("\n\nThank you for your purchase!\nPlease visit us again.",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            System.out.println("Successfully Generated PDF");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        // Insert data into Bill and Orders tables
        insertBillAndOrders();

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("PDF Generated successfully at your given path");
        if (alert.showAndWait().get() == ButtonType.OK) {
            navigateToPage(event, "8-dashboard.fxml");
        }
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        for (Product product : productList) {
            total += product.getPrice() * product.getQuantity();
        }
        return total;
    }

    private void updateProductStockInDatabase(Product product) {
        String query = "UPDATE Product SET Quantity = Quantity - ? WHERE ProductName = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, product.getQuantity()); // Deduct the product's quantity
            stmt.setString(2, product.getName()); // Identify the product by its name
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Stock updated successfully for product: " + product.getName());
            } else {
                System.out.println("Failed to update stock for product: " + product.getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertBillAndOrders() {
        String insertBillQuery = "INSERT INTO Bill (EmpID, CustomerName, Amount) VALUES (?, ?, ?)";
        String insertOrderQuery = "INSERT INTO Orders (BillID, SrNo, ProductName, Quantity, TotalPrice) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement insertBillStmt = conn.prepareStatement(insertBillQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery)) {

            // Insert into Bill table
            insertBillStmt.setInt(1, getEmployeeID()); // Replace with the correct employee ID
            insertBillStmt.setString(2, "Customer Name"); // Replace with actual customer name
            insertBillStmt.setDouble(3, calculateTotalAmount());
            insertBillStmt.executeUpdate();

            // Get the generated Bill ID
            ResultSet generatedKeys = insertBillStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                billID = generatedKeys.getInt(1);
            }

            // Insert into Orders table
            for (Product product : productList) {
                insertOrderStmt.setInt(1, billID);
                insertOrderStmt.setInt(2, getProductID(product.getName())); // Replace with method to get Product ID
                insertOrderStmt.setString(3, product.getName());
                insertOrderStmt.setInt(4, product.getQuantity());
                insertOrderStmt.setDouble(5, product.getPrice() * product.getQuantity());
                insertOrderStmt.addBatch();
            }
            insertOrderStmt.executeBatch();

            System.out.println("Bill and Orders inserted successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getEmployeeID() {
        // Replace with actual implementation to get employee ID based on session or context
        return 1000; // Example: return a dummy employee ID
    }

    private int getProductID(String productName) {
        // Replace with actual implementation to get product ID based on product name
        return 24; // Example: return a dummy product ID
    }
}
