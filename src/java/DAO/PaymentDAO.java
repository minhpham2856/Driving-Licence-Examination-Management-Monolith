package DAO;

import Models.Fee;
import Models.Payment;
import java.util.List;

public interface PaymentDAO {
    boolean insert(Payment payment);

    boolean insertWithFees(Payment payment, List<Fee> fees);

    Payment getByCandidateId(int candidateId);

    /** Hủy thanh toán thủ tục để làm lại từ đầu (không xóa bản ghi). */
    boolean cancelCompletedByCandidateId(int candidateId);
}
