package examstaff.dao.impl;


import shared.dbconnection.DBContext;

import examstaff.dao.ExamDeviceManageDAO;

import examstaff.dto.exam.ExamDeviceViewDTO;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ExamDeviceManageDAO for admin device management.
 * Returns ExamDeviceViewDTO objects with area names, supports CRUD operations.
 */
public class ExamDeviceManageDAOImpl extends DBContext implements ExamDeviceManageDAO {

    private static final String BASE_SELECT =
            "SELECT d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId, " +
            "       a.AreaName " +
            "FROM ExamDevice d " +
            "LEFT JOIN ExamArea a ON d.ExamAreaId = a.ExamAreaId ";

    /**
     * Searches devices by name keyword and/or status filter.
     *
     * @param keyword optional text to match against DeviceName
     * @param status  optional exact match on Status column
     * @return list of matching ExamDeviceViewDTO objects
     */
    @Override
    public List<ExamDeviceViewDTO> search(String keyword, String status) {
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

        List<ExamDeviceViewDTO> list = new ArrayList<>();
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
     * Retrieves a device view DTO by primary key.
     *
     * @param examDeviceId the ExamDeviceId
     * @return the ExamDeviceViewDTO, or null if not found
     */
    @Override
    public ExamDeviceViewDTO findById(int examDeviceId) {
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
     * Inserts a new device record with created/updated by tracking.
     *
     * @param d         the device data to insert
     * @param createdBy the user ID creating this device
     * @return the new ExamDeviceId, or 0 on failure
     */
    @Override
    public int insert(ExamDeviceViewDTO d, Integer createdBy) {
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

    /**
     * Updates device fields and sets UpdatedAt/UpdatedByUserId.
     *
     * @param d         the device data with updated values
     * @param updatedBy the user ID performing the update
     * @return true if at least one row was updated
     */
    @Override
    public boolean update(ExamDeviceViewDTO d, Integer updatedBy) {
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
    public int countAll() {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamDevice");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the number of devices with the given status.
     *
     * @param status the status value to count
     * @return the count
     */
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

    /** Sets an Integer parameter; uses SQL NULL when the value is null. */
    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }

    /** Maps a ResultSet row into an ExamDeviceViewDTO including area name. */
    private ExamDeviceViewDTO map(ResultSet rs) throws SQLException {
        ExamDeviceViewDTO d = new ExamDeviceViewDTO();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        d.setAreaName(rs.getString("AreaName"));
        return d;
    }
}
