package com.lightshop.model;

import java.sql.Timestamp;

public class Category {
    private int id;
    private String name;
    private String icon;
    private int parentId;
    private int sortOrder;
    private int status; // 0: disabled, 1: active
    private Timestamp createdAt;

    public Category() {}

    public Category(String name, String icon, int parentId, int sortOrder) {
        this.name = name;
        this.icon = icon;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.status = 1;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

