package dao.impl;
import dao.CandidateAnswerDAO;
import dbconnection.DBContext;
import model.CandidateAnswer;
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
}
