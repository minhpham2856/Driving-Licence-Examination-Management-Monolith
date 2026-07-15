package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;

import java.util.List;

/**
 * Phân tích trạng thái thủ tục của thí sinh phục vụ báo cáo kỳ thi.
 */
public interface ExamReportProcedureStatusService {

    /**
     * Phân loại / tổng hợp trạng thái thủ tục (ảnh, thanh toán, …) trong danh sách.
     *
     * @param candidates danh sách thí sinh
     * @param webRoot    thư mục gốc web để kiểm tra ảnh vật lý nếu cần
     * @return DTO trạng thái thủ tục báo cáo
     */
    ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates, String webRoot);
}
