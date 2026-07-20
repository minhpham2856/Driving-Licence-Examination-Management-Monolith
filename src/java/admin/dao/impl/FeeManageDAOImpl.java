package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.FeeManageDAO;
import admin.model.FeeView;
import admin.model.LicenceFeeView;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeeManageDAOImpl extends DBContext implements FeeManageDAO {

    // ---------- Fee ----------
    @Override
    public List<FeeView> listFees(String keyword, Boolean active) {
        StringBuilder sql = new StringBuilder("SELECT FeeId, FeeName, FeeType, IsActive FROM Fee WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) { sql.append(" AND (FeeName LIKE ? OR FeeType LIKE ?) "); String k="%"+keyword.trim()+"%"; ps.add(k); ps.add(k); }
        if (active != null) { sql.append(" AND IsActive = ? "); ps.add(active); }
        sql.append(" ORDER BY FeeId DESC ");
        List<FeeView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(mapFee(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<FeeView> listActiveFees() {
        List<FeeView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement("SELECT FeeId, FeeName, FeeType, IsActive FROM Fee WHERE IsActive=1 ORDER BY FeeName");
             ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(mapFee(rs)); }
        catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public FeeView findFee(int feeId) {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT FeeId, FeeName, FeeType, IsActive FROM Fee WHERE FeeId=?")) {
            st.setInt(1, feeId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapFee(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insertFee(FeeView f) {
        try (PreparedStatement st = getConnection().prepareStatement("INSERT INTO Fee (FeeName, FeeType, IsActive) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, f.getFeeName()); st.setString(2, f.getFeeType()); st.setBoolean(3, f.isActive());
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean updateFee(FeeView f) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE Fee SET FeeName=?, FeeType=?, IsActive=? WHERE FeeId=?")) {
            st.setString(1, f.getFeeName()); st.setString(2, f.getFeeType()); st.setBoolean(3, f.isActive()); st.setInt(4, f.getFeeId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean setFeeActive(int feeId, boolean active) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE Fee SET IsActive=? WHERE FeeId=?")) {
            st.setBoolean(1, active); st.setInt(2, feeId); return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteFee(int feeId) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM Fee WHERE FeeId=?")) {
            st.setInt(1, feeId); return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean feeNameExists(String feeName, int excludeId) {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Fee WHERE FeeName=? AND FeeId<>?")) {
            st.setString(1, feeName); st.setInt(2, excludeId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public int countFees() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Fee"); ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ---------- Licence_Fee ----------
    private static final String LF_BASE =
            "SELECT lf.LicenceFeeId, lf.LicenceId, lf.FeeId, lf.Amount, l.LicenceClass, f.FeeName, f.FeeType " +
            "FROM Licence_Fee lf JOIN Fee f ON f.FeeId = lf.FeeId " +
            "LEFT JOIN Licence l ON l.LicenceId = lf.LicenceId ";

    @Override
    public List<LicenceFeeView> listLicenceFees(Integer licenceId, Integer feeId) {
        StringBuilder sql = new StringBuilder(LF_BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (licenceId != null && licenceId > 0) { sql.append(" AND lf.LicenceId = ? "); ps.add(licenceId); }
        if (feeId != null && feeId > 0) { sql.append(" AND lf.FeeId = ? "); ps.add(feeId); }
        sql.append(" ORDER BY lf.LicenceFeeId DESC ");
        List<LicenceFeeView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(mapLF(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public LicenceFeeView findLicenceFee(int licenceFeeId) {
        try (PreparedStatement st = getConnection().prepareStatement(LF_BASE + " WHERE lf.LicenceFeeId = ?")) {
            st.setInt(1, licenceFeeId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return mapLF(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int insertLicenceFee(Integer licenceId, int feeId, BigDecimal amount) {
        try (PreparedStatement st = getConnection().prepareStatement("INSERT INTO Licence_Fee (LicenceId, FeeId, Amount) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            if (licenceId == null) st.setNull(1, Types.INTEGER); else st.setInt(1, licenceId);
            st.setInt(2, feeId); st.setBigDecimal(3, amount);
            if (st.executeUpdate() == 0) return 0;
            try (ResultSet k = st.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public boolean updateLicenceFee(int licenceFeeId, Integer licenceId, int feeId, BigDecimal amount) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE Licence_Fee SET LicenceId=?, FeeId=?, Amount=? WHERE LicenceFeeId=?")) {
            if (licenceId == null) st.setNull(1, Types.INTEGER); else st.setInt(1, licenceId);
            st.setInt(2, feeId); st.setBigDecimal(3, amount); st.setInt(4, licenceFeeId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean deleteLicenceFee(int licenceFeeId) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM Licence_Fee WHERE LicenceFeeId=?")) {
            st.setInt(1, licenceFeeId); return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean pairExists(Integer licenceId, int feeId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Licence_Fee WHERE FeeId=? AND LicenceFeeId<>? AND " +
                (licenceId == null ? "LicenceId IS NULL" : "LicenceId=?");
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setInt(1, feeId); st.setInt(2, excludeId);
            if (licenceId != null) st.setInt(3, licenceId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public int countLicenceFees() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM Licence_Fee"); ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private FeeView mapFee(ResultSet rs) throws SQLException {
        FeeView f = new FeeView();
        f.setFeeId(rs.getInt("FeeId")); f.setFeeName(rs.getString("FeeName"));
        f.setFeeType(rs.getString("FeeType")); f.setActive(rs.getBoolean("IsActive"));
        return f;
    }
    private LicenceFeeView mapLF(ResultSet rs) throws SQLException {
        LicenceFeeView v = new LicenceFeeView();
        v.setLicenceFeeId(rs.getInt("LicenceFeeId"));
        int lid = rs.getInt("LicenceId"); v.setLicenceId(rs.wasNull() ? null : lid);
        v.setLicenceClass(rs.getString("LicenceClass"));
        v.setFeeId(rs.getInt("FeeId"));
        v.setFeeName(rs.getString("FeeName"));
        v.setFeeType(rs.getString("FeeType"));
        v.setAmount(rs.getBigDecimal("Amount"));
        return v;
    }
}
