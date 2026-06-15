package DAO.Impl;

import Constants.Db2Mappings;
import DBConnection.DBContext;
import DAO.WalkInCandidateDAO;
import Models.Person;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WalkInCandidateDAOImpl extends DBContext implements WalkInCandidateDAO {

    @Override
    public boolean insertWalkIn(Person person) {
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
            String username = person.getGovIdNo() != null && !person.getGovIdNo().isBlank()
                    ? person.getGovIdNo()
                    : ("walkin_" + System.currentTimeMillis());
            String email = person.getEmail() != null && !person.getEmail().isBlank()
                    ? person.getEmail()
                    : (username + "@walkin.local");

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
                        person.setUserId(userId);
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
}
