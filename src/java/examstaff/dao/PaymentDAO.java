package examstaff.dao;

import examstaff.model.Payment;

public interface PaymentDAO {

    boolean insert(Payment payment);

    // --- mainTest method ---
    boolean hasCompletedPayment(int examEnrollmentId);

    // --- CleanMyBranch methods ---
    Payment getByCandidateId(int candidateId);

    int resolveEnrollmentId(int candidateId);
}
