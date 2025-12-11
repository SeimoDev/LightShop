package com.lightshop.dao;

import com.lightshop.model.Settings;
import com.lightshop.util.DatabaseUtil;

import java.sql.*;

public class SettingsDao {

    public Settings get() {
        String sql = "SELECT * FROM settings WHERE id = 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Return default settings
        return new Settings();
    }

    public boolean update(Settings settings) {
        String sql = "INSERT INTO settings (id, site_name, logo, description, keywords, contact_phone, contact_email, address, copyright) " +
            "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT(id) DO UPDATE SET " +
            "site_name = excluded.site_name, logo = excluded.logo, description = excluded.description, " +
            "keywords = excluded.keywords, contact_phone = excluded.contact_phone, contact_email = excluded.contact_email, " +
            "address = excluded.address, copyright = excluded.copyright";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, settings.getSiteName());
            stmt.setString(2, settings.getLogo());
            stmt.setString(3, settings.getDescription());
            stmt.setString(4, settings.getKeywords());
            stmt.setString(5, settings.getContactPhone());
            stmt.setString(6, settings.getContactEmail());
            stmt.setString(7, settings.getAddress());
            stmt.setString(8, settings.getCopyright());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Settings mapResultSet(ResultSet rs) throws SQLException {
        Settings settings = new Settings();
        settings.setId(rs.getInt("id"));
        settings.setSiteName(rs.getString("site_name"));
        settings.setLogo(rs.getString("logo"));
        settings.setDescription(rs.getString("description"));
        settings.setKeywords(rs.getString("keywords"));
        settings.setContactPhone(rs.getString("contact_phone"));
        settings.setContactEmail(rs.getString("contact_email"));
        settings.setAddress(rs.getString("address"));
        settings.setCopyright(rs.getString("copyright"));
        return settings;
    }
}

