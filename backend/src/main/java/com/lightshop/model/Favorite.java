package com.lightshop.model;

import java.sql.Timestamp;

public class Favorite {
    private int id;
    private int userId;
    private int productId;
    private Timestamp createdAt;

    // Associated product info
    private String productName;
    private double productPrice;
    private String productImage;

    public Favorite() {}

    public Favorite(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
}

