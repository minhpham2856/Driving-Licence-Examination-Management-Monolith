package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamDeviceDAO;
import Models.ExamDevice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceDAOImpl extends DBContext implements ExamDeviceDAO {

    @Override
    public List<ExamDevice> getAvailableDevices(String typeFilter) {
        List<ExamDevice> list = new ArrayList<>();
        String sql = "select * from ExamDevice where status = 'Operational'";
        if (typeFilter != null && !typeFilter.isEmpty()) {
            sql += " and deviceType like ?";
        }
        sql += " order by deviceName";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (typeFilter != null && !typeFilter.isEmpty()) {
                ps.setString(1, "%" + typeFilter + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamDevice ed = new ExamDevice();
                    ed.setId(rs.getInt("id"));
                    ed.setAreaId(rs.getInt("areaId"));
                    ed.setDeviceType(rs.getString("deviceType"));
                    ed.setDeviceName(rs.getString("deviceName"));
                    ed.setStatus(rs.getString("status"));
                    list.add(ed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lọc thiết bị theo hạng bằng:
     * - category "motorbike" → deviceType chứa "gắn máy" (A1, A2)
     * - category "car"       → deviceType chứa "ô tô"     (B1, B2, C, ...)
     */
    @Override
    public List<ExamDevice> getAvailableDevicesByCategory(String category) {
        String keyword = "motorbike".equalsIgnoreCase(category) ? "gắn máy" : "ô tô";
        return getAvailableDevices(keyword);
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "update ExamDevice set status = ? where id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
