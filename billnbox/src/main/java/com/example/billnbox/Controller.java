package com.example.billnbox;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class Controller {

    private final String username = SessionManager.getInstance().getUsername();
    private final String shopname = SessionManager.getInstance().getShopname();
    private final String shopaddress = SessionManager.getInstance().getShopaddress();

    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private Label productsSoldLabel, earningsLabel, nearingexpirydateLabel, belowlowquantityLabel, expiredproductsLabel;

    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private PieChart pieChart;
    @FXML
    private Button logButton; // Reference to the Log button
    @FXML
    private Button employeesButton; // Reference to the Employees button
    @FXML
    private TextField yearInput;
    NotificationControllerNew notificationControllerNew = new NotificationControllerNew();
    @FXML
    private Text topOne, topTwo, topThree, topFour, topFive, notificationCount;
    @FXML
    private ImageView bellIcon;

    @FXML
    private AnchorPane notificationPane;

    private boolean isOwner = LoginController.getIsOwner(); // To determine if the user is an owner
    private int ownerId = SessionManager.getOwnerID();

    @FXML
    public void initialize() {
        if (notificationCount != null) {
            notificationCount.setText(String.valueOf(NotificationControllerNew.getNotificationCount()));
        }
        if (comboBox != null) {
            ObservableList<String> items = FXCollections.observableArrayList("Today", "This Week", "This Month", "This Year");
            comboBox.setItems(items);


            // Load default data
            comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadTopSoldProductsPieChart();
                }
            });

            // Set default selection
            comboBox.getSelectionModel().select("Today");
            loadTopSoldProductsPieChart();  // Load today's sales initially
        }

        // Set visibility of buttons based on ownership
        if (logButton != null) {
            logButton.setVisible(isOwner);
            employeesButton.setVisible(isOwner);
        }

        if (yearInput != null) {
            // Add event handler for yearInput to update the graph when Enter is pressed
            yearInput.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    loadMonthlyEarningsFromInput(); // Update the graph
                }
            });
        }

        if (yearInput != null) {
            // Load earnings for the current year or from the year input field
            loadMonthlyEarningsFromInput();
        }

        if (productsSoldLabel != null) {
            int totalProductsSold = getTotalProductsSoldToday();
            productsSoldLabel.setText(String.valueOf(totalProductsSold));
            loadNearingExpiryProducts();
            loadBelowMinimumQuantityProducts();
            loadTotalEarningsToday();
            loadExpiredProducts();



        }

        if (bellIcon != null && notificationPane != null) {
            notificationPane.setVisible(false);
            bellIcon.setOnMouseEntered(event -> notificationPane.setVisible(true));
            bellIcon.setOnMouseExited(event -> notificationPane.setVisible(false));
        }

        // Load charts and analysis data in a separate thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
//                loadTop5SoldProducts();

                return null;
            }@Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    if (comboBox != null) {
                        loadTopSoldProductsPieChart();
                    }
                });
            }
        };
        new Thread(task).start();
    }

    private void loadTopSoldProductsPieChart() {
        if (pieChart != null) {
            pieChart.getData().clear();
            pieChart.setLabelLineLength(5);
        }
        String selectedRange = Optional.ofNullable(comboBox.getSelectionModel().getSelectedItem()).orElse("Today"); // Set a default value if selectedRange is null
        String sqlQuery = "";

        int ownerId = SessionManager.getInstance().getOwnerID(); // Get the OwnerID as an integer

        switch (selectedRange) {
            case "Today":
                sqlQuery = "SELECT p.Category, SUM(o.Quantity) AS total_sold " +
                        "FROM Orders o " +
                        "JOIN Bill b ON o.BillID = b.BillID " +
                        "JOIN Product p ON o.SrNo = p.SrNo " +
                        "WHERE DATE(b.Time) = CURDATE() " +
                        "AND p.OwnerID = ? " + // Filter by OwnerID
                        "GROUP BY p.Category";
                break;
            case "This Week":
                sqlQuery = "SELECT p.Category, SUM(o.Quantity) AS total_sold " +
                        "FROM Orders o " +
                        "JOIN Bill b ON o.BillID = b.BillID " +
                        "JOIN Product p ON o.SrNo = p.SrNo " +
                        "WHERE WEEK(b.Time) = WEEK(CURDATE()) AND YEAR(b.Time) = YEAR(CURDATE()) " +
                        "AND p.OwnerID = ? " + // Filter by OwnerID
                        "GROUP BY p.Category";
                break;
            case "This Month":
                sqlQuery = "SELECT p.Category, SUM(o.Quantity) AS total_sold " +
                        "FROM Orders o " +
                        "JOIN Bill b ON o.BillID = b.BillID " +
                        "JOIN Product p ON o.SrNo = p.SrNo " +
                        "WHERE MONTH(b.Time) = MONTH(CURDATE()) AND YEAR(b.Time) = YEAR(CURDATE()) " +
                        "AND p.OwnerID = ? " + // Filter by OwnerID
                        "GROUP BY p.Category";
                break;
            case "This Year":
                sqlQuery = "SELECT p.Category, SUM(o.Quantity) AS total_sold " +
                        "FROM Orders o " +
                        "JOIN Bill b ON o.BillID = b.BillID " +
                        "JOIN Product p ON o.SrNo = p.SrNo " +
                        "WHERE YEAR(b.Time) = YEAR(CURDATE()) " +
                        "AND p.OwnerID = ? " + // Filter by OwnerID
                        "GROUP BY p.Category";
                break;
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setInt(1, ownerId); // Set the OwnerID parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("Category");
                    double totalSold = rs.getDouble("total_sold");

                    PieChart.Data data = new PieChart.Data(category, totalSold);
                    if (pieChart != null) {
                        pieChart.getData().add(data);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void notificationClick(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("17-notification-tab(new).fxml"));

            // Set a controller factory that can pass parameters to the constructor
            loader.setControllerFactory(controllerClass -> {
                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            });

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
    public void notification(javafx.scene.input.MouseEvent mouseEvent) {
        notificationPane.setVisible(true); // Directly access notificationPane
    }

    @FXML
    public void notificationDragExited(javafx.scene.input.MouseEvent mouseEvent) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1000));
        pause.setOnFinished(event -> notificationPane.setVisible(false));
        pause.play();
    }

    private void loadExpiredProducts() {
        String sql = "SELECT COUNT(*) AS ExpiredProductsCount " +
                "FROM Product " +
                "WHERE ExpDate <= CURDATE() " +
                "AND OwnerID = ?"; // Assuming OwnerID is used to filter products for the current owner

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, SessionManager.getInstance().getOwnerID());  // Replace with appropriate OwnerID logic
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int ExpiredProductsCount = rs.getInt("ExpiredProductsCount");
                // Update the label with the count of products nearing expiry
                expiredproductsLabel.setText(String.valueOf(ExpiredProductsCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadNearingExpiryProducts() {
        String sql = "SELECT COUNT(*) AS nearingExpiryCount " +
                "FROM Product " +
                "WHERE ExpDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) " +
                "AND OwnerID = ?"; // Assuming OwnerID is used to filter products for the current owner

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, SessionManager.getInstance().getOwnerID());  // Replace with appropriate OwnerID logic
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int nearingExpiryCount = rs.getInt("nearingExpiryCount");
                // Update the label with the count of products nearing expiry
                nearingexpirydateLabel.setText(String.valueOf(nearingExpiryCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBelowMinimumQuantityProducts() {
        String sql = "SELECT COUNT(*) AS belowMinimumQuantityCount " +
                "FROM Product " +
                "WHERE Quantity <= LowQuantityAlert " +
                "AND OwnerID = ?"; // Assuming OwnerID is used to filter products for the current owner

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Retrieve OwnerID from SessionManager
            Integer ownerId = SessionManager.getInstance().getOwnerID();
            pstmt.setInt(1, ownerId); // Set the OwnerID parameter as a String

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int belowMinimumQuantityCount = rs.getInt("belowMinimumQuantityCount");
                // Update the label with the count of products nearing minimum quantity
                belowlowquantityLabel.setText(String.valueOf(belowMinimumQuantityCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTotalEarningsToday() {
        double totalEarnings = getTotalEarningsToday();
        earningsLabel.setText(String.format("%.2f", totalEarnings)); // Format to two decimal places
    }

    private double getTotalEarningsToday() {
        double totalEarnings = 0.0;

        // SQL query to get total earnings for today, filtered by OwnerID
        String sql = "SELECT SUM(Amount) AS total_amount " +
                "FROM Bill " +
                "WHERE DATE(Time) = CURDATE() " +
                "AND OwnerID = ?"; // Filter by OwnerID

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Integer ownerId = SessionManager.getInstance().getOwnerID(); // Retrieve OwnerID from SessionManager
            pstmt.setInt(1, ownerId); // Set the OwnerID parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalEarnings = rs.getDouble("total_amount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalEarnings; // Return the total earnings
    }

    private int getTotalProductsSoldToday() {
        int totalProductsSold = 0;

        String sql = "SELECT SUM(o.Quantity) AS total_quantity " +
                "FROM Orders o " +
                "JOIN Bill b ON o.BillID = b.BillID " +
                "WHERE DATE(b.Time) = CURDATE() " +
                "AND b.OwnerID = ?"; // Filter by OwnerID

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Integer ownerId = SessionManager.getInstance().getOwnerID(); // Retrieve OwnerID from SessionManager
            pstmt.setInt(1, ownerId); // Set the OwnerID parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalProductsSold = rs.getInt("total_quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalProductsSold;
    }

    /*
    private void loadTop5SoldProducts() {
        String selectedRange = comboBox.getSelectionModel().getSelectedItem();
        String sqlQuery = "";

        switch (selectedRange) {
            case "Today":
                sqlQuery = getTodaySalesQuery();
                break;
            case "This Week":
                sqlQuery = getThisWeekSalesQuery();
                break;
            case "This Month":
                sqlQuery = getThisMonthSalesQuery();
                break;
            case "This Year":
                sqlQuery = getThisYearSalesQuery();
                break;
        }

        try {
            Connection conn = getConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sqlQuery);

            // List to store product names and their total sold quantities
            ObservableList<String> topProducts = FXCollections.observableArrayList();
            int counter = 0;
            while (rs.next() && topProducts.size() < 5) {
                counter++;
                String productName = rs.getString("productName");
                int totalSold = rs.getInt("total_sold");

                topProducts.add(counter + ".  " + productName + " (Sold: " + totalSold + ")");
            }

            // Set the labels with product names or blank if there are fewer than 5 products
            setProductLabels(topProducts);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setProductLabels(ObservableList<String> topProducts) {
        // Ensure the labels are updated with top product names, or blank if not available
        topOne.setText(topProducts.size() > 0 ? topProducts.get(0) : "");
        topTwo.setText(topProducts.size() > 1 ? topProducts.get(1) : "");
        topThree.setText(topProducts.size() > 2 ? topProducts.get(2) : "");
        topFour.setText(topProducts.size() > 3 ? topProducts.get(3) : "");
        topFive.setText(topProducts.size() > 4 ? topProducts.get(4) : "");
    }
    private String getTodaySalesQuery() {
        return "SELECT o.ProductName, SUM(o.Quantity) AS total_sold " +
                "FROM Orders o " +
                "JOIN Bill b ON o.BillID = b.BillID " +
                "WHERE DATE(b.Time) = CURDATE() " +
                "GROUP BY o.ProductName " +
                "ORDER BY total_sold DESC LIMIT 5;";
    }

    private String getThisWeekSalesQuery() {
        return "SELECT o.ProductName, SUM(o.Quantity) AS total_sold " +
                "FROM Orders o " +
                "JOIN Bill b ON o.BillID = b.BillID " +
                "WHERE WEEK(b.Time) = WEEK(CURDATE()) AND YEAR(b.Time) = YEAR(CURDATE()) " +
                "GROUP BY o.ProductName " +
                "ORDER BY total_sold DESC LIMIT 5;";
    }

    private String getThisMonthSalesQuery() {
        return "SELECT o.ProductName, SUM(o.Quantity) AS total_sold " +
                "FROM Orders o " +
                "JOIN Bill b ON o.BillID = b.BillID " +
                "WHERE MONTH(b.Time) = MONTH(CURDATE()) AND YEAR(b.Time) = YEAR(CURDATE()) " +
                "GROUP BY o.ProductName " +
                "ORDER BY total_sold DESC LIMIT 5;";
    }

    private String getThisYearSalesQuery() {
        return "SELECT o.ProductName, SUM(o.Quantity) AS total_sold " +
                "FROM Orders o " +
                "JOIN Bill b ON o.BillID = b.BillID " +
                "WHERE YEAR(b.Time) = YEAR(CURDATE()) " +
                "GROUP BY o.ProductName " +
                "ORDER BY total_sold DESC LIMIT 5;";
    }
*/
    // Load monthly earnings based on input or default to the current year
    private void loadMonthlyEarningsFromInput() {
        try {
            String yearText = yearInput.getText().trim();
            int year = yearText.isEmpty() ? java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    : Integer.parseInt(yearText);
            loadMonthlyEarnings(year);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year input: " + yearInput.getText());
            yearInput.setText(""); // Clear the input field for correction
        }
    }

    private void loadMonthlyEarnings(int year) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Earnings for " + year + " (in Rupees)");

        // SQL query to get monthly earnings for the specified year
        String sql = "SELECT MONTH(Time) AS month, SUM(Amount) AS total_amount " +
                "FROM Bill " +
                "WHERE YEAR(Time) = ? and OwnerID=? " +
                "GROUP BY MONTH(Time) " +
                "ORDER BY MONTH(Time)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the year parameter in the SQL query
            pstmt.setInt(1, year);
            pstmt.setInt(2, SessionManager.getInstance().getOwnerID());

            try (ResultSet rs = pstmt.executeQuery()) {
                // Initialize an array to hold total amounts for each month
                double[] monthlyEarnings = new double[12];

                while (rs.next()) {
                    int month = rs.getInt("month");
                    double totalAmount = rs.getDouble("total_amount");

                    // Store total amount for the corresponding month (0-indexed for array)
                    monthlyEarnings[month - 1] = totalAmount;
                }

                // Add data to the series for each month (January to December)
                for (int i = 0; i < monthlyEarnings.length; i++) {
                    series.getData().add(new XYChart.Data<>(getMonthName(i + 1), monthlyEarnings[i]));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add the series to the BarChart
        if (barChart != null) {
            barChart.getData().add(series);
        }
    }

    // Method to get month name from month number
    private String getMonthName(int month) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return monthNames[month - 1]; // Adjusting for 0-index
    }

    // Initialize Dashboard method where notification system is started
    @FXML
    public void initializeDashboard() {
        int ownerid = SessionManager.getInstance().getOwnerID();
    }

    @FXML
    private void profileButton(ActionEvent event) {
        loadScene(event, "9-profile.fxml");
    }

    @FXML
    private void inventoryButton(ActionEvent event) {
        loadScene(event, "10-inventory.fxml");
    }

    @FXML
    private void billButton(ActionEvent event) {
        loadScene(event, "11-bill.fxml");
    }

    @FXML
    private void logButton(ActionEvent event) {
        loadScene(event, "13-log.fxml");
    }

    @FXML
    private void employeesButton(ActionEvent event) {
        loadScene(event, "12a-employees.fxml");
    }

    @FXML
    private void changepassButton(ActionEvent event) {
        loadScene(event, "14-change-password.fxml");
    }

    @FXML
    private void confirmLogout(ActionEvent event) {
        String currentUser = SessionManager.getUsername();
        logActivity(currentUser, "Logged out");
        loadScene(event, "16-logout-confirmation.fxml");
    }

    @FXML
    private void LoginPage(ActionEvent event) {
        loadScene(event, "1-login-page.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // If loading the dashboard, also initialize BarChart data and notifications
            if (fxmlFileName.equals("8-dashboard.fxml")) {
                Controller controller = loader.getController();
                controller.initializeDashboard();  // Initialize the dashboard and notifications
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logActivity(String username, String activity) {
        String sql = "INSERT INTO logs (date, time, User, activity, OwnerID) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
            pstmt.setTime(2, java.sql.Time.valueOf(java.time.LocalTime.now()));
            pstmt.setString(3, username);
            pstmt.setString(4, activity);
            pstmt.setInt(5, SessionManager.getInstance().getOwnerID());  // OwnerID from SessionManager

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void LogInButton(ActionEvent event) {
        loadScene(event, "8-dashboard.fxml");
    }
}