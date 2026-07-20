package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.ExamZoneDAO;
import admin.model.ZoneView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamZoneDAOImpl extends DBContext implements ExamZoneDAO {

    private static final String BASE =
            "SELECT z.ExamZoneId, z.ZoneName, z.[Location], z.IsActive, " +
            "  (SELECT COUNT(*) FROM ExamArea a WHERE a.ExamZoneId = z.ExamZoneId) AS AreaCount " +
            "FROM ExamZone z ";

    @Override
    public List<ZoneView> search(String keyword, Boolean active) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (z.ZoneName LIKE ? OR z.[Location] LIKE ?) ");
            String k = "%" + keyword.trim() + "%"; ps.add(k); ps.add(k);
        }
        if (active != null) { sql.append(" AND z.IsActive = ? "); ps.add(active); }
        sql.append(" ORDER BY z.ExamZoneId DESC ");
        List<ZoneView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public ZoneView findById(int id) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE z.ExamZoneId = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insert(ZoneView z) {
        String sql = "INSERT INTO ExamZone (ZoneName, [Location], IsActive) VALUES (?,?,?)";
        try (PreparedStatement st = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, z.getZoneName());
            st.setString(2, z.getLocation());
            st.setBoolean(3, z.isActive());
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean update(ZoneView z) {
        String sql = "UPDATE ExamZone SET ZoneName=?, [Location]=?, IsActive=? WHERE ExamZoneId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, z.getZoneName());
            st.setString(2, z.getLocation());
            st.setBoolean(3, z.isActive());
            st.setInt(4, z.getZoneId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean setActive(int id, boolean active) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE ExamZone SET IsActive=? WHERE ExamZoneId=?")) {
            st.setBoolean(1, active); st.setInt(2, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM ExamZone WHERE ExamZoneId=?")) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM ExamZone");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public List<ZoneView> listActive() {
        List<ZoneView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE z.IsActive = 1 ORDER BY z.ZoneName");
             ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private ZoneView map(ResultSet rs) throws SQLException {
        ZoneView z = new ZoneView();
        z.setZoneId(rs.getInt("ExamZoneId"));
        z.setZoneName(rs.getString("ZoneName"));
        z.setLocation(rs.getString("Location"));
        z.setActive(rs.getBoolean("IsActive"));
        z.setAreaCount(rs.getInt("AreaCount"));
        return z;
    }
}
