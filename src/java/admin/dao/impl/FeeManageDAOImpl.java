package admin.dao.impl;

import admin.db.DBContext;
import admin.dao.FeeManageDAO;
import admin.dto.FeeView;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeeManageDAOImpl extends DBContext implements FeeManageDAO {

    private static final String BASE_SELECT =
            "SELECT f.FeeId, f.FeeName, f.FeeType, f.Amount, f.IsActive, f.UpdatedAt, f.LicenceId, " +
            "       l.LicenceClass " +
            "FROM Fee f LEFT JOIN Licence l ON f.LicenceId = l.LicenceId ";

    @Override
    public List<FeeView> search(String keyword, Integer licenceId, String feeType, Boolean active) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) { sql.append(" AND f.FeeName LIKE ? "); ps.add("%" + keyword.trim() + "%"); }
        if (licenceId != null && licenceId > 0) { sql.append(" AND f.LicenceId = ? "); ps.add(licenceId); }
        if (feeType != null && !feeType.isBlank()) { sql.append(" AND f.FeeType = ? "); ps.add(feeType.trim()); }
        if (active != null) { sql.append(" AND f.IsActive = ? "); ps.add(active); }
        sql.append(" ORDER BY f.FeeId DESC ");

        List<FeeView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public FeeView findById(int feeId) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE_SELECT + " WHERE f.FeeId = ?")) {
            st.setInt(1, feeId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insert(FeeView f, Integer actorId) {
        String sql = "INSERT INTO Fee (FeeName, FeeType, Amount, IsActive, LicenceId, CreatedByUserId, UpdatedByUserId) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement st = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, f.getFeeName());
            st.setString(2, f.getFeeType());
            st.setBigDecimal(3, f.getAmount());
            st.setBoolean(4, f.isActive());
            setNullableInt(st, 5, f.getLicenceId());
            setNullableInt(st, 6, actorId);
            setNullableInt(st, 7, actorId);
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet keys = st.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean update(FeeView f, Integer actorId) {
        String sql = "UPDATE Fee SET FeeName=?, FeeType=?, Amount=?, IsActive=?, LicenceId=?, " +
                     "UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE FeeId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, f.getFeeName());
            st.setString(2, f.getFeeType());
            st.setBigDecimal(3, f.getAmount());
            st.setBoolean(4, f.isActive());
            setNullableInt(st, 5, f.getLicenceId());
            setNullableInt(st, 6, actorId);
            st.setInt(7, f.getFeeId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int feeId) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM Fee WHERE FeeId = ?")) {
            st.setInt(1, feeId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Fee");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int countByType(String feeType) {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Fee WHERE FeeType = ?")) {
            st.setString(1, feeType);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private void setNullableInt(PreparedStatement st, int idx, Integer v) throws SQLException {
        if (v == null) st.setNull(idx, java.sql.Types.INTEGER); else st.setInt(idx, v);
    }

    private FeeView map(ResultSet rs) throws SQLException {
        FeeView f = new FeeView();
        f.setFeeId(rs.getInt("FeeId"));
        f.setFeeName(rs.getString("FeeName"));
        f.setFeeType(rs.getString("FeeType"));
        f.setAmount(rs.getBigDecimal("Amount"));
        f.setActive(rs.getBoolean("IsActive"));
        f.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        int lid = rs.getInt("LicenceId"); f.setLicenceId(rs.wasNull() ? null : lid);
        f.setLicenceClass(rs.getString("LicenceClass"));
        return f;
    }
}
