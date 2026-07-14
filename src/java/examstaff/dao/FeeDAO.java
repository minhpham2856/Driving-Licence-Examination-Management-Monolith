package examstaff.dao;

import shared.model.Fee;
import java.util.List;

/**
 * DAO lệ phí ({@link Fee}).
 */
public interface FeeDAO {

    /**
     * Lấy danh sách lệ phí thủ tục áp dụng theo hạng GPLX.
     *
     * @param licenseCode      mã hạng bằng
     * @param requiresRoadTest có phần thi đường trường hay không
     * @return danh sách phí phù hợp
     */
    List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest);

    /**
     * Lấy các khoản phí gắn với một thanh toán.
     *
     * @param paymentId mã thanh toán
     * @return danh sách phí
     */
    List<Fee> getFeesByPaymentId(int paymentId);
}
