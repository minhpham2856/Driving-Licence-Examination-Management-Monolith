package examstaff.dao;

import shared.model.Payment;

public interface PaymentDAO {

    boolean insert(Payment payment);

    Payment getByCandidateId(int candidateId);

    int resolveEnrollmentId(int candidateId);
}
