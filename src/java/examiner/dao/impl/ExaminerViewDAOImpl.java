package examiner.dao.impl;

import shared.dbconnection.DBContext;
import shared.enums.SectionType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import examiner.dao.ExaminerViewDAO;
import shared.model.ExamArea;

// JDBC implementation for ExaminerView; examiner module DAO layer only.
public class ExaminerViewDAOImpl extends DBContext implements ExaminerViewDAO {


    // Returns the first exam area linked to one exam (Exam_ExamArea).
    @Override
    public ExamArea getIfPrimaryByExam(int examId) {
        String sql = "SELECT TOP 1 ea.ExamAreaId, ea.AreaName, ea.AreaType, ea.Capacity, ea.Location, ea.ExamZoneId "
                + "FROM Exam_ExamArea eea "
                + "JOIN ExamArea ea ON ea.ExamAreaId = eea.ExamAreaId "
                + "WHERE eea.ExamId = ? ORDER BY ea.ExamAreaId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExamArea area = new ExamArea();
                    area.setExamAreaId(rs.getInt("ExamAreaId"));
                    area.setAreaName(rs.getString("AreaName"));
                    area.setAreaType(rs.getString("AreaType"));
                    int cap = rs.getInt("Capacity");
                    if (rs.wasNull()) {
                        area.setCapacity(null);
                    } else {
                        area.setCapacity(cap);
                    }
                    area.setLocation(rs.getString("Location"));
                    area.setExamZoneId(rs.getInt("ExamZoneId"));
                    return area;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Batch-loads theory answer stats [correct, wrong, unanswered] per enrollment.
    @Override
    public Map<Integer, int[]> getAllTheoryStatsByExam(int examId) {
        Map<Integer, int[]> stats = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer = q.CorrectAnswer THEN 1 ELSE 0 END) AS correctCount,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer <> q.CorrectAnswer THEN 1 ELSE 0 END) AS wrongCount,
                       SUM(CASE WHEN ca.Answer IS NULL OR ca.Answer = '' THEN 1 ELSE 0 END) AS unansweredCount
                FROM ExamEnrollment ec
                LEFT JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ec.ExamEnrollmentId
                LEFT JOIN ExamSection sec ON sec.ExamSectionId = ees.ExamSectionId
                    AND sec.SectionType = ?
                LEFT JOIN TheoryPaper tp ON tp.ExamEnrollmentSectionId = ees.ExamEnrollmentSectionId
                    AND sec.ExamSectionId IS NOT NULL
                LEFT JOIN CandidateAnswer ca ON ca.TheoryPaperId = tp.TheoryPaperId
                LEFT JOIN Question q ON q.QuestionId = ca.QuestionId
                WHERE ec.ExamId = ?
                GROUP BY ec.ExamEnrollmentId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, SectionType.THEORY.getValue());
            ps.setInt(2, examId);
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

    // Batch-loads section scores per enrollment for one exam and section type.
    @Override
    public Map<Integer, Double> getAllSectionScoresByExam(int examId, String sectionType) {
        Map<Integer, Double> scores = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, es.Score
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ec.ExamId = ?
                """;
        if (sectionType != null && !sectionType.isBlank()) {
            sql += " AND sec.SectionType = ?";
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            if (sectionType != null && !sectionType.isBlank()) {
                ps.setString(2, sectionType);
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

    // Batch-loads overall pass flags per enrollment for one exam.
    @Override
    public Map<Integer, Boolean> getAllPassFlagsByExam(int examId) {
        Map<Integer, Boolean> flags = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, er.IsPassed
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                WHERE ec.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    // Batch-loads device id to device name for devices used in one exam.
    @Override
    public Map<Integer, String> getAllDeviceNamesByExam(int examId) {
        Map<Integer, String> names = new HashMap<>();
        String sql = """
                SELECT ed.ExamDeviceId, ed.DeviceName
                FROM ExamDevice ed
                JOIN Exam_ExamArea sea ON sea.ExamAreaId = ed.ExamAreaId
                WHERE sea.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    // Loads score deduction rules for one section type and exam licence.
    @Override
    public List<Map<String, Object>> getAllScoreDeductionRulesByExam(String sectionType, int examId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT sd.ScoreDeductionId, sd.Reason, sd.Points, sd.IsCritical
                 FROM ScoreDeduction sd
                 JOIN ExamSection es ON es.ExamSectionId = sd.ExamSectionId
                 WHERE es.SectionType = ?
                   AND (? <= 0 OR es.ExamId = ?)
                   AND (? <= 0 OR sd.LicenceId = (
                       SELECT LicenceId FROM Exam WHERE ExamId = ?
                   ))
                ORDER BY sd.IsCritical ASC, sd.ScoreDeductionId ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, sectionType != null && !sectionType.isBlank()
                    ? sectionType.trim() : SectionType.LAYOUT.getValue());
            ps.setInt(2, examId);
            ps.setInt(3, examId);
            ps.setInt(4, examId);
            ps.setInt(5, examId);
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

    // Loads deduction occurrence counts keyed by ScoreDeductionId for one candidate/exam.
    @Override
    public Map<Integer, int[]> getAllDeductionOccurrencesByExam(int candidateId, int examId) {
        Map<Integer, int[]> occurrences = new HashMap<>();
        String sql = """
                SELECT dr.ScoreDeductionId, dr.OccurrenceCount
                FROM ExamEnrollment ee
                JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN DeductionRecord dr ON dr.ExamScoreId = es.ExamScoreId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    // Loads current score and critical-disqualification flag for one candidate section.
    @Override
    public Map<String, Object> getIfScoreSummaryByCandidateAndExam(int candidateId, int examId, String sectionType) {
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
                  AND sec.SectionType = ?
                ORDER BY es.ExamScoreId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            ps.setString(3, sectionType != null && !sectionType.isBlank()
                    ? sectionType.trim() : SectionType.LAYOUT.getValue());
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
