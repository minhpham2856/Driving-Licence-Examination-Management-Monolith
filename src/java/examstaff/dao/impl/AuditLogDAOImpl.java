package examstaff.dao.impl;


import examstaff.dbconnection.DBContext;

import examstaff.dao.AuditLogDAO;

import examstaff.model.Audit;
import examstaff.dto.user.AuditDTO;

import examstaff.dto.staff.StaffProcedureKpiDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AuditLogDAO for reading and writing audit trail records.
 * Supports paginated queries by user/date and staff KPI calculation.
 */
public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    private static final String AUDIT_SELECT = """
            SELECT a.AuditId AS id,
                   a.EntityName AS tableName,
                   TRY_CAST(a.EntityId AS INT) AS recordId,
                   a.Action AS action,
                   a.OldValue AS oldValue,
                   a.NewValue AS newValue,
                   ISNULL(a.Details, a.Reason) AS details,
                   a.Reason AS reason,
                   a.UserId AS changedBy,
                   a.CreatedAt AS changedAt,
                   NULL AS ipAddress,
                   NULL AS examId,
                   ISNULL(u.Username, p.FullName) AS changerName
            FROM Audit a
            LEFT JOIN [User] u ON u.UserId = a.UserId
            LEFT JOIN Profile p ON p.UserId = u.UserId
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
                    }, false);
        }
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, offset);
                    ps.setInt(3, pageSize);
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
        String paymentStatusIn = examstaff.enums.PaymentStatus.sqlInClause();
        String sql = """
                SELECT COUNT(DISTINCT x.candidateId) AS completedCount,
                       ISNULL(SUM(x.TotalAmount), 0) AS totalFees
                FROM (
                    SELECT DISTINCT
                        c.CandidateId AS candidateId,
                        p.PaymentId,
                        p.TotalAmount
                    FROM Audit a
                    INNER JOIN Candidate c ON (
                        c.CandidateId = TRY_CAST(NULLIF(NULLIF(LTRIM(RTRIM(a.EntityId)), ''), '0') AS INT)
                        OR a.Reason LIKE N'%' + c.CandidateNumber + N'%'
                    )
                    INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                    INNER JOIN Payment p ON p.ExamEnrollmentId = ee.ExamEnrollmentId
                        AND p.PaymentStatus IN ("""
                + paymentStatusIn + """
                        )
                    WHERE a.UserId = ?
                      AND (
                            a.EntityName IN (N'Thanh toán', N'Payment')
                            OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                            OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU PHI%'
                          )
                      AND (
                            UPPER(ISNULL(a.Action, N'')) IN (
                                N'INSERT', N'UPDATE', N'THÊM', N'NHẬP', N'CẬP NHẬT'
                            )
                            OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                          )
                """;
        if (hasDate) {
            sql += "                      AND CAST(a.CreatedAt AS DATE) = ?\n";
        }
        sql += """
                ) x
                WHERE x.candidateId IS NOT NULL
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

    /** Maps a ResultSet row to an AuditDTO using the aliased column names from AUDIT_SELECT. */
    private AuditDTO mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditDTO log = new AuditDTO();
        log.setTableName(rs.getString("tableName"));
        log.setAction(rs.getString("action"));
        log.setOldValue(rs.getString("oldValue"));
        log.setNewValue(rs.getString("newValue"));
        log.setDetails(rs.getString("details"));
        log.setReason(rs.getString("reason"));
        log.setChangedAt(rs.getTimestamp("changedAt"));
        return log;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
