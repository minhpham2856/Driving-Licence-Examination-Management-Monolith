package dao.impl;

import dao.ExaminerSessionDataDAO;
import dbconnection.DBContext;
import dto.examiner.ExaminerAnswerStats;
import dto.examiner.ExaminerPaperState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExaminerSessionDataDAOImpl extends DBContext implements ExaminerSessionDataDAO {

    @Override
    public String findExamCodeBySessionId(int sessionId) {
        String sql = """
                SELECT e.ExamCode
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ExamCode");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Integer, ExaminerPaperState> findPaperStatesBySessionId(int sessionId) {
        Map<Integer, ExaminerPaperState> map = new HashMap<>();
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
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExaminerPaperState state = new ExaminerPaperState();
                    state.setStarted(rs.getTimestamp("StartedAt") != null);
                    state.setSubmitted(rs.getTimestamp("SubmittedAt") != null);
                    map.put(rs.getInt("CandidateId"), state);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<Integer, ExaminerAnswerStats> findAnswerStatsBySessionId(int sessionId) {
        Map<Integer, ExaminerAnswerStats> map = new HashMap<>();
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
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int correct = rs.getInt("correctCnt");
                    int wrong = rs.getInt("wrongCnt");
                    int unanswered = rs.getInt("unansweredCnt");
                    if (correct + wrong + unanswered == 0) {
                        continue;
                    }
                    ExaminerAnswerStats stats = new ExaminerAnswerStats();
                    stats.setCorrect(correct);
                    stats.setWrong(wrong);
                    stats.setUnanswered(unanswered);
                    map.put(rs.getInt("CandidateId"), stats);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> findDevicesBySessionId(int sessionId) {
        List<Map<String, Object>> devices = new ArrayList<>();
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
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
            e.printStackTrace();
        }
        return devices;
    }

    @Override
    public List<Map<String, Object>> findScoreDeductions() {
        List<Map<String, Object>> deductions = new ArrayList<>();
        String sql = """
                SELECT ScoreDeductionId, [Reason], Points, IsCritical
                FROM ScoreDeduction
                ORDER BY ScoreDeductionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("ScoreDeductionId"));
                row.put("reason", rs.getString("Reason"));
                row.put("points", rs.getBigDecimal("Points"));
                row.put("critical", rs.getBoolean("IsCritical"));
                deductions.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deductions;
    }

    @Override
    public Map<String, Object> findSessionExportMeta(int sessionId) {
        Map<String, Object> meta = new LinkedHashMap<>();
        String sql = """
                SELECT s.SessionName,
                       e.ExamCode,
                       CAST(s.StartTime AS DATE) AS examDate,
                       CAST(s.StartTime AS TIME) AS startTime,
                       CAST(s.EndTime AS TIME) AS endTime
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    meta.put("sessionName", rs.getString("SessionName"));
                    meta.put("examCode", rs.getString("ExamCode"));
                    meta.put("examDate", rs.getDate("examDate"));
                    meta.put("startTime", rs.getTime("startTime"));
                    meta.put("endTime", rs.getTime("endTime"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meta;
    }

    @Override
    public List<Map<String, Object>> findScoreViolationRows(int sessionId) {
        List<Map<String, Object>> rows = new ArrayList<>();
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
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sbd", rs.getString("sbd"));
                    row.put("fullName", rs.getString("fullName"));
                    row.put("sectionName", rs.getString("sectionName"));
                    row.put("violationReason", rs.getString("violationReason"));
                    row.put("deductionPoints", rs.getBigDecimal("deductionPoints"));
                    row.put("critical", rs.getBoolean("critical"));
                    row.put("currentScore", rs.getBigDecimal("currentScore"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
