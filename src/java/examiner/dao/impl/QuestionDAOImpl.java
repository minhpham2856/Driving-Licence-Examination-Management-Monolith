package examiner.dao.impl;
import examiner.dao.QuestionDAO;
import dbconnection.DBContext;
import examiner.model.Question;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class QuestionDAOImpl extends DBContext implements QuestionDAO {
    @Override
    public List<Question> findByIds(List<Integer> questionIds) {
        List<Question> list = new ArrayList<>();
        if (questionIds == null || questionIds.isEmpty()) return list;
        StringBuilder sql = new StringBuilder("SELECT * FROM Question WHERE QuestionId IN (");
        for (int i = 0; i < questionIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < questionIds.size(); i++) {
                ps.setInt(i + 1, questionIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("QuestionId"));
                    q.setQuestionNumber(rs.getInt("QuestionNumber"));
                    q.setImageUrl(rs.getString("ImageUrl"));
                    q.setCorrectAnswer(rs.getString("CorrectAnswer"));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public List<Question> findAll() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM Question";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Question q = new Question();
                q.setQuestionId(rs.getInt("QuestionId"));
                q.setQuestionNumber(rs.getInt("QuestionNumber"));
                q.setImageUrl(rs.getString("ImageUrl"));
                q.setCorrectAnswer(rs.getString("CorrectAnswer"));
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
