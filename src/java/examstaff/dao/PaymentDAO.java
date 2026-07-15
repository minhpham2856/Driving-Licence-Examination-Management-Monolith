package examstaff.dao;

import shared.model.Payment;

/**
 * DAO thanh toán lệ phí ({@link Payment}).
 * Thao tác trên bảng {@code Payment} và liên kết {@code ExamEnrollment} theo thí sinh.
 */
public interface PaymentDAO {

    /**
     * Thêm bản ghi thanh toán mới.
     * Thực thi INSERT vào bảng {@code Payment} với PaymentStatus, PaymentMethod,
     * TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId.
     *
     * @param payment entity thanh toán; cần có {@code ExamEnrollmentId} hợp lệ
     * @return {@code true} nếu INSERT thành công; {@code false} nếu thất bại
     */
    boolean insert(Payment payment);

    /**
     * Lấy thanh toán mới nhất theo mã thí sinh.
     * Thực thi SELECT TOP 1 trên {@code Payment} INNER JOIN {@code ExamEnrollment}
     * theo {@code CandidateId}, ưu tiên bản ghi mới nhất.
     *
     * @param candidateId mã thí sinh ({@code Candidate.CandidateId})
     * @return entity {@link Payment} nếu tìm thấy; {@code null} nếu thí sinh chưa có thanh toán
     */
    Payment getByCandidateId(int candidateId);

    /**
     * Tra mã ghi danh ({@code ExamEnrollmentId}) mới nhất của thí sinh.
     * Thực thi SELECT TOP 1 {@code ExamEnrollmentId} FROM {@code ExamEnrollment}
     * WHERE {@code CandidateId = ?}.
     *
     * @param candidateId mã thí sinh cần tra ghi danh
     * @return mã {@code ExamEnrollmentId} mới nhất; {@code -1} nếu không có ghi danh
     */
    int resolveEnrollmentId(int candidateId);
}
