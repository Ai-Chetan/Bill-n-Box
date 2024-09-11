package com.example.billnbox;

import java.util.Date;

public class InventoryTable {
    private int Sr_No;
    private String Product_Name;
    private String Category;
    private int Quantity;
    private double Price;
    private Date Mfg_Date;
    private Date Exp_Date;
    private int Low_Quantity_Alert;

    public InventoryTable(int srNo, String productName, String category, int quantity, double price, Date mfgDate, Date expDate, int lowQuantityAlert) {
        this.Sr_No = srNo;
        this.Product_Name = productName;
        this.Category = category;
        this.Quantity = quantity;
        this.Price = price;
        this.Mfg_Date = mfgDate;
        this.Exp_Date = expDate;
        this.Low_Quantity_Alert = lowQuantityAlert;
    }

    // Getters and setters for each property
    public int getSr_No() { return Sr_No; }
    public void setSr_No(int srNo) { this.Sr_No = srNo; }

    public String getProduct_Name() { return Product_Name; }
    public void setProduct_Name(String productName) { this.Product_Name = productName; }

    public String getCategory() { return Category; }
    public void setCategory(String category) { this.Category = category; }

    public int getQuantity() { return Quantity; }
    public void setQuantity(int quantity) { this.Quantity = quantity; }

    public double getPrice() { return Price; }
    public void setPrice(double price) { this.Price = price; }

    public Date getMfg_Date() { return Mfg_Date; }
    public void setMfg_Date(Date mfgDate) { this.Mfg_Date = mfgDate; }

    public Date getExp_Date() { return Exp_Date; }
    public void setExp_Date(Date expDate) { this.Exp_Date = expDate; }

    public int getLow_Quantity_Alert() { return Low_Quantity_Alert; }
    public void setLow_Quantity_Alert(int lowQuantityAlert) { this.Low_Quantity_Alert = lowQuantityAlert; }
}
