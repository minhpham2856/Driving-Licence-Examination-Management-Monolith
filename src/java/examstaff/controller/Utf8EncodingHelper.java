package examstaff.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gán encoding UTF-8 cho request/response và Content-Type JSON (Presentation).
 *
 * Vai trò:
 * Đảm bảo request/response exam staff dùng UTF-8 thống nhất
 * (form tiếng Việt, JSON API). Tránh lỗi mojibake trên param POST và body JSON.
 *
 * Luồng sử dụng:
 * - Đầu servlet/JSP động: apply(request, response) hoặc applyRequest
 * - API JSON: applyJson(response) trước ghi body
 * - ExamStaffPageSupport gọi applyRequest khi prepare page
 *
 * Ai gọi:
 * ExamSelectServlet, PublicCallServlet, PublicCallStateServlet,
 * ExamStaffPageSupport và servlet xử lý form tiếng Việt.
 */
public final class Utf8EncodingHelper {

    /** Tên charset dùng thống nhất trong exam staff. */
    public static final String UTF_8 = "UTF-8";

    /** Không khởi tạo. */
    private Utf8EncodingHelper() {
    }

    /**
     * Set CharacterEncoding UTF-8 trên request (bỏ qua nếu container từ chối).
     * @param request request HTTP
     */
    public static void applyRequest(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        try {
            request.setCharacterEncoding(UTF_8);
        } catch (Exception ignored) {
            // container may reject if already read
        }
    }

    /**
     * Set CharacterEncoding UTF-8 trên response.
     * @param response response HTTP
     */
    public static void applyResponse(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setCharacterEncoding(UTF_8);
    }

    /**
     * Áp UTF-8 cho cả request và response.
     * @param request  request HTTP
     * @param response response HTTP
     */
    public static void apply(HttpServletRequest request, HttpServletResponse response) {
        applyRequest(request);
        applyResponse(response);
    }

    /**
     * UTF-8 response + Content-Type: application/json;charset=UTF-8.
     * @param response response HTTP
     */
    public static void applyJson(HttpServletResponse response) {
        applyResponse(response);
        response.setContentType("application/json;charset=UTF-8");
    }
}
