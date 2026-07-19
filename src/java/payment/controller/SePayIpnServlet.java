package payment.controller;

import payment.dto.sepay.SePayIpnResult;
import payment.service.impl.SePayPaymentServiceImpl;
import payment.service.SePayPaymentService;
import payment.util.sepay.SePayConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Webhook IPN SePay — <b>nguồn sự thật</b> khi ghi nhận đã thanh toán.
 * <p>
 * Cấu hình trên SePay Dashboard:
 * URL = {@code {SEPAY_APP_BASE_URL}/payment/sepay/ipn} (phải public, thường qua ngrok khi local),
 * method POST, auth header {@code X-Secret-Key}.
 * <p>
 * Khác return URL (success/cancel): IPN do server SePay gọi, không phụ thuộc trình duyệt staff.
 */
@WebServlet("/payment/sepay/ipn")
public class SePayIpnServlet extends HttpServlet {

    private final SePayPaymentService sePayService = new SePayPaymentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Raw body nguyên gốc — chữ ký HMAC phải khớp đúng chuỗi này (không parse form trước)
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String secret = request.getHeader(SePayConstants.HEADER_SECRET_KEY);
        String signature = request.getHeader(SePayConstants.HEADER_SIGNATURE);
        String timestamp = request.getHeader(SePayConstants.HEADER_TIMESTAMP);

        // Xác thực → parse ORDER_PAID → ghi Payment nếu chưa có
        SePayIpnResult result = sePayService.handleIpn(rawBody, secret, signature, timestamp);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(result.isAccepted() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(result.responseJson());
    }
}
