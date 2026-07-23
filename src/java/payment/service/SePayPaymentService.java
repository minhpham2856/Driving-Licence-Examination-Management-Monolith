package payment.service;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;

/**
 * Facade cổng SePay (dùng bởi Examstaff desk; Registrant chỉ đọc Payment sau IPN/cash).
 * <ol>
 *   <li>{@link #createCheckout} + {@link #buildAutoSubmitHtml} — mở QR (chưa ghi Payment)</li>
 *   <li>{@link #handleIpn} — webhook ghi Payment (nguồn sự thật)</li>
 *   <li>{@link #generateInvoiceNumber} — {@code DLEM-CHK-…} gắn candidate/enrollment</li>
 * </ol>
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
