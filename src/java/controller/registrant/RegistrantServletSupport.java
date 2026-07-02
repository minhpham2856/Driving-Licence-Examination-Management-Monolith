package controller.registrant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Helper servlet cổng thí sinh: copy model, flash message, build redirect URL. */
public final class RegistrantServletSupport {

    private RegistrantServletSupport() {
    }

    public static void copyModelToRequest(Map<String, Object> model, HttpServletRequest request) {
        if (model == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    public static void forwardView(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    public static void consumeFlash(HttpServletRequest request, String sessionAttr, String requestAttr) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object value = session.getAttribute(sessionAttr);
        if (value != null) {
            request.setAttribute(requestAttr, value.toString());
            session.removeAttribute(sessionAttr);
        }
    }

    public static void setFlash(HttpSession session, String sessionAttr, String message) {
        if (session != null && message != null) {
            session.setAttribute(sessionAttr, message);
        }
    }

    public static void appendQueryParam(StringBuilder url, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append(name)
                .append('=')
                .append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8))
                .append('&');
    }

    public static void trimTrailingAmpersand(StringBuilder url) {
        int len = url.length();
        if (len > 0 && url.charAt(len - 1) == '&') {
            url.setLength(len - 1);
        }
    }

    public static int parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
