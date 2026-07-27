package auth.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.Attributes;

// Shared flash/session helpers for auth login and registration pages.
public final class AuthFlashUtil {

    private AuthFlashUtil() {
    }

    // Move one attribute from session to request and remove it from session.
    public static void promote(HttpServletRequest request, HttpSession session,
            String sessionKey, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    // Move auth flash messages from session to request.
    public static void promoteLoginFlash(HttpServletRequest request) {
        HttpSession session = request.getSession();
        promote(request, session, Attributes.Session.SUCCESS_MESSAGE, Attributes.Request.SUCCESS);
        promote(request, session, Attributes.Session.ERROR_MESSAGE, Attributes.Request.ERROR);
    }

    // Move temporary registration credentials from session to request.
    public static void promoteRegistrationCredentials(HttpServletRequest request) {
        HttpSession session = request.getSession();
        promote(request, session, Attributes.Session.REGISTRATION_USERNAME, Attributes.Request.REGISTRATION_USERNAME);
        promote(request, session, Attributes.Session.REGISTRATION_PASSWORD, Attributes.Request.REGISTRATION_PASSWORD);
    }

    // Forward to a JSP with a standard auth error attribute.
    public static void forwardWithError(HttpServletRequest request, HttpServletResponse response,
            String jspPath, String message) throws ServletException, IOException {
        request.setAttribute(Attributes.Request.ERROR, message);
        request.getRequestDispatcher(jspPath).forward(request, response);
    }
}
