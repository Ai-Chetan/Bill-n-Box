package com.example.billnbox;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/BillNBoxDB";
    private static final String USER = "root";
    private static final String PASSWORD = "aarya16";

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
