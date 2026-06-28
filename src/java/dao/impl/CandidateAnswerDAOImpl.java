package dao.impl;

import dao.CandidateAnswerDAO;
import dbconnection.DBContext;
import model.exam.CandidateAnswer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CandidateAnswerDAOImpl extends DBContext implements CandidateAnswerDAO {
    @Override
    public List<CandidateAnswer> findByTheoryPaperId(int theoryPaperId) {
        List<CandidateAnswer> list = new ArrayList<>();
        String sql = "SELECT * FROM CandidateAnswer WHERE TheoryPaperId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, theoryPaperId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CandidateAnswer ca = new CandidateAnswer();
                    ca.setCandidateAnswerId(rs.getInt("CandidateAnswerId"));
                    ca.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    ca.setQuestionId(rs.getInt("QuestionId"));
                    ca.setAnswer(rs.getString("Answer"));
                    list.add(ca);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<CandidateAnswer> findByTheoryPaperIds(List<Integer> theoryPaperIds) {
        List<CandidateAnswer> list = new ArrayList<>();
        if (theoryPaperIds == null || theoryPaperIds.isEmpty()) return list;

        StringBuilder sb = new StringBuilder("SELECT ca.TheoryPaperId, ca.Answer, q.CorrectAnswer FROM CandidateAnswer ca ");
        sb.append("LEFT JOIN Question q ON ca.QuestionId = q.QuestionId ");
        sb.append("WHERE ca.TheoryPaperId IN (");
        for (int i = 0; i < theoryPaperIds.size(); i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")");

        try (PreparedStatement ps = getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < theoryPaperIds.size(); i++) {
                ps.setInt(i + 1, theoryPaperIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CandidateAnswer ca = new CandidateAnswer();
                    ca.setTheoryPaperId(rs.getInt("TheoryPaperId"));
                    ca.setAnswer(rs.getString("Answer"));
                    
                    
                    
                    
                    
                    String ans = ca.getAnswer();
                    String correctAns = rs.getString("CorrectAnswer");
                    
                    boolean isCorrect = ans != null && correctAns != null && ans.equals(correctAns);
                    
                    
                    
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
