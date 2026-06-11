package DAO.Impl;

import DBConnection.DBContext;
import DAO.UserDAO;
import Models.User;
import Models.Person;
import Models.Role;
import java.sql.*;

public class UserDAOImpl extends DBContext implements UserDAO {

    private static final String USER_SELECT = """
                     select u.id,
                     	u.personId,
                     	u.username,
                     	u.email,
                     	u.passwordHash,
                     	u.roleId,
                     	u.isActive,
                     	u.lastLoginAt,
                     	u.createdAt,
                     	p.govIdNo,
                     	p.fullName,
                     	p.dateOfBirth,
                     	p.gender,
                     	p.phoneNo,
                     	p.email as personEmail,
                     	p.address,
                     	p.photoUrl,
                     	p.isWalkIn,
                     	p.createdAt p_createdAt,
                     	p.updatedAt p_updatedAt,
                     	p.approvalStatus,
                     	p.rejectionReason,
                     	r.roleName
                     from [User] u
                     left join Person p on u.personId = p.id
                     join Role r on u.roleId = r.id
                     """;

    @Override
    public User getById(int id) {
        String sql = USER_SELECT + " where u.id = ?";

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
        String sql = USER_SELECT + " where u.username = ?";

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
        String sql = USER_SELECT + " where u.username = ? or u.email = ? or p.email = ? or p.phoneNo = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = USER_SELECT + " where u.email = ?";

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
                     insert into [User] (personId, username, email, passwordHash, roleId, isActive, lastLoginAt)
                     values (?, ?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            if (user.getPersonId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, user.getPersonId());
            }
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setInt(5, user.getRoleId());
            ps.setBoolean(6, user.isIsActive());

            if (user.getLastLoginAt() == null) {
                ps.setNull(7, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(7, user.getLastLoginAt());
            }

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
                     set passwordHash = ?
                     where id = ?
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

    @Override
    public boolean updatePersonId(int userId, int personId) {
        String sql = """
                     update [User]
                     set personId = ?
                     where id = ?
                     """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setInt(2, userId);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateActive(int userId, boolean isActive) {
        String sql = """
                     update [User]
                     set isActive = ?
                     where id = ?
                     """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setBoolean(1, isActive);
                ps.setInt(2, userId);

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setPersonId((Integer) rs.getObject("personId"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setRoleId(rs.getInt("roleId"));
        user.setIsActive(rs.getBoolean("isActive"));
        user.setLastLoginAt(rs.getTimestamp("lastLoginAt"));
        user.setCreatedAt(rs.getTimestamp("createdAt"));

        if (user.getPersonId() != null) {
            Person person = new Person();

            person.setId(user.getPersonId());
            person.setGovIdNo(rs.getString("govIdNo"));
            person.setFullName(rs.getString("fullName"));
            person.setDateOfBirth(rs.getDate("dateOfBirth"));
            person.setGender(rs.getBoolean("gender"));
            person.setPhoneNo(rs.getString("phoneNo"));
            person.setEmail(rs.getString("personEmail"));
            person.setAddress(rs.getString("address"));
            person.setPhotoUrl(rs.getString("photoUrl"));
            person.setIsWalkIn(rs.getBoolean("isWalkIn"));
            person.setCreatedAt(rs.getTimestamp("p_createdAt"));
            person.setUpdatedAt(rs.getTimestamp("p_updatedAt"));
            person.setApprovalStatus(rs.getString("approvalStatus"));
            person.setRejectionReason(rs.getString("rejectionReason"));
            user.setPerson(person);
        }

        Role role = new Role();

        role.setId(rs.getInt("roleId"));
        role.setRoleName(rs.getString("roleName"));
        user.setRole(role);

        return user;
    }
}
