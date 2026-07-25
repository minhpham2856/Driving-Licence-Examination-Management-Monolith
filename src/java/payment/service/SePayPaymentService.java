package payment.service;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;

/**
 * Giao diện facade tích hợp cổng SePay cho bàn thủ tục (Examstaff); Registrant chỉ đọc Payment sau IPN/tiền mặt.
 * Luồng checkout → IPN → return: (1) createCheckout + buildAutoSubmitHtml — tạo form signed, mở QR, chưa ghi Payment;
 * (2) handleIpn — webhook server-to-server, ghi Payment Hoàn tất (nguồn sự thật);
 * (3) return URL success/cancel/error — chỉ UX trình duyệt, không ghi DB.
 * generateInvoiceNumber sinh mã DLEM-CHK-… gắn Candidate/ExamEnrollment cho IPN parse.
 */
public interface SePayPaymentService {

    /** Đủ MERCHANT_ID + SECRET_KEY trong .env. */
    boolean isConfigured();

    /** true khi SEPAY_ENV ≠ production. */
    boolean sandbox();

    /** Ký form checkout HMAC → session field POST lên pay.sepay.vn. */
    SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException;

    /** HTML form hidden + JS submit — body popup desk. */
    String buildAutoSubmitHtml(SePayCheckoutSession session);

    /** DLEM-{prefix}-{candidateId}-{timestamp} */
    String generateInvoiceNumber(String businessPrefix, long candidateId);

    /** DLEM-{prefix}-{candidateId}-{enrollmentId}-{timestamp} — desk dùng prefix CHK. */
    String generateInvoiceNumber(String businessPrefix, long candidateId, long enrollmentId);

    /** SEPAY_APP_BASE_URL/payment/sepay/ipn — khai báo trên SePay Dashboard. */
    String ipnCallbackUrl();

    /**
     * Xác thực X-Secret-Key / HMAC → parse ORDER_PAID+CAPTURED → INSERT Payment.
     * Trả accepted=true thì HTTP 200; false thì 401 hoặc 400.
     */
    SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader);
}
