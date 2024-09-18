package com.example.billnbox;

public class SessionManager {
    private static SessionManager instance;
    private String username;
    private String shopname;
    private String shopaddress;

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

    public void setShopname(String username) {
        this.shopname = shopname;
    }

    public String getShopname() {
        return shopname;
    }

    public void setShopaddress(String username) {
        this.shopaddress = shopaddress;
    }

    public String getShopaddress() {
        return shopaddress;
    }
}
