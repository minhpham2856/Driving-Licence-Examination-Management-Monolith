package examstaff.controller;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

/**
 * Trích xuất userId / username từ session đăng nhập (Presentation).
 * Đọc Attributes.Session.USER kiểu UserDTO.
 *
 * Vai trò:
 * Helper đọc thông tin staff đang đăng nhập từ session HTTP
 * (userId cho audit, username cho hiển thị/export). Trả giá trị mặc định an toàn khi thiếu session.
 *
 * Luồng sử dụng:
 * - Servlet cần staffId trước ghi audit hoặc load dữ liệu cá nhân
 * - resolveUserId(session) → id dương hoặc 0/defaultId
 * - resolveUsername(session) → tên hiển thị trên export/UI
 *
 * Ai gọi:
 * AuditServlet, AuditExportServlet, ExamControlServlet,
 * ExaminerAllocationServlet, ProcedureServlet và servlet cần audit/log theo user.
 */
public final class SessionUserHelper {

    /** Không khởi tạo. */
    private SessionUserHelper() {
    }

    /**
     * Lấy userId từ session; trả 0 nếu không có.
     * @param session session HTTP (có thể null)
     * @return userId > 0 hoặc 0
     */
    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, 0);
    }

    /**
     * Lấy userId từ session; fallback defaultId nếu thiếu/không hợp lệ.
     * @param session   session HTTP
     * @param defaultId giá trị khi không resolve được
     * @return userId dương hoặc defaultId
     */
    public static int resolveUserId(HttpSession session, int defaultId) {
        if (session == null) {
            return defaultId;
        }
        Object raw = session.getAttribute(Attributes.Session.USER);
        if (raw instanceof UserDTO) {
            UserDTO user = (UserDTO) raw;
            if (user.getUserId() > 0) {
                return user.getUserId();
            }
        }
        return defaultId;
    }

    /**
     * Lấy username từ session; chuỗi rỗng nếu không có.
     * @param session session HTTP
     * @return username hoặc ""
     */
    public static String resolveUsername(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object raw = session.getAttribute(Attributes.Session.USER);
        if (raw instanceof UserDTO) {
            UserDTO user = (UserDTO) raw;
            if (user.getUsername() != null) {
                return user.getUsername();
            }
        }
        return "";
    }
}
