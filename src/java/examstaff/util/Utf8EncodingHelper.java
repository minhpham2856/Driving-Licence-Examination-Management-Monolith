package examstaff.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gán encoding UTF-8 cho request/response và Content-Type JSON.
 */
public final class Utf8EncodingHelper {

    public static final String UTF_8 = "UTF-8";

    private Utf8EncodingHelper() {
    }

    /**
     * Đặt character encoding UTF-8 cho request (bỏ qua nếu container từ chối).
     *
     * @param request request cần set encoding
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
     * Đặt character encoding UTF-8 cho response.
     *
     * @param response response cần set encoding
     */
    public static void applyResponse(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setCharacterEncoding(UTF_8);
    }

    /**
     * Áp UTF-8 cho cả request và response.
     *
     * @param request  request
     * @param response response
     */
    public static void apply(HttpServletRequest request, HttpServletResponse response) {
        applyRequest(request);
        applyResponse(response);
    }

    /**
     * UTF-8 + Content-Type {@code application/json;charset=UTF-8}.
     *
     * @param response response JSON
     */
    public static void applyJson(HttpServletResponse response) {
        applyResponse(response);
        response.setContentType("application/json;charset=UTF-8");
    }
}
