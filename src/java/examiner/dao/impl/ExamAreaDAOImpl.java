package examiner.dao.impl;

import java.sql.*;
import shared.dbconnection.DBContext;
import examiner.dao.ExamAreaDAO;
import examiner.enums.ExamAreaType;
import shared.model.ExamArea;
import shared.model.ExamZone;
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
        int cap = rs.getInt("Capacity");
        if (rs.wasNull()) {
            a.setCapacity(null);
        } else {
            a.setCapacity(cap);
        }
        a.setLocation(rs.getString("Location"));
        a.setExamZoneId(rs.getInt("ExamZoneId"));
        return a;
    }

    private ExamArea mapWithZone(ResultSet rs) throws SQLException {
        ExamArea a = map(rs);
        String zoneName = rs.getString("ZoneName");
        if (zoneName != null) {
            ExamZone zone = new ExamZone();
            zone.setExamZoneId(rs.getInt("ExamZoneId"));
            zone.setZoneName(zoneName);
            zone.setLocation(rs.getString("ZoneLocation"));
            a.setExamZone(zone);
        }
        return a;
    }

    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ExamArea WHERE 1=1");
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasType = areaType != null && !areaType.isBlank();
        if (hasKw) {
            sql.append(" AND (AreaName LIKE ? OR Location LIKE ? OR AreaType LIKE ?)");
        }
        if (hasType) {
            sql.append(" AND AreaType = ?");
        }
        sql.append(" ORDER BY ExamAreaId DESC");
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasType) {
                ps.setString(i++, areaType.trim());
            }
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
    public ExamArea getById(int examAreaId) {
        String sql = "SELECT ea.*, ez.ZoneName, ez.[Location] AS ZoneLocation "
                + "FROM ExamArea ea "
                + "LEFT JOIN ExamZone ez ON ez.ExamZoneId = ea.ExamZoneId "
                + "WHERE ea.ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapWithZone(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ExamArea a) {
        String sql = "INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            if (a.getCapacity() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, a.getCapacity());
            }
            ps.setString(4, a.getLocation());
            ps.setInt(5, a.getExamZoneId());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
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
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            if (a.getCapacity() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, a.getCapacity());
            }
            ps.setString(4, a.getLocation());
            ps.setInt(5, a.getExamZoneId());
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
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ExamArea";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        List<ExamArea> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamArea WHERE AreaType = ? ORDER BY AreaName";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ExamAreaType.EXAM_ROOM.getValue());
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
        String sql = "SELECT DISTINCT ea.* FROM ExamArea ea "
                + "JOIN ExaminerSchedule es ON ea.ExamAreaId = es.ExamAreaId "
                + "WHERE es.ExamId = ? ORDER BY ea.AreaName";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
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
    public boolean isAreaInExam(int examId, int examAreaId) {
        String sql = "SELECT COUNT(*) FROM ExaminerSchedule WHERE ExamId = ? AND ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}

