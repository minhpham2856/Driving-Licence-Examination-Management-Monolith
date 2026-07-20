package admin.util;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Session helpers cho admin slice.
 * Đọc user bằng UserDTO + key Attributes.Session.USER.
 * Xác định Admin bằng ĐÚNG cơ chế của nhóm: RoleType.fromValue(roleName) == RoleType.ADMIN.
 */
public final class SessionUtil {

    public static final String FLASH_MSG = "flashMessage";
    public static final String FLASH_TYPE = "flashType"; // success | danger

    private SessionUtil() {}

    public static UserDTO getCurrentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object o = s.getAttribute(Attributes.Session.USER);
        return (o instanceof UserDTO) ? (UserDTO) o : null;
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    public static String roleName(UserDTO u) {
        if (u == null) return "";
        Role r = u.getRole();
        return (r == null || r.getRoleName() == null) ? "" : r.getRoleName();
    }

    /** Admin theo đúng cách nhóm phân giải role. */
    public static boolean isAdmin(HttpServletRequest req) {
        return isAdmin(getCurrentUser(req));
    }

    private static boolean isAdmin(UserDTO u) {
        if (u == null || u.getRole() == null) return false;
        return RoleType.ADMIN == RoleType.fromValue(u.getRole().getRoleName());
    }

    /** @return true nếu được đi tiếp; false nếu đã redirect/sendError. */
    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserDTO u = getCurrentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/staff/login");
            return false;
        }
        if (!isAdmin(u)) {
            // Thiết lập charset trước khi sendError để trình duyệt không bị lỗi font hiển thị
            resp.setContentType("text/html;charset=UTF-8");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return false;
        }
        return true;
    }

    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession(true);
        s.setAttribute(FLASH_TYPE, type);
        s.setAttribute(FLASH_MSG, message);
    }
}