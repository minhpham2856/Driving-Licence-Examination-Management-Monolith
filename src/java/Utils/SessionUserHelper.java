package Utils;

import Models.User;
import jakarta.servlet.http.HttpSession;

public final class SessionUserHelper {

    public static final int DEFAULT_STAFF_USER_ID = 3;

    private SessionUserHelper() {
    }

    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, DEFAULT_STAFF_USER_ID);
    }

    public static int resolveUserId(HttpSession session, int defaultId) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getId() > 0) ? user.getId() : defaultId;
    }
}
