package controller.registrant;

import model.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Helper xác thực dùng chung cho các servlet cổng thí sinh.
 * Tách riêng để servlet chỉ gọi một điểm kiểm tra session/role.
 */
public final class RegistrantAuth {

    private RegistrantAuth() {
    }

    /** @return User đã đăng nhập với role Registrant; null nếu không hợp lệ. */
    public static User requireRegistrant(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập khu vực thí sinh.");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Registrant";
        if (!"Registrant".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập khu vực thí sinh.");
            return null;
        }
        return user;
    }

    public static int requireProfileId(User user) {
        if (user.getProfileId() != null && user.getProfileId() > 0) {
            return user.getProfileId();
        }
        if (user.getProfile() != null && user.getProfile().getId() > 0) {
            return user.getProfile().getId();
        }
        return 0;
    }
}
