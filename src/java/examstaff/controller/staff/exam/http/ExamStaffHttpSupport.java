package examstaff.controller.staff.exam.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Helper HTTP Presentation cho exam staff: header cache, param examId, flash, redirect URL.
 * Không chứa nghiệp vụ.
 */
public final class ExamStaffHttpSupport {

    private ExamStaffHttpSupport() {
    }

    /** Gắn header no-cache / no-store / must-revalidate cho response. */
    public static void applyNoCacheHeaders(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * Đọc mã kỳ thi từ query/form param {@code examId}.
     */
    public static int parseExamIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        return parsePositiveIntParam(request, "examId");
    }

    /** Đọc {@code selectedExamId} dương từ session request (không tạo session mới). */
    public static Integer readSelectedExamId(HttpServletRequest request) {
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session == null) {
            return null;
        }
        Object selected = session.getAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID);
        if (selected instanceof Integer) {
            Integer id = (Integer) selected;
            if (id > 0) {
                return id;
            }
        }
        return null;
    }

    /**
     * Chuyển flash session → request attribute rồi xóa key flash.
     *
     * @param flashKey      key trên session
     * @param attributeName tên attribute trên request
     */
    public static void consumeFlash(HttpSession session, String flashKey,
            HttpServletRequest request, String attributeName) {
        if (session == null || request == null || flashKey == null) {
            return;
        }
        Object value = session.getAttribute(flashKey);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(flashKey);
        }
    }

    /**
     * Redirect an toàn: ưu tiên Referer trong /views/staff/examstaff/; không thì context + fallbackPath.
     */
    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        if (request == null) {
            return fallbackPath;
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/examstaff/")) {
            return referer;
        }
        String ctx = request.getContextPath();
        if (fallbackPath.startsWith("/")) {
            return ctx + fallbackPath;
        }
        return ctx + "/" + fallbackPath;
    }

    /** Cắt phần query string sau {@code ?}. */
    public static String stripQueryString(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    /**
     * Thêm hoặc thay thế một query param trên URL (giữ các param khác).
     */
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

    /** Parse param số nguyên dương (lấy value hợp lệ cuối cùng nếu multi-value). */
    private static int parsePositiveIntParam(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
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
}
