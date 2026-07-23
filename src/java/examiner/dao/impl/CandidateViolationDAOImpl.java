package examiner.dao.impl;

import examiner.dao.CandidateViolationDAO;
import shared.dbconnection.DBContext;
import shared.model.CandidateViolation;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
