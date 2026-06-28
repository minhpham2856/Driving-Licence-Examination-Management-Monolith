package dao.impl;


import dao.TheoryPaperDAO;

import dbconnection.DBContext;

import dao.TheoryPaperDAO;
import dbconnection.DBContext;
import model.exam.TheoryPaper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of TheoryPaperDAO for retrieving candidate theory paper
 * answers and matching them against correct answers for scoring.
 */
public class TheoryPaperDAOImpl extends DBContext implements TheoryPaperDAO {

    @Override
    public TheoryPaper findByExamCandidateId(int examCandidateId) {
        String sql = "SELECT * FROM TheoryPaper WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TheoryPaper tp = new TheoryPaper();
                    tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    tp.setExamCandidateId(rs.getInt("ExamCandidateId"));
                    tp.setExamDeviceId(rs.getInt("ExamDeviceId"));
                    tp.setStartedAt(rs.getTimestamp("StartedAt"));
                    tp.setFinishedAt(rs.getTimestamp("FinishedAt"));
                    return tp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
