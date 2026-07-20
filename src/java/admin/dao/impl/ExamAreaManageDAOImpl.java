package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.ExamAreaManageDAO;
import admin.model.AreaView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamAreaManageDAOImpl extends DBContext implements ExamAreaManageDAO {

    private static final String BASE =
            "SELECT a.ExamAreaId, a.AreaName, a.AreaType, a.Capacity, a.[Location], a.ExamZoneId, " +
            "  z.ZoneName, (SELECT COUNT(*) FROM ExamDevice d WHERE d.ExamAreaId = a.ExamAreaId) AS DeviceCount " +
            "FROM ExamArea a JOIN ExamZone z ON z.ExamZoneId = a.ExamZoneId ";

    @Override
    public List<AreaView> search(String keyword, String areaType, Integer zoneId) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (a.AreaName LIKE ? OR a.[Location] LIKE ?) ");
            String k = "%" + keyword.trim() + "%"; ps.add(k); ps.add(k);
        }
        if (areaType != null && !areaType.isBlank()) { sql.append(" AND a.AreaType = ? "); ps.add(areaType); }
        if (zoneId != null && zoneId > 0) { sql.append(" AND a.ExamZoneId = ? "); ps.add(zoneId); }
        sql.append(" ORDER BY a.ExamAreaId DESC ");
        return query(sql.toString(), ps);
    }

    @Override
    public List<AreaView> listByZone(int zoneId) {
        List<Object> ps = new ArrayList<>(); ps.add(zoneId);
        return query(BASE + " WHERE a.ExamZoneId = ? ORDER BY a.AreaName", ps);
    }

    private List<AreaView> query(String sql, List<Object> ps) {
        List<AreaView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public AreaView findById(int id) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE a.ExamAreaId = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insert(AreaView a) {
        String sql = "INSERT INTO ExamArea (AreaName, AreaType, Capacity, [Location], ExamZoneId) VALUES (?,?,?,?,?)";
        try (PreparedStatement st = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, a.getAreaName());
            st.setString(2, a.getAreaType());
            if (a.getCapacity() == null) st.setNull(3, Types.INTEGER); else st.setInt(3, a.getCapacity());
            st.setString(4, a.getLocation());
            st.setInt(5, a.getZoneId());
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean update(AreaView a) {
        String sql = "UPDATE ExamArea SET AreaName=?, AreaType=?, Capacity=?, [Location]=?, ExamZoneId=? WHERE ExamAreaId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, a.getAreaName());
            st.setString(2, a.getAreaType());
            if (a.getCapacity() == null) st.setNull(3, Types.INTEGER); else st.setInt(3, a.getCapacity());
            st.setString(4, a.getLocation());
            st.setInt(5, a.getZoneId());
            st.setInt(6, a.getAreaId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM ExamArea WHERE ExamAreaId=?")) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamArea");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private AreaView map(ResultSet rs) throws SQLException {
        AreaView a = new AreaView();
        a.setAreaId(rs.getInt("ExamAreaId"));
        a.setAreaName(rs.getString("AreaName"));
        a.setAreaType(rs.getString("AreaType"));
        int cap = rs.getInt("Capacity"); a.setCapacity(rs.wasNull() ? null : cap);
        a.setLocation(rs.getString("Location"));
        a.setZoneId(rs.getInt("ExamZoneId"));
        a.setZoneName(rs.getString("ZoneName"));
        a.setDeviceCount(rs.getInt("DeviceCount"));
        return a;
    }
}
