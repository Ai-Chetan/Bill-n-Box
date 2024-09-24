package com.example.billnbox;

public class DatabaseConfig {
    // Add Credentials here!
    private static final String URL = "jdbc:mysql://mysql-service-for-billnbox-bill-n-box.i.aivencloud.com:11492/defaultdb?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "";

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
