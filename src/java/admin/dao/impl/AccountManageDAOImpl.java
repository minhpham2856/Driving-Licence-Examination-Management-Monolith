package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.AccountManageDAO;
import admin.model.AccountView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountManageDAOImpl extends DBContext implements AccountManageDAO {

    private static final String BASE_SELECT =
            "SELECT u.UserId, u.Username, u.Email, u.[Role], u.[Status], u.CreatedAt, " +
            "       p.FullName, p.PhoneNumber, p.Sex, p.GovernmentIdNumber, p.Address, p.DateOfBirth " +
            "FROM [User] u LEFT JOIN Profile p ON p.UserId = u.UserId ";

    @Override
    public List<AccountView> search(String keyword, String dbRole, Boolean active) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> ps = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (u.Username LIKE ? OR u.Email LIKE ? OR p.FullName LIKE ? OR p.PhoneNumber LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            ps.add(k); ps.add(k); ps.add(k); ps.add(k);
        }
        if (dbRole != null && !dbRole.isBlank()) { sql.append(" AND u.[Role] = ? "); ps.add(dbRole); }
        if (active != null) { sql.append(" AND u.[Status] = ? "); ps.add(active); }
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
        try (PreparedStatement st = getConnection().prepareStatement(BASE_SELECT + " WHERE u.UserId = ?")) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public int create(AccountView a, String passwordPlain, Integer actorId) {
        Connection conn = getConnection();
        String userSql = "INSERT INTO [User] (Username, Email, PasswordHash, [Role], [Status], MustChangePassword, CreatedByUserId, UpdatedByUserId) " +
                         "VALUES (?,?,?,?,?,1,?,?)";
        String profSql = "INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId, CreatedByUserId, UpdatedByUserId) " +
                         "VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            int userId;
            try (PreparedStatement st = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, a.getUsername());
                st.setString(2, a.getEmail());
                st.setString(3, passwordPlain);
                st.setString(4, a.getRole());
                st.setBoolean(5, a.isActive());
                setNullableInt(st, 6, actorId);
                setNullableInt(st, 7, actorId);
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
                st.setString(4, a.getSex());
                st.setString(5, a.getGovId());
                st.setString(6, a.getAddress());
                st.setInt(7, userId);
                setNullableInt(st, 8, actorId);
                setNullableInt(st, 9, actorId);
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
    public boolean update(AccountView a, String newPasswordOrNull, Integer actorId) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            String userSql = (newPasswordOrNull == null || newPasswordOrNull.isBlank())
                    ? "UPDATE [User] SET Email=?, [Role]=?, [Status]=?, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE UserId=?"
                    : "UPDATE [User] SET Email=?, [Role]=?, [Status]=?, PasswordHash=?, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE UserId=?";
            try (PreparedStatement st = conn.prepareStatement(userSql)) {
                int i = 1;
                st.setString(i++, a.getEmail());
                st.setString(i++, a.getRole());
                st.setBoolean(i++, a.isActive());
                if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) st.setString(i++, newPasswordOrNull);
                setNullableInt(st, i++, actorId);
                st.setInt(i, a.getUserId());
                st.executeUpdate();
            }
            String updProf = "UPDATE Profile SET FullName=?, DateOfBirth=?, PhoneNumber=?, Sex=?, GovernmentIdNumber=?, Address=?, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE UserId=?";
            int affected;
            try (PreparedStatement st = conn.prepareStatement(updProf)) {
                st.setString(1, a.getFullName());
                st.setDate(2, a.getDateOfBirth());
                st.setString(3, a.getPhone());
                st.setString(4, a.getSex());
                st.setString(5, a.getGovId());
                st.setString(6, a.getAddress());
                setNullableInt(st, 7, actorId);
                st.setInt(8, a.getUserId());
                affected = st.executeUpdate();
            }
            if (affected == 0) {
                String insProf = "INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId, CreatedByUserId, UpdatedByUserId) VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement st = conn.prepareStatement(insProf)) {
                    st.setString(1, a.getFullName());
                    st.setDate(2, a.getDateOfBirth());
                    st.setString(3, a.getPhone());
                    st.setString(4, a.getSex());
                    st.setString(5, a.getGovId());
                    st.setString(6, a.getAddress());
                    st.setInt(7, a.getUserId());
                    setNullableInt(st, 8, actorId);
                    setNullableInt(st, 9, actorId);
                    st.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    @Override
    public boolean resetPassword(int userId, String newPasswordPlain, Integer actorId) {
        String sql = "UPDATE [User] SET PasswordHash=?, MustChangePassword=1, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE UserId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, newPasswordPlain);
            setNullableInt(st, 2, actorId);
            st.setInt(3, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean setStatus(int userId, boolean active, Integer actorId) {
        String sql = "UPDATE [User] SET [Status]=?, UpdatedAt=GETDATE(), UpdatedByUserId=? WHERE UserId=?";
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setBoolean(1, active); setNullableInt(st, 2, actorId); st.setInt(3, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean delete(int userId) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM Profile WHERE UserId=?")) {
                st.setInt(1, userId); st.executeUpdate();
            }
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM [User] WHERE UserId=?")) {
                st.setInt(1, userId);
                int n = st.executeUpdate();
                conn.commit();
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    @Override public boolean usernameExists(String username, int ex) { return existsUser("u.Username", username, ex); }
    @Override public boolean emailExists(String email, int ex) { return existsUser("u.Email", email, ex); }
    @Override public boolean phoneExists(String phone, int ex) {
        return countPositive("SELECT COUNT(*) FROM Profile p WHERE p.PhoneNumber = ? AND p.UserId <> ?", phone, ex);
    }
    @Override public boolean govIdExists(String govId, int ex) {
        return countPositive("SELECT COUNT(*) FROM Profile p WHERE p.GovernmentIdNumber = ? AND p.UserId <> ?", govId, ex);
    }
    private boolean existsUser(String col, String val, int ex) {
        return countPositive("SELECT COUNT(*) FROM [User] u WHERE " + col + " = ? AND u.UserId <> ?", val, ex);
    }
    private boolean countPositive(String sql, String val, int excludeId) {
        try (PreparedStatement st = getConnection().prepareStatement(sql)) {
            st.setString(1, val); st.setInt(2, excludeId);
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

    @Override
    public int countByRole(String dbRole) {
        try (PreparedStatement st = getConnection().prepareStatement("SELECT COUNT(*) FROM [User] WHERE [Role] = ?")) {
            st.setString(1, dbRole);
            try (ResultSet rs = st.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private void setNullableInt(PreparedStatement st, int idx, Integer v) throws SQLException {
        if (v == null) st.setNull(idx, java.sql.Types.INTEGER); else st.setInt(idx, v);
    }

    private AccountView map(ResultSet rs) throws SQLException {
        AccountView a = new AccountView();
        a.setUserId(rs.getInt("UserId"));
        a.setUsername(rs.getString("Username"));
        a.setEmail(rs.getString("Email"));
        a.setRole(rs.getString("Role"));
        a.setActive(rs.getBoolean("Status"));
        a.setCreatedAt(rs.getTimestamp("CreatedAt"));
        a.setFullName(rs.getString("FullName"));
        a.setPhone(rs.getString("PhoneNumber"));
        a.setSex(rs.getString("Sex"));
        a.setGovId(rs.getString("GovernmentIdNumber"));
        a.setAddress(rs.getString("Address"));
        a.setDateOfBirth(rs.getDate("DateOfBirth"));
        return a;
    }
}
