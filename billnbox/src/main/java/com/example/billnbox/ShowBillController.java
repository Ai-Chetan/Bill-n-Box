//package com.example.billnbox;
//
//import javafx.beans.property.SimpleDoubleProperty;
//import javafx.beans.property.SimpleIntegerProperty;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.print.PageLayout;
//import javafx.print.PrinterJob;
//import javafx.scene.Node;
//import javafx.scene.transform.Scale;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//
//public class ShowBillController {
//
//    @FXML
//    private TableView<BillController.Product> tableView;
//    @FXML
//    private TableColumn<BillController.Product, String> productNameColumn;
//    @FXML
//    private TableColumn<BillController.Product, Integer> quantityColumn;
//    @FXML
//    private TableColumn<BillController.Product, Double> priceColumn;
//    @FXML
//    private TableColumn<BillController.Product, Double> totalPriceColumn;
//
//    @FXML
//    public void initialize() {
//        tableView.setItems(SharedData.getProductList());
//
//        productNameColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
//        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
//        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
//        totalPriceColumn.setCellValueFactory(cellData ->
//                new SimpleDoubleProperty(cellData.getValue().getQuantity() * cellData.getValue().getPrice()).asObject());
//    }
//
//    private void navigateToPage(String fxmlFile) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + fxmlFile));
//            Parent root = loader.load();
//            Stage stage = (Stage) tableView.getScene().getWindow();
//            stage.setScene(new Scene(root));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void handlePrintBill(javafx.event.ActionEvent actionEvent) {
//        PrinterJob job = PrinterJob.createPrinterJob();
//        if (job != null) {
//            try {
//                Node rootNode = tableView.getScene().getRoot();
//                double nodeWidth = rootNode.getBoundsInLocal().getWidth();
//                double nodeHeight = rootNode.getBoundsInLocal().getHeight();
//
//                PageLayout pageLayout = job.getJobSettings().getPageLayout();
//                double printableWidth = pageLayout.getPrintableWidth();
//                double printableHeight = pageLayout.getPrintableHeight();
//
//                double scaleX = printableWidth / nodeWidth;
//                double scaleY = printableHeight / nodeHeight;
//                double scale = Math.min(scaleX, scaleY);
//
//                rootNode.getTransforms().add(new Scale(scale, scale));
//
//                boolean printed = job.printPage(rootNode);
//                if (printed) {
//                    job.endJob();
//                } else {
//                    System.out.println("Print job failed.");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @FXML
//    private void handleEditBill(javafx.event.ActionEvent actionEvent) {
//        navigateToPage("11-bill.fxml");
//    }
//}
