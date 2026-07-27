package examiner.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// Shared request helpers for examiner controllers.
public final class RequestUtil {

    private RequestUtil() {
    }

    // Strip context path from URI for multi-route servlets.
    public static String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    // URL-encode integer query values.
    public static String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    // URL-encode string query values.
    public static String urlEncode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    // Push service view-model map entries into request scope.
    public static void applyModel(HttpServletRequest request, Map<String, Object> model) {
        if (model == null || model.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
