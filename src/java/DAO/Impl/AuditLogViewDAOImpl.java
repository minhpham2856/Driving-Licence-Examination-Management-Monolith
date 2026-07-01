package DAO.Impl;

import DBConnection.DBContext;
import DAO.AuditLogViewDAO;
import Models.AuditView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogViewDAOImpl extends DBContext implements AuditLogViewDAO {

    private static final String BASE =
            "SELECT a.AuditId, a.Action, a.EntityName, a.NewValue, a.Reason, a.Details, a.OldValue, " +
            "       a.CreatedAt, a.UserId, u.Username, u.[Role] AS RoleDb, p.FullName " +
            "FROM Audit a " +
            "LEFT JOIN [User] u ON u.UserId = a.UserId " +
            "LEFT JOIN Profile p ON p.UserId = a.UserId ";

    private String whereClause(String keyword, String dbRole, String action,
                               String dateFrom, String dateTo, List<Object> params) {
        StringBuilder w = new StringBuilder(" WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            w.append(" AND (u.Username LIKE ? OR p.FullName LIKE ? OR a.NewValue LIKE ? OR a.Reason LIKE ? OR a.EntityName LIKE ?) ");
            String k = "%" + keyword.trim() + "%";
            params.add(k); params.add(k); params.add(k); params.add(k); params.add(k);
        }
        if (dbRole != null && !dbRole.isBlank()) { w.append(" AND u.[Role] = ? "); params.add(dbRole); }
        if (action != null && !action.isBlank()) { w.append(" AND a.Action = ? "); params.add(action); }
        if (dateFrom != null && !dateFrom.isBlank()) { w.append(" AND CAST(a.CreatedAt AS DATE) >= ? "); params.add(dateFrom); }
        if (dateTo != null && !dateTo.isBlank()) { w.append(" AND CAST(a.CreatedAt AS DATE) <= ? "); params.add(dateTo); }
        return w.toString();
    }

    @Override
    public List<AuditView> search(String keyword, String dbRole, String action,
                                  String dateFrom, String dateTo, int page, int pageSize) {
        List<Object> params = new ArrayList<>();
        String sql = BASE + whereClause(keyword, dbRole, action, dateFrom, dateTo, params)
                + " ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        int safePage = Math.max(page, 1), safeSize = Math.max(pageSize, 1);
        params.add((safePage - 1) * safeSize);
        params.add(safeSize);

        List<AuditView> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public int count(String keyword, String dbRole, String action, String dateFrom, String dateTo) {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM Audit a LEFT JOIN [User] u ON u.UserId = a.UserId " +
                     "LEFT JOIN Profile p ON p.UserId = a.UserId " +
                     whereClause(keyword, dbRole, action, dateFrom, dateTo, params);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int countAll() {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM Audit");
             ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int countByAction(String action) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT COUNT(*) FROM Audit WHERE Action = ?")) {
            ps.setString(1, action);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private AuditView map(ResultSet rs) throws SQLException {
        AuditView v = new AuditView();
        v.setAuditId(rs.getLong("AuditId"));
        v.setCreatedAt(rs.getTimestamp("CreatedAt"));
        v.setFullName(rs.getString("FullName"));
        v.setUsername(rs.getString("Username"));
        v.setRoleDb(rs.getString("RoleDb"));
        v.setAction(rs.getString("Action"));
        v.setEntityName(rs.getString("EntityName"));
        // human message: prefer NewValue, then Reason, then Details, then OldValue
        String detail = firstNonBlank(rs.getString("NewValue"), rs.getString("Reason"),
                rs.getString("Details"), rs.getString("OldValue"));
        v.setDetail(detail);
        return v;
    }

    private String firstNonBlank(String... vals) {
        for (String s : vals) if (s != null && !s.isBlank()) return s;
        return null;
    }
}
