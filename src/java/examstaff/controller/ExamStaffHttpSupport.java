package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dao.impl.InMemoryCallBoardDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Helper HTTP Presentation cho exam staff: header cache, param examId, flash, redirect URL.
 * Không chứa nghiệp vụ.
 */
public final class ExamStaffHttpSupport {

    /** Không khởi tạo. */
    private ExamStaffHttpSupport() {
    }

    /**
     * Repository CallBoard in-memory dùng chung (singleton JVM).
     * Tham số {@code ctx} giữ để tương thích caller cũ; không còn đọc/ghi ServletContext.
     */
    public static CallBoardDAO callBoardDao(ServletContext ctx) {
        return InMemoryCallBoardDAO.getInstance();
    }

    /** Repository CallBoard in-memory dùng chung (singleton JVM). */
    public static CallBoardDAO callBoardDao() {
        return InMemoryCallBoardDAO.getInstance();
    }

    /**
     * Gắn header no-cache / no-store / must-revalidate cho response.
     *
     * @param response response HTTP
     */
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
     *
     * @param request request HTTP
     * @return examId dương hoặc 0
     */
    public static int parseExamIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        return parsePositiveIntParam(request, "examId");
    }

    /**
     * Đọc {@code selectedExamId} dương từ session request (không tạo session mới).
     *
     * @param request request HTTP
     * @return Integer dương hoặc null
     */
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
     * Chuyển flash session → request attribute rồi xóa key flash (one-shot PRG).
     *
     * @param session       session chứa flash
     * @param flashKey      key trên session
     * @param request       request đích
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
     * Redirect an toàn: ưu tiên Referer trong {@code /examstaff/}; không thì context + fallbackPath.
     *
     * @param request      request hiện tại
     * @param fallbackPath đường dẫn tương đối (ví dụ {@code /examstaff/dashboard})
     * @return URL tuyệt đối tương đối context hoặc Referer
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

    /**
     * Cắt phần query string sau {@code ?}.
     *
     * @param url URL gốc
     * @return phần trước {@code ?}, hoặc null nếu url null
     */
    public static String stripQueryString(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    /**
     * Thêm hoặc thay thế một query param trên URL (giữ các param khác).
     * <p>
     * Luồng: tách base/query → duyệt param → thay key hoặc append → ghép lại.
     *
     * @param url   URL gốc
     * @param key   tên param
     * @param value giá trị mới
     * @return URL đã upsert; trả nguyên url nếu thiếu đầu vào
     */
    public static String upsertQueryParam(String url, String key, String value) {
        if (url == null || key == null || value == null) {
            return url;
        }
        String base = stripQueryString(url);
        String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
        StringBuilder rebuilt = new StringBuilder();
        boolean replaced = false;
        // Giữ param cũ; thay value nếu trùng key
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
        // Chưa có key → append
        if (!replaced) {
            if (rebuilt.length() > 0) {
                rebuilt.append('&');
            }
            rebuilt.append(key).append('=').append(value);
        }
        return base + "?" + rebuilt;
    }

    /**
     * Parse param số nguyên dương (lấy value hợp lệ cuối cùng nếu multi-value).
     *
     * @param request request HTTP
     * @param name    tên param
     * @return số dương hoặc 0
     */
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
