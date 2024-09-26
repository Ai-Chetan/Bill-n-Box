package com.example.billnbox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Controller {

    private final String username = SessionManager.getInstance().getUsername();
    private final String shopname = SessionManager.getInstance().getShopname();
    private final String shopaddress = SessionManager.getInstance().getShopaddress();

    @FXML
    private ComboBox<String> comboBox;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private Button logButton; // Reference to the Log button
    @FXML
    private Button employeesButton; // Reference to the Employees button
    @FXML
    private TextField yearInput;

    // Reference to NotificationController
    private NotificationController notificationController;

    private boolean isOwner = LoginController.getIsOwner(); // To determine if the user is an owner

    @FXML
    public void initialize() {
        if (comboBox != null) {
            ObservableList<String> items = FXCollections.observableArrayList("Today", "This Week", "This Month", "This Year", "All Time");
            comboBox.setItems(items);
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
    }

    private void loadMonthlyEarningsFromInput() {
        try {
            String yearText = yearInput.getText().trim(); // Get trimmed input
            int year;

            // Check if input is empty and set year to current year if it is
            if (yearText.isEmpty()) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            } else {
                year = Integer.parseInt(yearText); // Parse year from input
            }

            loadMonthlyEarnings(year); // Load earnings for the year
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid number
            System.out.println("Invalid year input: " + yearInput.getText());
            yearInput.setText(""); // Clear the input field for correction
        }
    }

    private void loadMonthlyEarnings(int year) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Earnings for " + year);

        // SQL query to get monthly earnings for the specified year
        String sql = "SELECT MONTH(Time) AS month, SUM(Amount) AS total_amount " +
                "FROM Bill " +
                "WHERE YEAR(Time) = ? " +
                "GROUP BY MONTH(Time) " +
                "ORDER BY MONTH(Time)";

        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the year parameter in the SQL query
            pstmt.setInt(1, year);

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
        notificationController = new NotificationController();
        notificationController.StartNotification();  // Start notifications on dashboard load
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
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
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