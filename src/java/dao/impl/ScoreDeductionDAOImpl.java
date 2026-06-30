package dao.impl;

import dao.ScoreDeductionDAO;
import dbconnection.DBContext;
import model.ScoreDeduction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class ScoreDeductionDAOImpl extends DBContext implements ScoreDeductionDAO {

    private static final String BASE_SELECT =
            "SELECT ScoreDeductionId, Reason, Points, IsCritical, ExamSectionId, SortOrder FROM ScoreDeduction";

    @Override
    public ScoreDeduction findById(int scoreDeductionId) {
        String sql = BASE_SELECT + " WHERE ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, scoreDeductionId);
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
    public int insert(ScoreDeduction deduction) {
        String sql = "INSERT INTO ScoreDeduction (Reason, Points, IsCritical, ExamSectionId, SortOrder) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, deduction.getReason());
            ps.setDouble(2, deduction.getPoints());
            ps.setBoolean(3, deduction.isCritical());
            if (deduction.getExamSectionId() != null) {
                ps.setInt(4, deduction.getExamSectionId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setInt(5, deduction.getSortOrder());
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
    public boolean update(ScoreDeduction deduction) {
        String sql = "UPDATE ScoreDeduction SET Reason=?, Points=?, IsCritical=?, ExamSectionId=?, SortOrder=? "
                + "WHERE ScoreDeductionId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, deduction.getReason());
            ps.setDouble(2, deduction.getPoints());
            ps.setBoolean(3, deduction.isCritical());
            if (deduction.getExamSectionId() != null) {
                ps.setInt(4, deduction.getExamSectionId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setInt(5, deduction.getSortOrder());
            ps.setInt(6, deduction.getScoreDeductionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int scoreDeductionId) {
        String sql = "DELETE FROM ScoreDeduction WHERE ScoreDeductionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, scoreDeductionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ScoreDeduction";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int scoreDeductionId, int delta) {
        if (candidateId <= 0 || sessionId <= 0 || scoreDeductionId <= 0 || delta == 0) {
            return false;
        }
        try {
            Connection conn = getConnection();
            if (conn == null) {
                return false;
            }
            ScoreDeduction rule = findById(scoreDeductionId);
            if (rule == null) {
                return false;
            }
            int examEnrollmentId = resolveExamEnrollmentId(conn, candidateId, sessionId);
            if (examEnrollmentId <= 0) {
                return false;
            }
            int examResultId = resolveOrCreateExamResult(conn, examEnrollmentId);
            if (examResultId <= 0) {
                return false;
            }
            int sectionId = rule.getExamSectionId() != null
                    ? rule.getExamSectionId()
                    : resolveSessionSectionId(conn, sessionId);
            if (sectionId <= 0) {
                return false;
            }
            int examScoreId = resolveOrCreateExamScore(conn, examResultId, sectionId);
            if (examScoreId <= 0) {
                return false;
            }
            if (!applyDelta(conn, examScoreId, scoreDeductionId, delta)) {
                return false;
            }
            recalculateScore(conn, examScoreId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int resolveExamEnrollmentId(Connection conn, int candidateId, int sessionId) throws SQLException {
        String sql = "SELECT ExamEnrollmentId FROM ExamEnrollment WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return 0;
    }

    private static int resolveOrCreateExamResult(Connection conn, int examEnrollmentId) throws SQLException {
        String selectSql = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamResultId");
                }
            }
        }
        String insertSql = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examEnrollmentId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        return 0;
    }

    private static int resolveSessionSectionId(Connection conn, int sessionId) throws SQLException {
        String sql = "SELECT TOP 1 ExamSectionId FROM Session_ExamSection WHERE SessionId = ? ORDER BY ExamSectionId";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        String fallback = "SELECT TOP 1 ExamSectionId FROM ExamSection WHERE SectionName = N'Sa hình'";
        try (PreparedStatement ps = conn.prepareStatement(fallback);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("ExamSectionId");
            }
        }
        return 0;
    }

    private static int resolveOrCreateExamScore(Connection conn, int examResultId, int sectionId) throws SQLException {
        String selectSql = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, examResultId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamScoreId");
                }
            }
        }
        String insertSql = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, 100)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examResultId);
            ps.setInt(2, sectionId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        return 0;
    }

    private static boolean applyDelta(Connection conn, int examScoreId, int scoreDeductionId, int delta)
            throws SQLException {
        String selectSql = "SELECT OccurrenceCount FROM DeductionRecord WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
        int current = 0;
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, examScoreId);
            ps.setInt(2, scoreDeductionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    current = rs.getInt("OccurrenceCount");
                }
            }
        }
        int next = current + delta;
        if (current == 0 && delta > 0) {
            String insertSql = "INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, examScoreId);
                ps.setInt(2, scoreDeductionId);
                ps.setInt(3, delta);
                ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                return ps.executeUpdate() > 0;
            }
        }
        if (current > 0) {
            if (next <= 0) {
                String deleteSql = "DELETE FROM DeductionRecord WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, examScoreId);
                    ps.setInt(2, scoreDeductionId);
                    return ps.executeUpdate() > 0;
                }
            }
            String updateSql = "UPDATE DeductionRecord SET OccurrenceCount = ?, RecordedAt = ? "
                    + "WHERE ExamScoreId = ? AND ScoreDeductionId = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, next);
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                ps.setInt(3, examScoreId);
                ps.setInt(4, scoreDeductionId);
                return ps.executeUpdate() > 0;
            }
        }
        return false;
    }

    private static void recalculateScore(Connection conn, int examScoreId) throws SQLException {
        String sql = "SELECT SUM(sd.Points * dr.OccurrenceCount) AS totalDeduction, "
                + "MAX(CASE WHEN sd.IsCritical = 1 AND dr.OccurrenceCount > 0 THEN 1 ELSE 0 END) AS hasCritical "
                + "FROM DeductionRecord dr "
                + "JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId "
                + "WHERE dr.ExamScoreId = ?";
        double totalDeduction = 0;
        boolean hasCritical = false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examScoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDeduction = rs.getDouble("totalDeduction");
                    hasCritical = rs.getBoolean("hasCritical");
                }
            }
        }
        double score = hasCritical ? 0 : Math.max(0, 100 - totalDeduction);
        String updateSql = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setDouble(1, score);
            ps.setInt(2, examScoreId);
            ps.executeUpdate();
        }
    }

    private static ScoreDeduction map(ResultSet rs) throws SQLException {
        ScoreDeduction deduction = new ScoreDeduction();
        deduction.setScoreDeductionId(rs.getInt("ScoreDeductionId"));
        deduction.setReason(rs.getString("Reason"));
        deduction.setPoints(rs.getDouble("Points"));
        deduction.setCritical(rs.getBoolean("IsCritical"));
        int sectionId = rs.getInt("ExamSectionId");
        if (!rs.wasNull()) {
            deduction.setExamSectionId(sectionId);
        }
        deduction.setSortOrder(rs.getInt("SortOrder"));
        return deduction;
    }
}
