package examiner.dao.impl;

import examiner.dao.DeductionRecordDAO;
import shared.dbconnection.DBContext;
import shared.model.DeductionRecord;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeductionRecordDAOImpl extends DBContext implements DeductionRecordDAO {

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

    @Override
    public boolean add(DeductionRecord record) {
        String sql = "INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, record.getExamScoreId());
            ps.setInt(2, record.getScoreDeductionId());
            ps.setInt(3, record.getOccurrenceCount());
            ps.setTimestamp(4, record.getRecordedAt());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateOccurrence(int examScoreId, int scoreDeductionId, int occurrenceCount, Timestamp recordedAt) {
        String sql = "UPDATE DeductionRecord SET OccurrenceCount = ?, RecordedAt = ? "
                + "WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, occurrenceCount);
            ps.setTimestamp(2, recordedAt);
            ps.setInt(3, examScoreId);
            ps.setInt(4, scoreDeductionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    @Override
    public List<Map<String, Object>> getTopReasons(int limit) {
        int safeLimit = limit > 0 ? limit : 5;
        String sql = "SELECT TOP (?) sd.Reason, COUNT(*) AS TotalCount "
                + "FROM DeductionRecord dr "
                + "INNER JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId "
                + "GROUP BY sd.Reason "
                + "ORDER BY TotalCount DESC";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("reason", rs.getString("Reason"));
                    row.put("count", rs.getInt("TotalCount"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}

