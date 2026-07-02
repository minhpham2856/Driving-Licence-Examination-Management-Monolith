package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * UTF-8 cho servlet/JSP exam staff và màn hình gọi loa TV.
 */
public final class Utf8EncodingHelper {

    public static final String UTF_8 = "UTF-8";

    private Utf8EncodingHelper() {
    }

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

    public static void applyResponse(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setCharacterEncoding(UTF_8);
    }

    public static void apply(HttpServletRequest request, HttpServletResponse response) {
        applyRequest(request);
        applyResponse(response);
    }

    public static void applyHtml(HttpServletResponse response) {
        applyResponse(response);
        response.setContentType("text/html;charset=UTF-8");
    }

    public static void applyJson(HttpServletResponse response) {
        applyResponse(response);
        response.setContentType("application/json;charset=UTF-8");
    }
}
