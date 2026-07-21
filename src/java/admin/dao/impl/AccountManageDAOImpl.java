package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.AccountManageDAO;
import admin.model.AccountView;
import admin.model.RoleOption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import auth.util.PasswordUtil;

public class AccountManageDAOImpl extends DBContext implements AccountManageDAO {

    private static final String BASE =
            "SELECT u.UserId, u.Username, u.Email, u.RoleId, r.RoleName, u.IsActive, u.MustChangePassword, " +
            "       p.FullName, p.PhoneNumber, p.Sex, p.GovernmentIdNumber, p.Address, p.DateOfBirth " +
            "FROM [User] u JOIN [Role] r ON r.RoleId = u.RoleId " +
            "LEFT JOIN Profile p ON p.UserId = u.UserId ";

    @Override
    public List<AccountView> search(String keyword, Integer roleId, Boolean active) {
        StringBuilder sql = new StringBuilder(BASE).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.Username LIKE ? OR u.Email LIKE ? OR p.FullName LIKE ? OR p.PhoneNumber LIKE ?) ");
            String k = "%" + keyword.trim() + "%"; ps.add(k); ps.add(k); ps.add(k); ps.add(k);
        }
        if (roleId != null && roleId > 0) { sql.append(" AND u.RoleId = ? "); ps.add(roleId); }
        if (active != null) { sql.append(" AND u.IsActive = ? "); ps.add(active); }
        sql.append(" ORDER BY u.UserId DESC ");
        List<AccountView> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++) st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public AccountView findById(int userId) {
        try (PreparedStatement st = getConnection().prepareStatement(BASE + " WHERE u.UserId = ?")) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<RoleOption> listRoles() {
        List<RoleOption> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement("SELECT RoleId, RoleName FROM [Role] ORDER BY RoleId");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                RoleOption o = new RoleOption();
                o.setRoleId(rs.getInt("RoleId"));
                o.setRoleName(rs.getString("RoleName"));
                list.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public int create(AccountView a, int roleId, boolean sexMale, String passwordPlain) {
        Connection conn = getConnection();
        String userSql = "INSERT INTO [User] (Username, Email, PasswordHash, RoleId, IsActive, MustChangePassword) VALUES (?,?,?,?,?,1)";
        String profSql = "INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId) VALUES (?,?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            int userId;
            try (PreparedStatement st = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, a.getUsername());
                st.setString(2, a.getEmail());
                st.setString(3, PasswordUtil.hash(passwordPlain));
                st.setInt(4, roleId);
                st.setBoolean(5, a.isActive());
                if (st.executeUpdate() == 0) { conn.rollback(); return 0; }
                try (ResultSet keys = st.getGeneratedKeys()) {
                    if (!keys.next()) { conn.rollback(); return 0; }
                    userId = keys.getInt(1);
                }
            }
            try (PreparedStatement st = conn.prepareStatement(profSql)) {
                st.setString(1, a.getFullName());
                st.setDate(2, a.getDateOfBirth());
                st.setString(3, a.getPhone());
                st.setBoolean(4, sexMale);
                st.setString(5, a.getGovId());
                if (a.getAddress() == null || a.getAddress().isBlank()) st.setNull(6, Types.NVARCHAR); else st.setString(6, a.getAddress());
                st.setInt(7, userId);
                st.executeUpdate();
            }
            conn.commit();
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return 0;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    @Override
    public boolean resetPassword(int userId, String newPasswordPlain) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE [User] SET PasswordHash=?, MustChangePassword=1 WHERE UserId=?")) {
            st.setString(1, PasswordUtil.hash(newPasswordPlain));
            st.setInt(2, userId);
            
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean setStatus(int userId, boolean active) {
        try (PreparedStatement st = getConnection().prepareStatement("UPDATE [User] SET IsActive=? WHERE UserId=?")) {
            st.setBoolean(1, active); st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(int userId) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM Profile WHERE UserId=?")) { st.setInt(1, userId); st.executeUpdate(); }
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM [User] WHERE UserId=?")) {
                st.setInt(1, userId);
                int n = st.executeUpdate(); conn.commit(); return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    @Override public boolean usernameExists(String v) { return positive("SELECT COUNT(*) FROM [User] WHERE Username=?", v); }
    @Override public boolean emailExists(String v) { return positive("SELECT COUNT(*) FROM [User] WHERE Email=?", v); }
    @Override public boolean phoneExists(String v) { return positive("SELECT COUNT(*) FROM Profile WHERE PhoneNumber=?", v); }
    @Override public boolean govIdExists(String v) { return positive("SELECT COUNT(*) FROM Profile WHERE GovernmentIdNumber=?", v); }

    private boolean positive(String sql, String val) {
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, val);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public int countAll() {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM [User]");
             ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private AccountView map(ResultSet rs) throws SQLException {
        AccountView a = new AccountView();
        a.setUserId(rs.getInt("UserId"));
        a.setUsername(rs.getString("Username"));
        a.setEmail(rs.getString("Email"));
        a.setRoleId(rs.getInt("RoleId"));
        a.setRoleName(rs.getString("RoleName"));
        a.setActive(rs.getBoolean("IsActive"));
        a.setMustChange(rs.getBoolean("MustChangePassword"));
        a.setFullName(rs.getString("FullName"));
        a.setPhone(rs.getString("PhoneNumber"));
        a.setSexMale(rs.getBoolean("Sex"));
        a.setGovId(rs.getString("GovernmentIdNumber"));
        a.setAddress(rs.getString("Address"));
        a.setDateOfBirth(rs.getDate("DateOfBirth"));
        return a;
    }
}
