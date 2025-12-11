package com.lightshop.dao;

import com.lightshop.model.Favorite;
import com.lightshop.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    public List<Favorite> findByUserId(int userId, int page, int pageSize) {
        List<Favorite> favorites = new ArrayList<>();
        String sql = "SELECT f.*, p.name as product_name, p.price as product_price, p.images as product_image " +
            "FROM favorites f JOIN products p ON f.product_id = p.id " +
            "WHERE f.user_id = ? AND p.status = 1 ORDER BY f.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favorites.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favorites;
    }

    public boolean exists(int userId, int productId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int create(Favorite favorite) {
        String sql = "INSERT INTO favorites (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, favorite.getUserId());
            stmt.setInt(2, favorite.getProductId());
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

    public boolean delete(int userId, int productId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM favorites f JOIN products p ON f.product_id = p.id WHERE f.user_id = ? AND p.status = 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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

    private Favorite mapResultSet(ResultSet rs) throws SQLException {
        Favorite favorite = new Favorite();
        favorite.setId(rs.getInt("id"));
        favorite.setUserId(rs.getInt("user_id"));
        favorite.setProductId(rs.getInt("product_id"));
        favorite.setCreatedAt(rs.getTimestamp("created_at"));
        favorite.setProductName(rs.getString("product_name"));
        favorite.setProductPrice(rs.getDouble("product_price"));
        String images = rs.getString("product_image");
        if (images != null && !images.isEmpty() && images.startsWith("[")) {
            images = images.replace("[", "").replace("]", "").replace("\"", "").split(",")[0];
        }
        favorite.setProductImage(images);
        return favorite;
    }
}

