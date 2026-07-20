package admin.dao.impl;

import shared.dbconnection.DBContext;
import admin.dao.UserSecurityDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSecurityDAOImpl extends DBContext implements UserSecurityDAO {

    @Override
    public boolean mustChangePassword(int userId) {
        String sql = "SELECT MustChangePassword FROM [User] WHERE UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getBoolean(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean setMustChange(int userId, boolean value) {
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE [User] SET MustChangePassword = ? WHERE UserId = ?")) {
            ps.setBoolean(1, value); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public String getPasswordHash(int userId) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT PasswordHash FROM [User] WHERE UserId = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordPlain) {
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE [User] SET PasswordHash = ? WHERE UserId = ?")) {
            ps.setString(1, newPasswordPlain); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
