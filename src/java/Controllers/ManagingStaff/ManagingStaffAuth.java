package Controllers.ManagingStaff;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Helper xác thực servlet ban quản lý (ManagingStaff).
 */
public final class ManagingStaffAuth {

    private ManagingStaffAuth() {
    }

    public static User requireManagingStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập khu vực quản lý.");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        if (!"ManagingStaff".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập khu vực quản lý.");
            return null;
        }
        return user;
    }
}
