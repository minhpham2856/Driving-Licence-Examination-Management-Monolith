package examstaff.dao;

import shared.model.Payment;

/**
 * Cổng truy cập thanh toán lệ phí (Payment).
 *
 * Vai trò trong kiến trúc:
 * Ghi và đọc giao dịch thanh toán gắn ExamEnrollment. Thường được gọi
 * từ luồng thủ tục (ExamRegistrationDAO.updatePayment có thể ủy quyền impl riêng).
 * <pre>
 *   ProcedureServlet / ExamRegistrationDAOImpl
 *            │  insert / getByCandidateId
 *            ▼
 *      PaymentDAO  ◄── PaymentDAOImpl
 *            │
 *            ▼  Payment (ExamEnrollmentId, PaymentStatus, TotalAmount…)
 *         DLEM_DB_2
 * </pre>
 *
 * Hợp đồng:
 * - insert — bắt buộc ExamEnrollmentId hợp lệ; trả PaymentId sinh ra
 * - getByCandidateId — TOP 1 payment mới nhất của thí sinh
 * - resolveEnrollmentId — tra ExamEnrollmentId mới nhất (helper trước INSERT)
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.PaymentDAOImpl.
 */
public interface PaymentDAO {

    /**
     * Thêm bản ghi thanh toán mới.
     * Thực thi INSERT vào bảng Payment với PaymentStatus, PaymentMethod,
     * TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId.
     * @param payment entity thanh toán; cần có ExamEnrollmentId hợp lệ
     * @return true nếu INSERT thành công; false nếu thất bại
     */
    boolean insert(Payment payment);

    /**
     * Lấy thanh toán mới nhất theo mã thí sinh.
     * Thực thi SELECT TOP 1 trên Payment INNER JOIN ExamEnrollment
     * theo CandidateId, ưu tiên bản ghi mới nhất.
     * @param candidateId mã thí sinh (Candidate.CandidateId)
     * @return entity Payment nếu tìm thấy; null nếu thí sinh chưa có thanh toán
     */
    Payment getByCandidateId(int candidateId);

    /**
     * Tra mã ghi danh (ExamEnrollmentId) mới nhất của thí sinh.
     * Thực thi SELECT TOP 1 ExamEnrollmentId FROM ExamEnrollment
     * WHERE CandidateId = ?.
     * @param candidateId mã thí sinh cần tra ghi danh
     * @return mã ExamEnrollmentId mới nhất; -1 nếu không có ghi danh
     */
    int resolveEnrollmentId(int candidateId);
}
