package com.example.billnbox;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3307/BillNBoxDB";
    private static final String USER = "root";
    private static final String PASSWORD = "root!123";

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
