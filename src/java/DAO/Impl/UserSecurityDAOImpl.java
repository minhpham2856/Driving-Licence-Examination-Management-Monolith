package DAO.Impl;

import DBConnection.DBContext;
import DAO.UserSecurityDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSecurityDAOImpl extends DBContext implements UserSecurityDAO {

    @Override
    public boolean mustChangePassword(int userId) {
        String sql = "SELECT MustChangePassword FROM [User] WHERE UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean setMustChange(int userId, boolean value) {
        String sql = "UPDATE [User] SET MustChangePassword = ? WHERE UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, value);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public String getPasswordHash(int userId) {
        String sql = "SELECT PasswordHash FROM [User] WHERE UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordPlain) {
        String sql = "UPDATE [User] SET PasswordHash = ?, UpdatedAt = GETDATE() WHERE UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newPasswordPlain);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
