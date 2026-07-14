package examiner.dao;

import shared.model.Payment;

// DAO contract for Payment persistence; examiner module SQL boundary.
public interface PaymentDAO {

    // Inserts a payment row for one exam enrollment.
    boolean add(Payment payment);

    // Returns true when a completed payment exists for one enrollment.
    boolean hasCompletedPayment(int examEnrollmentId);
}
