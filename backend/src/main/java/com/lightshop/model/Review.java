package com.lightshop.model;

import java.sql.Timestamp;

public class Review {
    private int id;
    private int orderId;
    private int orderItemId;
    private int userId;
    private int productId;
    private int rating; // 1-5
    private String content;
    private String images; // JSON array of image URLs
    private Timestamp createdAt;

    // Associated info
    private String username;
    private String userAvatar;
    private String productName;

    public Review() {}

    public Review(int orderId, int orderItemId, int userId, int productId, int rating, String content) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.content = content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}

