package examstaff.service;

import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

/**
 * Tính/giải phí thủ tục theo hồ sơ thí sinh tại bàn thủ tục.
 */
public interface ProcedureFeeQueryService {

    /**
     * Xác định các khoản phí thủ tục áp dụng cho hồ sơ.
     *
     * @param profile hồ sơ đăng ký thí sinh
     * @return kết quả phí (khoản mục, tổng, …)
     */
    ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile);
}
