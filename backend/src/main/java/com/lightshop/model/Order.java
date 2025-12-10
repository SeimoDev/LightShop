package com.lightshop.model;

import java.sql.Timestamp;
import java.util.List;

public class Order {
    public static final int STATUS_PENDING_PAYMENT = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_SHIPPED = 2;
    public static final int STATUS_DELIVERED = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELLED = 5;
    public static final int STATUS_REFUNDING = 6;
    public static final int STATUS_REFUNDED = 7;

    private int id;
    private String orderNo;
    private int userId;
    private double totalAmount;
    private double shippingFee;
    private int status;
    private int addressId;
    private String addressSnapshot; // JSON snapshot of address at order time
    private String remark;
    private Timestamp createdAt;
    private Timestamp paidAt;
    private Timestamp shippedAt;
    private Timestamp completedAt;

    // Associated data
    private List<OrderItem> items;
    private String username;

    public Order() {
        this.status = STATUS_PENDING_PAYMENT;
        this.shippingFee = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public String getAddressSnapshot() { return addressSnapshot; }
    public void setAddressSnapshot(String addressSnapshot) { this.addressSnapshot = addressSnapshot; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }

    public Timestamp getShippedAt() { return shippedAt; }
    public void setShippedAt(Timestamp shippedAt) { this.shippedAt = shippedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatusText() {
        switch (status) {
            case STATUS_PENDING_PAYMENT: return "待付款";
            case STATUS_PAID: return "待发货";
            case STATUS_SHIPPED: return "已发货";
            case STATUS_DELIVERED: return "已送达";
            case STATUS_COMPLETED: return "已完成";
            case STATUS_CANCELLED: return "已取消";
            case STATUS_REFUNDING: return "退款中";
            case STATUS_REFUNDED: return "已退款";
            default: return "未知状态";
        }
    }
}

