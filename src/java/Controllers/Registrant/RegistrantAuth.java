package Controllers.Registrant;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <b>Tiện ích xác thực dùng chung</b> cho mọi trang khu vực thí sinh ({@code /registrant/...}).
 *
 * <p><b>Session là gì?</b> Sau khi login, server lưu object {@link User} vào session HTTP
 * với key {@code "user"}. Các servlet sau đó đọc lại để biết ai đang thao tác.</p>
 *
 * <p><b>Callback SEPay</b> (success, IPN) <b>không</b> dùng class này — SEPay gọi URL công khai,
 * không mang session đăng nhập của thí sinh (trừ khi cùng browser còn cookie session).</p>
 */
public final class RegistrantAuth {

    private RegistrantAuth() {
    }

    /**
     * Bắt buộc đã đăng nhập trước khi xem trang thí sinh.
     *
     * <p>Nếu session không có user: ghi flash {@code errorMessage} và redirect về {@code /login},
     * đồng thời trả {@code null} — servlet caller phải {@code return} ngay, không xử lý tiếp.</p>
     *
     * @param loginMessage câu hiển thị trên trang login khi bị chặn
     */
    public static User requireUser(HttpServletRequest request, HttpServletResponse response, String loginMessage)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user != null) {
            return user;
        }
        request.getSession(true).setAttribute("errorMessage", loginMessage);
        response.sendRedirect(request.getContextPath() + "/login");
        return null;
    }

    /**
     * <b>Flash message (PRG pattern):</b> sau POST thành công thường redirect GET;
     * thông báo lưu tạm trong session rồi chuyển sang request attribute một lần và xóa session key.
     *
     * <p>Ví dụ: {@code transferFlash(request, "successMessage", "success")} sau redirect profile.</p>
     */
    public static void transferFlash(HttpServletRequest request, String sessionKey, String requestAttribute) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestAttribute, value);
            session.removeAttribute(sessionKey);
        }
    }
}
