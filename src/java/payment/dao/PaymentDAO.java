package payment.dao;

import payment.dto.PaymentRecord;

public interface PaymentDAO {
    boolean insert(PaymentRecord payment);

    /** Tổng lệ phí đã thanh toán thành công của thí sinh (theo UserId). */
    double sumCompletedPaymentsByUserId(int userId);

    /** Kiểm tra giao dịch đã ghi nhận (idempotent IPN). */
    boolean existsCompletedByTransactionReference(String transactionReference);
}
