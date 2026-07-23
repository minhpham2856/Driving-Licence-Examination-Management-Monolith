package payment.dao;

import payment.dto.PaymentRecord;

/**
 * Giao diện truy cập bảng {@code Payment} — dùng chung tiền mặt desk, SePay IPN và dashboard Registrant.
 * <p>
 * {@link #insert} ghi Payment Hoàn tất (Cash hoặc sau IPN {@code ORDER_PAID}/{@code CAPTURED});
 * {@link #existsCompletedByTransactionReference} chống webhook trùng (idempotent IPN);
 * {@link #sumCompletedPaymentsByUserId} tổng lệ phí đã nộp qua join Profile↔Candidate↔ExamEnrollment.
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
