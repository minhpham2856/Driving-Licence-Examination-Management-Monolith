package DAO.Impl;

import DBConnection.DBContext;
import DAO.PersonDAO;
import Models.Person;
import java.sql.*;

public class PersonDAOImpl extends DBContext implements PersonDAO {

    @Override
    public Person getById(int id) {
        String sql = """
                     select * from Person where id = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByEmail(String email) {
        String sql = """
                     select * from Person where email = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByGovIdNo(String govIdNo) {
        String sql = """
                     select * from Person where govIdNo = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, govIdNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person getByPhoneNo(String phoneNo) {
        String sql = """
                     select * from Person where phoneNo = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPerson(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Person person) {
        String sql = """
                     insert into Person (govIdNo, fullName, dateOfBirth, gender, phoneNo, email, address, photoUrl, isWalkIn, approvalStatus, rejectionReason) 
                     values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            if (person.getGovIdNo() == null) {
                ps.setNull(1, Types.NVARCHAR);
            } else {
                ps.setString(1, person.getGovIdNo());
            }

            ps.setString(2, person.getFullName());
            ps.setDate(3, person.getDateOfBirth());
            ps.setBoolean(4, person.isGender());
            ps.setString(5, person.getPhoneNo());

            if (person.getEmail() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, person.getEmail());
            }

            if (person.getAddress() == null) {
                ps.setNull(7, Types.NVARCHAR);
            } else {
                ps.setString(7, person.getAddress());
            }

            if (person.getPhotoUrl() == null) {
                ps.setNull(8, Types.NVARCHAR);
            } else {
                ps.setString(8, person.getPhotoUrl());
            }

            ps.setBoolean(9, person.isIsWalkIn());
            ps.setString(10, person.getApprovalStatus() != null ? person.getApprovalStatus() : "Pending");

            if (person.getRejectionReason() == null) {
                ps.setNull(11, Types.NVARCHAR);
            } else {
                ps.setString(11, person.getRejectionReason());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    person.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Person person) {
        String sql = """
                     update Person 
                     set govIdNo = ?, fullName = ?, dateOfBirth = ?, gender = ?, phoneNo = ?, email = ?, address = ?, photoUrl = ?, isWalkIn = ?, updatedAt = getutcdate(), approvalStatus = ?, rejectionReason = ? 
                     where id = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (person.getGovIdNo() == null) {
                ps.setNull(1, Types.NVARCHAR);
            } else {
                ps.setString(1, person.getGovIdNo());
            }

            ps.setString(2, person.getFullName());
            ps.setDate(3, person.getDateOfBirth());
            ps.setBoolean(4, person.isGender());
            ps.setString(5, person.getPhoneNo());

            if (person.getEmail() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, person.getEmail());
            }

            if (person.getAddress() == null) {
                ps.setNull(7, Types.NVARCHAR);
            } else {
                ps.setString(7, person.getAddress());
            }

            if (person.getPhotoUrl() == null) {
                ps.setNull(8, Types.NVARCHAR);
            } else {
                ps.setString(8, person.getPhotoUrl());
            }

            ps.setBoolean(9, person.isIsWalkIn());
            ps.setString(10, person.getApprovalStatus());

            if (person.getRejectionReason() == null) {
                ps.setNull(11, Types.NVARCHAR);
            } else {
                ps.setString(11, person.getRejectionReason());
            }

            ps.setInt(12, person.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updatePhotoUrl(int personId, String photoUrl) {
        String sql = """
                     update Person
                     set photoUrl = ?, updatedAt = getutcdate()
                     where id = ?
                     """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                if (photoUrl == null) {
                    ps.setNull(1, Types.NVARCHAR);
                } else {
                    ps.setString(1, photoUrl);
                }
                ps.setInt(2, personId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean markPendingReview(int personId) {
        String sql = """
                     update Person
                     set approvalStatus = 'Pending', rejectionReason = null, updatedAt = getutcdate()
                     where id = ?
                     """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        Person person = new Person();

        person.setId(rs.getInt("id"));
        person.setGovIdNo(rs.getString("govIdNo"));
        person.setFullName(rs.getString("fullName"));
        person.setDateOfBirth(rs.getDate("dateOfBirth"));
        person.setGender(rs.getBoolean("gender"));
        person.setPhoneNo(rs.getString("phoneNo"));
        person.setEmail(rs.getString("email"));
        person.setAddress(rs.getString("address"));
        person.setPhotoUrl(rs.getString("photoUrl"));
        person.setIsWalkIn(rs.getBoolean("isWalkIn"));
        person.setCreatedAt(rs.getTimestamp("createdAt"));
        person.setUpdatedAt(rs.getTimestamp("updatedAt"));
        person.setApprovalStatus(rs.getString("approvalStatus"));
        person.setRejectionReason(rs.getString("rejectionReason"));

        return person;
    }
}
