package com.example.billnbox;

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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogController {

    @FXML
    private TableView<LogEntry> LogEntry;

    @FXML
    private TableColumn<LogEntry, String> dateColumn;

    @FXML
    private TableColumn<LogEntry, String> timeColumn;

    @FXML
    private TableColumn<LogEntry, String> userColumn;

    @FXML
    private TableColumn<LogEntry, String> activityColumn;

    public void initialize() {
        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));

        // Load data from the database
        if(LogEntry!=null) {
            loadLogData();

        }
    }

    // Method to load logs from the database
    private void loadLogData() {
        ObservableList<LogEntry> logData = FXCollections.observableArrayList();


        try (Connection conn = DatabaseConfig.getConnection()) {
            // Fetch the OwnerID from the session
            int ownerID = SessionManager.getOwnerID();

            // Query to fetch logs based on OwnerID
            String sql = "SELECT date, time, User, activity FROM logs WHERE OwnerID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ownerID); // Set the OwnerID for filtering logs
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String date = rs.getString("date");
                String time = rs.getString("time");
                String userId = rs.getString("User");
                String activity = rs.getString("activity");

                // Add log entry to the list
                logData.add(new LogEntry(date, time, userId, activity));
            }

            // Set the data into the TableView
            LogEntry.setItems(logData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // LogEntry class to represent a single log entry
    public static class LogEntry {
        private final String date;
        private final String time;
        private final String userId;
        private final String activity;

        public LogEntry(String date, String time, String userId, String activity) {
            this.date = date;
            this.time = time;
            this.userId = userId;
            this.activity = activity;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getUser() {
            return userId;
        }

        public String getActivity() {
            return activity;
        }
    }

    // Navigate to a specific page
    private void navigateToPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/billnbox/" + "8-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Action method to go back to the dashboard
    @FXML
    private void LogInButton(ActionEvent event) {
        navigateToPage(event);
    }
}
