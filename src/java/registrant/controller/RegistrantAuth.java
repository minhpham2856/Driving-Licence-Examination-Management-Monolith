package registrant.controller;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import registrant.util.RegistrantSessionSupport;

/**
 * Lớp tiện ích xác thực cho tầng controller cổng thí sinh.
 * Mọi servlet dưới /registrant/* gọi requireRegistrant trước khi xử lý: kiểm tra session USER, role REGISTRANT, redirect /login hoặc HTTP 403 nếu không hợp lệ.
 * requireProfileId lấy ProfileId từ UserDTO đã qua kiểm tra.
 */
public final class RegistrantAuth {

    private RegistrantAuth() {
    }

    /** Trả về UserDTO đã đăng nhập với role Registrant; null nếu không hợp lệ. */
    public static UserDTO requireRegistrant(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        UserDTO user = session != null ? (UserDTO) session.getAttribute(Attributes.Session.USER) : null;
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute(Attributes.Session.ERROR_MESSAGE,
                    "Bạn cần đăng nhập để truy cập khu vực thí sinh.");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Role role = user.getRole();
        if (role == null || RoleType.fromValue(role.getRoleName()) != RoleType.REGISTRANT) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập khu vực thí sinh.");
            return null;
        }
        return user;
    }

    /** Trả về ProfileId từ session UserDTO (0 nếu chưa gắn hồ sơ). */
    public static int requireProfileId(UserDTO user) {
        return RegistrantSessionSupport.profileId(user);
    }
}
