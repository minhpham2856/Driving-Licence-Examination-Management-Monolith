package admin.util;

import admin.constants.Roles;
import admin.model.Role;
import admin.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Session helpers for the admin slice.
 *
 * LoginServlet (shared) stores {@code Models.User} under attribute {@code "user"}.
 * This util adapts that object into {@link admin.model.User} so admin code stays
 * self-contained without importing the shared Models package.
 */
public final class SessionUtil {

    /** Session attribute name used across the whole project. */
    public static final String CURRENT_USER = "user";
    public static final String FLASH_MSG = "flashMessage";
    public static final String FLASH_TYPE = "flashType"; // success | danger

    private SessionUtil() {}

    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        return adaptUser(s.getAttribute(CURRENT_USER));
    }

    /**
     * Map session payload (admin.model.User or shared Models.User) to admin.model.User.
     */
    public static User adaptUser(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof User) {
            return (User) raw;
        }
        try {
            User u = new User();
            Method getId = raw.getClass().getMethod("getId");
            Object idVal = getId.invoke(raw);
            if (idVal instanceof Number) {
                u.setId(((Number) idVal).intValue());
            }
            Method getUsername = findMethod(raw.getClass(), "getUsername");
            if (getUsername != null) {
                u.setUsername((String) getUsername.invoke(raw));
            }
            Method getRole = raw.getClass().getMethod("getRole");
            Object roleObj = getRole.invoke(raw);
            if (roleObj != null) {
                Role role = new Role();
                Method getRoleName = roleObj.getClass().getMethod("getRoleName");
                role.setRoleName((String) getRoleName.invoke(roleObj));
                Method getRoleId = findMethod(roleObj.getClass(), "getId");
                if (getRoleId != null) {
                    Object rid = getRoleId.invoke(roleObj);
                    if (rid instanceof Number) {
                        role.setId(((Number) rid).intValue());
                    }
                }
                u.setRole(role);
            }
            return u;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Method findMethod(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    public static String roleName(User u) {
        if (u == null || u.getRole() == null) {
            return "";
        }
        return u.getRole().getRoleName() == null ? "" : u.getRole().getRoleName();
    }

    public static boolean isAdmin(HttpServletRequest req) {
        return Roles.ADMIN.equalsIgnoreCase(roleName(getCurrentUser(req)));
    }

    /**
     * Guards an admin-only endpoint.
     * @return true if the request may proceed; false if a redirect/error was already sent.
     */
    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = getCurrentUser(req);
        if (u == null) {
            HttpSession s = req.getSession(true);
            s.setAttribute("errorMessage", "ban can dang nhap de truy cap");
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!Roles.ADMIN.equalsIgnoreCase(roleName(u))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ban khong co quyen truy cap trang nay");
            return false;
        }
        return true;
    }

    /** Put a one-time flash message into the session (read+cleared on the next page). */
    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession(true);
        s.setAttribute(FLASH_TYPE, type);
        s.setAttribute(FLASH_MSG, message);
    }
}
