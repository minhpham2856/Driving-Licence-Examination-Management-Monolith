package auth.util;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.Attributes;

// Shared session helpers for auth controllers.
public final class AuthSessionUtil {

    private AuthSessionUtil() {
    }

    // Invalidate current session, create a fresh one, and store flash message.
    public static void logoutWithMessage(HttpServletRequest request, HttpServletResponse response,
            String redirectPath, String message) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(Attributes.Session.SUCCESS_MESSAGE, message);
        response.sendRedirect(request.getContextPath() + redirectPath);
    }

    // Read logged-in user from current session.
    public static UserDTO sessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UserDTO) session.getAttribute(Attributes.Session.USER);
    }
}
