package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.LicenceManageDAO;
import admin.model.LicenceView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Licence (hạng GPLX). Chỉ thao tác các cột chắc chắn có:
 * LicenceId, LicenceClass, Description, MinimumAge, ValidForYears.
 * (Cột UpgradeFrom... để NULL - có thể bổ sung sau khi xác nhận tên cột.)
 */
public class LicenceManageDAOImpl extends DBContext implements LicenceManageDAO {

    private static final String BASE =
            "SELECT l.LicenceId, l.LicenceClass, l.Description, l.MinimumAge, l.ValidForYears, " +
            "  (SELECT COUNT(*) FROM Licence_Fee lf WHERE lf.LicenceId = l.LicenceId) AS FeeCount " +
            "FROM Licence l ";

    @Override
    public List<LicenceView> search(String keyword) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (l.LicenceClass LIKE ? OR l.Description LIKE ?) ");
            String k = "%" + keyword.trim() + "%"; ps.add(k); ps.add(k);
        }
        sql.append(" ORDER BY l.LicenceClass ");
        return query(sql.toString(), ps);
    }

    @Override
    public List<LicenceView> listAll() { return query(BASE + " ORDER BY l.LicenceClass", new ArrayList<>()); }

    private List<LicenceView> query(String sql, List<Object> ps) {
        List<LicenceView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public LicenceView findById(int id) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE l.LicenceId = ?")) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insert(LicenceView l) {
        String sql = "INSERT INTO Licence (LicenceClass, Description, MinimumAge, ValidForYears) VALUES (?,?,?,?)";
        try (PreparedStatement st = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, l.getLicenceClass());
            if (l.getDescription() == null || l.getDescription().isBlank()) st.setNull(2, Types.NVARCHAR); else st.setString(2, l.getDescription());
            st.setInt(3, l.getMinimumAge());
            st.setInt(4, l.getValidForYears());
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean update(LicenceView l) {
        String sql = "UPDATE Licence SET LicenceClass=?, Description=?, MinimumAge=?, ValidForYears=? WHERE LicenceId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, l.getLicenceClass());
            if (l.getDescription() == null || l.getDescription().isBlank()) st.setNull(2, Types.NVARCHAR); else st.setString(2, l.getDescription());
            st.setInt(3, l.getMinimumAge());
            st.setInt(4, l.getValidForYears());
            st.setInt(5, l.getLicenceId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM Licence WHERE LicenceId=?")) {
            st.setInt(1, id); return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean classExists(String licenceClass, int excludeId) {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Licence WHERE LicenceClass=? AND LicenceId<>?")) {
            st.setString(1, licenceClass); st.setInt(2, excludeId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Licence");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private LicenceView map(ResultSet rs) throws SQLException {
        LicenceView l = new LicenceView();
        l.setLicenceId(rs.getInt("LicenceId"));
        l.setLicenceClass(rs.getString("LicenceClass"));
        l.setDescription(rs.getString("Description"));
        l.setMinimumAge(rs.getInt("MinimumAge"));
        l.setValidForYears(rs.getInt("ValidForYears"));
        l.setFeeCount(rs.getInt("FeeCount"));
        return l;
    }
}
