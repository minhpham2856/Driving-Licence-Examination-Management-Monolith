package payment.dao;

import payment.dto.PaymentRecord;

/**
 * Truy cập bảng {@code Payment}.
 * <ul>
 *   <li>{@link #insert} — Cash desk hoặc SePay IPN</li>
 *   <li>{@link #sumCompletedPaymentsByUserId} — Dashboard Registrant {@code totalFee}</li>
 *   <li>{@link #existsCompletedByTransactionReference} — chống IPN trùng</li>
 * </ul>
 */
public interface PaymentDAO {

    /**
     * Ghi Payment hoàn tất; resolve ExamEnrollmentId từ CandidateId nếu thiếu.
     * @return true nếu insert OK (có PaymentId generated)
     */
    boolean insert(PaymentRecord payment);

    /**
     * Tổng lệ phí đã nộp của user portal (join CCCD Profile↔Candidate).
     */
    double sumCompletedPaymentsByUserId(int userId);

    /** true nếu đã có Payment hoàn tất cùng TransactionReference (idempotent IPN). */
    boolean existsCompletedByTransactionReference(String transactionReference);
}
