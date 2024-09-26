package com.example.billnbox;

public class SessionManager {
    private static SessionManager instance;
    private static String username;
    private String shopname;
    private String shopaddress;
    private static int ownerID;

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

    public static String getUsername() {
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

    // Setters and getters for OwnerID
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public static int getOwnerID() {
        return ownerID;
    }

}
