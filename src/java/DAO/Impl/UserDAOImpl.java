package DAO.Impl;

import DBConnection.DBContext;
import DAO.UserDAO;
import Models.Person;
import Models.Role;
import Models.User;
import java.sql.*;

public class UserDAOImpl extends DBContext implements UserDAO {

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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = USER_SELECT + " where u.Username = ? or u.Email = ? or p.PhoneNumber = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    @Override
    public User getByEmail(String email) {
        String sql = USER_SELECT + " where u.Email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = """
                     insert into [User] (Username, Email, PasswordHash, [Role], [Status])
                     values (?, ?, ?, ?, ?)
                     """;

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Registrant";

        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"UserId"})) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, roleName);
            ps.setBoolean(5, user.isIsActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
        user.setPersonId(profileId);

        Role role = new Role();
        role.setRoleName(rs.getString("Role"));
        user.setRole(role);

        if (profileId != null) {
            Person person = new Person();
            person.setId(profileId);
            person.setUserId(rs.getInt("UserId"));
            person.setFullName(rs.getString("FullName"));
            person.setDateOfBirth(rs.getDate("DateOfBirth"));
            person.setPhoneNo(rs.getString("PhoneNumber"));
            person.setGovIdNo(rs.getString("GovernmentIdNumber"));
            person.setAddress(rs.getString("Address"));
            person.setEmail(user.getEmail());
            person.setGender(mapSexToGender(rs.getString("Sex")));
            user.setPerson(person);
        }

        return user;
    }

    private boolean mapSexToGender(String sex) {
        return sex != null && "Nữ".equalsIgnoreCase(sex.trim());
    }
}
