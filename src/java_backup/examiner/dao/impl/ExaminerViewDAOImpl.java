package examiner.dao.impl;

import shared.dbconnection.DBContext;
import examiner.enums.SectionType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import examiner.dao.ExaminerViewDAO;

public class ExaminerViewDAOImpl extends DBContext implements ExaminerViewDAO {

    @Override
    public String findLicenceClassByExamId(int examId) {
        String sql = """
                SELECT l.LicenceClass
                FROM Exam e
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE e.ExamId = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("LicenceClass");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "-";
    }

    @Override
    public Integer findPrimaryExamAreaId(int examId) {
        String sql = "SELECT TOP 1 ExamAreaId FROM Exam_ExamArea WHERE ExamId = ? ORDER BY ExamAreaId";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamAreaId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Integer, int[]> loadTheoryStatsByExam(int examId) {
        Map<Integer, int[]> stats = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer = q.CorrectAnswer THEN 1 ELSE 0 END) AS correctCount,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer <> q.CorrectAnswer THEN 1 ELSE 0 END) AS wrongCount,
                       SUM(CASE WHEN ca.Answer IS NULL OR ca.Answer = '' THEN 1 ELSE 0 END) AS unansweredCount
                FROM ExamEnrollment ec
                LEFT JOIN TheoryPaper tp ON tp.ExamEnrollmentId = ec.ExamEnrollmentId
                LEFT JOIN CandidateAnswer ca ON ca.TheoryPaperId = tp.TheoryPaperId
                LEFT JOIN Question q ON q.QuestionId = ca.QuestionId
                WHERE ec.ExamId = ?
                GROUP BY ec.ExamEnrollmentId
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getInt("ExamEnrollmentId"), new int[]{
                        rs.getInt("correctCount"),
                        rs.getInt("wrongCount"),
                        rs.getInt("unansweredCount")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public Map<Integer, Double> loadSectionScoresByExam(int examId, String sectionName) {
        Map<Integer, Double> scores = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, es.Score
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ec.ExamId = ?
                """;
        if (sectionName != null && !sectionName.isBlank()) {
            sql += " AND sec.SectionName = ?";
        }
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            if (sectionName != null && !sectionName.isBlank()) {
                ps.setString(2, sectionName);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    scores.put(rs.getInt("ExamEnrollmentId"), rs.getDouble("Score"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    @Override
    public Map<Integer, Boolean> loadPassFlagsByExam(int examId) {
        Map<Integer, Boolean> flags = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, er.IsPassed
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                WHERE ec.ExamId = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flags.put(rs.getInt("ExamEnrollmentId"), rs.getBoolean("IsPassed"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flags;
    }

    @Override
    public Map<Integer, String> loadDeviceNamesByExam(int examId) {
        Map<Integer, String> names = new HashMap<>();
        String sql = """
                SELECT ed.ExamDeviceId, ed.DeviceName
                FROM ExamDevice ed
                JOIN Exam_ExamArea sea ON sea.ExamAreaId = ed.ExamAreaId
                WHERE sea.ExamId = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.put(rs.getInt("ExamDeviceId"), rs.getString("DeviceName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    @Override
    public List<Map<String, Object>> loadScoreDeductionRules(String sectionName, int examId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT sd.ScoreDeductionId, sd.Reason, sd.Points, sd.IsCritical
                FROM ScoreDeduction sd
                JOIN ExamSection es ON es.ExamSectionId = sd.ExamSectionId
                WHERE es.SectionName = ?
                  AND (? <= 0 OR sd.LicenceId = (
                      SELECT LicenceId FROM Exam WHERE ExamId = ?
                  ))
                ORDER BY sd.ScoreDeductionId
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionName != null && !sectionName.isBlank()
                    ? sectionName.trim() : SectionType.LAYOUT.getValue());
            ps.setInt(2, examId);
            ps.setInt(3, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ScoreDeductionId"));
                    row.put("reason", rs.getString("Reason"));
                    row.put("points", rs.getDouble("Points"));
                    row.put("critical", rs.getBoolean("IsCritical"));
                    row.put("occurrenceCount", 0);
                    row.put("count", 0);
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Map<Integer, int[]> loadDeductionOccurrences(int candidateId, int examId) {
        Map<Integer, int[]> occurrences = new HashMap<>();
        String sql = """
                SELECT dr.ScoreDeductionId, dr.OccurrenceCount
                FROM ExamEnrollment ee
                JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN DeductionRecord dr ON dr.ExamScoreId = es.ExamScoreId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    occurrences.put(rs.getInt("ScoreDeductionId"),
                            new int[]{rs.getInt("OccurrenceCount")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return occurrences;
    }

    @Override
    public Map<Integer, java.util.Date> loadDeductionRecordedAt(int candidateId, int examId) {
        Map<Integer, java.util.Date> recordedAt = new HashMap<>();
        String sql = """
                SELECT dr.ScoreDeductionId, dr.RecordedAt
                FROM ExamEnrollment ee
                JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN DeductionRecord dr ON dr.ExamScoreId = es.ExamScoreId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("RecordedAt");
                    if (ts != null) {
                        recordedAt.put(rs.getInt("ScoreDeductionId"), new java.util.Date(ts.getTime()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recordedAt;
    }

    @Override
    public Map<String, Object> loadScoreSummary(int candidateId, int examId, String sectionName) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("currentScore", 100);
        summary.put("scoreDisqualified", false);
        if (candidateId <= 0 || examId <= 0) {
            return summary;
        }
        String sql = """
                SELECT TOP 1 es.Score,
                       CASE WHEN EXISTS (
                           SELECT 1
                           FROM DeductionRecord dr
                           JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId
                           WHERE dr.ExamScoreId = es.ExamScoreId
                             AND sd.IsCritical = 1
                             AND dr.OccurrenceCount > 0
                       ) THEN 1 ELSE 0 END AS hasCritical
                FROM ExamEnrollment ee
                JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND sec.SectionName = ?
                ORDER BY es.ExamScoreId
                """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            ps.setString(3, sectionName != null && !sectionName.isBlank()
                    ? sectionName.trim() : SectionType.LAYOUT.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.put("currentScore", (int) Math.round(rs.getDouble("Score")));
                    summary.put("scoreDisqualified", rs.getBoolean("hasCritical"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }
}
