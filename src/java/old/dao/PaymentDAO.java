package dao;

import model.Payment;

public interface PaymentDAO {

    boolean insert(Payment payment);

    boolean hasCompletedPayment(int examEnrollmentId);
}
