package com.example.billnbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:mysql://mysql-service-for-billnbox-bill-n-box.i.aivencloud.com:11492/defaultdb?sslMode=REQUIRED";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASSWORD = "AVNS_1k_4ghIDpnv5PydV72W";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Error :", e);
        }
    }
}