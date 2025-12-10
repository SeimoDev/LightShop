package com.lightshop.model;

import java.sql.Timestamp;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private double originalPrice;
    private int stock;
    private String images; // JSON array of image URLs
    private int categoryId;
    private String categoryName;
    private int sales;
    private int status; // 0: off shelf, 1: on shelf
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Product() {}

    public Product(String name, String description, double price, int stock, int categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.originalPrice = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.sales = 0;
        this.status = 1;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getSales() { return sales; }
    public void setSales(int sales) { this.sales = sales; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

