package examstaff.controller;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

/**
 * Trích xuất userId / username từ session đăng nhập (Presentation).
 * Đọc {@link Attributes.Session#USER} kiểu {@link UserDTO}.
 */
public final class SessionUserHelper {

    /** Không khởi tạo. */
    private SessionUserHelper() {
    }

    /**
     * Lấy userId từ session; trả 0 nếu không có.
     *
     * @param session session HTTP (có thể null)
     * @return userId &gt; 0 hoặc 0
     */
    public static int resolveUserId(HttpSession session) {
        return resolveUserId(session, 0);
    }

    /**
     * Lấy userId từ session; fallback {@code defaultId} nếu thiếu/không hợp lệ.
     *
     * @param session   session HTTP
     * @param defaultId giá trị khi không resolve được
     * @return userId dương hoặc defaultId
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
     * Lấy username từ session; chuỗi rỗng nếu không có.
     *
     * @param session session HTTP
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
