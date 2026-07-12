package examstaff.dao.view.impl;

import examstaff.dao.view.ExamSessionViewDAO;
import examstaff.dbconnection.DBContext;
import examstaff.model.view.ExamSessionSummary;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Read model kỳ thi (schema mới: một hàng / Exam, không còn [Session]).
 * {@code sessionId} trên view = {@code ExamId} để tương thích UI cũ.
 */
public class ExamSessionViewDAOImpl extends DBContext implements ExamSessionViewDAO {

    private static final String EXAM_SELECT =
            "SELECT e.ExamId AS sessionId, "
            + "e.ExamId AS examId, "
            + "CAST(1 AS BIT) AS isMorningSession, "
            + "COALESCE(NULLIF(LTRIM(RTRIM(e.ExamCode)), N''), "
            + "  N'Hạng ' + l.LicenceClass + N' — ' + CONVERT(NVARCHAR(10), e.ExamDate, 103)) AS sessionName, "
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
            + "N'Lý thuyết + Thực hành' AS examTypeName, "
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
    public List<ExamSessionSummary> findAllOrdered() {
        return fetchList(EXAM_SELECT
                + " ORDER BY CAST(e.ExamDate AS DATE) DESC, e.StartTime DESC");
    }

    @Override
    public ExamSessionSummary findByExamId(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }
        return fetchOne(EXAM_SELECT + " WHERE e.ExamId = ?", sessionId);
    }

    private List<ExamSessionSummary> fetchList(String sql) {
        List<ExamSessionSummary> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ExamSessionSummary fetchOne(String sql, int examId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ExamSessionSummary mapRow(ResultSet rs) throws SQLException {
        ExamSessionSummary row = new ExamSessionSummary();
        row.setExamId(rs.getInt("examId"));
        if (row.getExamId() <= 0) {
            row.setExamId(rs.getInt("sessionId"));
        }
        row.setMorningSession(rs.getBoolean("isMorningSession"));
        row.setSessionName(rs.getString("sessionName"));
        row.setLicenseTypeId(rs.getInt("licenseTypeId"));
        row.setExamTypeId(rs.getInt("examTypeId"));
        row.setExamDate(rs.getDate("examDate"));
        row.setShiftStartTime(rs.getTime("shiftStartTime"));
        row.setShiftEndTime(rs.getTime("shiftEndTime"));
        Timestamp scheduledStart = rs.getTimestamp("scheduledStartAt");
        row.setScheduledStartAt(scheduledStart);
        row.setScheduledEndAt(rs.getTimestamp("scheduledEndAt"));
        row.setAreaId(rs.getInt("areaId"));
        row.setStatus(rs.getString("status"));
        row.setMaxCandidates(rs.getInt("maxCandidates"));
        row.setRegisteredCount(rs.getInt("registeredCount"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        row.setCreatedAt(createdAt != null ? createdAt : scheduledStart);
        row.setLicenseCode(rs.getString("licenseCode"));
        row.setExamCode(rs.getString("examCode"));
        row.setExamTypeName(rs.getString("examTypeName"));
        row.setAreaName(rs.getString("areaName"));
        return row;
    }
}
