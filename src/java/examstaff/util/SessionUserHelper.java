package examstaff.util;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

/** Trích xuất userId / username từ attribute session — helper kỹ thuật thuần. */
public final class SessionUserHelper {

    private SessionUserHelper() {
    }

    /**
     * Lấy userId từ session; không có thì trả {@code 0}.
     *
     * @param session session chứa {@link Attributes.Session#USER}
     * @return userId &gt; 0, hoặc {@code 0}
     */
    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, 0);
    }

    /**
     * Lấy userId từ session; không hợp lệ thì trả {@code defaultId}.
     *
     * @param session    session chứa user (null = dùng default)
     * @param defaultId  giá trị mặc định khi không đọc được
     * @return userId hợp lệ hoặc {@code defaultId}
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
     * Lấy username từ session; không có thì trả chuỗi rỗng.
     *
     * @param session session chứa user
     * @return username hoặc {@code ""}
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
