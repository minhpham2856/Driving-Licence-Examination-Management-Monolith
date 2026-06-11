package Controllers.Registrant;

import Services.Impl.SepayPaymentServiceImpl;
import Services.SepayPaymentService;
import Utils.SepayIpnParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <b>Webhook IPN (Instant Payment Notification)</b> — SEPay gọi server khi giao dịch thành công.
 *
 * <p><b>Khác gì trang success?</b></p>
 * <ul>
 *   <li>Trang success: trình duyệt thí sinh quay về → chỉ hiển thị thông báo, <b>không</b> cập nhật DB</li>
 *   <li>IPN: SEPay server POST JSON từ internet → <b>đây mới là nơi</b> đánh dấu Payment Completed</li>
 * </ul>
 *
 * <p>URL đầy đủ: {@code {sepay.appBaseUrl}/registrant/payment/sepay-ipn} — khai báo trên my.sepay.vn.</p>
 * <p>Không dùng {@link RegistrantAuth} vì caller là SEPay, không phải thí sinh đăng nhập.</p>
 *
 * <p>Khi {@code notification_type = ORDER_PAID}:</p>
 * <ol>
 *   <li>{@link SepayIpnParser} đọc invoice + amount từ JSON</li>
 *   <li>{@link SepayPaymentServiceImpl#handleOrderPaid} cập nhật Payment + ExamRegistration + registeredCount</li>
 * </ol>
 */
@WebServlet("/registrant/payment/sepay-ipn")
public class SepayIpnServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SepayIpnServlet.class.getName());

    private final SepayPaymentService sepayPaymentService = new SepayPaymentServiceImpl();

    /**
     * SEPay có thể gọi HEAD để kiểm tra URL IPN tồn tại trước khi lưu cấu hình merchant.
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Nhận POST JSON từ SEPay sau khi khách chuyển khoản thành công.
     *
     * <p>Phải trả HTTP 200 và {@code {"success":true}} — nếu không SEPay sẽ gửi lại IPN nhiều lần.</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Bước 1: xác thực — header X-Secret-Key phải khớp sepay.secretKey trong .env
        String secretHeader = request.getHeader("X-Secret-Key");
        if (!sepayPaymentService.verifyIpnSecret(secretHeader)) {
            LOG.warning("SEPay IPN rejected: invalid X-Secret-Key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid secret\"}");
            return;
        }

        // Bước 2: đọc toàn bộ body JSON (một dòng hoặc nhiều dòng)
        String body = request.getReader().lines().collect(Collectors.joining());
        var parsed = SepayIpnParser.parse(body);

        // Bước 3: chỉ xử lý ORDER_PAID — các loại notification khác trả 400
        if (parsed.isEmpty() || !"ORDER_PAID".equalsIgnoreCase(parsed.get().notificationType())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Unsupported notification\"}");
            return;
        }

        // Bước 4: cập nhật database (tìm Payment theo invoice, mark Completed, tăng registeredCount)
        var data = parsed.get();
        LOG.info("SEPay IPN ORDER_PAID invoice=" + data.orderInvoiceNumber() + " amount=" + data.orderAmount());
        boolean ok = sepayPaymentService.handleOrderPaid(data.orderInvoiceNumber(), data.orderAmount());
        if (!ok) {
            LOG.warning("SEPay IPN handleOrderPaid failed for invoice=" + data.orderInvoiceNumber());
        }

        // Bước 5: báo SEPay đã nhận — 200 + success:true để ngừng retry
        response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(ok ? "{\"success\":true}" : "{\"success\":false}");
    }
}
