package examstaff.dao;

import shared.model.Payment;

/**
 * DAO thanh toán lệ phí ({@link Payment}).
 */
public interface PaymentDAO {

    /**
     * Thêm bản ghi thanh toán mới.
     *
     * @param payment entity thanh toán (cần {@code ExamEnrollmentId})
     * @return {@code true} nếu insert thành công
     */
    boolean insert(Payment payment);

    /**
     * Lấy thanh toán mới nhất theo mã thí sinh.
     *
     * @param candidateId mã thí sinh
     * @return entity hoặc {@code null}
     */
    Payment getByCandidateId(int candidateId);

    /**
     * Tra mã ghi danh ({@code ExamEnrollmentId}) mới nhất của thí sinh.
     *
     * @param candidateId mã thí sinh
     * @return mã ghi danh, hoặc -1 nếu không có
     */
    int resolveEnrollmentId(int candidateId);
}
