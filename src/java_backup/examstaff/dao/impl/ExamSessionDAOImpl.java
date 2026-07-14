package examstaff.dao.impl;


import shared.dbconnection.DBContext;

import examstaff.dao.ExamSessionDAO;

import examstaff.dto.ExamSummaryDTO;

import examstaff.dto.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ExamSessionDAO.
 * Schema DLEM_DB_2: má»™t ká»³ thi = má»™t {@link model.Exam}; {@code sessionId} = {@code ExamId}.
 */
public class ExamSessionDAOImpl extends DBContext implements ExamSessionDAO {

    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS id, "
            + "e.ExamId AS examId, "
            + "CAST(1 AS BIT) AS isMorningSession, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Háº¡ng ' + l.LicenceClass + N' â€” ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS sessionName, "
            + "e.LicenceId AS licenseTypeId, "
            + "1 AS examTypeId, "
            + "CAST(e.ExamDate AS DATE) AS examDate, "
            + "CAST(e.StartTime AS TIME) AS shiftStartTime, "
            + "CAST(e.EndTime AS TIME) AS shiftEndTime, "
            + "e.StartTime AS scheduledStartAt, "
            + "e.EndTime AS scheduledEndAt, "
            + "ISNULL(ea.ExamAreaId, 0) AS areaId, "
            + "e.[Status] AS status, "
            + "ISNULL(ea.Capacity, 100) AS maxCandidates, "
            + "(SELECT COUNT(*) FROM ExamEnrollment ee2 WHERE ee2.ExamId = e.ExamId) AS registeredCount, "
            + "e.StartTime AS createdAt, "
            + "l.LicenceClass AS licenseCode, "
            + "e.ExamCode AS examCode, "
            + "N'LÃ½ thuyáº¿t + Thá»±c hÃ nh' AS examTypeName, "
            + "ea.AreaName AS areaName "
            + "FROM Exam e "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId "
            + "LEFT JOIN ( "
            + "    SELECT exa.ExamId, MIN(exa.ExamAreaId) AS ExamAreaId "
            + "    FROM Exam_ExamArea exa "
            + "    GROUP BY exa.ExamId "
            + ") pick ON pick.ExamId = e.ExamId "
            + "LEFT JOIN ExamArea ea ON ea.ExamAreaId = pick.ExamAreaId";

    @Override
    public Session findById(int id) {
        String sql = "SELECT ExamId, StartTime, EndTime, [Status] FROM Exam WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session s = new Session();
                    s.setId(rs.getInt("ExamId"));
                    s.setMorningSession(true);
                    s.setStartTime(rs.getTimestamp("StartTime"));
                    s.setEndTime(rs.getTimestamp("EndTime"));
                    s.setStatus(rs.getString("Status"));
                    s.setExamId(rs.getInt("ExamId"));
                    s.setSessionName(examstaff.util.SessionLabel.shiftLabel(true));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExamSummaryDTO getById(int id) {
        if (id <= 0) {
            return null;
        }
        return fetchOne(EXAM_SELECT + " WHERE e.ExamId = ?", id);
    }

    private ExamSummaryDTO fetchOne(String sql, int examId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExamSummaryDTO> getActiveSessions() {
        return fetchList(EXAM_SELECT
                + " WHERE e.[Status] IN (N'ChÆ°a diá»…n ra', N'Má»Ÿ', N'Äang diá»…n ra', "
                + "'Scheduled', 'Open', 'InProgress')"
                + " ORDER BY CAST(e.ExamDate AS DATE), CAST(e.StartTime AS TIME)");
    }

    @Override
    public List<ExamSummaryDTO> getAllSessions() {
        return fetchList(EXAM_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, CAST(e.StartTime AS TIME) DESC");
    }

    @Override
    public List<ExamSummaryDTO> getAllSessionsBasic() {
        return getAllSessions();
    }

    @Override
    public List<ExamSummaryDTO> getExamDayPickerOptions() {
        return fetchList(EXAM_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, l.LicenceClass");
    }

    @Override
    public List<ExamSummaryDTO> getSessionsByExamDate(Date examDate) {
        if (examDate == null) {
            return List.of();
        }
        List<ExamSummaryDTO> list = new ArrayList<>();
        String sql = EXAM_SELECT + " WHERE CAST(e.ExamDate AS DATE) = ? ORDER BY CAST(e.StartTime AS TIME)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, examDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExamSession(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateStatus(int sessionId, String status) {
        String sql = "UPDATE Exam SET [Status] = ? WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean finishSession(int sessionId, String status, Timestamp endTime) {
        String sql = "UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, endTime != null ? endTime : new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<ExamSummaryDTO> fetchList(String sql) {
        List<ExamSummaryDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExamSummaryDTO mapResultSetToExamSession(ResultSet rs) throws SQLException {
        ExamSummaryDTO es = new ExamSummaryDTO();
        es.setId(rs.getInt("id"));
        es.setExamId(rs.getInt("examId"));
        es.setMorningSession(rs.getBoolean("isMorningSession"));
        es.setSessionName(rs.getString("sessionName"));
        es.setLicenseTypeId(rs.getInt("licenseTypeId"));
        es.setExamTypeId(rs.getInt("examTypeId"));
        es.setExamDate(rs.getDate("examDate"));
        es.setShiftStartTime(rs.getTime("shiftStartTime"));
        es.setShiftEndTime(rs.getTime("shiftEndTime"));
        Timestamp scheduledStart = rs.getTimestamp("scheduledStartAt");
        es.setScheduledStartAt(rs.wasNull() ? null : scheduledStart);
        Timestamp scheduledEnd = rs.getTimestamp("scheduledEndAt");
        es.setScheduledEndAt(rs.wasNull() ? null : scheduledEnd);
        es.setAreaId(rs.getInt("areaId"));
        es.setStatus(rs.getString("status"));
        es.setMaxCandidates(rs.getInt("maxCandidates"));
        es.setRegisteredCount(rs.getInt("registeredCount"));
        Timestamp created = rs.getTimestamp("createdAt");
        es.setCreatedAt(rs.wasNull() ? scheduledStart : created);
        es.setLicenseCode(rs.getString("licenseCode"));
        es.setExamCode(rs.getString("examCode"));
        es.setExamTypeName(rs.getString("examTypeName"));
        es.setAreaName(rs.getString("areaName"));
        return es;
    }
}

