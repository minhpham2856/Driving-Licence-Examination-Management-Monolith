package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamSessionDAO;
import DTOs.SessionDTO;
import Models.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation of ExamSessionDAO for managing exam sessions.
public class ExamSessionDAOImpl extends DBContext implements ExamSessionDAO {

    // Retrieves a basic Session model by primary key (no joins, no DTO mapping).
    @Override
    public Session findById(int id) {
        String sql = "SELECT * FROM [Session] WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session s = new Session();
                    s.setId(rs.getInt("SessionId"));
                    s.setSessionName(rs.getString("SessionName"));
                    s.setStartTime(rs.getTimestamp("StartTime"));
                    s.setEndTime(rs.getTimestamp("EndTime"));
                    s.setStatus(rs.getString("Status"));
                    s.setExamId(rs.getInt("ExamId"));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String SESSION_SELECT = """
            SELECT s.SessionId AS id,
                   s.SessionName AS sessionName,
                   e.LicenceId AS licenseTypeId,
                   ISNULL(sect.examTypeId, 1) AS examTypeId,
                   CAST(s.StartTime AS DATE) AS examDate,
                   CAST(s.StartTime AS TIME) AS shiftStartTime,
                   CAST(s.EndTime AS TIME) AS shiftEndTime,
                   ISNULL(sea.ExamAreaId, 0) AS areaId,
                   s.[Status] AS status,
                   ISNULL(ea.Capacity, 100) AS maxCandidates,
                   (SELECT COUNT(*) FROM Exam_Candidate ec2 WHERE ec2.SessionId = s.SessionId) AS registeredCount,
                   s.StartTime AS createdAt,
                   l.LicenceClass AS licenseCode,
                   sect.examTypeName,
                   ea.AreaName AS areaName
            FROM [Session] s
            JOIN Exam e ON e.ExamId = s.ExamId
            JOIN Licence l ON l.LicenceId = e.LicenceId
            LEFT JOIN (
                SELECT ses.SessionId, MIN(sea2.ExamAreaId) AS ExamAreaId
                FROM Session_ExamArea sea2
                JOIN [Session] ses ON ses.SessionId = sea2.SessionId
                GROUP BY ses.SessionId
            ) sea ON sea.SessionId = s.SessionId
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId
            LEFT JOIN (
                SELECT ses.SessionId,
                       MIN(es.ExamSectionId) AS examSectionId,
                       CASE
                           WHEN MIN(es.SectionName) LIKE N'%Lý thuyết%' OR MIN(es.SectionName) LIKE '%Theory%' THEN 1
                           WHEN MIN(es.SectionName) LIKE N'%Thực hành%' OR MIN(es.SectionName) LIKE '%Practical%' THEN 2
                           WHEN MIN(es.SectionName) LIKE N'%Đường%' OR MIN(es.SectionName) LIKE '%Road%' THEN 4
                           ELSE 1
                       END AS examTypeId,
                       MIN(es.SectionName) AS examTypeName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                GROUP BY ses.SessionId
            ) sect ON sect.SessionId = s.SessionId
            """;

    // Retrieves a rich SessionDTO by primary key with all joined fields.
    @Override
    public SessionDTO getById(int id) {
        String sql = SESSION_SELECT + " WHERE s.SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
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

    // Returns sessions with status Scheduled, Open, or InProgress,
    @Override
    public List<SessionDTO> getActiveSessions() {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT
                + " WHERE s.[Status] IN ('Scheduled', 'Open', 'InProgress')"
                + " ORDER BY CAST(s.StartTime AS DATE), CAST(s.StartTime AS TIME)";
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

    // Returns all sessions ordered by start time descending.
    @Override
    public List<SessionDTO> getAllSessions() {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, CAST(s.StartTime AS TIME) DESC";
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

    // Returns sessions scheduled on a specific date.
    @Override
    public List<SessionDTO> getSessionsByExamDate(Date examDate) {
        List<SessionDTO> list = new ArrayList<>();
        String sql = SESSION_SELECT + " WHERE CAST(s.StartTime AS DATE) = ? ORDER BY CAST(s.StartTime AS TIME)";
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

    // Updates the status of a session.
    @Override
    public boolean updateStatus(int sessionId, String status) {
        String sql = "UPDATE [Session] SET [Status] = ? WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private SessionDTO mapResultSetToExamSession(ResultSet rs) throws SQLException {
        SessionDTO es = new SessionDTO();
        es.setId(rs.getInt("id"));
        es.setSessionName(rs.getString("sessionName"));
        es.setLicenseTypeId(rs.getInt("licenseTypeId"));
        es.setExamTypeId(rs.getInt("examTypeId"));
        es.setExamDate(rs.getDate("examDate"));
        es.setShiftStartTime(rs.getTime("shiftStartTime"));
        es.setShiftEndTime(rs.getTime("shiftEndTime"));
        es.setAreaId(rs.getInt("areaId"));
        es.setStatus(rs.getString("status"));
        es.setMaxCandidates(rs.getInt("maxCandidates"));
        es.setRegisteredCount(rs.getInt("registeredCount"));
        Timestamp created = rs.getTimestamp("createdAt");
        es.setCreatedAt(rs.wasNull() ? null : created);
        es.setLicenseCode(rs.getString("licenseCode"));
        es.setExamTypeName(rs.getString("examTypeName"));
        es.setAreaName(rs.getString("areaName"));
        return es;
    }
}
