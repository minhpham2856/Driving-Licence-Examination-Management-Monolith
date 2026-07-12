package examstaff.util;

import shared.model.User;
import examstaff.service.RoleService;
import examstaff.service.impl.RoleServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class SessionUtil {

    public static final String CURRENT_USER = "user";
    public static final String FLASH_MSG = "flashMessage";
    public static final String FLASH_TYPE = "flashType";

    private static final RoleService ROLE_SERVICE = new RoleServiceImpl();

    private SessionUtil() {
    }

    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s == null) ? null : (User) s.getAttribute(CURRENT_USER);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    public static String roleName(User u) {
        if (u == null || u.getRoleId() <= 0) {
            return "";
        }
        String name = ROLE_SERVICE.getRoleNameById(u.getRoleId());
        return name != null ? name : "";
    }

    public static boolean isAdmin(HttpServletRequest req) {
        return examstaff.enums.UserRole.isAdmin(roleName(getCurrentUser(req)));
    }

    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = getCurrentUser(req);
        if (u == null) {
            HttpSession s = req.getSession(true);
            s.setAttribute("errorMessage", "ban can dang nhap de truy cap");
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!examstaff.enums.UserRole.isAdmin(roleName(u))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ban khong co quyen truy cap trang nay");
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

