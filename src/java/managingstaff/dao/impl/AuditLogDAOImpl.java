package managingstaff.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import managingstaff.dao.AuditLogDAO;
import managingstaff.dto.AuditDTO;
import shared.dbconnection.DBContext;

public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    private static final String SELECT = """
            SELECT a.AuditId, a.Action, a.EntityName, a.EntityId, a.OldValue,
                   a.NewValue, a.Reason, a.Details, a.CreatedAt,
                   COALESCE(NULLIF(p.FullName,''),u.Username,N'Hệ thống') ChangerName
            FROM Audit a
            LEFT JOIN [User] u ON u.UserId=a.UserId
            LEFT JOIN Profile p ON p.UserId=u.UserId
            """;

    @Override
    public boolean insert(int userId, String action, String entityName, String entityId,
            String oldValue, String newValue, String reason, String details) {
        String sql = "INSERT INTO Audit (UserId,Action,EntityName,EntityId,OldValue,NewValue,Reason,Details)"
                + " VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (userId > 0) ps.setInt(1, userId); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, truncate(action, 50));
            ps.setString(3, truncate(entityName, 255));
            ps.setString(4, truncate(entityId, 255));
            ps.setString(5, oldValue);
            ps.setString(6, newValue);
            ps.setString(7, reason);
            ps.setString(8, details);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public List<AuditDTO> getLogsByUserAndDate(int userId, String date) {
        return searchUserLogsPaginated(userId, "", "", date, date, 1, 200);
    }

    @Override
    public List<AuditDTO> searchUserLogsPaginated(int userId, String keyword, String action,
            String startDate, String endDate, int page, int pageSize) {
        Filter filter = filter(userId, keyword, action, startDate, endDate);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(pageSize, 10000));
        List<Object> params = new ArrayList<>(filter.params());
        params.add((safePage - 1) * safeSize);
        params.add(safeSize);
        String sql = SELECT + filter.where()
                + " ORDER BY a.CreatedAt DESC,a.AuditId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AuditDTO> result = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải nhật ký thao tác", ex);
        }
    }

    @Override
    public int countUserLogs(int userId, String keyword, String action,
            String startDate, String endDate) {
        Filter filter = filter(userId, keyword, action, startDate, endDate);
        String sql = "SELECT COUNT(*) FROM Audit a LEFT JOIN [User] u ON u.UserId=a.UserId"
                + " LEFT JOIN Profile p ON p.UserId=u.UserId " + filter.where();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, filter.params());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đếm nhật ký thao tác", ex);
        }
    }

    private Filter filter(int userId, String keyword, String action,
            String startDate, String endDate) {
        StringBuilder where = new StringBuilder(" WHERE a.UserId=?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            where.append(" AND (a.Action LIKE ? OR a.EntityName LIKE ? OR a.EntityId LIKE ?")
                    .append(" OR a.OldValue LIKE ? OR a.NewValue LIKE ? OR a.Reason LIKE ?")
                    .append(" OR a.Details LIKE ? OR u.Username LIKE ? OR p.FullName LIKE ?)");
            for (int i = 0; i < 9; i++) params.add(like);
        }
        if (hasText(action)) { where.append(" AND UPPER(a.Action)=UPPER(?)"); params.add(action.trim()); }
        if (hasText(startDate)) { where.append(" AND a.CreatedAt>=CAST(? AS DATE)"); params.add(startDate.trim()); }
        if (hasText(endDate)) { where.append(" AND a.CreatedAt<DATEADD(DAY,1,CAST(? AS DATE))"); params.add(endDate.trim()); }
        return new Filter(where.toString(), params);
    }

    private static AuditDTO map(ResultSet rs) throws SQLException {
        AuditDTO dto = new AuditDTO();
        dto.setId(rs.getLong("AuditId"));
        dto.setAction(rs.getString("Action"));
        dto.setTableName(rs.getString("EntityName"));
        dto.setRecordId(rs.getString("EntityId"));
        dto.setOldValue(rs.getString("OldValue"));
        dto.setNewValue(rs.getString("NewValue"));
        dto.setReason(rs.getString("Reason"));
        dto.setDetails(rs.getString("Details"));
        dto.setChangedAt(rs.getTimestamp("CreatedAt"));
        dto.setChangerName(rs.getString("ChangerName"));
        return dto;
    }

    private static void bind(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) ps.setObject(i + 1, values.get(i));
    }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
    private static String truncate(String value, int max) {
        String safe = value == null ? "" : value;
        return safe.length() <= max ? safe : safe.substring(0, max);
    }
    private record Filter(String where, List<Object> params) { }
}
