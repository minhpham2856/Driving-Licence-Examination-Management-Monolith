package DAO.Impl;

import Constants.Db2Mappings;
import DBConnection.DBContext;
import DAO.UserDAO;
import Models.Person;
import Models.Role;
import Models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl extends DBContext implements UserDAO {

    private static final String USER_SELECT = """
            SELECT u.UserId AS id,
                   p.ProfileId AS personId,
                   u.Username AS username,
                   u.PasswordHash AS passwordHash,
                   u.[Role] AS roleName,
                   u.[Status] AS isActive,
                   CAST(NULL AS DATETIME) AS lastLoginAt,
                   CAST(NULL AS DATETIME) AS createdAt,
                   p.GovernmentIdNumber AS govIdNo,
                   p.FullName AS fullName,
                   CAST(p.DateOfBirth AS DATE) AS dateOfBirth,
                   CASE WHEN p.Sex IN (N'Nam', N'Male', N'M') THEN 0 ELSE 1 END AS gender,
                   p.PhoneNumber AS phoneNo,
                   u.Email AS email,
                   p.Address AS address,
                   photo.PhotoImageUrl AS photoUrl,
                   0 AS isWalkIn,
                   CAST(NULL AS DATETIME) AS p_createdAt,
                   CAST(NULL AS DATETIME) AS p_updatedAt,
                   N'Approved' AS approvalStatus,
                   NULL AS rejectionReason
            FROM [User] u
            JOIN Profile p ON p.UserId = u.UserId
            LEFT JOIN (
                SELECT c.UserId, c.PhotoImageUrl,
                       ROW_NUMBER() OVER (PARTITION BY c.UserId ORDER BY c.CandidateId DESC) AS rn
                FROM Candidate c
            ) photo ON photo.UserId = u.UserId AND photo.rn = 1
            """;

    @Override
    public User getById(int id) {
        String sql = USER_SELECT + " WHERE u.UserId = ?";
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
        String sql = USER_SELECT + " WHERE u.Username = ?";
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
        String sql = USER_SELECT + " WHERE u.Username = ? OR u.Email = ? OR p.PhoneNumber = ?";
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
    public boolean insert(User user) {
        String sqlUser = """
                INSERT INTO [User] (Username, Email, PasswordHash, [Role], [Status])
                VALUES (?, ?, ?, ?, ?)
                """;
        String sqlProfile = """
                INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                Role role = user.getRole();
                String roleName = role != null ? role.getRoleName() : "Candidate";
                Person person = user.getPerson();
                ps.setString(1, user.getUsername());
                ps.setString(2, person != null && person.getEmail() != null ? person.getEmail() : user.getUsername() + "@dlem.local");
                ps.setString(3, user.getPasswordHash());
                ps.setString(4, roleName);
                ps.setBoolean(5, user.isIsActive());
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) {
                        connection.rollback();
                        return false;
                    }
                    user.setId(gk.getInt(1));
                }
            }
            if (user.getPerson() != null) {
                Person person = user.getPerson();
                try (PreparedStatement ps = connection.prepareStatement(sqlProfile, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, person.getFullName());
                    ps.setDate(2, person.getDateOfBirth());
                    ps.setString(3, person.getPhoneNo());
                    ps.setString(4, Db2Mappings.sexFromGender(person.isGender()));
                    ps.setString(5, person.getGovIdNo());
                    ps.setString(6, person.getAddress());
                    ps.setInt(7, user.getId());
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            user.setPersonId(gk.getInt(1));
                            person.setId(gk.getInt(1));
                        }
                    }
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public List<User> getByRoleName(String roleName) {
        List<User> list = new ArrayList<>();
        String sql = USER_SELECT + " WHERE u.[Role] = ? AND u.[Status] = 1 ORDER BY p.FullName";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, roleName);
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

    @Override
    public boolean updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE [User] SET PasswordHash = ? WHERE UserId = ?";
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
        String roleName = rs.getString("roleName");
        int roleId = Db2Mappings.roleIdFromName(roleName);

        user.setId(rs.getInt("id"));
        user.setPersonId(rs.getInt("personId"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setRoleId(roleId);
        user.setIsActive(rs.getInt("isActive") == 1);
        Timestamp lastLogin = rs.getTimestamp("lastLoginAt");
        user.setLastLoginAt(rs.wasNull() ? null : lastLogin);
        Timestamp created = rs.getTimestamp("createdAt");
        user.setCreatedAt(rs.wasNull() ? null : created);

        Person person = new Person();
        person.setId(rs.getInt("personId"));
        person.setGovIdNo(rs.getString("govIdNo"));
        person.setFullName(rs.getString("fullName"));
        person.setDateOfBirth(rs.getDate("dateOfBirth"));
        person.setGender(rs.getBoolean("gender"));
        person.setPhoneNo(rs.getString("phoneNo"));
        person.setEmail(rs.getString("email"));
        person.setAddress(rs.getString("address"));
        person.setPhotoUrl(rs.getString("photoUrl"));
        person.setIsWalkIn(rs.getBoolean("isWalkIn"));
        Timestamp pCreated = rs.getTimestamp("p_createdAt");
        person.setCreatedAt(rs.wasNull() ? null : pCreated);
        Timestamp pUpdated = rs.getTimestamp("p_updatedAt");
        person.setUpdatedAt(rs.wasNull() ? null : pUpdated);
        person.setApprovalStatus(rs.getString("approvalStatus"));
        person.setRejectionReason(rs.getString("rejectionReason"));
        user.setPerson(person);

        user.setRole(new Role(roleId, roleName));
        return user;
    }
}
