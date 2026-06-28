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

    @Override
    public int createManagedSession(String sessionName, int licenceId, int examAreaId, int examSectionId,
                                    Timestamp startTime, Timestamp endTime, String centreName) {
        Connection conn = null;
        try {
            conn = new DBContext().getConnection();
            conn.setAutoCommit(false);

            int examId = createExam(conn, licenceId, startTime, centreName);
            int sessionId;
            String insertSession = """
                    INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
                    VALUES (?, ?, ?, N'Scheduled', ?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSession, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sessionName);
                ps.setTimestamp(2, startTime);
                ps.setTimestamp(3, endTime);
                ps.setInt(4, examId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Cannot read generated SessionId");
                    }
                    sessionId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES (?, ?)")) {
                ps.setInt(1, sessionId);
                ps.setInt(2, examAreaId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES (?, ?)")) {
                ps.setInt(1, sessionId);
                ps.setInt(2, examSectionId);
                ps.executeUpdate();
            }

            conn.commit();
            return sessionId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
        return -1;
    }

    private int createExam(Connection conn, int licenceId, Timestamp startTime, String centreName) throws SQLException {
        String licenceClass = getLicenceClass(conn, licenceId);
        String examCode = buildExamCode(conn, licenceClass, startTime);
        String sql = """
                INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
                VALUES (?, ?, ?, N'Scheduled', ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, examCode);
            ps.setTimestamp(2, startTime);
            ps.setString(3, centreName);
            ps.setInt(4, licenceId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Cannot create exam");
    }

    private String getLicenceClass(Connection conn, int licenceId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT LicenceClass FROM Licence WHERE LicenceId = ?")) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        throw new SQLException("Licence not found: " + licenceId);
    }

    private String buildExamCode(Connection conn, String licenceClass, Timestamp startTime) throws SQLException {
        String datePart = startTime.toLocalDateTime().toLocalDate().toString().replace("-", "");
        String prefix = "EX-" + licenceClass + "-" + datePart;
        String sql = "SELECT COUNT(*) FROM Exam WHERE ExamCode LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return count == 0 ? prefix : prefix + "-" + (count + 1);
            }
        }
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
