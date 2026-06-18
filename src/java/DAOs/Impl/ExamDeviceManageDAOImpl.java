package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamDeviceManageDAO;
import Models.ExamDeviceView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceManageDAOImpl extends DBContext implements ExamDeviceManageDAO {

    // Device -> Room -> Area, so the admin list can show room + area names.
    private static final String BASE_SELECT =
            "SELECT d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamRoomId, d.ExamAreaId, " +
            "       r.RoomName, a.AreaName " +
            "FROM ExamDevice d " +
            "LEFT JOIN ExamRoom r ON d.ExamRoomId = r.ExamRoomId " +
            "LEFT JOIN ExamArea a ON d.ExamAreaId = a.ExamAreaId ";

    @Override
    public List<ExamDeviceView> search(String keyword, Integer roomId, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND d.DeviceName LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }
        if (roomId != null && roomId > 0) {
            sql.append(" AND d.ExamRoomId = ? ");
            params.add(roomId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND d.[Status] = ? ");
            params.add(status.trim());
        }
        sql.append(" ORDER BY d.ExamDeviceId DESC ");

        List<ExamDeviceView> list = new ArrayList<>();
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
    public ExamDeviceView findById(int examDeviceId) {
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

    /** ExamAreaId is auto-filled from the chosen room's area (keeps the NOT NULL column valid). */
    @Override
    public int insert(ExamDeviceView d, Integer createdBy) {
        String sql = "INSERT INTO ExamDevice (DeviceName, DeviceType, [Status], ExamRoomId, ExamAreaId, " +
                     "CreatedByUserId, UpdatedByUserId) " +
                     "VALUES (?,?,?,?, (SELECT ExamAreaId FROM ExamRoom WHERE ExamRoomId = ?), ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamRoomId());
            ps.setInt(5, d.getExamRoomId());
            setNullableInt(ps, 6, createdBy);
            setNullableInt(ps, 7, createdBy);
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
    public boolean update(ExamDeviceView d, Integer updatedBy) {
        String sql = "UPDATE ExamDevice SET DeviceName=?, DeviceType=?, [Status]=?, ExamRoomId=?, " +
                     "ExamAreaId=(SELECT ExamAreaId FROM ExamRoom WHERE ExamRoomId=?), " +
                     "UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE ExamDeviceId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamRoomId());
            ps.setInt(5, d.getExamRoomId());
            setNullableInt(ps, 6, updatedBy);
            ps.setInt(7, d.getExamDeviceId());
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

    private ExamDeviceView map(ResultSet rs) throws SQLException {
        ExamDeviceView d = new ExamDeviceView();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        int roomId = rs.getInt("ExamRoomId"); d.setExamRoomId(rs.wasNull() ? 0 : roomId);
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        d.setRoomName(rs.getString("RoomName"));
        d.setAreaName(rs.getString("AreaName"));
        return d;
    }
}
