package examiner.dao.impl;

import java.sql.*;
import java.util.*;
import shared.dbconnection.DBContext;
import examiner.dao.UserDAO;
import shared.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.enums.RoleType;

// JDBC implementation for User; examiner module DAO layer only.
public class UserDAOImpl extends DBContext implements UserDAO {
    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());
    private static final String USER_SELECT = """
                     select UserId,
                     	Username,
                     	Email,
                     	PasswordHash,
                     	RoleId,
                     	IsActive
                     from [User]
                     """;
    // Loads one user row by primary key.
    @Override
    public User get(int id) {
        String sql = USER_SELECT + " where UserId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Loads one user row by username.
    @Override
    public User getByUsername(String username) {
        String sql = USER_SELECT + " where Username = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Loads one user by username, email, or linked profile government id.
    @Override
    public User getByIdentifier(String identifier) {
        String sql = USER_SELECT + """
                 WHERE Username = ?
                    OR Email = ?
                    OR UserId IN (
                        SELECT UserId FROM Profile WHERE GovernmentIdNumber = ?
                    )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Loads one user row by email address.
    @Override
    public User getByEmail(String email) {
        String sql = USER_SELECT + " where Email = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Inserts a new user row.
    @Override
    public boolean add(User user) {
        Connection conn = getConnection();
        if (conn == null) {
            LOG.severe("Cannot insert user: database connection is unavailable.");
            return false;
        }
        String sql = """
                     insert into [User] (Username, Email, PasswordHash, RoleId, IsActive)
                     values (?, ?, ?, ?, ?)
                     """;
        int roleId = user.getRoleId();
        if (roleId <= 0) {
            LOG.warning("Cannot insert user: RoleId is required.");
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, roleId);
            ps.setBoolean(5, user.isActive());
            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
            }
            return user.getUserId() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to insert user {0}: {1}",
                    new Object[]{user.getEmail(), e.getMessage()});
        }
        return false;
    }
    // Updates password hash for one user.
    @Override
    public boolean updatePassword(int userId, String passwordHash) {
        String sql = """
                     update [User]
                     set PasswordHash = ?
                     where UserId = ?
                     """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // Private helper: map result set to user.
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setActive(rs.getBoolean("IsActive"));
        user.setRoleId(rs.getInt("RoleId"));
        return user;
    }
    // Batch-loads user rows for a list of user ids.
    @Override
    public List<User> getAllByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(",");
            }
        }
        String sql = USER_SELECT + " WHERE UserId IN (" + placeholders.toString() + ")";
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // Lists active users with the examiner role.
    @Override
    public List<User> getAllActiveExaminers() {
        String sql = "SELECT u.UserId, u.Username, u.Email, u.PasswordHash, u.RoleId, u.IsActive "
                + "FROM [User] u "
                + "INNER JOIN Role r ON r.RoleId = u.RoleId "
                + "WHERE r.RoleName = ? AND u.IsActive = 1 "
                + "ORDER BY u.Username";
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, RoleType.EXAMINER.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // Returns total count of user rows.
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM [User]";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Searches users for admin screens with optional role and status filters.
    @Override
    public List<User> getFilteredForAdmin(String keyword, String roleFilter, String statusFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT u.UserId, u.Username, u.Email, u.PasswordHash, u.RoleId, u.IsActive "
                + "FROM [User] u INNER JOIN Role r ON r.RoleId = u.RoleId WHERE 1=1");
        boolean hasKw = keyword != null && !keyword.isBlank();
        boolean hasRole = roleFilter != null && !roleFilter.isBlank();
        boolean hasStatus = statusFilter != null && !statusFilter.isBlank();
        if (hasKw) {
            sql.append(" AND (u.Username LIKE ? OR u.Email LIKE ? OR u.UserId IN "
                    + "(SELECT UserId FROM Profile WHERE FullName LIKE ? OR PhoneNumber LIKE ?))");
        }
        if (hasRole) {
            sql.append(" AND r.RoleName = ?");
        }
        if (hasStatus) {
            if ("active".equals(statusFilter)) {
                sql.append(" AND u.IsActive = 1");
            } else if ("locked".equals(statusFilter) || "inactive".equals(statusFilter)) {
                sql.append(" AND u.IsActive = 0");
            }
        }
        sql.append(" ORDER BY u.UserId DESC");
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (hasRole) {
                ps.setString(idx++, mapRoleFilterToDbName(roleFilter.trim()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Maps admin UI role filter keys to database RoleName values.
    private static String mapRoleFilterToDbName(String filter) {
        if ("admin".equals(filter)) {
            return RoleType.ADMIN.getValue();
        }
        if ("coi_thi".equals(filter)) {
            return RoleType.EXAM_STAFF.getValue();
        }
        if ("cham_thi".equals(filter)) {
            return RoleType.EXAMINER.getValue();
        }
        if ("candidate".equals(filter)) {
            return RoleType.CANDIDATE.getValue();
        }
        return filter;
    }
}

