package dao.impl;

import dbconnection.DBContext;
import dao.ExamDeviceDAO;
import model.exam.ExamDevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceDAOImpl extends DBContext implements ExamDeviceDAO {

    private static final String BASE_SELECT = "SELECT ExamDeviceId, DeviceName, DeviceType, IsActive, ExamAreaId FROM ExamDevice";

    @Override
    public ExamDevice findById(int examDeviceId) {
        String sql = BASE_SELECT + " WHERE ExamDeviceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ExamDevice d) {
        String sql = "INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setBoolean(3, d.isActive());
            ps.setInt(4, d.getExamAreaId());
            if (ps.executeUpdate() == 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean update(ExamDevice d) {
        String sql = "UPDATE ExamDevice SET DeviceName=?, DeviceType=?, IsActive=?, ExamAreaId=? WHERE ExamDeviceId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setBoolean(3, d.isActive());
            ps.setInt(4, d.getExamAreaId());
            ps.setInt(5, d.getExamDeviceId());
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
        String sql = "SELECT COUNT(*) FROM ExamDevice";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean updateStatus(int examDeviceId, boolean isActive) {
        String sql = "UPDATE ExamDevice SET IsActive = ? WHERE ExamDeviceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, examDeviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExamDevice> getDevicesByAreaId(int examAreaId) {
        List<ExamDevice> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE ExamAreaId = ? ORDER BY DeviceName";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamDevice> findByAreaIds(List<Integer> areaIds) {
        List<ExamDevice> list = new ArrayList<>();
        if (areaIds == null || areaIds.isEmpty()) return list;
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE ExamAreaId IN (");
        for (int i = 0; i < areaIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < areaIds.size(); i++) {
                ps.setInt(i + 1, areaIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExamDevice map(ResultSet rs) throws SQLException {
        ExamDevice d = new ExamDevice();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setActive(rs.getBoolean("IsActive"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        return d;
    }
}
