package service;

import model.payment.sepay.SePayCheckoutRequest;
import model.payment.sepay.SePayCheckoutSession;
import model.payment.sepay.SePayIpnResult;
import model.payment.sepay.SePayPaymentException;

/**
 * Facade thanh toán SePay — module khác inject / new {@code SePayPaymentServiceImpl} và gọi.
 *
 * <h3>Luồng khuyến nghị</h3>
 * <ol>
 *   <li>{@link #createCheckout(SePayCheckoutRequest)} — tạo form đã ký</li>
 *   <li>{@link #buildAutoSubmitHtml(SePayCheckoutSession)} — redirect khách sang SePay</li>
 *   <li>Servlet IPN gọi {@link #handleIpn(String, String, String, String)} — cập nhật đơn nội bộ</li>
 * </ol>
 *
 * <p>Không gắn với registrant; staff / quầy thu phí / API nội bộ tự wire servlet.</p>
 */
public interface SePayPaymentService {

    boolean isConfigured();

    boolean sandbox();

    /**
     * Tạo phiên checkout (form POST + chữ ký HMAC).
     * {@code orderInvoiceNumber} phải unique trên toàn hệ thống.
     */
    SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException;

    /** HTML form tự submit — nhúng JSP hoặc {@code response.getWriter().write(...)}. */
    String buildAutoSubmitHtml(SePayCheckoutSession session);

    /**
     * Sinh mã hóa đơn chuẩn: {@code DLEM-{prefix}-{id}-{epoch}}.
     */
    String generateInvoiceNumber(String businessPrefix, long internalOrderId);

    /**
     * Xử lý IPN từ SePay.
     *
     * @param rawBody body JSON thô (đọc từ {@code request.getInputStream()}, không parse trước)
     * @param secretHeader giá trị header {@code X-Secret-Key}
     * @param signatureHeader giá trị header {@code X-SePay-Signature} (tuỳ chọn)
     * @param timestampHeader giá trị header {@code X-SePay-Timestamp} (tuỳ chọn)
     */
    SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader);
}
