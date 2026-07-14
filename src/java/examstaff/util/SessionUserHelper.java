package examstaff.util;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

public final class SessionUserHelper {

    private SessionUserHelper() {
    }

    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, 0);
    }

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
