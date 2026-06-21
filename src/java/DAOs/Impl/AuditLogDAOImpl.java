package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.AuditLogDAO;
import Models.Audit;
import DTOs.AuditDTO;
import DTOs.StaffProcedureKpiDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AuditLogDAO for reading and writing audit trail records.
 * Supports paginated queries, session-scoped log filtering, staff KPI calculation,
 * and violation (WARNING action) retrieval.
 */
public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    private static final String AUDIT_SELECT = """
            SELECT a.AuditId AS id,
                   a.EntityName AS tableName,
                   TRY_CAST(a.EntityId AS INT) AS recordId,
                   a.Action AS action,
                   a.OldValue AS oldValue,
                   a.NewValue AS newValue,
                   a.Details AS details,
                   a.Reason AS reason,
                   a.UserId AS changedBy,
                   a.CreatedAt AS changedAt,
                   NULL AS ipAddress,
                   NULL AS sessionId,
                   ISNULL(u.Username, p.FullName) AS changerName
            FROM Audit a
            LEFT JOIN [User] u ON u.UserId = a.UserId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            """;

    private static final String SESSION_AUDIT_WHERE = """
            WHERE EXISTS (
                SELECT 1
                FROM Exam_Candidate ec
                INNER JOIN Candidate c ON c.CandidateId = ec.CandidateId
                WHERE ec.SessionId = ?
                  AND (
                        TRY_CAST(a.EntityId AS INT) = c.CandidateId
                        OR a.NewValue LIKE N'%' + c.CandidateNumber + N'%'
                        OR a.Reason LIKE N'%' + c.CandidateNumber + N'%'
                        OR a.OldValue LIKE N'%' + c.CandidateNumber + N'%'
                        OR a.Details LIKE N'%' + c.CandidateNumber + N'%'
                      )
            )
            """;

    /**
     * Inserts a new audit log entry with sensible defaults for null fields.
     * Generates the audit ID via RETURN_GENERATED_KEYS.
     *
     * @param log the Audit entry to persist
     * @return true if insertion succeeded
     */
    @Override
    public boolean insert(Audit log) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tbl = log.getEntityName();
            if (tbl == null || tbl.trim().isEmpty()) {
                tbl = "Profile";
            }
            String act = log.getAction() != null ? log.getAction() : "UPDATE";
            int userId = log.getUserId() != null && log.getUserId() > 0 ? log.getUserId() : 3;
            String recId = log.getEntityId() != null ? log.getEntityId() : "0";

            ps.setInt(1, userId);
            ps.setString(2, act);
            if (log.getReason() != null) {
                ps.setString(3, log.getReason());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
            ps.setString(4, tbl);
            ps.setString(5, recId);
            if (log.getOldValue() != null) {
                ps.setString(6, log.getOldValue());
            } else {
                ps.setNull(6, Types.NVARCHAR);
            }
            if (log.getNewValue() != null) {
                ps.setString(7, log.getNewValue());
            } else {
                ps.setNull(7, Types.NVARCHAR);
            }
            if (log.getDetails() != null) {
                ps.setString(8, log.getDetails());
            } else {
                ps.setNull(8, Types.NVARCHAR);
            }
            ps.setTimestamp(9, log.getCreatedAt() != null ? log.getCreatedAt() : new Timestamp(System.currentTimeMillis()));

            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        log.setAuditId(gk.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("AuditLogDAOImpl insert failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns today's audit log entries for a specific user (capped at 200 rows).
     *
     * @param userId the user whose logs to retrieve
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getLogsByUserToday(int userId) {
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> ps.setInt(1, userId), true);
    }

    /**
     * Returns all audit log entries created today (capped at 200 rows).
     *
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getAllLogsToday() {
        return queryLogs(AUDIT_SELECT + " WHERE a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> {}, false);
    }

    /**
     * Returns logs for a specific user, optionally filtered by date (capped at 200 rows).
     *
     * @param userId  the user ID
     * @param dateStr optional date string (yyyy-MM-dd); if null/empty returns all dates
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    }, true);
        }
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? ORDER BY a.CreatedAt DESC",
                ps -> ps.setInt(1, userId), true);
    }

    /**
     * Returns all logs optionally filtered by date (capped at 200 rows).
     *
     * @param dateStr optional date string (yyyy-MM-dd)
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getAllLogsByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC",
                    ps -> ps.setString(1, dateStr), false);
        }
        return queryLogs(AUDIT_SELECT + " ORDER BY a.CreatedAt DESC", ps -> {}, false);
    }

    /**
     * Paginated query of audit logs for a user, with optional date filter.
     * Uses OFFSET-FETCH for server-side pagination.
     *
     * @param userId   the user ID
     * @param dateStr  optional date filter (yyyy-MM-dd)
     * @param page     the page number (1-based)
     * @param pageSize the number of rows per page
     * @return list of AuditDTO records for the requested page
     */
    @Override
    public List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                        ps.setInt(3, offset);
                        ps.setInt(4, pageSize);
                    }, true);
        }
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, offset);
                    ps.setInt(3, pageSize);
                }, true);
    }

    /**
     * Paginated query of all audit logs, with optional date filter.
     *
     * @param dateStr  optional date filter (yyyy-MM-dd)
     * @param page     the page number (1-based)
     * @param pageSize the number of rows per page
     * @return list of AuditDTO records for the requested page
     */
    @Override
    public List<AuditDTO> getAllLogsByDatePaginated(String dateStr, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                    ps -> {
                        ps.setString(1, dateStr);
                        ps.setInt(2, offset);
                        ps.setInt(3, pageSize);
                    }, false);
        }
        return queryLogs(AUDIT_SELECT + " ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                ps -> {
                    ps.setInt(1, offset);
                    ps.setInt(2, pageSize);
                }, false);
    }

    /**
     * Returns the total log count for a user, optionally filtered by date.
     *
     * @param userId  the user ID
     * @param dateStr optional date filter
     * @return the count
     */
    @Override
    public int getLogsCountByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("SELECT COUNT(*) FROM Audit WHERE UserId = ? AND CAST(CreatedAt AS DATE) = ?",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    });
        }
        return count("SELECT COUNT(*) FROM Audit WHERE UserId = ?", ps -> ps.setInt(1, userId));
    }

    /**
     * Returns the total log count, optionally filtered by date.
     *
     * @param dateStr optional date filter
     * @return the count
     */
    @Override
    public int getAllLogsCountByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("SELECT COUNT(*) FROM Audit WHERE CAST(CreatedAt AS DATE) = ?",
                    ps -> ps.setString(1, dateStr));
        }
        return count("SELECT COUNT(*) FROM Audit", ps -> {});
    }

    /**
     * Executes a log query with parameter binding and optional row cap (TOP 200).
     */
    private List<AuditDTO> queryLogs(String sql, SqlBinder binder, boolean limited) {
        List<AuditDTO> list = new ArrayList<>();
        String finalSql = limited ? sql.replaceFirst("SELECT", "SELECT TOP 200") : sql;
        try (PreparedStatement ps = getConnection().prepareStatement(finalSql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Executes a COUNT query with parameter binding. */
    private int count(String sql, SqlBinder binder) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Calculates staff KPI: number of completed payments and total fee amount
     * processed by a staff member, optionally filtered by date.
     *
     * @param userId     the staff user ID
     * @param filterDate optional date filter (yyyy-MM-dd)
     * @return StaffProcedureKpiDTO with completedCount and totalFees
     */
    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        boolean hasDate = filterDate != null && !filterDate.trim().isEmpty();
        String sql = """
                SELECT COUNT(*) AS completedCount,
                       ISNULL(SUM(x.TotalAmount), 0) AS totalFees
                FROM (
                    SELECT DISTINCT p.PaymentId, p.TotalAmount
                    FROM Payment p
                    INNER JOIN Candidate c ON c.CandidateId = p.CandidateId
                    WHERE p.PaymentStatus IN ('Completed', 'Paid')
                      AND c.PhotoImageUrl IS NOT NULL
                      AND LEN(LTRIM(RTRIM(c.PhotoImageUrl))) > 0
                      AND EXISTS (
                          SELECT 1
                          FROM Audit a
                          WHERE a.UserId = ?
                            AND a.EntityName = 'Payment'
                            AND a.Action = 'INSERT'
                            AND (
                                TRY_CAST(a.EntityId AS INT) = c.CandidateId
                                OR a.NewValue LIKE N'%' + c.CandidateNumber + N'%'
                                OR a.Reason LIKE N'%' + c.CandidateNumber + N'%'
                            )
                """;
        if (hasDate) {
            sql += "                            AND CAST(a.CreatedAt AS DATE) = ?\n";
        }
        sql += """
                      )
                ) x
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (hasDate) {
                ps.setString(2, filterDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StaffProcedureKpiDTO(rs.getInt("completedCount"), rs.getDouble("totalFees"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StaffProcedureKpiDTO(0, 0);
    }

    /**
     * Paginated query for logs related to a session (without search filter).
     *
     * @param sessionId the SessionId
     * @param page      page number (1-based)
     * @param pageSize  rows per page
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize) {
        return getLogsForSessionPaginated(sessionId, page, pageSize, null);
    }

    /**
     * Returns the count of log entries for a session (without search filter).
     *
     * @param sessionId the SessionId
     * @return the count
     */
    @Override
    public int getLogsCountForSession(int sessionId) {
        return getLogsCountForSession(sessionId, null);
    }

    /**
     * Paginated query for session logs with an optional text search across
     * action, entity name, values, reason, details, and changer name.
     *
     * @param sessionId   the SessionId
     * @param page        page number (1-based)
     * @param pageSize    rows per page
     * @param searchQuery optional full-text search keyword
     * @return list of AuditDTO records
     */
    @Override
    public List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;
        String searchClause = buildSessionSearchClause(searchQuery);
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE + searchClause
                + " ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AuditDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindSessionParams(ps, sessionId, searchQuery);
            ps.setInt(paramIndexAfterSearch(searchQuery), offset);
            ps.setInt(paramIndexAfterSearch(searchQuery) + 1, safeSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns the count of session log entries, optionally filtered by search keyword.
     *
     * @param sessionId   the SessionId
     * @param searchQuery optional search keyword
     * @return the count
     */
    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        String searchClause = buildSessionSearchClause(searchQuery);
        String sql = "SELECT COUNT(*) FROM Audit a "
                + "LEFT JOIN [User] u ON u.UserId = a.UserId "
                + "LEFT JOIN Profile p ON p.UserId = u.UserId "
                + SESSION_AUDIT_WHERE + searchClause;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindSessionParams(ps, sessionId, searchQuery);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Builds the WHERE clause fragment for session search, or empty string if no query. */
    private static String buildSessionSearchClause(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return "";
        }
        return """
                 AND (
                    a.Action LIKE ?
                    OR a.EntityName LIKE ?
                    OR a.NewValue LIKE ?
                    OR a.OldValue LIKE ?
                    OR a.Reason LIKE ?
                    OR a.Details LIKE ?
                    OR ISNULL(u.Username, p.FullName) LIKE ?
                 )
                """;
    }

    /** Binds parameters for session-scoped audit queries including optional search. */
    private static void bindSessionParams(PreparedStatement ps, int sessionId, String searchQuery)
            throws SQLException {
        ps.setInt(1, sessionId);
        if (searchQuery != null && !searchQuery.isBlank()) {
            String pattern = "%" + searchQuery.trim() + "%";
            for (int i = 2; i <= 8; i++) {
                ps.setString(i, pattern);
            }
        }
    }

    /** Calculates the next parameter index after session ID and optional search params. */
    private static int paramIndexAfterSearch(String searchQuery) {
        return (searchQuery != null && !searchQuery.isBlank()) ? 9 : 2;
    }

    /**
     * Retrieves violation (WARNING action) logs for a session, capped at the given limit.
     *
     * @param sessionId the SessionId
     * @param limit     maximum rows to return (clamped between 1 and 5000)
     * @return list of AuditDTO records with action = 'WARNING'
     */
    @Override
    public List<AuditDTO> getViolationLogsForSession(int sessionId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE
                + " AND UPPER(a.Action) = 'WARNING' ORDER BY a.CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        List<AuditDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Maps a ResultSet row to an AuditDTO using the aliased column names from AUDIT_SELECT. */
    private AuditDTO mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditDTO log = new AuditDTO();
        log.setId(rs.getLong("id"));
        log.setTableName(rs.getString("tableName"));
        log.setRecordId(rs.getInt("recordId"));
        if (rs.wasNull()) {
            log.setRecordId(null);
        }
        log.setAction(rs.getString("action"));
        log.setOldValue(rs.getString("oldValue"));
        log.setNewValue(rs.getString("newValue"));
        log.setDetails(rs.getString("details"));
        log.setReason(rs.getString("reason"));
        log.setChangedBy(rs.getInt("changedBy"));
        log.setChangedAt(rs.getTimestamp("changedAt"));
        log.setIpAddress(rs.getString("ipAddress"));
        log.setSessionId(rs.getString("sessionId"));
        log.setChangerName(rs.getString("changerName"));
        return log;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
