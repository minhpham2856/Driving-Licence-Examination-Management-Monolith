package examstaff.util;

import jakarta.servlet.http.HttpSession;
import shared.model.User;

/**
 * Doc user tu session host ({@code model.User} do Auth/Login dat vao) — khong dung examstaff.model.User.
 */
public final class SessionUserHelper {

    public static final int DEFAULT_STAFF_USER_ID = 3;

    private SessionUserHelper() {
    }

    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, DEFAULT_STAFF_USER_ID);
    }

    public static int resolveUserId(HttpSession session, int defaultId) {
        if (session == null) {
            return defaultId;
        }
        Object raw = session.getAttribute("user");
        if (raw instanceof User user && user.getUserId() > 0) {
            return user.getUserId();
        }
        return defaultId;
    }

    public static String resolveUsername(HttpSession session) {
        if (session == null) {
            return "";
        }
        Object raw = session.getAttribute("user");
        if (raw instanceof User user && user.getUsername() != null) {
            return user.getUsername();
        }
        return "";
    }
}
