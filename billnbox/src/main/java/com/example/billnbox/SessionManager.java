package com.example.billnbox;

public class SessionManager {
    private static SessionManager instance;
    private String username;

    // Private constructor to restrict instantiation
    private SessionManager() { }

    // Get the single instance of the class
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Setters and getters
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
