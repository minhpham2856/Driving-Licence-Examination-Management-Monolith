package examiner.dao.impl;

import examiner.dao.ExamScoreDAO;
import dbconnection.DBContext;
import examiner.model.ExamScore;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExamScoreDAOImpl extends DBContext implements ExamScoreDAO {

    private static final String BASE_SELECT =
            "SELECT ExamScoreId, ExamResultId, ExamSectionId, Score FROM ExamScore";

    @Override
    public ExamScore getByExamResultAndSection(int examResultId, int examSectionId) {
        String sql = BASE_SELECT + " WHERE ExamResultId = ? AND ExamSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examResultId);
            ps.setInt(2, examSectionId);
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
    public int add(ExamScore score) {
        String sql = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, score.getExamResultId());
            ps.setInt(2, score.getExamSectionId());
            ps.setDouble(3, score.getScore());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean updateScore(int examScoreId, double score) {
        String sql = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDouble(1, score);
            ps.setInt(2, examScoreId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean recalculateFromDeductions(int examScoreId) {
        String sql = "SELECT SUM(sd.Points * dr.OccurrenceCount) AS totalDeduction, "
                + "MAX(CASE WHEN sd.IsCritical = 1 AND dr.OccurrenceCount > 0 THEN 1 ELSE 0 END) AS hasCritical "
                + "FROM DeductionRecord dr "
                + "JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId "
                + "WHERE dr.ExamScoreId = ?";
        double totalDeduction = 0;
        boolean hasCritical = false;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examScoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDeduction = rs.getDouble("totalDeduction");
                    hasCritical = rs.getBoolean("hasCritical");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        double score = hasCritical ? 0 : Math.max(0, 100 - totalDeduction);
        return updateScore(examScoreId, score);
    }

    private static ExamScore map(ResultSet rs) throws SQLException {
        ExamScore score = new ExamScore();
        score.setExamScoreId(rs.getInt("ExamScoreId"));
        score.setExamResultId(rs.getInt("ExamResultId"));
        score.setExamSectionId(rs.getInt("ExamSectionId"));
        score.setScore(rs.getDouble("Score"));
        return score;
    }
}
