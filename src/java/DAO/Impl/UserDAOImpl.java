package DAO.Impl;

import Constants.Db2Mappings;
import DBConnection.DBContext;
import DAO.UserDAO;
import Models.Profile;
import Models.Role;
import Models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOImpl extends DBContext implements UserDAO {

    private static final Logger LOG = Logger.getLogger(UserDAOImpl.class.getName());

    private static final String USER_SELECT = """
                     select u.UserId,
                     	u.Username,
                     	u.Email,
                     	u.PasswordHash,
                     	u.[Role],
                     	u.[Status],
                     	p.ProfileId,
                     	p.FullName,
                     	p.DateOfBirth,
                     	p.PhoneNumber,
                     	p.Sex,
                     	p.GovernmentIdNumber,
                     	p.Address
                     from [User] u
                     left join Profile p on p.UserId = u.UserId
                     """;

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

    @Override
    public boolean insert(User user) {
        Connection conn = getConnection();
        if (conn == null) {
            LOG.severe("Cannot insert user: database connection is unavailable.");
            return false;
        }

        String sql = """
                     insert into [User] (Username, Email, PasswordHash, [Role], [Status])
                     values (?, ?, ?, ?, ?)
                     """;

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Registrant";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, roleName);
            ps.setBoolean(5, user.isIsActive());

            if (ps.executeUpdate() == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }

            return user.getId() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to insert user {0}: {1}",
                    new Object[] { user.getEmail(), e.getMessage() });
        }

        return false;
    }

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

    @Override
    public boolean deactivate(int userId) {
        String sql = """
                     update [User]
                     set [Status] = 0
                     where UserId = ?
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to deactivate user {0}: {1}",
                    new Object[] { userId, e.getMessage() });
        }

        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setIsActive(rs.getBoolean("Status"));

        Integer profileId = (Integer) rs.getObject("ProfileId");
        user.setProfileId(profileId);

        String roleName = rs.getString("Role");
        Role role = Db2Mappings.roleFromName(roleName);
        user.setRole(role);
        user.setRoleId(role.getId());

        if (profileId != null) {
            Profile profile = new Profile();
            profile.setId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            profile.setDateOfBirth(rs.getDate("DateOfBirth"));
            profile.setPhoneNo(rs.getString("PhoneNumber"));
            profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setGender(Db2Mappings.genderFromSex(rs.getString("Sex")));
            user.setProfile(profile);
        }

        return user;
    }
}
