package auth.util;

import auth.dao.UserDAO;
import auth.dao.impl.UserDAOImpl;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.Attributes;
import shared.model.User;

// Revalidate session user IsActive against DB (admin may deactivate while logged in).
public final class ActiveAccountGuard {

    private static final UserDAO userDAO = new UserDAOImpl();

    private ActiveAccountGuard() {
    }

    public static boolean isSessionUserActive(HttpSession session) {
        if (session == null) {
            return true;
        }
        Object raw = session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            return true;
        }
        UserDTO dto = (UserDTO) raw;
        User fresh = userDAO.getById(dto.getUserId());
        return fresh != null && fresh.isActive();
    }

    public static void requireActiveOrInvalidate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object raw = session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            return;
        }
        if (isSessionUserActive(session)) {
            return;
        }

        session.invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(Attributes.Session.ERROR_MESSAGE,
                "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
        response.sendRedirect(request.getContextPath() + loginPathFor(request));
    }

    private static String loginPathFor(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri;
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            path = uri.substring(context.length());
        }
        if (path.startsWith("/registrant")) {
            return "/login";
        }
        return "/staff/login";
    }
}
