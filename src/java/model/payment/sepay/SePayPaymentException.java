package model.payment.sepay;

/** Lỗi tích hợp SePay (cấu hình, validate, ký). */
public class SePayPaymentException extends Exception {

    public SePayPaymentException(String message) {
        super(message);
    }

    public SePayPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
