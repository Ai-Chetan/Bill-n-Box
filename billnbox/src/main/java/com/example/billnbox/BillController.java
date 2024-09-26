package com.example.billnbox;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BillController {

    private final String username = SessionManager.getInstance().getUsername(); // Variable to store the username
    private String shopname ;
    private String shopaddress ;
    private String email ;
    private String phoneno ;
    private int billID; // Variable to store the generated Bill ID
    private boolean isOwner = LoginController.getIsOwner();

    public void getShopInfo() {
        String ownerId = null;
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            // Check if the user is an Owner or Employee
            if (isOwner) {
                // Directly fetch Owner's info
                String ownerSql = "SELECT ShopName, ShopAddress, EmailID, PhoneNo FROM Owner WHERE Username = ?";
                try (PreparedStatement ownerStmt = conn.prepareStatement(ownerSql)) {
                    ownerStmt.setString(1, username);
                    ResultSet ownerRs = ownerStmt.executeQuery();
                    if (ownerRs.next()) {
                        setShopInfo(ownerRs);
                    } else {
                        System.out.println("Owner not found.");
                    }
                }
            } else {
                // Fetch the OwnerID from Employee table
                String employeeSql = "SELECT OwnerID FROM Employee WHERE Username = ?";
                try (PreparedStatement employeeStmt = conn.prepareStatement(employeeSql)) {
                    employeeStmt.setString(1, username);
                    ResultSet employeeRs = employeeStmt.executeQuery();
                    if (employeeRs.next()) {
                        ownerId = employeeRs.getString("OwnerID");
                    } else {
                        System.out.println("Employee not found.");
                        return;
                    }
                }

                // Fetch the Owner's info based on OwnerID
                String ownerSql = "SELECT ShopName, ShopAddress, EmailID, PhoneNo FROM Owner WHERE OwnerID = ?";
                try (PreparedStatement ownerStmt = conn.prepareStatement(ownerSql)) {
                    ownerStmt.setString(1, ownerId);
                    ResultSet ownerRs = ownerStmt.executeQuery();
                    if (ownerRs.next()) {
                        setShopInfo(ownerRs);
                    } else {
                        System.out.println("Owner not found for the given OwnerID.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error accessing database: " + e.getMessage());
        }
    }

    private void setShopInfo(ResultSet rs) throws SQLException {
         shopname = rs.getString("ShopName");
         shopaddress = rs.getString("ShopAddress");
         email = rs.getString("EmailID");
         phoneno = rs.getString("PhoneNo");
    }

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
    @FXML
    private TextField customerName, date;

    private ObservableList<Product> productList;
    private Product selectedProduct;

    @FXML
    public void initialize() {
        date.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
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
        getShopInfo();

        Document document = new Document();

        String CustomerName = customerName.getText();
        date.setText(String.valueOf(LocalDate.now()));

        try {
            // Insert data into Bill and Orders tables
            insertBillAndOrders();


            String PDF_FILEPATH = RegistrationController.FilePath;
            String PDF_NAME = "Bill - " + billID + ".pdf";

            if (PDF_FILEPATH == null) {
                PDF_FILEPATH = "C:/Users/Dell/Desktop/SEM 3";
            }

            // Create the PDF writer instance
            PdfWriter.getInstance(document, new FileOutputStream(new File(PDF_FILEPATH + PDF_NAME)));
            document.open();

// Set fonts
            Font shopNameFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font shopInfoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font contactFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font sectionDividerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GRAY);

// Add shop name
            Paragraph shopName = new Paragraph(shopname, shopNameFont);
            shopName.setAlignment(Element.ALIGN_CENTER);
            shopName.setSpacingAfter(10f); // Add some space below shop name
            document.add(shopName);

// Add shop address
            Paragraph shopAddress = new Paragraph(shopaddress, shopInfoFont);
            shopAddress.setAlignment(Element.ALIGN_CENTER);
            shopAddress.setSpacingAfter(5f); // Add some space below shop address
            document.add(shopAddress);

// Add contact information (phone number and email) side by side
            PdfPTable contactTable = new PdfPTable(2);
            contactTable.setWidthPercentage(100); // Table width 100% of page
            contactTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            contactTable.setSpacingAfter(10f); // Add some space below contact info
            contactTable.setWidths(new float[] { 1f, 1f }); // Set equal column widths

// Create a cell for the phone number
            PdfPCell phoneCell = new PdfPCell(new Phrase("Contact: " + phoneno, contactFont));
            phoneCell.setBorder(PdfPCell.NO_BORDER);
            phoneCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Align left
            contactTable.addCell(phoneCell);

// Create a cell for the email
            PdfPCell emailCell = new PdfPCell(new Phrase("Email: " + email, contactFont));
            emailCell.setBorder(PdfPCell.NO_BORDER);
            emailCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Align right
            contactTable.addCell(emailCell);

// Add the contact table to the document
            document.add(contactTable);


// Add a visible divider after shop details
            Paragraph divider = new Paragraph("--------------------------------------------------------------------------------------------------------------------------------\n",
                    sectionDividerFont);
            divider.setAlignment(Element.ALIGN_CENTER);
            divider.setSpacingAfter(10f);
            document.add(divider);

// Add bill receipt title
            Paragraph title = new Paragraph("Bill Receipt", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15f); // Add space below the title
            document.add(title);

// Add bill ID and date
            // Create a table with 2 columns
            PdfPTable dateTable = new PdfPTable(2);
            dateTable.setWidthPercentage(100); // Set the width of the table to 100%
            dateTable.setSpacingAfter(10f); // Add space below the table

// Create a cell for the Bill ID
            PdfPCell billIdCell = new PdfPCell(new Phrase("Bill ID: " + billID, new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL)));
            billIdCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Align left
            billIdCell.setBorder(PdfPCell.NO_BORDER); // No border
            dateTable.addCell(billIdCell);

// Create a cell for the Date
            PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL)));
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Align right
            dateCell.setBorder(PdfPCell.NO_BORDER); // No border
            dateTable.addCell(dateCell);

// Add the table to the document
            document.add(dateTable);

// Add customer info
            Paragraph customerInfo = new Paragraph("Customer Name: " + CustomerName + "\n" + "Contact Details: " + "\n\n", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
            customerInfo.setSpacingAfter(15f); // Add space after customer info
            document.add(customerInfo);

// Create a PdfPTable with 4 columns
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100); // Set the width of the table to 100% of the page width
            table.setSpacingBefore(10f); // Add space before table

// Set column widths (optional but improves layout)
            table.setWidths(new float[] { 2f, 1f, 1.5f, 1f });

// Define fonts for the table
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

// Create header cells with background color
            PdfPCell header;

            header = new PdfPCell(new Phrase("Product Name", headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(BaseColor.GRAY);
            header.setPadding(5);
            table.addCell(header);

            header = new PdfPCell(new Phrase("Quantity", headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(BaseColor.GRAY);
            header.setPadding(5);
            table.addCell(header);

            header = new PdfPCell(new Phrase("Price per Quantity", headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(BaseColor.GRAY);
            header.setPadding(5);
            table.addCell(header);

            header = new PdfPCell(new Phrase("Total Price", headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(BaseColor.GRAY);
            header.setPadding(5);
            table.addCell(header);

// Add product rows
            for (Product product : productList) {
                PdfPCell cell;

                // Product Name
                cell = new PdfPCell(new Phrase(product.getName(), cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);

                // Quantity
                cell = new PdfPCell(new Phrase(String.valueOf(product.getQuantity()), cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);

                // Price per Quantity
                cell = new PdfPCell(new Phrase(String.format("%.2f", product.getPrice()), cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);

                // Total Price
                cell = new PdfPCell(new Phrase(String.format("%.2f", product.getQuantity() * product.getPrice()), cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);

                // Update the product stock in the database
                updateProductStockInDatabase(product);
            }

// Add the table to the document
            document.add(table);

// Add Grand Total
            Paragraph grandTotal = new Paragraph("Grand Total: " + String.format("%.2f", calculateTotalAmount()) + "\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            grandTotal.setAlignment(Element.ALIGN_RIGHT);
            grandTotal.setSpacingBefore(15f); // Add some space before the total
            document.add(grandTotal);

// Add a footer message
            Paragraph footer = new Paragraph("\nThank you for your purchase!\nPlease visit us again.",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20f); // Add space before the footer
            document.add(footer);

// Close the document
            document.close();

            System.out.println("Successfully Generated PDF");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bill generated successfully");
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
        String insertBillQuery = "INSERT INTO Bill (EmpID, OwnerID, CustomerName, Amount) VALUES (?, ?, ?, ?)";
        String insertOrderQuery = "INSERT INTO Orders (BillID, SrNo, ProductName, Quantity, TotalPrice) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement insertBillStmt = conn.prepareStatement(insertBillQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery)) {

            // Insert into Bill table
            if (isOwner) {
                insertBillStmt.setNull(1, java.sql.Types.INTEGER); // No EmpID for owner
                insertBillStmt.setInt(2, getUserID()); // Use OwnerID
            } else {
                insertBillStmt.setInt(1, getUserID()); // Use EmpID
                insertBillStmt.setNull(2, java.sql.Types.INTEGER); // No OwnerID for employee
            }

            insertBillStmt.setString(3, customerName.getText()); // Customer name
            insertBillStmt.setDouble(4, calculateTotalAmount()); // Amount
            insertBillStmt.executeUpdate();

            // Get the generated Bill ID
            ResultSet generatedKeys = insertBillStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                billID = generatedKeys.getInt(1);
            }

            // Insert into Orders table
            for (Product product : productList) {
                insertOrderStmt.setInt(1, billID);
                insertOrderStmt.setInt(2, getSrNo(product.getName())); // Fetch product ID dynamically
                insertOrderStmt.setString(3, product.getName());
                insertOrderStmt.setInt(4, product.getQuantity());
                insertOrderStmt.setDouble(5, product.getPrice() * product.getQuantity());
                insertOrderStmt.addBatch();
            }
            insertOrderStmt.executeBatch();

            System.out.println("Bill and Orders inserted successfully");

            // Execute bill insertion
            int affectedRows = insertBillStmt.executeUpdate();

            if (affectedRows > 0) {
                // Log entry for bill creation
                insertLog();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert a log into the logs table
    private void insertLog(){
        String activity = "Bill created of Amount " + calculateTotalAmount();
        String logQuery = "INSERT INTO logs (date, time, User, activity, OwnerID) VALUES (CURDATE(), CURTIME(), ?, ?, ?)";
        try (Connection conn1 = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement stmt = conn1.prepareStatement(logQuery)) {

            stmt.setString(1, SessionManager.getInstance().getUsername());  // User performing the action
            stmt.setString(2, activity);  // Activity description
            stmt.setInt(3, SessionManager.getInstance().getOwnerID());  // OwnerID from SessionManager

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getUserID() {
        int userID = -1; // Default if not found
        String query;

        if (isOwner) {
            // Query to get OwnerID
            query = "SELECT OwnerID FROM Owner WHERE Username = ?";
        } else {
            // Query to get EmployeeID
            query = "SELECT EmpID FROM Employee WHERE Username = ?";
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username); // Use the logged-in user's username
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (isOwner) {
                    userID = rs.getInt("OwnerID");
                } else {
                    userID = rs.getInt("EmpID");
                }
            } else {
                System.out.println((isOwner ? "Owner" : "Employee") + " not found for username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userID;
    }

    private int getSrNo(String productName) {
        int SrNo = -1; // Default if not found
        String query = "SELECT SrNo FROM Product WHERE ProductName = ?";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SrNo = rs.getInt("SrNo");
            } else {
                System.out.println("Product not found: " + productName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return SrNo;
    }
}
