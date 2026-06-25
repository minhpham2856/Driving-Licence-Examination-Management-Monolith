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

/**
 * JDBC implementation of ExamDeviceDAO for managing exam computer devices.
 * Joins with ExamArea to include the area name in results.
 */
public class ExamDeviceDAOImpl extends DBContext implements ExamDeviceDAO {

    // Device -> Room -> Area, so we can show room + area names.
    private static final String BASE_SELECT =
            "SELECT d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId, " +
            "       a.AreaName " +
            "FROM ExamDevice d " +
            "LEFT JOIN ExamArea a ON d.ExamAreaId = a.ExamAreaId ";

    /**
     * Searches devices by keyword (device name), optional status filter.
     * Note: roomId parameter is accepted for interface compatibility but not used
     * (filtering by area is done via getDevicesByAreaId instead).
     *
     * @param keyword optional text to match against DeviceName
     * @param roomId  unused (legacy parameter)
     * @param status  optional exact match on Status
     * @return list of matching ExamDevice objects
     */
    @Override
    public List<ExamDevice> search(String keyword, Integer roomId, String status) {
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

    /**
     * Retrieves a device by its primary key, including the area name.
     *
     * @param examDeviceId the ExamDeviceId
     * @return the ExamDevice, or null if not found
     */
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

    /**
     * Inserts a new ExamDevice and returns the generated key.
     *
     * @param d the ExamDevice to insert
     * @return the new ExamDeviceId, or 0 on failure
     */
    @Override
    public int insert(ExamDevice d) {
        String sql = "INSERT INTO ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId) " +
                     "VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            if (ps.executeUpdate() == 0) return 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Updates all mutable fields of a device.
     *
     * @param d the ExamDevice with updated values
     * @return true if at least one row was updated
     */
    @Override
    public boolean update(ExamDevice d) {
        String sql = "UPDATE ExamDevice SET DeviceName=?, DeviceType=?, [Status]=?, ExamAreaId=? " +
                     "WHERE ExamDeviceId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            ps.setInt(5, d.getExamDeviceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a device by its primary key.
     *
     * @param examDeviceId the ExamDeviceId to delete
     * @return true if deletion succeeded
     */
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

    /**
     * Returns the total number of devices.
     *
     * @return the count
     */
    @Override
    public int countAll() { return countWhere(null, null); }

    /**
     * Returns the number of devices with the given status.
     *
     * @param status the status value to count
     * @return the count
     */
    @Override
    public int countByStatus(String status) { return countWhere("[Status]", status); }

    /**
     * Updates only the status of a device.
     *
     * @param examDeviceId the target device ID
     * @param status       the new status value
     * @return true if at least one row was updated
     */
    @Override
    public boolean updateStatus(int examDeviceId, String status) {
        String sql = "UPDATE ExamDevice SET [Status] = ? WHERE ExamDeviceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, examDeviceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns all devices assigned to a specific exam area.
     *
     * @param examAreaId the parent ExamAreaId
     * @return list of devices in that area
     */
    @Override
    public List<ExamDevice> getDevicesByAreaId(int examAreaId) {
        List<ExamDevice> list = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE d.ExamAreaId = ? ORDER BY d.DeviceName";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Counts rows in ExamDevice, optionally filtered by a column value. */
    private int countWhere(String col, String val) {
        String sql = "SELECT COUNT(*) FROM ExamDevice" + (col != null ? " WHERE " + col + " = ?" : "");
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (col != null) ps.setString(1, val);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Maps a ResultSet row to an ExamDevice model, including the area name. */
    private ExamDevice map(ResultSet rs) throws SQLException {
        ExamDevice d = new ExamDevice();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        d.setAreaName(rs.getString("AreaName"));
        return d;
    }
}
