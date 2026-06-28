package dao.impl;



import dbconnection.DBContext;

import dao.UserDAO;

import model.user.Profile;
import model.user.Role;
import model.user.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of UserDAO using DBContext connection management. Maps
 * result sets to User model objects including nested Profile and Role.
 */
public class UserDAOImpl extends DBContext implements UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());

    private static final String USER_SELECT = """
                     select u.UserId,
                     	u.Username,
                     	u.Email,
                     	u.PasswordHash,
                     	r.RoleName,
                     	u.IsActive,
                     	p.ProfileId,
                     	p.FullName,
                     	p.DateOfBirth,
                     	p.PhoneNumber,
                     	p.Sex,
                     	p.GovernmentIdNumber,
                     	p.Address
                     from [User] u
                     join [Role] r on r.RoleId = u.RoleId
                     left join Profile p on p.UserId = u.UserId
                     """;

    /**
     * Retrieves a user by primary key, including nested Profile and Role.
     *
     * @param id the UserId
     * @return the User model, or null if not found
     */
    @Override
    public User getById(int id) {
        String sql = USER_SELECT + " where u.UserId = ?";

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
        String sql = USER_SELECT + " where u.Username = ?";

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
                 where u.Username = ?
                    or u.Email = ?
                    or p.PhoneNumber = ?
                    or p.GovernmentIdNumber = ?
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ps.setString(4, identifier);

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
        String sql = USER_SELECT + " where u.Email = ?";

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
            roleId = enums.UserRole.roleIdFromName("Registrant");
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

        String roleName = rs.getString("RoleName");
        Role role = enums.UserRole.roleFromName(roleName);
        user.setRoleId(role.getId());
        user.setRoleId(role.getId());

        if (false) {
            Profile profile = new Profile();
            
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
            profile.setPhoneNo(rs.getString("PhoneNumber"));
            profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setGender(enums.Gender.genderFromSex(rs.getString("Sex")));
            
        }

        return user;
    }
}


