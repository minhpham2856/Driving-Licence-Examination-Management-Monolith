package DAO.Impl;

import DBConnection.DBContext;
import DAO.UserDAO;
import Models.User;
import Models.Person;
import Models.Role;
import java.sql.*;

public class UserDAOImpl extends DBContext implements UserDAO {

    @Override
    public User getById(int id) {
        String sql = """
                     select u.*, 
                     	p.govIdNo, 
                     	p.fullName, 
                     	p.dateOfBirth, 
                     	p.gender, 
                     	p.phoneNo, 
                     	p.email, 
                     	p.address, 
                     	p.photoUrl, 
                     	p.isWalkIn, 
                     	p.createdAt p_createdAt, 
                     	p.updatedAt p_updatedAt, 
                     	p.approvalStatus, 
                     	p.rejectionReason, 
                     	r.roleName 
                     from User u 
                     join Person p on u.personId = p.id 
                     join Role r on u.roleId = r.id 
                     where u.id = ?
                     """;

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
        String sql = """
                     select u.*, 
                     	p.govIdNo, 
                     	p.fullName, 
                     	p.dateOfBirth, 
                     	p.gender, 
                     	p.phoneNo, 
                     	p.email, 
                     	p.address, 
                     	p.photoUrl, 
                     	p.isWalkIn, 
                     	p.createdAt p_createdAt, 
                     	p.updatedAt p_updatedAt, 
                     	p.approvalStatus, 
                     	p.rejectionReason, 
                     	r.roleName 
                     from User u 
                     join Person p on u.personId = p.id 
                     join Role r on u.roleId = r.id 
                     where u.username = ?
                     """;

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
        String sql = """
                     select u.*, 
                     	p.govIdNo, 
                     	p.fullName, 
                     	p.dateOfBirth, 
                     	p.gender, 
                     	p.phoneNo, 
                     	p.email, 
                     	p.address, 
                     	p.photoUrl, 
                     	p.isWalkIn, 
                     	p.createdAt p_createdAt, 
                     	p.updatedAt p_updatedAt, 
                     	p.approvalStatus, 
                     	p.rejectionReason, 
                     	r.roleName 
                     from User u 
                     join Person p on u.personId = p.id 
                     join Role r on u.roleId = r.id 
                     where u.username = ? or p.email = ? or p.phoneNo = ?
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

    @Override
    public boolean insert(User user) {
        String sql = """
                     insert into User (personId, username, passwordHash, roleId, isActive, lastLoginAt) 
                     values (?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user.getPersonId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getRoleId());
            ps.setBoolean(5, user.isIsActive());

            if (user.getLastLoginAt() == null) {
                ps.setNull(6, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, user.getLastLoginAt());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updatePassword(int userId, String passwordHash) {
        String sql = """
                     update User 
                     set passwordHash = ? 
                     where id = ?
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

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setPersonId(rs.getInt("personId"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setRoleId(rs.getInt("roleId"));
        user.setIsActive(rs.getBoolean("isActive"));
        user.setLastLoginAt(rs.getTimestamp("lastLoginAt"));
        user.setCreatedAt(rs.getTimestamp("createdAt"));

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
        person.setCreatedAt(rs.getTimestamp("p_createdAt"));
        person.setUpdatedAt(rs.getTimestamp("p_updatedAt"));
        person.setApprovalStatus(rs.getString("approvalStatus"));
        person.setRejectionReason(rs.getString("rejectionReason"));
        user.setPerson(person);

        Role role = new Role();

        role.setId(rs.getInt("roleId"));
        role.setRoleName(rs.getString("roleName"));
        user.setRole(role);

        return user;
    }
}
