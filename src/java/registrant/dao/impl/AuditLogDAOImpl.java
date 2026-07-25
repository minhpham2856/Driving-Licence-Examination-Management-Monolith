package registrant.dao.impl;

import registrant.dao.AuditLogDAO;
import registrant.dto.AuditLogEntry;
import shared.dbconnection.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai AuditLogDAO trên bảng Audit (DLEM_DB_2).
 * insert ghi hành động thí sinh/staff; getLogsByProfileId join User/Profile để lấy tên người thay đổi phục vụ timeline track-profile.jsp.
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

    private static final String PROFILE_AUDIT_WHERE = """
            WHERE (
                /* EntityId = ProfileId (luồng registrant ghi audit) */
                (a.EntityName IN (N'Profile', N'Document', N'ExamRegistration', N'Hồ sơ', N'Tài liệu hồ sơ')
                 AND TRY_CAST(a.EntityId AS INT) = ?)
                /* EntityId = ExamRegistrationId thuộc profile (staff / seed) */
                OR EXISTS (
                    SELECT 1 FROM ExamRegistration er
                    WHERE er.ProfileId = ?
                      AND TRY_CAST(a.EntityId AS INT) = er.ExamRegistrationId
                      AND a.EntityName IN (N'ExamRegistration', N'Profile', N'Document',
                                           N'Hồ sơ', N'Tài liệu hồ sơ')
                )
                /* EntityId = CandidateId thuộc profile (qua CCCD) */
                OR EXISTS (
                    SELECT 1 FROM Candidate c
                    INNER JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
                    WHERE prof.ProfileId = ?
                      AND TRY_CAST(a.EntityId AS INT) = c.CandidateId
                )
            )
            """;

    /** Insert AuditLogEntry vào bảng Audit. */
    @Override
    public boolean insert(AuditLogEntry log) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tbl = log.getTableName();
            if (tbl == null || tbl.trim().isEmpty()) {
                tbl = "Profile";
            }
            String act = log.getAction() != null ? log.getAction() : "UPDATE";
            int userId = log.getChangedBy() > 0 ? log.getChangedBy() : 3;
            int recId = log.getRecordId() != null ? log.getRecordId() : 0;

            ps.setInt(1, userId);
            ps.setString(2, act);
            if (log.getReason() != null) {
                ps.setString(3, log.getReason());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
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
            if (log.getDetails() != null) {
                ps.setString(8, log.getDetails());
            } else {
                ps.setNull(8, Types.NVARCHAR);
            }
            ps.setTimestamp(9, log.getChangedAt() != null ? log.getChangedAt() : new Timestamp(System.currentTimeMillis()));

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
        }
        return false;
    }

    /** Lấy log gần nhất theo profile (không lọc). */
    @Override
    public List<AuditLogEntry> getLogsByProfileId(int profileId, int limit) {
        return listLogsByProfileIdFiltered(profileId, 1, limit, null, null, null, null);
    }

    /** Query audit có lọc + phân trang (nội bộ). */
    private List<AuditLogEntry> listLogsByProfileIdFiltered(int profileId, int page, int pageSize,
            String searchQuery, String actionFilter, String fromDate, String toDate) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int offset = (safePage - 1) * safeSize;
        String filterClause = buildProfileFilterClause(searchQuery, actionFilter, fromDate, toDate);
        String sql = AUDIT_SELECT + PROFILE_AUDIT_WHERE + filterClause
                + " ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AuditLogEntry> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindProfileFilterParams(ps, profileId, searchQuery, actionFilter, fromDate, toDate, offset, safeSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static AuditLogEntry mapResultSet(ResultSet rs) throws SQLException {
        AuditLogEntry log = new AuditLogEntry();
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

    private static String buildProfileFilterClause(String searchQuery, String actionFilter,
            String fromDate, String toDate) {
        StringBuilder sb = new StringBuilder();
        if (actionFilter != null && !actionFilter.isBlank() && !"all".equalsIgnoreCase(actionFilter.trim())) {
            sb.append(" AND UPPER(a.Action) = ? ");
        }
        if (fromDate != null && !fromDate.isBlank()) {
            sb.append(" AND CAST(a.CreatedAt AS DATE) >= ? ");
        }
        if (toDate != null && !toDate.isBlank()) {
            sb.append(" AND CAST(a.CreatedAt AS DATE) <= ? ");
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            sb.append("""
                     AND (
                        a.Action LIKE ?
                        OR a.EntityName LIKE ?
                        OR a.NewValue LIKE ?
                        OR a.OldValue LIKE ?
                        OR a.Reason LIKE ?
                        OR a.Details LIKE ?
                        OR ISNULL(u.Username, p.FullName) LIKE ?
                     )
                    """);
        }
        return sb.toString();
    }

    private static void bindProfileFilterParams(PreparedStatement ps, int profileId,
            String searchQuery, String actionFilter, String fromDate, String toDate,
            int offset, int pageSize) throws SQLException {
        int idx = 1;
        ps.setInt(idx++, profileId); // EntityId = ProfileId
        ps.setInt(idx++, profileId); // ExamRegistration.ProfileId
        ps.setInt(idx++, profileId); // Profile.ProfileId (Candidate via CCCD)
        idx = bindProfileFilterValues(ps, idx, searchQuery, actionFilter, fromDate, toDate);
        ps.setInt(idx++, offset);
        ps.setInt(idx, pageSize);
    }

    private static int bindProfileFilterValues(PreparedStatement ps, int startIdx,
            String searchQuery, String actionFilter, String fromDate, String toDate) throws SQLException {
        int idx = startIdx;
        if (actionFilter != null && !actionFilter.isBlank() && !"all".equalsIgnoreCase(actionFilter.trim())) {
            ps.setString(idx++, actionFilter.trim().toUpperCase());
        }
        if (fromDate != null && !fromDate.isBlank()) {
            ps.setString(idx++, fromDate.trim());
        }
        if (toDate != null && !toDate.isBlank()) {
            ps.setString(idx++, toDate.trim());
        }
        if (searchQuery != null && !searchQuery.isBlank()) {
            String pattern = "%" + searchQuery.trim() + "%";
            for (int i = 0; i < 7; i++) {
                ps.setString(idx++, pattern);
            }
        }
        return idx;
    }
}
