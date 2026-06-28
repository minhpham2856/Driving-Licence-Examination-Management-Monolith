package dao.impl;

import dbconnection.DBContext;

import dao.UserDAO;

import model.user.Profile;
import model.user.User;
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
import service.EnumMappingService;
import service.impl.EnumMappingServiceImpl;
import service.impl.RoleServiceImpl;

public class UserDAOImpl extends DBContext implements UserDAO {

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

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

    @Override
    public User getById(int id) {
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

    /**
     * Finds a user by their login username.
     *
     * @param username the exact username
     * @return the User model, or null if not found
     */
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

    /**
     * Looks up a user by any of: Username, Email, PhoneNumber, or
     * GovernmentIdNumber.
     *
     * @param identifier the search value to match against multiple columns
     * @return the User model, or null if not found
     */
    @Override
    public User getByIdentifier(String identifier) {
        String sql = USER_SELECT + """
                 where Username = ?
                    or Email = ?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(2, identifier);

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

    /**
     * Finds a user by their email address.
     *
     * @param email the exact email
     * @return the User model, or null if not found
     */
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

    /**
     * Inserts a new User record with RETURN_GENERATED_KEYS to populate the user
     * ID. Defaults the role to Registrant if none is set.
     *
     * @param user the User to insert (id will be populated on success)
     * @return true if the insert succeeded and a key was generated
     */
    @Override
    public boolean insert(User user) {
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
            service.RoleService roleService = new RoleServiceImpl();
            roleId = roleService.getRoleIdByName("Registrant");
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

    /**
     * Updates the password hash for the given user.
     *
     * @param userId the target UserId
     * @param passwordHash the new BCrypt-style hash
     * @return true if at least one row was updated
     */
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

    /**
     * Maps the current row of a ResultSet (from USER_SELECT) to a User model,
     * including the nested Profile and Role objects.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setActive(rs.getBoolean("IsActive"));

        user.setRoleId(rs.getInt("RoleId"));

        return user;
    @Override
    public List<User> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(",");
            }
        }
        String sql = USER_SELECT + " WHERE UserId IN (" + placeholders.toString() + ")";
        List<User> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
