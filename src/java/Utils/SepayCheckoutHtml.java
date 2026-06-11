package Utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * <b>Trang HTML trung gian</b> — tự động gửi (POST) form sang cổng thanh toán SEPay.
 *
 * <p><b>Tại sao không dùng JSP?</b></p>
 * <ul>
 *   <li>Response là HTML thuần viết từ Java — không cần file JSP riêng</li>
 *   <li>Tránh lộ tên nhà cung cấp / cấu hình trên giao diện thí sinh</li>
 *   <li>Form chứa hidden field + signature — POST trực tiếp tới pay.sepay.vn</li>
 * </ul>
 *
 * <p>Được gọi từ {@link Controllers.Registrant.RegisterExamServlet} và
 * {@link Controllers.Registrant.SepayCheckoutServlet}.</p>
 */
public final class SepayCheckoutHtml {

    private SepayCheckoutHtml() {
    }

    /**
     * Ghi response HTML: form ẩn + nút dự phòng + script auto-submit sau 300ms.
     *
     * @param checkoutUrl URL từ {@code sepay.checkoutUrl} (action của form)
     * @param fields      map đã ký từ {@link Services.Impl.SepayPaymentServiceImpl#buildCheckoutFields}
     */
    public static void writeAutoSubmitForm(HttpServletResponse response, String checkoutUrl, Map<String, String> fields)
            throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.write("<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">");
            // meta refresh: sau 15s vẫn chuyển nếu JavaScript bị tắt
            out.write("<meta http-equiv=\"refresh\" content=\"15;url=");
            out.write(escapeAttr(checkoutUrl));
            out.write("\">");
            out.write("<title>Đang chuyển đến cổng thanh toán</title>");
            out.write("<style>body{font-family:system-ui,sans-serif;padding:2rem;max-width:40rem;}</style>");
            out.write("</head><body>");
            out.write("<h1>Đang chuyển đến cổng thanh toán</h1>");
            out.write("<p>Vui lòng đợi trong giây lát. Nếu không tự chuyển, bấm nút bên dưới.</p>");
            out.write("<form id=\"sepayForm\" method=\"POST\" accept-charset=\"UTF-8\" action=\"");
            out.write(escapeAttr(checkoutUrl));
            out.write("\">");

            // Render hidden input đúng thứ tự SEPay yêu cầu — khớp lúc ký chữ ký
            for (String name : SepaySignatureUtil.FORM_FIELD_ORDER) {
                if (!fields.containsKey(name)) {
                    continue;
                }
                out.write("<input type=\"hidden\" name=\"");
                out.write(escapeAttr(name));
                out.write("\" value=\"");
                out.write(escapeAttr(fields.get(name)));
                out.write("\">");
            }

            out.write("<button type=\"submit\">Tiếp tục thanh toán</button>");
            out.write("</form>");
            // Auto-submit nhanh hơn meta refresh — UX mượt hơn
            out.write("<script>setTimeout(function(){document.getElementById('sepayForm').submit();},300);</script>");
            out.write("</body></html>");
        }
    }

    /** Tránh XSS / vỡ HTML khi URL hoặc giá trị field chứa &, ", < ... */
    private static String escapeAttr(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
