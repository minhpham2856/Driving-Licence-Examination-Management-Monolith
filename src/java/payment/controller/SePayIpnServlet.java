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

/** Endpoint IPN SePay (/payment/sepay/ipn): raw body + auth headers → handleIpn → JSON 200/401. */
@WebServlet("/payment/sepay/ipn")
public class SePayIpnServlet extends HttpServlet {

    private final SePayPaymentService sePayService = new SePayPaymentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Đọc raw bytes — không parse form — vì HMAC ký trên đúng chuỗi này
        String rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String secret = request.getHeader(SePayConstants.HEADER_SECRET_KEY);
        String signature = request.getHeader(SePayConstants.HEADER_SIGNATURE);
        String timestamp = request.getHeader(SePayConstants.HEADER_TIMESTAMP);

        SePayIpnResult result = sePayService.handleIpn(rawBody, secret, signature, timestamp);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(result.isAccepted() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(result.responseJson());
    }
}
