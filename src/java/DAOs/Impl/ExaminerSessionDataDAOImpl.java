package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExaminerSessionDataDAO;
import DTOs.ExaminerAnswerStatsDTO;
import DTOs.ExaminerPaperStateDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExaminerSessionDataDAOImpl implements ExaminerSessionDataDAO {

    private final DBContext ctx;

    public ExaminerSessionDataDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public String findExamCodeBySessionId(int sessionId) {
        String sql = """
                select e.ExamCode
                from [Session] s
                join Exam e on e.ExamId = s.ExamId
                where s.SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
    public Map<Integer, ExaminerPaperStateDTO> findPaperStatesBySessionId(int sessionId) {
        Map<Integer, ExaminerPaperStateDTO> map = new HashMap<>();
        String sql = """
                select c.CandidateId,
                       tp.StartedAt,
                       tp.SubmittedAt
                from ExamEnrollment ec
                join Candidate c on c.CandidateId = ec.CandidateId
                outer apply (
                    select top 1 t.StartedAt, t.SubmittedAt
                    from TheoryPaper t
                    where t.ExamEnrollmentId = ec.ExamEnrollmentId
                    order by t.TheoryPaperId desc
                ) tp
                where ec.SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExaminerPaperStateDTO state = new ExaminerPaperStateDTO();
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
    public Map<Integer, ExaminerAnswerStatsDTO> findAnswerStatsBySessionId(int sessionId) {
        Map<Integer, ExaminerAnswerStatsDTO> map = new HashMap<>();
        String sql = """
                select c.CandidateId,
                       SUM(case when ca.Answer is not null and ca.Answer = q.CorrectAnswer then 1 else 0 end) as correctCnt,
                       SUM(case when ca.Answer is not null and ca.Answer <> q.CorrectAnswer then 1 else 0 end) as wrongCnt,
                       SUM(case when ca.Answer is null or LTRIM(RTRIM(ca.Answer)) = '' then 1 else 0 end) as unansweredCnt
                from ExamEnrollment ec
                join Candidate c on c.CandidateId = ec.CandidateId
                join TheoryPaper tp on tp.ExamEnrollmentId = ec.ExamEnrollmentId
                left join CandidateAnswer ca on ca.TheoryPaperId = tp.TheoryPaperId
                left join Question q on q.QuestionId = ca.QuestionId
                where ec.SessionId = ?
                group by c.CandidateId
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int correct = rs.getInt("correctCnt");
                    int wrong = rs.getInt("wrongCnt");
                    int unanswered = rs.getInt("unansweredCnt");
                    if (correct + wrong + unanswered == 0) {
                        continue;
                    }
                    ExaminerAnswerStatsDTO stats = new ExaminerAnswerStatsDTO();
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
                select ed.ExamDeviceId,
                       ed.DeviceName,
                       ed.DeviceType,
                       ed.[Status],
                       ea.AreaName
                from ExamDevice ed
                join ExamArea ea on ea.ExamAreaId = ed.ExamAreaId
                where ed.ExamAreaId in (
                    select sea.ExamAreaId
                    from Session_ExamArea sea
                    where sea.SessionId = ?
                )
                order by ed.DeviceName
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
                select ScoreDeductionId, [Reason], Points, IsCritical
                from ScoreDeduction
                order by ScoreDeductionId
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql);
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
    public List<Map<String, Object>> findScoreDeductionsBySectionId(int examSectionId) {
        List<Map<String, Object>> deductions = new ArrayList<>();
        if (examSectionId <= 0) {
            return deductions;
        }

        String sql = """
                select ScoreDeductionId, [Reason], Points, IsCritical, SortOrder
                from ScoreDeduction
                where ExamSectionId = ?
                order by SortOrder, ScoreDeductionId
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examSectionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ScoreDeductionId"));
                    row.put("reason", rs.getString("Reason"));
                    row.put("points", rs.getBigDecimal("Points"));
                    row.put("critical", rs.getBoolean("IsCritical"));
                    row.put("sortOrder", rs.getInt("SortOrder"));
                    deductions.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deductions;
    }

    @Override
    public Integer findExamSectionIdForSession(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }

        String sql = """
                select top 1 ses.ExamSectionId
                from Session_ExamSection ses
                where ses.SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, Object> findSessionExportMeta(int sessionId) {
        Map<String, Object> meta = new LinkedHashMap<>();
        String sql = """
                select s.SessionName,
                       e.ExamCode,
                       CAST(s.StartTime as DATE) as examDate,
                       CAST(s.StartTime as TIME) as startTime,
                       CAST(s.EndTime as TIME) as endTime
                from [Session] s
                join Exam e on e.ExamId = s.ExamId
                where s.SessionId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
                select c.CandidateNumber as sbd,
                       c.FullName as fullName,
                       sec.SectionName as sectionName,
                       sd.[Reason] as violationReason,
                       sd.Points as deductionPoints,
                       sd.IsCritical as critical,
                       es.Score as currentScore
                from ExamEnrollment ec
                join Candidate c on c.CandidateId = ec.CandidateId
                join ExamResult er on er.ExamEnrollmentId = ec.ExamEnrollmentId
                join ExamScore es on es.ExamResultId = er.ExamResultId
                join Score_Deduction sded on sded.ExamScoreId = es.ExamScoreId
                join ScoreDeduction sd on sd.ScoreDeductionId = sded.ScoreDeductionId
                join ExamSection sec on sec.ExamSectionId = es.ExamSectionId
                where ec.SessionId = ?
                order by c.CandidateNumber, sd.ScoreDeductionId
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
