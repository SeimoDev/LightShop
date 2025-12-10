package com.lightshop.model;

import java.sql.Timestamp;

public class Banner {
    private int id;
    private String title;
    private String image;
    private String link;
    private int sortOrder;
    private int status; // 0: disabled, 1: active
    private Timestamp createdAt;

    public Banner() {
        this.status = 1;
    }

    public Banner(String title, String image, String link, int sortOrder) {
        this.title = title;
        this.image = image;
        this.link = link;
        this.sortOrder = sortOrder;
        this.status = 1;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

