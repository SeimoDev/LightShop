package com.lightshop.dao;

import com.lightshop.model.Banner;
import com.lightshop.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BannerDao {

    public Banner findById(int id) {
        String sql = "SELECT * FROM banners WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
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

    public List<Banner> findActive() {
        List<Banner> banners = new ArrayList<>();
        String sql = "SELECT * FROM banners WHERE status = 1 ORDER BY sort_order ASC, id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                banners.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banners;
    }

    public List<Banner> findAll() {
        List<Banner> banners = new ArrayList<>();
        String sql = "SELECT * FROM banners ORDER BY sort_order ASC, id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                banners.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banners;
    }

    public int create(Banner banner) {
        String sql = "INSERT INTO banners (title, image, link, sort_order, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, banner.getTitle());
            stmt.setString(2, banner.getImage());
            stmt.setString(3, banner.getLink());
            stmt.setInt(4, banner.getSortOrder());
            stmt.setInt(5, banner.getStatus());
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

    public boolean update(Banner banner) {
        String sql = "UPDATE banners SET title = ?, image = ?, link = ?, sort_order = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, banner.getTitle());
            stmt.setString(2, banner.getImage());
            stmt.setString(3, banner.getLink());
            stmt.setInt(4, banner.getSortOrder());
            stmt.setInt(5, banner.getStatus());
            stmt.setInt(6, banner.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM banners WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Banner mapResultSet(ResultSet rs) throws SQLException {
        Banner banner = new Banner();
        banner.setId(rs.getInt("id"));
        banner.setTitle(rs.getString("title"));
        banner.setImage(rs.getString("image"));
        banner.setLink(rs.getString("link"));
        banner.setSortOrder(rs.getInt("sort_order"));
        banner.setStatus(rs.getInt("status"));
        banner.setCreatedAt(rs.getTimestamp("created_at"));
        return banner;
    }
}

