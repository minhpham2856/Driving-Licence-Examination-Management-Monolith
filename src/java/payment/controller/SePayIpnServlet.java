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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Webhook IPN SePay — <b>nguồn sự thật</b> khi ghi nhận đã thanh toán.
 * <p>
 * Cấu hình trên SePay Dashboard:
 * URL = {@code {SEPAY_APP_BASE_URL}/payment/sepay/ipn} (phải public, thường qua ngrok khi local),
 * method POST, auth {@code X-Secret-Key} = secret IPN (thường = {@code SEPAY_SECRET_KEY}
 * hoặc {@code SEPAY_IPN_SECRET} nếu Dashboard dùng secret riêng).
 * <p>
 * Khác return URL (success/cancel): IPN do server SePay gọi, không phụ thuộc trình duyệt staff.
 */
@WebServlet("/payment/sepay/ipn")
public class SePayIpnServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SePayIpnServlet.class.getName());

    private final SePayPaymentService sePayService = new SePayPaymentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Raw body nguyên gốc — chữ ký HMAC phải khớp đúng chuỗi này (không parse form trước)
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String secret = firstNonBlank(
                request.getHeader(SePayConstants.HEADER_SECRET_KEY),
                extractApikeyAuthorization(request.getHeader("Authorization")));
        String signature = request.getHeader(SePayConstants.HEADER_SIGNATURE);
        String timestamp = request.getHeader(SePayConstants.HEADER_TIMESTAMP);

        // Xác thực → parse ORDER_PAID → ghi Payment nếu chưa có
        SePayIpnResult result = sePayService.handleIpn(rawBody, secret, signature, timestamp);
        response.setContentType("application/json;charset=UTF-8");
        if (result.isAccepted()) {
            response.setStatus(HttpServletResponse.SC_OK);
            LOG.info("[SePay IPN] accepted bodyLen=" + (rawBody != null ? rawBody.length() : 0));
        } else {
            // Auth fail → 401; payload lỗi → 400 (ngrok/log dễ phân biệt)
            boolean authFail = result.getErrorMessage() != null
                    && result.getErrorMessage().toLowerCase().contains("unauthorized");
            response.setStatus(authFail
                    ? HttpServletResponse.SC_UNAUTHORIZED
                    : HttpServletResponse.SC_BAD_REQUEST);
            LOG.log(Level.WARNING, "[SePay IPN] rejected: {0} | hasXSecretKey={1} hasSignature={2} bodyLen={3}",
                    new Object[]{
                            result.getErrorMessage(),
                            secret != null && !secret.isBlank(),
                            signature != null && !signature.isBlank(),
                            rawBody != null ? rawBody.length() : 0
                    });
        }
        response.getWriter().write(result.responseJson());
    }

    /** {@code Authorization: Apikey xxx} (một số cấu hình webhook SePay). */
    private static String extractApikeyAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Apikey ", 0, 7)) {
            return v.substring(7).trim();
        }
        return null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }
}
