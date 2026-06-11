package DAO;

import Models.DashboardActivity;
import Models.PaymentRecord;
import Models.PendingRegistrationContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * DAO thanh toán — Payment + tra cứu pending SEPay (tách khỏi ExamRegistrationDAO).
 */
public interface PaymentDAO {

    int insertPending(int examRegistrationId, BigDecimal amount, String paymentMethod,
            String transactionReference, Timestamp paymentExpiresAt);

    boolean deleteByRegistrationId(int examRegistrationId);

    Optional<PaymentRecord> findByTransactionReference(String transactionReference);

    boolean markCompleted(int paymentId);

    /** Đánh dấu payment hết hạn / hủy — chỉ từ Pending. */
    boolean markCancelled(int paymentId);

    /** Hủy mọi payment Pending quá hạn của thí sinh; trả số bản ghi payment bị hủy. */
    int cancelOverduePendingForPerson(int personId);

    BigDecimal sumCompletedByPersonId(int personId);

    List<DashboardActivity> findRecentPaymentActivitiesByPersonId(int personId, int limit);

    Optional<PendingRegistrationContext> findResumablePending(int personId, int registrationId);

    /** Hủy ExamRegistration tương ứng các Payment đã Cancel (quá hạn SEPay). */
    int cancelRegistrationsForOverduePayments(int personId);
}
