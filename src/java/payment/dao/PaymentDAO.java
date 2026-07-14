package payment.dao;

import payment.dto.PaymentRecord;

/** Payment (DLEM_DB_2) — cần ExamEnrollmentId do staff tạo trước. */
public interface PaymentDAO {
    /** IPN Paid; resolve enrollment từ CandidateId nếu thiếu. */
    boolean insert(PaymentRecord payment);

    double sumCompletedPaymentsByUserId(int userId);

    /** Idempotent IPN theo TransactionReference. */
    boolean existsCompletedByTransactionReference(String transactionReference);
}
