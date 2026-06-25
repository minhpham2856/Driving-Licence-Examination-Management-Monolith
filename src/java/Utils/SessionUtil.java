package Utils;

import Utils.ExamConstants;
import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Session helpers for the admin slice.
public final class SessionUtil {

    // Session attribute name used across the whole project.
    public static final String CURRENT_USER = "user";
    public static final String FLASH_MSG = "flashMessage";
    public static final String FLASH_TYPE = "flashType"; // success | danger

    private SessionUtil() {}

    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s == null) ? null : (User) s.getAttribute(CURRENT_USER);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    public static String roleName(User u) {
        if (u == null || u.getRole() == null) return "";
        return u.getRole().getRoleName() == null ? "" : u.getRole().getRoleName();
    }

    public static boolean isAdmin(HttpServletRequest req) {
        return ExamConstants.ROLE_ADMIN.equalsIgnoreCase(roleName(getCurrentUser(req)));
    }

    // Guards an admin-only endpoint.
    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = getCurrentUser(req);
        if (u == null) {
            HttpSession s = req.getSession(true);
            s.setAttribute("errorMessage", "ban can dang nhap de truy cap");
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!ExamConstants.ROLE_ADMIN.equalsIgnoreCase(roleName(u))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ban khong co quyen truy cap trang nay");
            return false;
        }
        return true;
    }

    // Put a one-time flash message into the session (read+cleared on the next page).
    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession(true);
        s.setAttribute(FLASH_TYPE, type);
        s.setAttribute(FLASH_MSG, message);
    }
}
