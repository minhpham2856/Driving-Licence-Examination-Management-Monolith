package dao.impl;

import dao.AuditDAO;
import dbconnection.DBContext;
import model.Audit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditDAOImpl extends DBContext implements AuditDAO {

    private static final String BASE_SELECT
            = "SELECT AuditId, UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt "
            + "FROM Audit";

    @Override
    public int insert(Audit audit) {
        if (audit == null) {
            return 0;
        }
        String sql = "INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (audit.getUserId() != null && audit.getUserId() > 0) {
                ps.setInt(1, audit.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, audit.getAction());
            ps.setString(3, audit.getReason());
            ps.setString(4, audit.getEntityName());
            ps.setString(5, audit.getEntityId());
            ps.setString(6, audit.getOldValue());
            ps.setString(7, audit.getNewValue());
            ps.setString(8, audit.getDetails());
            Timestamp createdAt = audit.getCreatedAt() != null ? audit.getCreatedAt() : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(9, createdAt);
            if (ps.executeUpdate() == 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Audit> getRecentLogs(int limit) {
        int safeLimit = limit > 0 ? limit : 50;
        String sql = BASE_SELECT + " ORDER BY CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return queryList(sql, ps -> ps.setInt(1, safeLimit));
    }

    @Override
    public List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) {
        int safePage = Math.max(page, 1);
        int safeSize = pageSize > 0 ? pageSize : 20;
        int offset = (safePage - 1) * safeSize;
        StringBuilder sql = new StringBuilder(BASE_SELECT)
                .append(" WHERE EntityId LIKE ?");
        String pattern = "%" + sessionId + "-%";
        if (searchQuery != null && !searchQuery.isBlank()) {
            sql.append(" AND (Reason LIKE ? OR NewValue LIKE ? OR Details LIKE ?)");
        }
        sql.append(" ORDER BY CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        return queryList(sql.toString(), ps -> {
            int idx = 1;
            ps.setString(idx++, pattern);
            if (searchQuery != null && !searchQuery.isBlank()) {
                String q = "%" + searchQuery.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, safeSize);
        });
    }

    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Audit WHERE EntityId LIKE ?");
        String pattern = "%" + sessionId + "-%";
        if (searchQuery != null && !searchQuery.isBlank()) {
            sql.append(" AND (Reason LIKE ? OR NewValue LIKE ? OR Details LIKE ?)");
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, pattern);
            if (searchQuery != null && !searchQuery.isBlank()) {
                String q = "%" + searchQuery.trim() + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx, q);
            }
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
    public List<Audit> getViolationLogsForSession(int sessionId, int limit) {
        int safeLimit = limit > 0 ? limit : 20;
        String sql = "SELECT a.AuditId, a.UserId, a.Action, a.Reason, a.EntityName, a.EntityId, "
                + "a.OldValue, a.NewValue, a.Details, a.CreatedAt "
                + "FROM Audit a "
                + "INNER JOIN ExamEnrollment e ON TRY_CAST(a.EntityId AS INT) = e.CandidateId "
                + "WHERE e.SessionId = ? "
                + "AND (a.Action = N'Cảnh báo' OR a.NewValue LIKE N'%Vi phạm%' OR a.NewValue LIKE N'%đình chỉ%') "
                + "ORDER BY a.CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return queryList(sql, ps -> {
            ps.setInt(1, sessionId);
            ps.setInt(2, safeLimit);
        });
    }

    @Override
    public List<Audit> searchAll(String keyword, int limit) {
        int safeLimit = limit > 0 ? limit : 100;
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" WHERE Action LIKE ? OR EntityName LIKE ? OR NewValue LIKE ? "
                    + "OR Details LIKE ? OR Reason LIKE ?");
        }
        sql.append(" ORDER BY CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
        return queryList(sql.toString(), ps -> {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            ps.setInt(idx, safeLimit);
        });
    }

    @Override
    public List<Audit> getLogsByUser(int userId, String dateFilter) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE UserId = ?");
        Timestamp start = null;
        Timestamp end = null;
        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            try {
                java.sql.Date day = java.sql.Date.valueOf(dateFilter.trim());
                start = new Timestamp(day.getTime());
                end = new Timestamp(day.getTime() + (24L * 60 * 60 * 1000) - 1);
                sql.append(" AND CreatedAt >= ? AND CreatedAt <= ?");
            } catch (IllegalArgumentException ex) {
                // Invalid date format; fall back to no date filter.
            }
        }
        sql.append(" ORDER BY CreatedAt DESC");
        return queryList(sql.toString(), ps -> {
            int idx = 1;
            ps.setInt(idx++, userId);
            if (start != null) {
                ps.setTimestamp(idx++, start);
                ps.setTimestamp(idx, end);
            }
        });
    }

    private List<Audit> queryList(String sql, StatementBinder binder) {
        List<Audit> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Audit map(ResultSet rs) throws SQLException {
        Audit audit = new Audit();
        audit.setAuditId(rs.getLong("AuditId"));
        int userId = rs.getInt("UserId");
        if (!rs.wasNull()) {
            audit.setUserId(userId);
        }
        audit.setAction(rs.getString("Action"));
        audit.setReason(rs.getString("Reason"));
        audit.setEntityName(rs.getString("EntityName"));
        audit.setEntityId(rs.getString("EntityId"));
        audit.setOldValue(rs.getString("OldValue"));
        audit.setNewValue(rs.getString("NewValue"));
        audit.setDetails(rs.getString("Details"));
        audit.setCreatedAt(rs.getTimestamp("CreatedAt"));
        return audit;
    }

    @FunctionalInterface
    private interface StatementBinder {

        void bind(PreparedStatement ps) throws SQLException;
    }
}
