package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;

/**
 * Xem trước và ghi nhận thanh toán phí thủ tục (tiền mặt) của thí sinh.
 */
public interface ProcedurePaymentService {

    /**
     * Xem trước phí thủ tục trước khi xác nhận thanh toán.
     *
     * @param candidateId      mã đăng ký thí sinh
     * @param licenseCode      mã hạng bằng
     * @param requiresRoadTest true nếu cần sát hạch đường trường
     * @return kết quả phí dự kiến
     */
    ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest);

    /**
     * Ghi nhận thanh toán tiền mặt thủ tục cho hồ sơ.
     *
     * @param profile hồ sơ đăng ký thí sinh
     * @return true nếu ghi nhận thành công
     */
    boolean recordProcedureCashPayment(ExamRegistrationDTO profile);
}
