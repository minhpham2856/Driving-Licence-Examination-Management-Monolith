package controller.staff.exam.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class ExamStaffHttpSupport {

    private ExamStaffHttpSupport() {
    }

    public static void applyNoCacheHeaders(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    public static int parseSessionIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        String[] values = request.getParameterValues("sessionId");
        if (values == null || values.length == 0) {
            values = request.getParameterValues("examSessionId");
        }
        if (values == null || values.length == 0) {
            return 0;
        }
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] == null || values[i].isBlank()) {
                continue;
            }
            try {
                int parsed = Integer.parseInt(values[i].trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public static Integer readSelectedSessionId(HttpServletRequest request) {
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session == null) {
            return null;
        }
        Object selected = session.getAttribute("selectedSessionId");
        if (selected instanceof Integer id && id > 0) {
            return id;
        }
        return null;
    }

    public static void consumeFlash(HttpSession session, String sessionKey,
            HttpServletRequest request, String attributeName) {
        if (session == null || request == null || sessionKey == null) {
            return;
        }
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(sessionKey);
        }
    }

    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        if (request == null) {
            return fallbackPath;
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/views/staff/examstaff/")) {
            return referer;
        }
        String ctx = request.getContextPath();
        if (fallbackPath.startsWith("/")) {
            return ctx + fallbackPath;
        }
        return ctx + "/" + fallbackPath;
    }

    public static String stripQueryString(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    public static String upsertQueryParam(String url, String key, String value) {
        if (url == null || key == null || value == null) {
            return url;
        }
        String base = stripQueryString(url);
        String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
        StringBuilder rebuilt = new StringBuilder();
        boolean replaced = false;
        if (!query.isBlank()) {
            for (String part : query.split("&")) {
                if (part.isBlank()) {
                    continue;
                }
                if (part.startsWith(key + "=")) {
                    if (!replaced) {
                        if (rebuilt.length() > 0) {
                            rebuilt.append('&');
                        }
                        rebuilt.append(key).append('=').append(value);
                        replaced = true;
                    }
                } else {
                    if (rebuilt.length() > 0) {
                        rebuilt.append('&');
                    }
                    rebuilt.append(part);
                }
            }
        }
        if (!replaced) {
            if (rebuilt.length() > 0) {
                rebuilt.append('&');
            }
            rebuilt.append(key).append('=').append(value);
        }
        return base + "?" + rebuilt;
    }
}
