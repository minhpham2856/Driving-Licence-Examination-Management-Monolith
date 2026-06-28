package dao.impl;

import dao.ExamCandidateDAO;
import dbconnection.DBContext;
import model.exam.ExamCandidate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExamCandidateDAOImpl extends DBContext implements ExamCandidateDAO {
    @Override
    public ExamCandidate findBySessionAndCandidate(int sessionId, int candidateId) {
        String sql = "SELECT * FROM Exam_Candidate WHERE SessionId = ? AND CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExamCandidate ec = new ExamCandidate();
                    ec.setExamCandidateId(rs.getInt("ExamCandidateId"));
                    ec.setExamId(rs.getInt("ExamId"));
                    ec.setCandidateId(rs.getInt("CandidateId"));
                    ec.setSessionId(rs.getInt("SessionId"));
                    return ec;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
