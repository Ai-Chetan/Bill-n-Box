package com.example.billnbox;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class InventoryController {
    @FXML
    private TableView<InventoryTable> inventoryTableView;

    @FXML
    private TableColumn<InventoryTable, Integer> srNoColumn;

    @FXML
    private TableColumn<InventoryTable, String> productNameColumn;

    @FXML
    private TableColumn<InventoryTable, String> categoryColumn;

    @FXML
    private TableColumn<InventoryTable, Integer> quantityColumn;

    @FXML
    private TableColumn<InventoryTable, Double> priceColumn;

    // Change Date columns to LocalDate
    @FXML
    private TableColumn<InventoryTable, LocalDate> mfgDateColumn;

    @FXML
    private TableColumn<InventoryTable, LocalDate> expDateColumn;

    @FXML
    private TableColumn<InventoryTable, Integer> lowQuantityAlertColumn;

    @FXML
    private void BackToDashboard(ActionEvent event) {
        try {
            // Load the dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("8-dashboard.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Set the scene to the dashboard
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        // Set up the columns
        srNoColumn.setCellValueFactory(new PropertyValueFactory<>("Sr_No"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("Product_Name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("Category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("Quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("Price"));
        mfgDateColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(
                () -> convertToLocalDate(cellData.getValue().getMfg_Date())
        ));
        expDateColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(
                () -> convertToLocalDate(cellData.getValue().getExp_Date())
        ));
        lowQuantityAlertColumn.setCellValueFactory(new PropertyValueFactory<>("Low_Quantity_Alert"));

        // Enable editing for the table
        inventoryTableView.setEditable(true);

        // Set cell factories to allow editing for each column
        srNoColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        mfgDateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        expDateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        lowQuantityAlertColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // Handle edit commit events for all columns
        srNoColumn.setOnEditCommit(event -> event.getRowValue().setSr_No(event.getNewValue()));
        productNameColumn.setOnEditCommit(event -> event.getRowValue().setProduct_Name(event.getNewValue()));
        categoryColumn.setOnEditCommit(event -> event.getRowValue().setCategory(event.getNewValue()));
        quantityColumn.setOnEditCommit(event -> event.getRowValue().setQuantity(event.getNewValue()));
        priceColumn.setOnEditCommit(event -> event.getRowValue().setPrice(event.getNewValue()));
        mfgDateColumn.setOnEditCommit(event -> event.getRowValue().setMfg_Date(convertToDate(event.getNewValue())));
        expDateColumn.setOnEditCommit(event -> event.getRowValue().setExp_Date(convertToDate(event.getNewValue())));
        lowQuantityAlertColumn.setOnEditCommit(event -> event.getRowValue().setLow_Quantity_Alert(event.getNewValue()));

        // Add data to the table
        ObservableList<InventoryTable> inventoryList = getInventoryList();
        inventoryTableView.setItems(inventoryList);

        // Set a fixed cell size
        inventoryTableView.setFixedCellSize(25);

        // Dynamically adjust the TableView height to match the number of items
        inventoryTableView.prefHeightProperty().bind(inventoryTableView.fixedCellSizeProperty().multiply(Bindings.size(inventoryList).add(1.01)));

        // Hide the TableView if there's no data
        inventoryTableView.setVisible(!inventoryList.isEmpty());
    }


    // Sample data for the table
    private ObservableList<InventoryTable> getInventoryList() {
        return FXCollections.observableArrayList(
                new InventoryTable(1, "Notebook", "Stationery", 100, 2.99, new Date(122, 5, 15), new Date(125, 5, 15), 10),
                new InventoryTable(2, "Ball Pen", "Stationery", 200, 0.99, new Date(123, 1, 22), new Date(124, 1, 22), 20),
                new InventoryTable(3, "Pencil", "Stationery", 150, 0.50, new Date(121, 9, 10), new Date(123, 9, 10), 15),
                new InventoryTable(4, "Marker", "Stationery", 80, 1.25, new Date(122, 7, 18), new Date(124, 7, 18), 10),
                new InventoryTable(5, "Eraser", "Stationery", 250, 0.30, new Date(123, 3, 5), new Date(124, 3, 5), 30),
                new InventoryTable(6, "Highlighter", "Stationery", 90, 1.75, new Date(121, 10, 11), new Date(123, 10, 11), 10),
                new InventoryTable(7, "Ruler", "Stationery", 120, 0.75, new Date(122, 6, 20), new Date(125, 6, 20), 12),
                new InventoryTable(8, "Glue Stick", "Stationery", 60, 1.49, new Date(123, 4, 17), new Date(124, 4, 17), 8),
                new InventoryTable(9, "Stapler", "Stationery", 30, 3.99, new Date(122, 8, 14), new Date(124, 8, 14), 5),
                new InventoryTable(10, "Scissors", "Stationery", 50, 2.49, new Date(123, 2, 3), new Date(125, 2, 3), 10),
                new InventoryTable(11, "Sticky Notes", "Stationery", 70, 1.99, new Date(121, 11, 30), new Date(124, 11, 30), 15),
                new InventoryTable(12, "Binder Clips", "Stationery", 40, 2.25, new Date(122, 5, 25), new Date(124, 5, 25), 5),
                new InventoryTable(13, "Paper Clips", "Stationery", 300, 0.89, new Date(123, 7, 7), new Date(125, 7, 7), 50),
                new InventoryTable(14, "File Folder", "Stationery", 20, 4.99, new Date(122, 9, 12), new Date(125, 9, 12), 3),
                new InventoryTable(15, "Correction Pen", "Stationery", 110, 1.50, new Date(123, 6, 9), new Date(124, 6, 9), 12)
        );
    }

    // Convert LocalDate to Date for editing purposes
    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Convert Date to LocalDate for display in the TableView
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Hide the TableView if there's no data
}
