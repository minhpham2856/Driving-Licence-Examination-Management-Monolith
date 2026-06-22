package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.AuditLogDAO;
import Models.Audit;
import DTOs.AuditDTO;
import DTOs.StaffProcedureKpiDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    private final DBContext ctx;

    public AuditLogDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public boolean insert(Audit log) {
        String sql = """
                insert into Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<AuditDTO> getLogsByUserToday(int userId) {
        return queryLogs(AUDIT_SELECT + " where a.UserId = ? and a.CreatedAt >= CAST(GETDATE() as DATE) order by a.CreatedAt desc",
                ps -> ps.setInt(1, userId), true);
    }

    @Override
    public List<AuditDTO> getAllLogsToday() {
        return queryLogs(AUDIT_SELECT + " where a.CreatedAt >= CAST(GETDATE() as DATE) order by a.CreatedAt desc",
                ps -> {}, false);
    }

    @Override
    public List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " where a.UserId = ? and CAST(a.CreatedAt as DATE) = ? order by a.CreatedAt desc",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    }, true);
        }
        return queryLogs(AUDIT_SELECT + " where a.UserId = ? order by a.CreatedAt desc",
                ps -> ps.setInt(1, userId), true);
    }

    @Override
    public List<AuditDTO> getAllLogsByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " where CAST(a.CreatedAt as DATE) = ? order by a.CreatedAt desc",
                    ps -> ps.setString(1, dateStr), false);
        }
        return queryLogs(AUDIT_SELECT + " order by a.CreatedAt desc", ps -> {}, false);
    }

    @Override
    public List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " where a.UserId = ? and CAST(a.CreatedAt as DATE) = ? order by a.CreatedAt desc OFFSET ? rows FETCH next ? rows only",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                        ps.setInt(3, offset);
                        ps.setInt(4, pageSize);
                    }, true);
        }
        return queryLogs(AUDIT_SELECT + " where a.UserId = ? order by a.CreatedAt desc OFFSET ? rows FETCH next ? rows only",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, offset);
                    ps.setInt(3, pageSize);
                }, true);
    }

    @Override
    public List<AuditDTO> getAllLogsByDatePaginated(String dateStr, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " where CAST(a.CreatedAt as DATE) = ? order by a.CreatedAt desc OFFSET ? rows FETCH next ? rows only",
                    ps -> {
                        ps.setString(1, dateStr);
                        ps.setInt(2, offset);
                        ps.setInt(3, pageSize);
                    }, false);
        }
        return queryLogs(AUDIT_SELECT + " order by a.CreatedAt desc OFFSET ? rows FETCH next ? rows only",
                ps -> {
                    ps.setInt(1, offset);
                    ps.setInt(2, pageSize);
                }, false);
    }

    @Override
    public int getLogsCountByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("select count(*) from Audit where UserId = ? and CAST(CreatedAt as DATE) = ?",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    });
        }
        return count("select count(*) from Audit where UserId = ?", ps -> ps.setInt(1, userId));
    }

    @Override
    public int getAllLogsCountByDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("select count(*) from Audit where CAST(CreatedAt as DATE) = ?",
                    ps -> ps.setString(1, dateStr));
        }
        return count("select count(*) from Audit", ps -> {});
    }

    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        boolean hasDate = filterDate != null && !filterDate.trim().isEmpty();
        String sql = """
                select count(*) as completedCount,
                       ISNULL(SUM(x.TotalAmount), 0) as totalFees
                from (
                    select distinct p.PaymentId, p.TotalAmount
                    from Payment p
                    inner join Candidate c on c.CandidateId = p.CandidateId
                    where p.PaymentStatus in ('Completed', 'Paid')
                      and c.PhotoImageUrl is not null
                      and LEN(LTRIM(RTRIM(c.PhotoImageUrl))) > 0
                      and exists (
                          select 1
                          from Audit a
                          where a.UserId = ?
                            and a.EntityName = 'Payment'
                            and a.Action = 'INSERT'
                            and (
                                TRY_CAST(a.EntityId as INT) = c.CandidateId
                                or a.NewValue like N'%' + c.CandidateNumber + N'%'
                                or a.Reason like N'%' + c.CandidateNumber + N'%'
                            )
                """;
        if (hasDate) {
            sql += "                            and CAST(a.CreatedAt as DATE) = ?\n";
        }
        sql += """
                      )
                ) x
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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

    @Override
    public List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize) {
        return getLogsForSessionPaginated(sessionId, page, pageSize, null);
    }

    @Override
    public int getLogsCountForSession(int sessionId) {
        return getLogsCountForSession(sessionId, null);
    }

    @Override
    public List<AuditDTO> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;
        String searchClause = buildSessionSearchClause(searchQuery);
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE + searchClause + """
                 order by a.CreatedAt desc OFFSET ? rows FETCH next ? rows only
                """;
        List<AuditDTO> list = new ArrayList<>();

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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
        String sql = """
                select count(*) from Audit a
                left join [User] u on u.UserId = a.UserId
                left join Profile p on p.UserId = u.UserId
                """ + SESSION_AUDIT_WHERE + searchClause;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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

    @Override
    public List<AuditDTO> getViolationLogsForSession(int sessionId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 5000));
        String sql = AUDIT_SELECT + SESSION_AUDIT_WHERE + """
                 and UPPER(a.Action) = 'WARNING' order by a.CreatedAt desc OFFSET 0 rows FETCH next ? rows only
                """;
        List<AuditDTO> list = new ArrayList<>();

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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

    private List<AuditDTO> queryLogs(String sql, SqlBinder binder, boolean limited) {
        List<AuditDTO> list = new ArrayList<>();
        String finalSql = limited ? sql.replaceFirst("select", "select top 200") : sql;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(finalSql)) {
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
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
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

    private static String buildSessionSearchClause(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return "";
        }
        return """
                  and (
                    a.Action like ?
                    or a.EntityName like ?
                    or a.NewValue like ?
                    or a.OldValue like ?
                    or a.Reason like ?
                    or a.Details like ?
                    or ISNULL(u.Username, p.FullName) like ?
                  )
                """;
    }

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

    private static int paramIndexAfterSearch(String searchQuery) {
        return (searchQuery != null && !searchQuery.isBlank()) ? 9 : 2;
    }

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

    private static final String AUDIT_SELECT = """
            select a.AuditId as id,
                   a.EntityName as tableName,
                   TRY_CAST(a.EntityId as INT) as recordId,
                   a.Action as action,
                   a.OldValue as oldValue,
                   a.NewValue as newValue,
                   a.Details as details,
                   a.Reason as reason,
                   a.UserId as changedBy,
                   a.CreatedAt as changedAt,
                   NULL as ipAddress,
                   NULL as sessionId,
                   ISNULL(u.Username, p.FullName) as changerName
            from Audit a
            left join [User] u on u.UserId = a.UserId
            left join Profile p on p.UserId = u.UserId
            """;

    private static final String SESSION_AUDIT_WHERE = """
            where exists (
                select 1
                from ExamEnrollment ec
                inner join Candidate c on c.CandidateId = ec.CandidateId
                where ec.SessionId = ?
                  and (
                        TRY_CAST(a.EntityId as INT) = c.CandidateId
                        or a.NewValue like N'%' + c.CandidateNumber + N'%'
                        or a.Reason like N'%' + c.CandidateNumber + N'%'
                        or a.OldValue like N'%' + c.CandidateNumber + N'%'
                        or a.Details like N'%' + c.CandidateNumber + N'%'
                      )
            )
            """;
}
