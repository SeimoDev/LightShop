package com.lightshop.dao;

import com.lightshop.model.Order;
import com.lightshop.model.OrderItem;
import com.lightshop.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private final OrderItemDao orderItemDao = new OrderItemDao();

    public Order findById(int id) {
        String sql = "SELECT o.*, u.username FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSet(rs);
                    order.setItems(orderItemDao.findByOrderId(id));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Order findByOrderNo(String orderNo) {
        String sql = "SELECT o.*, u.username FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.order_no = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSet(rs);
                    order.setItems(orderItemDao.findByOrderId(order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> findByUserId(int userId, Integer status, int page, int pageSize) {
        List<Order> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, u.username FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE o.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSet(rs);
                    order.setItems(orderItemDao.findByOrderId(order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findAll(Integer status, String keyword, int page, int pageSize) {
        List<Order> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, u.username FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (o.order_no LIKE ? OR u.username LIKE ?)");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSet(rs);
                    order.setItems(orderItemDao.findByOrderId(order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int countByUserId(int userId, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAll(Integer status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (o.order_no LIKE ? OR u.username LIKE ?)");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double sumTotalAmount() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status >= 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int create(Order order, List<OrderItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Create order
            String sql = "INSERT INTO orders (order_no, user_id, total_amount, shipping_fee, status, address_id, address_snapshot, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, order.getOrderNo());
            stmt.setInt(2, order.getUserId());
            stmt.setDouble(3, order.getTotalAmount());
            stmt.setDouble(4, order.getShippingFee());
            stmt.setInt(5, order.getStatus());
            stmt.setInt(6, order.getAddressId());
            stmt.setString(7, order.getAddressSnapshot());
            stmt.setString(8, order.getRemark());
            stmt.executeUpdate();

            int orderId;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    orderId = rs.getInt(1);
                } else {
                    conn.rollback();
                    return -1;
                }
            }
            stmt.close();

            // Create order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, product_image, price, quantity) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            for (OrderItem item : items) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setString(3, item.getProductName());
                itemStmt.setString(4, item.getProductImage());
                itemStmt.setDouble(5, item.getPrice());
                itemStmt.setInt(6, item.getQuantity());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();
            itemStmt.close();

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updateStatus(int id, int status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePaidAt(int id) {
        String sql = "UPDATE orders SET status = 1, paid_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateShippedAt(int id) {
        String sql = "UPDATE orders SET status = 2, shipped_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCompletedAt(int id) {
        String sql = "UPDATE orders SET status = 4, completed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Order mapResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setUserId(rs.getInt("user_id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setShippingFee(rs.getDouble("shipping_fee"));
        order.setStatus(rs.getInt("status"));
        order.setAddressId(rs.getInt("address_id"));
        order.setAddressSnapshot(rs.getString("address_snapshot"));
        order.setRemark(rs.getString("remark"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setPaidAt(rs.getTimestamp("paid_at"));
        order.setShippedAt(rs.getTimestamp("shipped_at"));
        order.setCompletedAt(rs.getTimestamp("completed_at"));
        try {
            order.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // Column doesn't exist
        }
        return order;
    }
}

