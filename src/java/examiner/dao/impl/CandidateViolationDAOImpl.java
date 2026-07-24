package examiner.dao.impl;

import examiner.dao.CandidateViolationDAO;
import shared.dbconnection.DBContext;
import shared.model.CandidateViolation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CandidateViolationDAOImpl extends DBContext implements CandidateViolationDAO {
    @Override
    public boolean addAndSuspend(int candidateId, CandidateViolation violation) {
        if (candidateId <= 0 || violation == null || violation.getExamEnrollmentSectionId() <= 0
                || violation.getCreatedBy() <= 0 || violation.getReason() == null) {
            return false;
        }
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO CandidateViolation "
                    + "(ExamEnrollmentSectionId, Reason, Details, EvidenceUrl, CreatedBy) VALUES (?, ?, ?, ?, ?)");
                    PreparedStatement suspend = connection.prepareStatement(
                            "UPDATE Candidate SET IsSuspended = 1 WHERE CandidateId = ?")) {
                insert.setInt(1, violation.getExamEnrollmentSectionId());
                insert.setString(2, violation.getReason());
                insert.setString(3, violation.getDetails());
                insert.setString(4, violation.getEvidenceUrl());
                insert.setInt(5, violation.getCreatedBy());
                suspend.setInt(1, candidateId);
                if (insert.executeUpdate() == 1 && suspend.executeUpdate() == 1) {
                    connection.commit();
                    return true;
                }
                connection.rollback();
            }
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            ex.printStackTrace();
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
        return false;
    }

    @Override
    public CandidateViolation getLatestByExamAndSbd(int examId, int sbd, String sectionType) {
        if (examId <= 0 || sbd <= 0 || sectionType == null || sectionType.isBlank()) {
            return null;
        }
        String sql = "SELECT TOP 1 cv.CandidateViolationId, cv.ExamEnrollmentSectionId, "
                + "cv.Reason, cv.Details, cv.EvidenceUrl, cv.CreatedBy, cv.CreatedAt "
                + "FROM CandidateViolation cv "
                + "JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentSectionId = cv.ExamEnrollmentSectionId "
                + "JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId "
                + "JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId "
                + "JOIN Candidate c ON c.CandidateId = ee.CandidateId "
                + "WHERE ee.ExamId = ? "
                + "  AND TRY_CAST(c.CandidateNumber AS INT) = ? "
                + "  AND es.SectionType = ? "
                + "ORDER BY cv.CreatedAt DESC, cv.CandidateViolationId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, sbd);
            ps.setString(3, sectionType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CandidateViolation violation = new CandidateViolation();
                    violation.setCandidateViolationId(rs.getInt("CandidateViolationId"));
                    violation.setExamEnrollmentSectionId(rs.getInt("ExamEnrollmentSectionId"));
                    violation.setReason(rs.getString("Reason"));
                    violation.setDetails(rs.getString("Details"));
                    violation.setEvidenceUrl(rs.getString("EvidenceUrl"));
                    violation.setCreatedBy(rs.getInt("CreatedBy"));
                    violation.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    return violation;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
