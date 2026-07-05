package dao.impl;

import dao.ExamRegistrationDAO;
import dbconnection.DBContext;
import model.ExamRegistration;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    private static final String BASE_SELECT =
            "SELECT ExamRegistrationId, RegistrationStatus, Notes, ProfileId, LicenceId FROM ExamRegistration";

    @Override
    public ExamRegistration getById(int examRegistrationId) {
        String sql = BASE_SELECT + " WHERE ExamRegistrationId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examRegistrationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getLatestIdByProfileAndLicence(int profileId, int licenceId) {
        String sql = "SELECT TOP 1 ExamRegistrationId FROM ExamRegistration "
                + "WHERE ProfileId = ? AND LicenceId = ? ORDER BY ExamRegistrationId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int add(ExamRegistration registration) {
        String sql = "INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, registration.getRegistrationStatus());
            ps.setString(2, registration.getNotes());
            ps.setInt(3, registration.getProfileId());
            ps.setInt(4, registration.getLicenceId());
            if (ps.executeUpdate() == 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean update(ExamRegistration registration) {
        String sql = "UPDATE ExamRegistration SET RegistrationStatus = ?, Notes = ? WHERE ExamRegistrationId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, registration.getRegistrationStatus());
            ps.setString(2, registration.getNotes());
            ps.setInt(3, registration.getExamRegistrationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStatusWithReviewNote(int examRegistrationId, String status, String message, int actorUserId) {
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?,
                    Notes = CONCAT(
                        CASE WHEN Notes IS NULL OR Notes = '' THEN '' ELSE Notes + ';' END,
                        'MESSAGE=', ?, ';REVIEWED_BY=', ?
                    )
                WHERE ExamRegistrationId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, message == null ? "" : message.replace(";", ","));
            ps.setInt(3, actorUserId);
            ps.setInt(4, examRegistrationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static ExamRegistration map(ResultSet rs) throws SQLException {
        ExamRegistration registration = new ExamRegistration();
        registration.setExamRegistrationId(rs.getInt("ExamRegistrationId"));
        registration.setRegistrationStatus(rs.getString("RegistrationStatus"));
        registration.setNotes(rs.getString("Notes"));
        registration.setProfileId(rs.getInt("ProfileId"));
        registration.setLicenceId(rs.getInt("LicenceId"));
        return registration;
    }
}
