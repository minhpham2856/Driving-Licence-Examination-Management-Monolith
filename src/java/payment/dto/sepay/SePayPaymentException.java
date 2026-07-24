package payment.dto.sepay;

/**
 * Ngoại lệ nghiệp vụ khi tích hợp SePay ở bước <b>checkout</b> (tạo phiên thanh toán).
 * <p>
 * Ném khi thiếu cấu hình ({@code SEPAY_MERCHANT_ID}, {@code SEPAY_SECRET_KEY}),
 * validate {@link SePayCheckoutRequest} thất bại, callback URL không hợp lệ hoặc lỗi ký form.
 * Bước IPN không dùng exception này — IPN trả {@link SePayIpnResult#reject} thay vì throw.
 */
public class SePayPaymentException extends Exception {

    public SePayPaymentException(String message) {
        super(message);
    }

    public SePayPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
