package dao.view.impl;

import dao.view.ExamSessionViewDAO;
import dbconnection.DBContext;
import model.view.ExamSessionSummary;
import util.examstaff.SessionLabel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExamSessionViewDAOImpl extends DBContext implements ExamSessionViewDAO {

    private static final String SESSION_SELECT =
            "SELECT s.SessionId AS sessionId, "
            + "s.ExamId AS examId, "
            + "s.IsMorningSession AS isMorningSession, "
            + SessionLabel.SQL_WITH_SECTION + " AS sessionName, "
            + "e.LicenceId AS licenseTypeId, "
            + "ISNULL(sect.examTypeId, 1) AS examTypeId, "
            + "CAST(s.StartTime AS DATE) AS examDate, "
            + "CAST(s.StartTime AS TIME) AS shiftStartTime, "
            + "CAST(s.EndTime AS TIME) AS shiftEndTime, "
            + "ISNULL(sea.ExamAreaId, 0) AS areaId, "
            + "s.[Status] AS status, "
            + "ISNULL(ea.Capacity, 100) AS maxCandidates, "
            + "(SELECT COUNT(*) FROM ExamEnrollment ee2 WHERE ee2.SessionId = s.SessionId) AS registeredCount, "
            + "s.StartTime AS createdAt, "
            + "l.LicenceClass AS licenseCode, "
            + "e.ExamCode AS examCode, "
            + "sect.examTypeName, "
            + "ea.AreaName AS areaName "
            + "FROM [Session] s "
            + "JOIN Exam e ON e.ExamId = s.ExamId "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId "
            + "LEFT JOIN ( "
            + "    SELECT ses.SessionId, MIN(sea2.ExamAreaId) AS ExamAreaId "
            + "    FROM Session_ExamArea sea2 "
            + "    JOIN [Session] ses ON ses.SessionId = sea2.SessionId "
            + "    GROUP BY ses.SessionId "
            + ") sea ON sea.SessionId = s.SessionId "
            + "LEFT JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId "
            + "LEFT JOIN ( "
            + "    SELECT ses.SessionId, "
            + "           CASE "
            + "               WHEN MIN(es.SectionName) LIKE N'%Lý thuyết%' OR MIN(es.SectionName) LIKE '%Theory%' THEN 1 "
            + "               WHEN MIN(es.SectionName) LIKE N'%Thực hành%' OR MIN(es.SectionName) LIKE N'%Sa hình%' OR MIN(es.SectionName) LIKE '%Practical%' THEN 2 "
            + "               WHEN MIN(es.SectionName) LIKE N'%Đường%' OR MIN(es.SectionName) LIKE '%Road%' THEN 4 "
            + "               ELSE 1 "
            + "           END AS examTypeId, "
            + "           MIN(es.SectionName) AS examTypeName "
            + "    FROM Session_ExamSection ses "
            + "    JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId "
            + "    GROUP BY ses.SessionId "
            + ") sect ON sect.SessionId = s.SessionId";

    private static final String SESSION_SELECT_BASIC =
            "SELECT s.SessionId AS sessionId, "
            + "s.ExamId AS examId, "
            + "s.IsMorningSession AS isMorningSession, "
            + SessionLabel.SQL_SHIFT_ONLY + " AS sessionName, "
            + "e.LicenceId AS licenseTypeId, "
            + "1 AS examTypeId, "
            + "CAST(s.StartTime AS DATE) AS examDate, "
            + "CAST(s.StartTime AS TIME) AS shiftStartTime, "
            + "CAST(s.EndTime AS TIME) AS shiftEndTime, "
            + "0 AS areaId, "
            + "s.[Status] AS status, "
            + "100 AS maxCandidates, "
            + "(SELECT COUNT(*) FROM ExamEnrollment ee2 WHERE ee2.SessionId = s.SessionId) AS registeredCount, "
            + "s.StartTime AS createdAt, "
            + "l.LicenceClass AS licenseCode, "
            + "e.ExamCode AS examCode, "
            + "CAST(NULL AS NVARCHAR(200)) AS examTypeName, "
            + "CAST(NULL AS NVARCHAR(200)) AS areaName "
            + "FROM [Session] s "
            + "JOIN Exam e ON e.ExamId = s.ExamId "
            + "JOIN Licence l ON l.LicenceId = e.LicenceId";

    @Override
    public List<ExamSessionSummary> findAllOrdered() {
        List<ExamSessionSummary> list = fetchList(SESSION_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, CAST(s.StartTime AS TIME) DESC");
        if (list.isEmpty()) {
            return findAllBasicOrdered();
        }
        return list;
    }

    @Override
    public List<ExamSessionSummary> findAllBasicOrdered() {
        return fetchList(SESSION_SELECT_BASIC
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, CAST(s.StartTime AS TIME) DESC");
    }

    @Override
    public ExamSessionSummary findBySessionId(int sessionId) {
        ExamSessionSummary row = fetchOne(SESSION_SELECT + " WHERE s.SessionId = ?", sessionId);
        if (row != null) {
            return row;
        }
        return fetchOne(SESSION_SELECT_BASIC + " WHERE s.SessionId = ?", sessionId);
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

    private ExamSessionSummary fetchOne(String sql, int sessionId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
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
        row.setSessionId(rs.getInt("sessionId"));
        row.setExamId(rs.getInt("examId"));
        row.setMorningSession(rs.getBoolean("isMorningSession"));
        row.setSessionName(rs.getString("sessionName"));
        row.setLicenseTypeId(rs.getInt("licenseTypeId"));
        row.setExamTypeId(rs.getInt("examTypeId"));
        row.setExamDate(rs.getDate("examDate"));
        row.setShiftStartTime(rs.getTime("shiftStartTime"));
        row.setShiftEndTime(rs.getTime("shiftEndTime"));
        row.setAreaId(rs.getInt("areaId"));
        row.setStatus(rs.getString("status"));
        row.setMaxCandidates(rs.getInt("maxCandidates"));
        row.setRegisteredCount(rs.getInt("registeredCount"));
        Timestamp created = rs.getTimestamp("createdAt");
        row.setCreatedAt(rs.wasNull() ? null : created);
        row.setLicenseCode(rs.getString("licenseCode"));
        row.setExamCode(rs.getString("examCode"));
        row.setExamTypeName(rs.getString("examTypeName"));
        row.setAreaName(rs.getString("areaName"));
        return row;
    }
}
