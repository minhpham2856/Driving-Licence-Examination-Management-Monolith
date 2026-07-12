package examstaff.dao.impl;

import examstaff.dbconnection.DBContext;
import examstaff.dao.ExamAreaDAO;
import examstaff.enums.ExamSection;
import examstaff.model.ExamArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamAreaDAOImpl implements ExamAreaDAO {
    private ExamArea map(ResultSet rs) throws SQLException {
        ExamArea a = new ExamArea();
        a.setExamAreaId(rs.getInt("ExamAreaId"));
        a.setAreaName(rs.getString("AreaName"));
        a.setAreaType(rs.getString("AreaType"));
        int capacity = rs.getInt("Capacity");
        a.setCapacity(rs.wasNull() ? null : capacity);
        a.setLocation(rs.getString("Location"));
        try {
            a.setExamZoneId(rs.getInt("ExamZoneId"));
        } catch (SQLException ignored) {
            // cột có thể thiếu trên schema cũ
        }
        return a;
    }

    @Override
    public ExamArea getById(int examAreaId) {
        String sql = "SELECT * FROM ExamArea WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        return getAvailableAreasByType(ExamSection.LY_THUYET.getDisplayName());
    }

    @Override
    public List<ExamArea> getAvailableAreasByType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return List.of();
        }
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, areaType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamArea> getAreasByExamId(int examId) {
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT ea.* FROM ExamArea ea "
                   + "JOIN Exam_ExamArea exa ON ea.ExamAreaId = exa.ExamAreaId "
                   + "WHERE exa.ExamId = ? ORDER BY ea.AreaName";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
