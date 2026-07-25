package payment.dto.sepay;

/**
 * Ngoại lệ nghiệp vụ khi tích hợp SePay ở bước checkout (tạo phiên thanh toán).
 * Ném khi thiếu cấu hình (SEPAY_MERCHANT_ID, SEPAY_SECRET_KEY),
 * validate SePayCheckoutRequest thất bại, callback URL không hợp lệ hoặc lỗi ký form.
 * Bước IPN không dùng exception này — IPN trả SePayIpnResult.reject thay vì throw.
 */
public class SePayPaymentException extends Exception {

    public SePayPaymentException(String message) {
        super(message);
    }

    public SePayPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
