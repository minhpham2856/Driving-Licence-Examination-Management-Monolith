package payment.service;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;

/**
 * Giao diện facade tích hợp cổng SePay cho bàn thủ tục (Examstaff); Registrant chỉ đọc Payment sau IPN/tiền mặt.
 * <p>
 * Ba bước luồng checkout → IPN → return:
 * <ol>
 *   <li>{@link #createCheckout} + {@link #buildAutoSubmitHtml} — tạo form signed, mở QR (chưa ghi {@code Payment})</li>
 *   <li>{@link #handleIpn} — webhook server-to-server, ghi {@code Payment} Hoàn tất (nguồn sự thật)</li>
 *   <li>Return URL (success/cancel/error) — chỉ UX trình duyệt, không ghi DB</li>
 * </ol>
 * {@link #generateInvoiceNumber} sinh mã {@code DLEM-CHK-…} gắn Candidate/ExamEnrollment cho IPN parse.
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

    /** {SEPAY_APP_BASE_URL}/payment/sepay/ipn — khai báo trên SePay Dashboard. */
    String ipnCallbackUrl();

    /**
     * Xác thực X-Secret-Key / HMAC → parse ORDER_PAID+CAPTURED → INSERT Payment.
     * @return accepted=true → HTTP 200; false → 401/400
     */
    SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader);
}
