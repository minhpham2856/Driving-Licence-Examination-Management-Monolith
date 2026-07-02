package examstaff.dao.impl;


import examstaff.dao.ExaminerSessionDataDAO;

import dbconnection.DBContext;

import examstaff.dto.examiner.ExaminerAnswerStatsDTO;

import examstaff.dto.examiner.ExaminerPaperStateDTO;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

 // JDBC implementation of {@link ExaminerSessionDataDAO}.
public class ExaminerSessionDataDAOImpl extends DBContext implements ExaminerSessionDataDAO {

    // Retrieves the exam code (e.g. "DT-2024-001") for the session's parent exam
    @Override
    public String findExamCodeBySessionId(int sessionId) {
        // SQL: join Session to Exam to get the exam code by session ID
        String sql = """
                SELECT e.ExamCode
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Return the exam code if a row exists
                if (rs.next()) {
                    return rs.getString("ExamCode");
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives null indicating no exam code found
            e.printStackTrace();
        }
        // Return null if the session has no linked exam or the query failed
        return null;
    }

    // Retrieves paper lifecycle states (started/submitted) for all candidates in a session
    @Override
    public Map<Integer, ExaminerPaperStateDTO> findPaperStatesBySessionId(int sessionId) {
        // Map to hold candidate ID -> paper state DTO pairs
        Map<Integer, ExaminerPaperStateDTO> map = new HashMap<>();
        // SQL: OUTER APPLY fetches the most recent theory paper per candidate
        String sql = """
                SELECT c.CandidateId,
                       tp.StartedAt,
                       tp.SubmittedAt
                FROM Exam_Candidate ec
                JOIN Candidate c ON c.CandidateId = ec.CandidateId
                OUTER APPLY (
                    SELECT TOP 1 t.StartedAt, t.SubmittedAt
                    FROM TheoryPaper t
                    WHERE t.ExamCandidateId = ec.ExamCandidateId
                    ORDER BY t.TheoryPaperId DESC
                ) tp
                WHERE ec.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Iterate over all candidates in the session
                while (rs.next()) {
                    // Create a new paper state DTO for this candidate
                    ExaminerPaperStateDTO state = new ExaminerPaperStateDTO();
                    // Started = true if StartedAt timestamp is non-null
                    state.setStarted(rs.getTimestamp("StartedAt") != null);
                    // Submitted = true if SubmittedAt timestamp is non-null
                    state.setSubmitted(rs.getTimestamp("SubmittedAt") != null);
                    // Store in the map keyed by candidate ID
                    map.put(rs.getInt("CandidateId"), state);
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated map
            e.printStackTrace();
        }
        return map;
    }

    // Retrieves answer statistics (correct/wrong/unanswered) for all candidates in a session
    @Override
    public Map<Integer, ExaminerAnswerStatsDTO> findAnswerStatsBySessionId(int sessionId) {
        // Map to hold candidate ID -> answer stats DTO pairs
        Map<Integer, ExaminerAnswerStatsDTO> map = new HashMap<>();
        // SQL: aggregates correct/wrong/unanswered counts per candidate using CASE expressions
        String sql = """
                SELECT c.CandidateId,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer = q.CorrectAnswer THEN 1 ELSE 0 END) AS correctCnt,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer <> q.CorrectAnswer THEN 1 ELSE 0 END) AS wrongCnt,
                       SUM(CASE WHEN ca.Answer IS NULL OR LTRIM(RTRIM(ca.Answer)) = '' THEN 1 ELSE 0 END) AS unansweredCnt
                FROM Exam_Candidate ec
                JOIN Candidate c ON c.CandidateId = ec.CandidateId
                JOIN TheoryPaper tp ON tp.ExamCandidateId = ec.ExamCandidateId
                LEFT JOIN CandidateAnswer ca ON ca.TheoryPaperId = tp.TheoryPaperId
                LEFT JOIN Question q ON q.QuestionId = ca.QuestionId
                WHERE ec.SessionId = ?
                GROUP BY c.CandidateId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Iterate over each candidate's aggregated stats
                while (rs.next()) {
                    // Read the aggregated counts from the result set
                    int correct = rs.getInt("correctCnt");
                    int wrong = rs.getInt("wrongCnt");
                    int unanswered = rs.getInt("unansweredCnt");
                    // Skip candidates with zero total answers (no theory paper submitted)
                    if (correct + wrong + unanswered == 0) {
                        continue;
                    }
                    // Create and populate the stats DTO
                    ExaminerAnswerStatsDTO stats = new ExaminerAnswerStatsDTO();
                    stats.setCorrect(correct);
                    stats.setWrong(wrong);
                    stats.setUnanswered(unanswered);
                    // Store in the map keyed by candidate ID
                    map.put(rs.getInt("CandidateId"), stats);
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated map
            e.printStackTrace();
        }
        return map;
    }

    // Retrieves all devices linked to the exam areas participating in a session
    @Override
    public List<Map<String, Object>> findDevicesBySessionId(int sessionId) {
        // List to hold device data maps
        List<Map<String, Object>> devices = new ArrayList<>();
        // SQL: join ExamDevice to ExamArea, filtered by session-area membership
        String sql = """
                SELECT ed.ExamDeviceId,
                       ed.DeviceName,
                       ed.DeviceType,
                       ed.[Status],
                       ea.AreaName
                FROM ExamDevice ed
                JOIN ExamArea ea ON ea.ExamAreaId = ed.ExamAreaId
                WHERE ed.ExamAreaId IN (
                    SELECT sea.ExamAreaId
                    FROM Session_ExamArea sea
                    WHERE sea.SessionId = ?
                )
                ORDER BY ed.DeviceName
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Build a map for each device row
                while (rs.next()) {
                    // Use LinkedHashMap to preserve insertion order for consistent display
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ExamDeviceId"));
                    row.put("name", rs.getString("DeviceName"));
                    row.put("type", rs.getString("DeviceType"));
                    row.put("status", rs.getString("Status"));
                    row.put("area", rs.getString("AreaName"));
                    devices.add(row);
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return devices;
    }

    // Retrieves all score deduction rules (global, not section-scoped)
    @Override
    public List<Map<String, Object>> findScoreDeductions() {
        // List to hold deduction data maps
        List<Map<String, Object>> deductions = new ArrayList<>();
        // SQL: fetch all deduction rules ordered by ID
        String sql = """
                SELECT ScoreDeductionId, [Reason], Points, IsCritical
                FROM ScoreDeduction
                ORDER BY ScoreDeductionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Build a map for each deduction rule
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("ScoreDeductionId"));
                row.put("reason", rs.getString("Reason"));
                // Points is stored as DECIMAL — use BigDecimal for precision
                row.put("points", rs.getBigDecimal("Points"));
                // IsCritical flag determines if this deduction causes automatic failure
                row.put("critical", rs.getBoolean("IsCritical"));
                deductions.add(row);
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return deductions;
    }

    // Retrieves score deduction rules scoped to a specific exam section
    @Override
    public List<Map<String, Object>> findScoreDeductionsBySectionId(int examSectionId) {
        // List to hold section-scoped deduction data maps
        List<Map<String, Object>> deductions = new ArrayList<>();
        // Guard: return empty list for invalid section IDs
        if (examSectionId <= 0) {
            return deductions;
        }
        // SQL: fetch deductions filtered by exam section, ordered by SortOrder then ID
        String sql = """
                SELECT ScoreDeductionId, [Reason], Points, IsCritical, SortOrder
                FROM ScoreDeduction
                WHERE ExamSectionId = ?
                ORDER BY SortOrder, ScoreDeductionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the exam section ID parameter
            ps.setInt(1, examSectionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Build a map for each deduction rule
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ScoreDeductionId"));
                    row.put("reason", rs.getString("Reason"));
                    row.put("points", rs.getBigDecimal("Points"));
                    row.put("critical", rs.getBoolean("IsCritical"));
                    // SortOrder determines the display order in the score entry UI
                    row.put("sortOrder", rs.getInt("SortOrder"));
                    deductions.add(row);
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return deductions;
    }

    // Retrieves the first exam section ID linked to the given session
    @Override
    public Integer findExamSectionIdForSession(int sessionId) {
        // Guard: return null for invalid session IDs
        if (sessionId <= 0) {
            return null;
        }
        // SQL: fetch the first exam section linked via the Session_ExamSection join table
        String sql = """
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                WHERE ses.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Return the first section ID if a row exists
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives null indicating no section found
            e.printStackTrace();
        }
        // Return null if the session has no linked exam sections
        return null;
    }

    // Retrieves export metadata (session name, exam code, date, times) for a session
    @Override
    public Map<String, Object> findSessionExportMeta(int sessionId) {
        // LinkedHashMap preserves insertion order for consistent metadata display
        Map<String, Object> meta = new LinkedHashMap<>();
        // SQL: join Session to Exam and extract date/time components
        String sql =
                "SELECT " + examstaff.util.SessionLabel.SQL_SHIFT_ONLY + " AS SessionName, "
                + "e.ExamCode, "
                + "CAST(s.StartTime AS DATE) AS examDate, "
                + "CAST(s.StartTime AS TIME) AS startTime, "
                + "CAST(s.EndTime AS TIME) AS endTime "
                + "FROM [Session] s "
                + "JOIN Exam e ON e.ExamId = s.ExamId "
                + "WHERE s.SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Populate the metadata map if a row exists
                if (rs.next()) {
                    meta.put("sessionName", rs.getString("SessionName"));
                    meta.put("examCode", rs.getString("ExamCode"));
                    // examDate is CAST to DATE for display formatting (dd/MM/yyyy)
                    meta.put("examDate", rs.getDate("examDate"));
                    // startTime and endTime are CAST to TIME for display formatting (HH:mm)
                    meta.put("startTime", rs.getTime("startTime"));
                    meta.put("endTime", rs.getTime("endTime"));
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated map
            e.printStackTrace();
        }
        return meta;
    }

    // Retrieves violation rows (score deductions applied to candidates) for the violations export
    @Override
    public List<Map<String, Object>> findScoreViolationRows(int sessionId) {
        // List to hold violation data maps for the export
        List<Map<String, Object>> rows = new ArrayList<>();
        // SQL: multi-table join from Exam_Candidate through to ScoreDeduction
        String sql = """
                SELECT c.CandidateNumber AS sbd,
                       c.FullName AS fullName,
                       sec.SectionName AS sectionName,
                       sd.[Reason] AS violationReason,
                       sd.Points AS deductionPoints,
                       sd.IsCritical AS critical,
                       es.Score AS currentScore
                FROM Exam_Candidate ec
                JOIN Candidate c ON c.CandidateId = ec.CandidateId
                JOIN ExamResult er ON er.ExamCandidateId = ec.ExamCandidateId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN Score_Deduction sded ON sded.ExamScoreId = es.ExamScoreId
                JOIN ScoreDeduction sd ON sd.ScoreDeductionId = sded.ScoreDeductionId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ec.SessionId = ?
                ORDER BY c.CandidateNumber, sd.ScoreDeductionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Bind the session ID parameter
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                // Build a map for each violation row
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sbd", rs.getString("sbd"));
                    row.put("fullName", rs.getString("fullName"));
                    row.put("sectionName", rs.getString("sectionName"));
                    row.put("violationReason", rs.getString("violationReason"));
                    // Deduction points as BigDecimal for precision
                    row.put("deductionPoints", rs.getBigDecimal("deductionPoints"));
                    // Critical flag indicates automatic failure
                    row.put("critical", rs.getBoolean("critical"));
                    // Current score after all deductions have been applied
                    row.put("currentScore", rs.getBigDecimal("currentScore"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            // Log the error — caller receives the partially-populated list
            e.printStackTrace();
        }
        return rows;
    }
}
