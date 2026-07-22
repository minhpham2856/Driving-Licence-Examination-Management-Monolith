package examstaff.dao;

import shared.model.Fee;
import java.util.List;

/**
 * Cổng truy cập lệ phí ({@link Fee}) — biểu phí theo hạng GPLX và theo payment.
 *
 * Vai trò trong kiến trúc:
 * Phục vụ màn thu lệ phí thủ tục: tra biểu phí áp dụng cho hạng bằng kỳ thi
 * và liệt kê các khoản đã gắn với một giao dịch {@code Payment}.
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
 * - {@link #getProcedureFees} — phí thủ tục active theo {@code licenceCode},
 *       lọc có/không đường trường ({@code requiresRoadTest})
 * - {@link #getFeesByPaymentId} — chi tiết phí đã chọn trong một {@code PaymentId}
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.FeeDAOImpl} — OUTER APPLY {@code Licence_Fee} theo hạng.
 */
public interface FeeDAO {

    /**
     * Lấy danh sách lệ phí thủ tục áp dụng theo hạng GPLX.
     * Thực thi SELECT trên {@code Fee} kèm số tiền từ {@code Licence_Fee}
     * (JOIN {@code Licence} theo {@code licenceCode}); có thể lọc phí có/không đường trường
     * tùy {@code requiresRoadTest}.
     * @param licenseCode      mã hạng bằng (ví dụ B1, B2, C…)
     * @param requiresRoadTest {@code true} nếu kỳ thi có phần đường trường; ảnh hưởng tập phí trả về
     * @return danh sách {@link Fee} phù hợp; rỗng nếu không có biểu phí cho hạng
     */
    List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest);

    /**
     * Lấy các khoản phí gắn với một thanh toán.
     * Thực thi SELECT trên {@code Payment_Fee} JOIN {@code Fee}, {@code Payment},
     * {@code ExamEnrollment}, {@code Exam}, {@code Licence} và {@code Licence_Fee}
     * để suy ra số tiền theo hạng; điều kiện {@code PaymentId = ?}.
     * @param paymentId mã thanh toán ({@code Payment.PaymentId})
     * @return danh sách {@link Fee} thuộc thanh toán; rỗng nếu chưa gắn phí
     */
    List<Fee> getFeesByPaymentId(int paymentId);
}
