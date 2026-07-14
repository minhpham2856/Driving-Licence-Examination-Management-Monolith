package examstaff.service;

import examstaff.dto.ReportPaymentSummaryDTO;

/**
 * Truy vấn tóm tắt thanh toán phục vụ báo cáo.
 */
public interface ReportFeeQueryService {

    /**
     * Lấy tổng hợp thanh toán của một thí sinh/đăng ký.
     *
     * @param candidateId mã đăng ký thí sinh
     * @return tóm tắt thanh toán, hoặc null nếu không có
     */
    ReportPaymentSummaryDTO findPaymentSummary(int candidateId);
}
