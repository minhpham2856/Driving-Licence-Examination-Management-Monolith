package dao.impl;

import dao.TheoryPaperDAO;
import dbconnection.DBContext;
import model.exam.TheoryPaper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TheoryPaperDAOImpl extends DBContext implements TheoryPaperDAO {

    @Override
    public TheoryPaper findByExamEnrollmentId(int examEnrollmentId) {
        String sql = "SELECT * FROM TheoryPaper WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TheoryPaper tp = new TheoryPaper();
                    tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    tp.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    tp.setExamDeviceId(rs.getInt("ExamDeviceId"));
                    tp.setStartedAt(rs.getTimestamp("StartedAt"));
                    tp.setSubmittedAt(rs.getTimestamp("FinishedAt"));
                    return tp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<TheoryPaper> findByExamEnrollmentIds(List<Integer> examEnrollmentIds) {
        List<TheoryPaper> list = new java.util.ArrayList<>();
        if (examEnrollmentIds == null || examEnrollmentIds.isEmpty()) {
            return list;
        }

        StringBuilder sb = new StringBuilder("SELECT * FROM TheoryPaper WHERE ExamEnrollmentId IN (");
        for (int i = 0; i < examEnrollmentIds.size(); i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")");

        try (PreparedStatement ps = getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < examEnrollmentIds.size(); i++) {
                ps.setInt(i + 1, examEnrollmentIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TheoryPaper tp = new TheoryPaper();
                    tp.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    tp.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    tp.setExamDeviceId(rs.getInt("ExamDeviceId"));
                    tp.setStartedAt(rs.getTimestamp("StartedAt"));
                    tp.setSubmittedAt(rs.getTimestamp("FinishedAt"));
                    list.add(tp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
