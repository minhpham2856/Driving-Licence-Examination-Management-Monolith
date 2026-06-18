package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamRoomDAO;
import Models.ExamRoom;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamRoomDAOImpl extends DBContext implements ExamRoomDAO {

    private static final String BASE_SELECT =
            "SELECT r.ExamRoomId, r.RoomName, r.RoomType, r.Capacity, r.Floor, r.[Status], " +
            "       r.ExamAreaId, r.CreatedAt, r.CreatedByUserId, r.UpdatedAt, r.UpdatedByUserId, " +
            "       a.AreaName, " +
            "       (SELECT COUNT(*) FROM ExamDevice d WHERE d.ExamRoomId = r.ExamRoomId) AS DeviceCount " +
            "FROM ExamRoom r LEFT JOIN ExamArea a ON r.ExamAreaId = a.ExamAreaId ";

    @Override
    public List<ExamRoom> search(String keyword, Integer areaId, String type, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND r.RoomName LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }
        if (areaId != null && areaId > 0) {
            sql.append(" AND r.ExamAreaId = ? ");
            params.add(areaId);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND r.RoomType = ? ");
            params.add(type.trim());
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.[Status] = ? ");
            params.add(status.trim());
        }
        sql.append(" ORDER BY r.ExamRoomId DESC ");

        List<ExamRoom> list = new ArrayList<>();
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
    public ExamRoom findById(int examRoomId) {
        String sql = BASE_SELECT + " WHERE r.ExamRoomId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ExamRoom r) {
        String sql = "INSERT INTO ExamRoom (RoomName, RoomType, Capacity, Floor, [Status], ExamAreaId, " +
                     "CreatedByUserId, UpdatedByUserId) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getRoomName());
            ps.setString(2, r.getRoomType());
            if (r.getCapacity() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, r.getCapacity());
            ps.setString(4, r.getFloor());
            ps.setString(5, r.getStatus());
            ps.setInt(6, r.getExamAreaId());
            setNullableInt(ps, 7, r.getCreatedByUserId());
            setNullableInt(ps, 8, r.getUpdatedByUserId());
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
    public boolean update(ExamRoom r) {
        String sql = "UPDATE ExamRoom SET RoomName=?, RoomType=?, Capacity=?, Floor=?, [Status]=?, " +
                     "ExamAreaId=?, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE ExamRoomId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getRoomName());
            ps.setString(2, r.getRoomType());
            if (r.getCapacity() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, r.getCapacity());
            ps.setString(4, r.getFloor());
            ps.setString(5, r.getStatus());
            ps.setInt(6, r.getExamAreaId());
            setNullableInt(ps, 7, r.getUpdatedByUserId());
            ps.setInt(8, r.getExamRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int examRoomId) {
        String sql = "DELETE FROM ExamRoom WHERE ExamRoomId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examRoomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // FK violation -> false
        }
        return false;
    }

    @Override
    public int countAll() { return countWhere(null, null); }

    @Override
    public int countByStatus(String status) { return countWhere("[Status]", status); }

    @Override
    public int countByType(String type) { return countWhere("RoomType", type); }

    private int countWhere(String col, String val) {
        String sql = "SELECT COUNT(*) FROM ExamRoom" + (col != null ? " WHERE " + col + " = ?" : "");
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

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, val);
    }

    private ExamRoom map(ResultSet rs) throws SQLException {
        ExamRoom r = new ExamRoom();
        r.setExamRoomId(rs.getInt("ExamRoomId"));
        r.setRoomName(rs.getString("RoomName"));
        r.setRoomType(rs.getString("RoomType"));
        int cap = rs.getInt("Capacity");
        r.setCapacity(rs.wasNull() ? null : cap);
        r.setFloor(rs.getString("Floor"));
        r.setStatus(rs.getString("Status"));
        r.setExamAreaId(rs.getInt("ExamAreaId"));
        r.setCreatedAt(rs.getTimestamp("CreatedAt"));
        r.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        int cby = rs.getInt("CreatedByUserId"); r.setCreatedByUserId(rs.wasNull() ? null : cby);
        int uby = rs.getInt("UpdatedByUserId"); r.setUpdatedByUserId(rs.wasNull() ? null : uby);
        r.setAreaName(rs.getString("AreaName"));
        r.setComputerCount(rs.getInt("DeviceCount"));
        return r;
    }
}
