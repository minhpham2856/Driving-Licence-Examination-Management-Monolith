package dao;

import model.payment.Payment;

public interface PaymentDAO {

    // Them dang ky thi (qua Profile)
    boolean insert(Payment payment);
    // Lay theo candidate id

    Payment getByCandidateId(int candidateId);
}
