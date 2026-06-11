package Utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Trang HTML tối giản cho callback SEPay (public, không cần login/JSP). */
public final class SepayReturnHtml {

    private SepayReturnHtml() {
    }

    /**
     * Render trang kết quả thanh toán tối giản (không layout sidebar).
     *
     * @param title       tiêu đề H1 (vd. "Thanh toán thành công")
     * @param message     nội dung giải thích cho người dùng
     * @param contextPath context path ứng dụng — dùng cho link đăng nhập / kỳ thi
     */
    public static void write(HttpServletResponse response, String title, String message, String contextPath)
            throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter out = response.getWriter()) {
            out.write("<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">");
            out.write("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
            out.write("<title>");
            out.write(escape(title));
            out.write("</title>");
            out.write("<style>body{font-family:system-ui,sans-serif;max-width:36rem;margin:3rem auto;padding:0 1rem;}</style>");
            out.write("</head><body>");
            out.write("<h1>");
            out.write(escape(title));
            out.write("</h1><p>");
            out.write(escape(message));
            out.write("</p><p>");
            out.write("<a href=\"");
            out.write(escape(contextPath + "/login"));
            out.write("\">Đăng nhập</a> · ");
            out.write("<a href=\"");
            out.write(escape(contextPath + "/registrant/my-exams"));
            out.write("\">Kỳ thi của tôi</a>");
            out.write("</p></body></html>");
        }
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
