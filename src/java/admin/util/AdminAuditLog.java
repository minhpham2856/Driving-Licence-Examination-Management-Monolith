package admin.util;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.dbconnection.DBContext;
import shared.model.User;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Ghi nhật ký vào bảng Audit. THAY THẾ Utils.AuditLogHelper cũ (đã không còn).
 * Giữ NGUYÊN chữ ký persist(session, action, details, recordId) để chỗ gọi không phải sửa.
 * Lưu message vào cột NewValue; EntityName = "Quản trị" (để cột "Phân hệ" không rỗng).
 */
public final class AdminAuditLog {

    private AdminAuditLog() {}
    private static final class Db extends DBContext {}

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int recordId) {
        String sql = "INSERT INTO Audit (UserId, Action, EntityName, EntityId, NewValue, CreatedAt) VALUES (?,?,?,?,?,?)";
        Connection c = null;
        try {
            // Session lưu UserDTO (auth.dto.UserDTO); vẫn nhận User để tương thích ngược.
            Integer userId = null;
            Object u = (session != null) ? session.getAttribute(Attributes.Session.USER) : null;
            if (u instanceof UserDTO) userId = ((UserDTO) u).getUserId();
            else if (u instanceof User) userId = ((User) u).getUserId();

            c = new Db().getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (userId != null) ps.setInt(1, userId); else ps.setNull(1, java.sql.Types.INTEGER);
                ps.setString(2, action != null ? action : "UPDATE");
                ps.setString(3, "Quản trị");
                ps.setString(4, String.valueOf(recordId));
                ps.setString(5, details);
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignore) {}
        }
    }
}
