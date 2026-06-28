package dao.impl;


import dbconnection.DBContext;

import dao.AuditDAO;

import model.user.Audit;

import model.staff.StaffProcedureKpiModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AuditDAOImpl extends DBContext implements AuditDAO {

    private static final String AUDIT_SELECT = """
            SELECT a.AuditId,
                   a.EntityName,
                   a.EntityId,
                   a.Action,
                   a.OldValue,
                   a.NewValue,
                   a.Details,
                   a.Reason,
                   a.UserId,
                   a.CreatedAt
            FROM Audit a
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

    
    @Override
    public List<Audit> getLogsByUserToday(int userId) {
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> ps.setInt(1, userId), true);
    }

    
    @Override
    public List<Audit> getAllLogsToday() {
        return queryLogs(AUDIT_SELECT + " WHERE a.CreatedAt >= CAST(GETDATE() AS DATE) ORDER BY a.CreatedAt DESC",
                ps -> {}, false);
    }

    
    @Override
    public List<Audit> getLogsByUserAndDate(int userId, String dateStr) {
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
    public List<Audit> getAllLogsByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC",
                    ps -> ps.setString(1, dateStr), false);
        }
        return queryLogs(AUDIT_SELECT + " ORDER BY a.CreatedAt DESC", ps -> {}, false);
    }

    
    @Override
    public List<Audit> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
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
    public List<Audit> getAllLogsByDatePaginated(String dateStr, int page, int pageSize) {
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

    
    private List<Audit> queryLogs(String sql, SqlBinder binder, boolean limited) {
        List<Audit> list = new ArrayList<>();
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

    
    @Override
    public StaffProcedureKpiModel getStaffProcedureKpi(int userId, String filterDate) {
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
                    return new StaffProcedureKpiModel(rs.getInt("completedCount"), rs.getDouble("totalFees"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StaffProcedureKpiModel(0, 0);
    }

    
    @Override
    public List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize) {
        return getLogsForSessionPaginated(sessionId, page, pageSize, null);
    }

    
    @Override
    public int getLogsCountForSession(int sessionId) {
        return getLogsCountForSession(sessionId, null);
    }

    
    @Override
    public List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;
        String searchClause = buildSessionSearchClause(searchQuery);
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE + searchClause
                + " ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Audit> list = new ArrayList<>();
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

    
    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        String searchClause = buildSessionSearchClause(searchQuery);
        String sql = "SELECT COUNT(*) FROM Audit a "
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
                 )
                """;
    }

    
    private static void bindSessionParams(PreparedStatement ps, int sessionId, String searchQuery)
            throws SQLException {
        ps.setInt(1, sessionId);
        if (searchQuery != null && !searchQuery.isBlank()) {
            String pattern = "%" + searchQuery.trim() + "%";
            for (int i = 2; i <= 7; i++) {
                ps.setString(i, pattern);
            }
        }
    }

    
    private static int paramIndexAfterSearch(String searchQuery) {
        return (searchQuery != null && !searchQuery.isBlank()) ? 8 : 2;
    }

    
    @Override
    public List<Audit> getViolationLogsForSession(int sessionId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE
                + " AND UPPER(a.Action) = 'WARNING' ORDER BY a.CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        List<Audit> list = new ArrayList<>();
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

    @Override
    public List<Audit> getRecentLogs(int limit) {
        List<Audit> list = new ArrayList<>();
        String sql = "SELECT TOP (" + limit + ") * FROM Audit ORDER BY CreatedAt DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    private Audit mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        Audit log = new Audit();
        log.setAuditId(rs.getLong("AuditId"));
        log.setEntityName(rs.getString("EntityName"));
        log.setEntityId(rs.getString("EntityId"));
        log.setAction(rs.getString("Action"));
        log.setOldValue(rs.getString("OldValue"));
        log.setNewValue(rs.getString("NewValue"));
        log.setDetails(rs.getString("Details"));
        log.setReason(rs.getString("Reason"));
        int userId = rs.getInt("UserId");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }
        log.setCreatedAt(rs.getTimestamp("CreatedAt"));
        return log;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }
}
