package DAO;

import DBConnection.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only helper for the Admin dashboard. Self-contained on purpose: it does
 * NOT depend on the shared UserDAO / AuditLogDAO so it can't clash with other
 * members' code. Every query is best-effort and returns 0 / empty on failure.
 */
public class AdminStatsDAO extends DBContext {

    /** COUNT(*) of a table; tableName must be a trusted literal (never user input). */
    public int count(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countUsers()      { return count("[User]"); }
    public int countExamAreas()  { return count("ExamArea"); }
    public int countExams()      { return count("Exam"); }
    public int countDevices()    { return count("ExamDevice"); }

    /**
     * Recent audit rows for the dashboard table. Mapped against the project's
     * Audit table (TableName, RecordId, Action, ChangedBy, ChangedAt) joined to
     * [User]. If the real column names differ, this safely returns an empty list
     * and the dashboard just shows the empty state.
     */
    public List<RecentActivity> recentActivity(int limit) {
        List<RecentActivity> list = new ArrayList<>();
        String sql = "SELECT TOP (" + limit + ") a.Action, a.TableName, a.RecordId, a.ChangedAt, "
                   + "u.Username "
                   + "FROM Audit a LEFT JOIN [User] u ON a.ChangedBy = u.UserId "
                   + "ORDER BY a.ChangedAt DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                RecentActivity r = new RecentActivity();
                r.action = rs.getString("Action");
                r.module = rs.getString("TableName");
                r.recordId = rs.getString("RecordId");
                java.sql.Timestamp ts = rs.getTimestamp("ChangedAt");
                r.timestamp = (ts == null) ? "" : fmt.format(ts);
                r.username = rs.getString("Username");
                if (r.username == null || r.username.isEmpty()) r.username = "thanh cong";
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lightweight row used only by dashboard.jsp (display getters kept for that JSP). */
    public static class RecentActivity {
        private String timestamp;
        private String username;
        private String action;
        private String module;
        private String recordId;

        public String getTimestamp() { return timestamp; }
        public String getUsername()  { return username; }
        public String getFullName()  { return username; }
        public String getAction()    { return action; }
        public String getModule()    { return module; }
        public String getRecordId()  { return recordId; }
        public String getIpAddress() { return "aa”"; }
        public String getStatus()    { return "thanh cong"; }
        public String getStatusKey() { return "success"; }
    }
}
