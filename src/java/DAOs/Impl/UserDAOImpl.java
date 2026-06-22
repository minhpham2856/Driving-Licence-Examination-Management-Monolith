package DAOs.Impl;

import Utils.ExamConstants;
import DBConnection.DBContext;
import DAOs.UserDAO;
import Models.Profile;
import Models.Role;
import Models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAOImpl implements UserDAO {

    private final DBContext ctx;
    private static final String USER_SELECT = """
                     select u.UserId,
                     	u.Username,
                     	u.Email,
                     	u.PasswordHash,
                     	r.RoleName as [Role],
                     	u.[Status],
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

    public UserDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public User getById(int id) {
        String sql = USER_SELECT + " where u.UserId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
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

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Looks up a user by any of: Username, Email, PhoneNumber, or GovernmentIdNumber.
    @Override
    public User getByIdentifier(String identifier) {
        String sql = USER_SELECT + """
                 where u.Username = ?
                    or u.Email = ?
                    or p.PhoneNumber = ?
                    or p.GovernmentIdNumber = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ps.setString(4, identifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
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

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(User user) {
        Connection conn = ctx.getConnection();
        if (conn == null) {
            return false;
        }

        String sql = """
                     insert into [User] (Username, Email, PasswordHash, RoleId, [Status])
                     values (?, ?, ?, ?, ?)
                     """;

        // Defaults role to registrant if none provided
        int roleId = user.getRole() != null ? user.getRole().getRoleId() : user.getRoleId();
        if (roleId <= 0) {
            roleId = ExamConstants.roleIdFromName("Registrant");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, roleId);
            ps.setBoolean(5, user.isStatus());

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
            e.printStackTrace();
        }

        return false;
    }

    // Updates the password hash
    @Override
    public boolean updatePassword(int userId, String passwordHash) {
        String sql = """
                     update [User]
                     set PasswordHash = ?
                     where UserId = ?
                     """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Maps ResultSet to model
    private User mapToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setUserId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setStatus(rs.getBoolean("Status"));

        Integer profileId = (Integer) rs.getObject("ProfileId");

        String roleName = rs.getString("Role");
        Role role = ExamConstants.roleFromName(roleName);
        user.setRole(role);
        user.setRoleId(role.getRoleId());

        if (profileId != null) {
            Profile profile = new Profile();
            profile.setProfileId(profileId);
            profile.setUserId(rs.getInt("UserId"));
            profile.setFullName(rs.getString("FullName"));
            profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
            profile.setPhoneNo(rs.getString("PhoneNumber"));
            profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
            profile.setAddress(rs.getString("Address"));
            profile.setGender(ExamConstants.genderFromSex(rs.getString("Sex")));
            user.setProfile(profile);
        }

        return user;
    }
}
