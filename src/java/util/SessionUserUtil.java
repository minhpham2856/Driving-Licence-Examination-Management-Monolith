package util;

import model.User;
import jakarta.servlet.http.HttpSession;

public final class SessionUserUtil {

    public static final int DEFAULT_STAFF_USER_ID = 3;

    private SessionUserUtil() {
    }

    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, DEFAULT_STAFF_USER_ID);
    }

    public static int resolveUserId(HttpSession session, int defaultId) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : defaultId;
    }
}
