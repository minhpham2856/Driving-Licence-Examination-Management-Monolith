package DAO.Impl;

import Constants.Db2Mappings;
import DBConnection.DBContext;
import DAO.PersonDAO;
import Models.Person;
import java.sql.*;

public class PersonDAOImpl extends DBContext implements PersonDAO {

    private static final String PERSON_SELECT = """
            SELECT p.ProfileId AS id,
                   p.GovernmentIdNumber AS govIdNo,
                   p.FullName AS fullName,
                   CAST(p.DateOfBirth AS DATE) AS dateOfBirth,
                   CASE WHEN p.Sex IN (N'Nam', N'Male', N'M') THEN 0 ELSE 1 END AS gender,
                   p.PhoneNumber AS phoneNo,
                   u.Email AS email,
                   p.Address AS address,
                   c.PhotoImageUrl AS photoUrl,
                   CASE WHEN er.RegistrationStatus = 'WalkIn' THEN 1 ELSE 0 END AS isWalkIn,
                   CAST(NULL AS DATETIME) AS createdAt,
                   CAST(NULL AS DATETIME) AS updatedAt,
                   ISNULL(er.RegistrationStatus, N'Approved') AS approvalStatus,
                   NULL AS rejectionReason
            FROM Profile p
            JOIN [User] u ON u.UserId = p.UserId
            LEFT JOIN (
                SELECT er1.ProfileId, er1.RegistrationStatus,
                       ROW_NUMBER() OVER (PARTITION BY er1.ProfileId ORDER BY er1.ExamRegistrationId DESC) AS rn
                FROM ExamRegistration er1
            ) er ON er.ProfileId = p.ProfileId AND er.rn = 1
            LEFT JOIN (
                SELECT c1.UserId, c1.PhotoImageUrl,
                       ROW_NUMBER() OVER (PARTITION BY c1.UserId ORDER BY c1.CandidateId DESC) AS rn
                FROM Candidate c1
            ) c ON c.UserId = p.UserId AND c.rn = 1
            """;

    @Override
    public Person getById(int id) {
        String sql = PERSON_SELECT + " WHERE p.ProfileId = ?";
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
        String sql = PERSON_SELECT + " WHERE u.Email = ?";
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
        String sql = PERSON_SELECT + " WHERE p.GovernmentIdNumber = ?";
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
    public boolean insert(Person person) {
        String sqlUser = """
                INSERT INTO [User] (Username, Email, PasswordHash, [Role], [Status])
                VALUES (?, ?, 'e10adc3949ba59abbe56e057f20f883e', 'Candidate', 1)
                """;
        String sqlProfile = """
                INSERT INTO Profile (FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address, UserId)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            connection.setAutoCommit(false);
            String username = person.getGovIdNo() != null ? person.getGovIdNo() : ("walkin_" + System.currentTimeMillis());
            String email = person.getEmail() != null ? person.getEmail() : (username + "@walkin.local");
            int userId;
            try (PreparedStatement ps = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) {
                        connection.rollback();
                        return false;
                    }
                    userId = gk.getInt(1);
                }
            }
            try (PreparedStatement ps = connection.prepareStatement(sqlProfile, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, person.getFullName());
                ps.setDate(2, person.getDateOfBirth());
                ps.setString(3, person.getPhoneNo());
                ps.setString(4, Db2Mappings.sexFromGender(person.isGender()));
                ps.setString(5, person.getGovIdNo());
                ps.setString(6, person.getAddress());
                ps.setInt(7, userId);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        person.setId(gk.getInt(1));
                    }
                }
            }
            connection.commit();
            return person.getId() > 0;
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
    public boolean update(Person person) {
        String sqlProfile = """
                UPDATE Profile
                SET GovernmentIdNumber = ?, FullName = ?, DateOfBirth = ?, Sex = ?,
                    PhoneNumber = ?, Address = ?
                WHERE ProfileId = ?
                """;
        String sqlUser = "UPDATE [User] SET Email = ? WHERE UserId = (SELECT UserId FROM Profile WHERE ProfileId = ?)";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sqlProfile)) {
                ps.setString(1, person.getGovIdNo());
                ps.setString(2, person.getFullName());
                ps.setDate(3, person.getDateOfBirth());
                ps.setString(4, Db2Mappings.sexFromGender(person.isGender()));
                ps.setString(5, person.getPhoneNo());
                ps.setString(6, person.getAddress());
                ps.setInt(7, person.getId());
                ps.executeUpdate();
            }
            if (person.getEmail() != null) {
                try (PreparedStatement ps = connection.prepareStatement(sqlUser)) {
                    ps.setString(1, person.getEmail());
                    ps.setInt(2, person.getId());
                    ps.executeUpdate();
                }
            }
            if (person.getPhotoUrl() != null) {
                String sqlPhoto = """
                        UPDATE Candidate SET PhotoImageUrl = ?
                        WHERE CandidateId = (
                            SELECT TOP 1 c.CandidateId FROM Candidate c
                            JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                            WHERE er.ProfileId = ? ORDER BY c.CandidateId DESC
                        )
                        """;
                try (PreparedStatement ps = connection.prepareStatement(sqlPhoto)) {
                    ps.setString(1, person.getPhotoUrl());
                    ps.setInt(2, person.getId());
                    ps.executeUpdate();
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
        Timestamp created = rs.getTimestamp("createdAt");
        person.setCreatedAt(rs.wasNull() ? null : created);
        Timestamp updated = rs.getTimestamp("updatedAt");
        person.setUpdatedAt(rs.wasNull() ? null : updated);
        person.setApprovalStatus(rs.getString("approvalStatus"));
        person.setRejectionReason(rs.getString("rejectionReason"));
        return person;
    }
}
