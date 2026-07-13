package examstaff.dao.impl;

import java.sql.*;
import shared.dbconnection.DBContext;
import examstaff.dao.ExamAreaDAO;
import examstaff.enums.SectionType;
import shared.model.ExamArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            // cá»™t cÃ³ thá»ƒ thiáº¿u trÃªn schema cÅ©
        }
        return a;
    }

    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ExamArea WHERE 1=1");
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasType = areaType != null && !areaType.isBlank();
        if (hasKw)   sql.append(" AND (AreaName LIKE ? OR Location LIKE ? OR AreaType LIKE ?)");
        if (hasType) sql.append(" AND AreaType = ?");
        sql.append(" ORDER BY ExamAreaId DESC");
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasType) ps.setString(i++, areaType.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
    public int insert(ExamArea a) {
        String sql = "INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            setIntOrNull(ps, 3, a.getCapacity());
            ps.setString(4, a.getLocation());
            int zoneId = a.getExamZoneId() > 0 ? a.getExamZoneId() : 1;
            ps.setInt(5, zoneId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean update(ExamArea a) {
        String sql = "UPDATE ExamArea SET AreaName = ?, AreaType = ?, Capacity = ?, [Location] = ?, ExamZoneId = ? WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            setIntOrNull(ps, 3, a.getCapacity());
            ps.setString(4, a.getLocation());
            int zoneId = a.getExamZoneId() > 0 ? a.getExamZoneId() : 1;
            ps.setInt(5, zoneId);
            ps.setInt(6, a.getExamAreaId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int examAreaId) {
        String sql = "DELETE FROM ExamArea WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ExamArea";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        return getAvailableAreasByType(examstaff.enums.SectionType.THEORY.getValue());
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
    public List<ExamArea> getAreasBySessionId(int sessionId) {
        return getAreasByExamId(sessionId);
    }

    @Override
    public boolean isAreaInSession(int sessionId, int examAreaId) {
        return isAreaInExam(sessionId, examAreaId);
    }

    // --- mainTest methods (alias implementations) ---

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
            // fallback: query qua ExaminerSchedule
            try (Connection c = new DBContext().getConnection();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT DISTINCT ea.* FROM ExamArea ea "
                   + "JOIN ExaminerSchedule es ON ea.ExamAreaId = es.ExamAreaId "
                   + "WHERE es.ExamId = ? ORDER BY ea.AreaName")) {
                ps.setInt(1, examId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public boolean isAreaInExam(int examId, int examAreaId) {
        String sql = "SELECT COUNT(*) FROM Exam_ExamArea WHERE ExamId = ? AND ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, val);
    }
}

