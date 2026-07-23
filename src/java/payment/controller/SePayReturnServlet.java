package payment.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet xử lý redirect trình duyệt sau thanh toán SePay (bước <b>return</b> — không phải IPN).
 * <p>
 * Ba URL: {@code /success} (UX thành công), {@code /cancel} (khách hủy), {@code /error} (lỗi cổng).
 * Không ghi bảng {@code Payment} — ghi nhận thật qua {@link payment.controller.SePayIpnServlet}.
 * Hủy/lỗi: đưa staff về bước 3 thu lệ phí ({@code procedure?step=3}); thành công: thông báo đóng tab,
 * desk cập nhật khi IPN tới hoặc bấm Kiểm tra.
 */
@WebServlet({"/payment/sepay/success", "/payment/sepay/error", "/payment/sepay/cancel"})
public class SePayReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleReturn(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Một số cổng gọi return bằng POST — xử lý giống GET
        handleReturn(request, response);
    }

    private void handleReturn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = request.getServletPath();
        boolean cancelled = path != null && path.endsWith("/cancel");
        boolean error = path != null && path.endsWith("/error");

        // --- Hủy hoặc lỗi: quay bàn thủ tục bước thu phí ---
        if (cancelled || error) {
            HttpSession session = request.getSession(false);
            String sbd = resolveSbd(request, session);
            if (session != null) {
                session.setAttribute("procedureStep", "3"); // giữ wizard ở bước lệ phí
                if (sbd != null) {
                    session.setAttribute("callingSbd", sbd);
                }
                // Ngừng cờ chờ IPN để desk không tiếp tục poll SBD này
                session.removeAttribute("sePayAwaitingSbd");
                session.removeAttribute("sePayAwaitingInvoice");
            }
            writeBackToPayment(response, paymentStepUrl(request, sbd, cancelled), cancelled);
            return;
        }

        // --- Thành công: chỉ thông báo; không ghi DB tại đây ---
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Thanh toán thành công</title>
                <style>
                body{font-family:system-ui,sans-serif;background:#f8fafc;color:#0f172a;display:flex;min-height:100vh;align-items:center;justify-content:center;margin:0}
                .box{max-width:420px;padding:1.5rem;background:#fff;border:1px solid #e2e8f0;border-radius:12px}
                h1{font-size:1.15rem;margin:0 0 .75rem}p{margin:0;line-height:1.55;color:#475569}
                </style></head><body><div class="box">
                <h1>Thanh toán thành công</h1>
                <p>SePay đã ghi nhận. Quầy cập nhật qua IPN hoặc bấm Kiểm tra đã thanh toán. Bạn có thể đóng tab này.</p>
                </div></body></html>
                """);
    }

    /** Ưu tiên ?sbd= trên URL; fallback session khi mở checkout từ bàn thủ tục. */
    private static String resolveSbd(HttpServletRequest request, HttpSession session) {
        String sbd = blankToNull(request.getParameter("sbd"));
        if (sbd != null) {
            return sbd;
        }
        if (session == null) {
            return null;
        }
        Object awaiting = session.getAttribute("sePayAwaitingSbd");
        if (awaiting == null) {
            return null;
        }
        String value = String.valueOf(awaiting).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * URL bước 3 desk: SePay + tiền mặt cùng hiện.
     * {@code #procedure-desk} giúp cuộn đúng khối thu phí.
     */
    private static String paymentStepUrl(HttpServletRequest request, String sbd, boolean cancelled) {
        String ctx = request.getContextPath() == null ? "" : request.getContextPath();
        StringBuilder url = new StringBuilder(ctx).append("/examstaff/procedure?step=3");
        if (sbd != null) {
            url.append("&sbd=").append(URLEncoder.encode(sbd, StandardCharsets.UTF_8));
        }
        url.append(cancelled ? "&sePayCancelled=1" : "&sePayError=1");
        url.append("#procedure-desk");
        return url.toString();
    }

    /**
     * Checkout mở bằng {@code window.open} → tab return là popup.
     * Ưu tiên: refresh tab gốc (opener) rồi đóng popup; không có opener thì redirect tab hiện tại.
     */
    private static void writeBackToPayment(HttpServletResponse response, String deskUrl, boolean cancelled)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String title = cancelled ? "Đã hủy thanh toán" : "Thanh toán chưa hoàn tất";
        String hint = "Đang về bước thu lệ phí để chọn lại phương thức…";
        response.getWriter().write("""
                <!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>%s</title>
                <style>
                body{font-family:system-ui,sans-serif;background:#f8fafc;color:#0f172a;display:flex;min-height:100vh;align-items:center;justify-content:center;margin:0}
                .box{max-width:420px;padding:1.5rem;background:#fff;border:1px solid #e2e8f0;border-radius:12px;text-align:center}
                h1{font-size:1.15rem;margin:0 0 .75rem}p{margin:0 0 1rem;line-height:1.55;color:#475569}
                a{color:#0052cc}
                </style></head><body><div class="box">
                <h1>%s</h1><p>%s</p>
                <p><a href="%s">Về bước thu lệ phí</a></p>
                </div>
                <script>
                (function () {
                  var next = %s;
                  try {
                    if (window.opener && !window.opener.closed) {
                      window.opener.location.href = next;
                      window.close();
                      return;
                    }
                  } catch (e) {}
                  window.location.replace(next);
                })();
                </script>
                </body></html>
                """.formatted(esc(title), esc(title), esc(hint), esc(deskUrl), jsonString(deskUrl)));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "")
                + "\"";
    }
}
