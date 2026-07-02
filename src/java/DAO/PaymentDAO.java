package dao;

import model.payment.Payment;

public interface PaymentDAO {
    boolean insert(Payment payment);

    /** Tổng lệ phí đã thanh toán thành công của thí sinh (theo UserId). */
    double sumCompletedPaymentsByUserId(int userId);
}
