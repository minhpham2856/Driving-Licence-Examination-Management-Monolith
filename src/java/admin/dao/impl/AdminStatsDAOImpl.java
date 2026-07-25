package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.AdminStatsDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class AdminStatsDAOImpl extends DBContext implements AdminStatsDAO {

    // whitelist tên bảng để tránh SQL injection qua tên bảng
    private static final List<String> ALLOWED = Arrays.asList(
            "ExamZone", "ExamArea", "ExamDevice", "Licence", "Fee", "Licence_Fee", "Audit", "Exam");

    @Override
    public int count(String table) {
        if (!ALLOWED.contains(table)) return 0;
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM [" + table + "]");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int countActiveAccounts() {
        // Bảng [User] dùng cột IsActive, không có cột Status
        String sql = "SELECT COUNT(*) FROM [User] WHERE IsActive = 1";
        try (PreparedStatement st = getConnection().prepareStatement(sql);
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
