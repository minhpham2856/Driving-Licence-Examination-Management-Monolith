package registrant.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Tiện ích dùng chung cho servlet cổng thí sinh (không chứa nghiệp vụ DB).
 * Hỗ trợ copy model Map → request attribute, forward JSP, đọc/xóa flash session, parse tham số số nguyên dương và ghép URL redirect (PRG sau POST).
 * Các service impl gọi lớp này thay vì lặp mã servlet.
 */
public final class RegistrantServletSupport {

    private RegistrantServletSupport() {
    }

    /** Sao chép toàn bộ entry của model sang request attribute. */
    public static void copyModelToRequest(Map<String, Object> model, HttpServletRequest request) {
        if (model == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /** Forward request tới JSP/viewPath chỉ định. */
    public static void forwardView(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    /** Đọc flash từ session sang request rồi xóa attribute session. */
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

    /** Ghi thông báo flash vào session để hiển thị sau redirect. */
    public static void setFlash(HttpSession session, String sessionAttr, String message) {
        if (session != null && message != null) {
            session.setAttribute(sessionAttr, message);
        }
    }

    /** Nối một query param đã URL-encode vào StringBuilder URL. */
    public static void appendQueryParam(StringBuilder url, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        url.append(name)
                .append('=')
                .append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8))
                .append('&');
    }

    /** Cắt ký tự & thừa ở cuối chuỗi URL đang dựng. */
    public static void trimTrailingAmpersand(StringBuilder url) {
        int len = url.length();
        if (len > 0 && url.charAt(len - 1) == '&') {
            url.setLength(len - 1);
        }
    }

    /** Parse chuỗi thành int dương; lỗi hoặc rỗng thì trả 0. */
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
