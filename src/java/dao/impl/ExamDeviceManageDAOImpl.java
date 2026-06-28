package dao.impl;


import dbconnection.DBContext;

import dao.ExamDeviceManageDAO;

import model.exam.ExamDevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceManageDAOImpl extends DBContext implements ExamDeviceManageDAO {

    private static final String BASE_SELECT =
            "SELECT d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId " +
            "FROM ExamDevice d ";

    @Override
    public List<ExamDevice> search(String keyword, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND d.DeviceName LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND d.[Status] = ? ");
            params.add(status.trim());
        }
        sql.append(" ORDER BY d.ExamDeviceId DESC ");

        List<ExamDevice> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ExamDevice findById(int examDeviceId) {
        String sql = BASE_SELECT + " WHERE d.ExamDeviceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ExamDevice d, Integer createdBy) {
        String sql = "INSERT INTO ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId, " +
                     "CreatedByUserId, UpdatedByUserId) " +
                     "VALUES (?,?,?,?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            setNullableInt(ps, 5, createdBy);
            setNullableInt(ps, 6, createdBy);
            if (ps.executeUpdate() == 0) return 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean update(ExamDevice d, Integer updatedBy) {
        String sql = "UPDATE ExamDevice SET DeviceName=?, DeviceType=?, [Status]=?, ExamAreaId=?, " +
                     "UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE ExamDeviceId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            setNullableInt(ps, 5, updatedBy);
            ps.setInt(6, d.getExamDeviceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int examDeviceId) {
        String sql = "DELETE FROM ExamDevice WHERE ExamDeviceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamDevice");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamDevice WHERE [Status] = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }

    private ExamDevice map(ResultSet rs) throws SQLException {
        ExamDevice d = new ExamDevice();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        return d;
    }
}
