package examstaff.dao;

import shared.model.Fee;
import java.util.List;

/**
 * DAO lệ phí ({@link Fee}).
 * Đọc bảng {@code Fee}, {@code Licence_Fee}, và liên kết {@code Payment_Fee} / thanh toán.
 */
public interface FeeDAO {

    /**
     * Lấy danh sách lệ phí thủ tục áp dụng theo hạng GPLX.
     * Thực thi SELECT trên {@code Fee} kèm số tiền từ {@code Licence_Fee}
     * (JOIN {@code Licence} theo {@code licenceCode}); có thể lọc phí có/không đường trường
     * tùy {@code requiresRoadTest}.
     *
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
     *
     * @param paymentId mã thanh toán ({@code Payment.PaymentId})
     * @return danh sách {@link Fee} thuộc thanh toán; rỗng nếu chưa gắn phí
     */
    List<Fee> getFeesByPaymentId(int paymentId);
}
