package com.lightshop.dao;

import com.lightshop.model.Review;
import com.lightshop.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public List<Review> findByProductId(int productId, int page, int pageSize) {
        List<Review> reviews = new ArrayList<>();
        String sql = """
            SELECT r.*, u.username, u.avatar as user_avatar
            FROM reviews r 
            JOIN users u ON r.user_id = u.id 
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
            LIMIT ? OFFSET ?
        """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public Review findByOrderItemId(int orderItemId) {
        String sql = "SELECT r.*, u.username, u.avatar as user_avatar FROM reviews r JOIN users u ON r.user_id = u.id WHERE r.order_item_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int create(Review review) {
        String sql = "INSERT INTO reviews (order_id, order_item_id, user_id, product_id, rating, content, images) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, review.getOrderId());
            stmt.setInt(2, review.getOrderItemId());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getProductId());
            stmt.setInt(5, review.getRating());
            stmt.setString(6, review.getContent());
            stmt.setString(7, review.getImages());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int countByProductId(int productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
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

    public double getAverageRating(int productId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Review mapResultSet(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setOrderId(rs.getInt("order_id"));
        review.setOrderItemId(rs.getInt("order_item_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setProductId(rs.getInt("product_id"));
        review.setRating(rs.getInt("rating"));
        review.setContent(rs.getString("content"));
        review.setImages(rs.getString("images"));
        review.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            review.setUsername(rs.getString("username"));
            review.setUserAvatar(rs.getString("user_avatar"));
        } catch (SQLException e) {
            // Column doesn't exist
        }
        return review;
    }
}

