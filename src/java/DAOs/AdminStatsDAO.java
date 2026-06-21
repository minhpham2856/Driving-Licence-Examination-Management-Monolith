package DAOs;

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
 * Cung cấp các phương thức thống kê nhanh cho trang tổng quan admin như
 * đếm người dùng, khu vực thi, kỳ thi, thiết bị và hoạt động gần đây.
 */
public class AdminStatsDAO extends DBContext {

    /**
     * Đếm số bản ghi của một bảng bất kỳ.
     *
     * @param tableName tên bảng (phải là literal tin cậy, tuyệt đối không phải dữ liệu người dùng)
     * @return số lượng bản ghi, 0 nếu có lỗi
     */
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

    /**
     * Đếm tổng số người dùng trong hệ thống.
     *
     * @return số lượng người dùng
     */
    public int countUsers()      { return count("[User]"); }

    /**
     * Đếm tổng số khu vực thi.
     *
     * @return số lượng khu vực thi
     */
    public int countExamAreas()  { return count("ExamArea"); }

    /**
     * Đếm tổng số kỳ thi.
     *
     * @return số lượng kỳ thi
     */
    public int countExams()      { return count("Exam"); }

    /**
     * Đếm tổng số thiết bị thi.
     *
     * @return số lượng thiết bị
     */
    public int countDevices()    { return count("ExamDevice"); }

    /**
     * Lấy danh sách hoạt động gần đây từ nhật ký Audit cho bảng dashboard.
     * Truy vấn JOIN với bảng [User] để lấy tên người dùng.
     * Nếu tên cột thực tế khác, phương thức an toàn trả về danh sách rỗng.
     *
     * @param limit số lượng bản ghi tối đa trả về
     * @return danh sách RecentActivity chứa thông tin hoạt động gần đây
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
                if (r.username == null || r.username.isEmpty()) r.username = "Há»‡ thá»‘ng";
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
        public String getIpAddress() { return "â€”"; }
        public String getStatus()    { return "ThÃ nh cÃ´ng"; }
        public String getStatusKey() { return "success"; }
    }
}
