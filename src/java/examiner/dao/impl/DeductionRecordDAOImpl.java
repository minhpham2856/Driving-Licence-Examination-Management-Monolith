package examiner.dao.impl;

import examiner.dao.DeductionRecordDAO;
import shared.dbconnection.DBContext;
import shared.model.DeductionRecord;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// JDBC implementation for DeductionRecord; examiner module DAO layer only.
public class DeductionRecordDAOImpl extends DBContext implements DeductionRecordDAO {

    // Returns occurrence count for one exam score and deduction rule pair.
    @Override
    public int getOccurrenceCount(int examScoreId, int scoreDeductionId) {
        String sql = "SELECT OccurrenceCount FROM DeductionRecord WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examScoreId);
            ps.setInt(2, scoreDeductionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("OccurrenceCount");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Inserts a new deduction occurrence row.
    @Override
    public boolean add(DeductionRecord record) {
        String sql = "INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, record.getExamScoreId());
            ps.setInt(2, record.getScoreDeductionId());
            ps.setInt(3, record.getOccurrenceCount());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates occurrence count for one score/rule pair.
    @Override
    public boolean updateOccurrence(int examScoreId, int scoreDeductionId, int occurrenceCount) {
        String sql = "UPDATE DeductionRecord SET OccurrenceCount = ? "
                + "WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, occurrenceCount);
            ps.setInt(2, examScoreId);
            ps.setInt(3, scoreDeductionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Deletes the deduction row for one exam score and rule.
    @Override
    public boolean deleteByExamScoreAndRule(int examScoreId, int scoreDeductionId) {
        String sql = "DELETE FROM DeductionRecord WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examScoreId);
            ps.setInt(2, scoreDeductionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
