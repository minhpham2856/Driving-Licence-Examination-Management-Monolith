package DAO.Impl;

import DAO.ExamAreaDAO;
import DBConnection.DBContext;
import Models.ExamArea;
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
        a.setCapacity(rs.getInt("Capacity"));
        a.setLocation(rs.getString("Location"));
        a.setCreatedAt(rs.getTimestamp("CreatedAt"));
        a.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        return a;
    }

    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM ExamArea WHERE 1=1");
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = areaType != null && !areaType.trim().isEmpty();
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
    public ExamArea findById(int examAreaId) {
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
        String sql = "INSERT INTO ExamArea (AreaName, AreaType, Capacity, Location, CreatedByUserId, UpdatedByUserId) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());
            setIntOrNull(ps, 5, a.getCreatedByUserId());
            setIntOrNull(ps, 6, a.getUpdatedByUserId());
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
        String sql = "UPDATE ExamArea SET AreaName = ?, AreaType = ?, Capacity = ?, Location = ?, "
                   + "UpdatedAt = GETDATE(), UpdatedByUserId = ? WHERE ExamAreaId = ?";
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());
            setIntOrNull(ps, 5, a.getUpdatedByUserId());
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
        } catch (SQLException e) {
            // most likely FK violation (area still referenced by devices/sessions)
            e.printStackTrace();
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

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }
}
