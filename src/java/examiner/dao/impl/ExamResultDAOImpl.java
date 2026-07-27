package examiner.dao.impl;

import examiner.dao.ExamResultDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamResult;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

// JDBC implementation for ExamResult; examiner module DAO layer only.
public class ExamResultDAOImpl extends DBContext implements ExamResultDAO {

    private static final String BASE_SELECT =
            "SELECT ExamResultId, ExamEnrollmentId, IsPassed, ResultDate FROM ExamResult";

    // Returns ExamResultId for one enrollment (0 if no result row exists).
    @Override
    public int getExamResultIdByExamEnrollmentId(int examEnrollmentId) {
        ExamResult result = getByExamEnrollmentId(examEnrollmentId);
        return result != null ? result.getExamResultId() : 0;
    }

    // Loads the exam result row for one enrollment.
    @Override
    public ExamResult getByExamEnrollmentId(int examEnrollmentId) {
        String sql = BASE_SELECT + " WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
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

    // Inserts a new exam result row and returns generated id.
    @Override
    public int add(ExamResult result) {
        // ResultDate is NOT NULL — always send a timestamp (DB default is bypassed by explicit NULL).
        String sql = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, result.getExamEnrollmentId());
            ps.setBoolean(2, result.isPassed());
            Timestamp resultDate = result.getResultDate();
            if (resultDate == null) {
                resultDate = new Timestamp(System.currentTimeMillis());
            }
            ps.setTimestamp(3, resultDate);
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

    // Updates pass flag only (does not change ResultDate).
    @Override
    public boolean updateIsPassed(int examResultId, boolean passed) {
        String sql = "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, passed);
            ps.setInt(2, examResultId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates pass flag and stamps ResultDate to now.
    @Override
    public boolean updatePassed(int examResultId, boolean passed) {
        String sql = "UPDATE ExamResult SET IsPassed = ?, ResultDate = ? WHERE ExamResultId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, passed);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, examResultId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Private helper: map.
    private static ExamResult map(ResultSet rs) throws SQLException {
        ExamResult result = new ExamResult();
        result.setExamResultId(rs.getInt("ExamResultId"));
        result.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        result.setPassed(rs.getBoolean("IsPassed"));
        result.setResultDate(rs.getTimestamp("ResultDate"));
        return result;
    }
}
