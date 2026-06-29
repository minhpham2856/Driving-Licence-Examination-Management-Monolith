package util;

import model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// Session helpers for the admin slice.
public final class SessionUtil {

    // Session attribute name used across the whole project.
    public static final String CURRENT_USER = "user";
    public static final String FLASH_MSG = "flashMessage";
    public static final String FLASH_TYPE = "flashType"; // success | danger

    private SessionUtil() {
    }

    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s == null) ? null : (User) s.getAttribute(CURRENT_USER);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    // Put a one-time flash message into the session (read+cleared on the next page).
    public static void flash(HttpServletRequest req, String type, String message) {
        HttpSession s = req.getSession(true);
        s.setAttribute(FLASH_TYPE, type);
        s.setAttribute(FLASH_MSG, message);
    }
}
