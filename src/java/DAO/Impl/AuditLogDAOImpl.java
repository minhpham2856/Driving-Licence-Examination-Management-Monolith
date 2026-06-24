package DAO.Impl;

import DBConnection.DBContext;
import DAO.AuditLogDAO;
import Models.AuditLog;
import Models.StaffProcedureKpi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    private static final String AUDIT_SELECT = """
            SELECT a.AuditId AS id,
                   a.EntityName AS tableName,
                   TRY_CAST(a.EntityId AS INT) AS recordId,
                   a.Action AS action,
                   a.OldValue AS oldValue,
                   a.NewValue AS newValue,
                   a.UserId AS changedBy,
                   a.CreatedAt AS changedAt,
                   NULL AS ipAddress,
                   NULL AS sessionId,
                   p.FullName AS changerName
            FROM Audit a
            LEFT JOIN [User] u ON u.UserId = a.UserId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            """;

    @Override
    public boolean insert(AuditLog log) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tbl = log.getTableName();
            if (tbl == null || tbl.trim().isEmpty()) {
                tbl = "Profile";
            }
            String act = log.getAction() != null ? log.getAction() : "UPDATE";
            int userId = log.getChangedBy() > 0 ? log.getChangedBy() : 3;
            int recId = log.getRecordId() != null ? log.getRecordId() : 0;

            ps.setInt(1, userId);
            ps.setString(2, act);
            ps.setString(3, log.getNewValue());
            ps.setString(4, tbl);
            ps.setString(5, String.valueOf(recId));
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
            ps.setTimestamp(8, log.getChangedAt() != null ? log.getChangedAt() : new Timestamp(System.currentTimeMillis()));

            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        log.setId(gk.getLong(1));
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

    @Override
    public List<AuditLog> getLogsByUserToday(int userId) {
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> ps.setInt(1, userId), true);
    }

    @Override
    public List<AuditLog> getAllLogsToday() {
        return queryLogs(AUDIT_SELECT + " WHERE a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> {}, false);
    }

    @Override
    public List<AuditLog> getLogsByUserAndDate(int userId, String dateStr) {
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

    @Override
    public List<AuditLog> getAllLogsByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC",
                    ps -> ps.setString(1, dateStr), false);
        }
        return queryLogs(AUDIT_SELECT + " ORDER BY a.CreatedAt DESC", ps -> {}, false);
    }

    @Override
    public List<AuditLog> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
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

    @Override
    public List<AuditLog> getAllLogsByDatePaginated(String dateStr, int page, int pageSize) {
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

    @Override
    public int getAllLogsCountByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("SELECT COUNT(*) FROM Audit WHERE CAST(CreatedAt AS DATE) = ?",
                    ps -> ps.setString(1, dateStr));
        }
        return count("SELECT COUNT(*) FROM Audit", ps -> {});
    }

    private List<AuditLog> queryLogs(String sql, SqlBinder binder, boolean limited) {
        List<AuditLog> list = new ArrayList<>();
        String finalSql = limited ? sql.replaceFirst("SELECT", "SELECT TOP 200") : sql;
        try (PreparedStatement ps = connection.prepareStatement(finalSql)) {
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

    private int count(String sql, SqlBinder binder) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    @Override
    public StaffProcedureKpi getStaffProcedureKpi(int userId, String filterDate) {
        boolean hasDate = filterDate != null && !filterDate.trim().isEmpty();
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT COUNT(DISTINCT x.CandidateId) AS completedCount,
                       ISNULL(SUM(x.feeAmount), 0) AS totalFees
                FROM (
                    SELECT DISTINCT
                           p.PaymentId,
                           p.CandidateId,
                """);
        sql.append(Utils.ProcedureFeeTotals.SQL_PAID_AMOUNT);
        sql.append("""
                       AS feeAmount
                    FROM Audit a
                    INNER JOIN Candidate c ON TRY_CAST(a.EntityId AS INT) = c.CandidateId
                    INNER JOIN Payment p ON p.CandidateId = c.CandidateId
                        AND p.PaymentId = (
                            SELECT TOP 1 p2.PaymentId
                            FROM Payment p2
                            WHERE p2.CandidateId = c.CandidateId
                              AND p2.PaymentStatus IN ('Completed', 'Paid')
                            ORDER BY p2.PaidAt DESC, p2.PaymentId DESC
                        )
                """);
        sql.append(Utils.ProcedureFeeTotals.SQL_FEE_LINES_JOIN);
        sql.append("""
                    WHERE a.UserId = ?
                      AND a.EntityName = N'Payment'
                      AND a.Action = N'INSERT'
                      AND TRY_CAST(a.EntityId AS INT) > 0
                      AND
                """);
        sql.append(Utils.ProcedureFeeTotals.SQL_PAYMENT_ACTIVE);
        if (hasDate) {
            sql.append("\n                      AND CAST(a.CreatedAt AS DATE) = ?");
        }
        sql.append("""
                ) x
                """);
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int param = 1;
            ps.setInt(param++, userId);
            if (hasDate) {
                ps.setString(param, filterDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StaffProcedureKpi(rs.getInt("completedCount"), rs.getDouble("totalFees"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StaffProcedureKpi(0, 0);
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        log.setTableName(rs.getString("tableName"));
        log.setRecordId(rs.getInt("recordId"));
        if (rs.wasNull()) {
            log.setRecordId(null);
        }
        log.setAction(rs.getString("action"));
        log.setOldValue(rs.getString("oldValue"));
        log.setNewValue(rs.getString("newValue"));
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
