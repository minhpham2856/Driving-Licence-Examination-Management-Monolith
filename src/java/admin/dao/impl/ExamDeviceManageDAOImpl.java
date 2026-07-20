package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.ExamDeviceManageDAO;
import admin.model.DeviceView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceManageDAOImpl extends DBContext implements ExamDeviceManageDAO {

    private static final String BASE =
            "SELECT d.ExamDeviceId, d.DeviceName, d.DeviceType, d.IsActive, d.ExamAreaId, " +
            "  a.AreaName, a.AreaType, a.ExamZoneId, z.ZoneName " +
            "FROM ExamDevice d JOIN ExamArea a ON a.ExamAreaId = d.ExamAreaId " +
            "JOIN ExamZone z ON z.ExamZoneId = a.ExamZoneId ";

    @Override
    public List<DeviceView> search(String keyword, String deviceType, Integer zoneId, Integer areaId) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (d.DeviceName LIKE ?) "); ps.add("%" + keyword.trim() + "%");
        }
        if (deviceType != null && !deviceType.isBlank()) { sql.append(" AND d.DeviceType = ? "); ps.add(deviceType); }
        if (zoneId != null && zoneId > 0) { sql.append(" AND a.ExamZoneId = ? "); ps.add(zoneId); }
        if (areaId != null && areaId > 0) { sql.append(" AND d.ExamAreaId = ? "); ps.add(areaId); }
        sql.append(" ORDER BY d.ExamDeviceId DESC ");
        List<DeviceView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public DeviceView findById(int id) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE d.ExamDeviceId = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insert(DeviceView d) {
        String sql = "INSERT INTO ExamDevice (DeviceName, DeviceType, IsActive, ExamAreaId) VALUES (?,?,?,?)";
        try (PreparedStatement st = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, d.getDeviceName());
            st.setString(2, d.getDeviceType());
            st.setBoolean(3, d.isActive());
            st.setInt(4, d.getAreaId());
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean update(DeviceView d) {
        String sql = "UPDATE ExamDevice SET DeviceName=?, DeviceType=?, IsActive=?, ExamAreaId=? WHERE ExamDeviceId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, d.getDeviceName());
            st.setString(2, d.getDeviceType());
            st.setBoolean(3, d.isActive());
            st.setInt(4, d.getAreaId());
            st.setInt(5, d.getDeviceId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean setActive(int id, boolean active) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE ExamDevice SET IsActive=? WHERE ExamDeviceId=?")) {
            st.setBoolean(1, active); st.setInt(2, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM ExamDevice WHERE ExamDeviceId=?")) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamDevice");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private DeviceView map(ResultSet rs) throws SQLException {
        DeviceView d = new DeviceView();
        d.setDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setActive(rs.getBoolean("IsActive"));
        d.setAreaId(rs.getInt("ExamAreaId"));
        d.setAreaName(rs.getString("AreaName"));
        d.setAreaType(rs.getString("AreaType"));
        d.setZoneId(rs.getInt("ExamZoneId"));
        d.setZoneName(rs.getString("ZoneName"));
        return d;
    }
}
