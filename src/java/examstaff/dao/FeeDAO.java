package examstaff.dao;

import shared.model.Fee;
import java.util.List;

/**
 * Cổng truy cập lệ phí (Fee) — biểu phí theo hạng GPLX và theo payment.
 *
 * Vai trò trong kiến trúc:
 * Phục vụ màn thu lệ phí thủ tục: tra biểu phí áp dụng cho hạng bằng kỳ thi
 * và liệt kê các khoản đã gắn với một giao dịch Payment.
 * <pre>
 *   ProcedureServlet / Payment flow
 *            │  getProcedureFees / getFeesByPaymentId
 *            ▼
 *      FeeDAO  ◄── FeeDAOImpl
 *            │
 *            ▼  Fee + Licence_Fee + Payment_Fee
 *         DLEM_DB_2
 * </pre>
 *
 * Hai nhóm truy vấn:
 * - getProcedureFees — phí thủ tục active theo licenceCode,
 *       lọc có/không đường trường (requiresRoadTest)
 * - getFeesByPaymentId — chi tiết phí đã chọn trong một PaymentId
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.FeeDAOImpl — OUTER APPLY Licence_Fee theo hạng.
 */
public interface FeeDAO {

    /**
     * Lấy danh sách lệ phí thủ tục áp dụng theo hạng GPLX.
     * Thực thi SELECT trên Fee kèm số tiền từ Licence_Fee
     * (JOIN Licence theo licenceCode); có thể lọc phí có/không đường trường
     * tùy requiresRoadTest.
     * @param licenseCode      mã hạng bằng (ví dụ B1, B2, C…)
     * @param requiresRoadTest true nếu kỳ thi có phần đường trường; ảnh hưởng tập phí trả về
     * @return danh sách Fee phù hợp; rỗng nếu không có biểu phí cho hạng
     */
    List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest);

    /**
     * Lấy các khoản phí gắn với một thanh toán.
     * Thực thi SELECT trên Payment_Fee JOIN Fee, Payment,
     * ExamEnrollment, Exam, Licence và Licence_Fee
     * để suy ra số tiền theo hạng; điều kiện PaymentId = ?.
     * @param paymentId mã thanh toán (Payment.PaymentId)
     * @return danh sách Fee thuộc thanh toán; rỗng nếu chưa gắn phí
     */
    List<Fee> getFeesByPaymentId(int paymentId);
}
