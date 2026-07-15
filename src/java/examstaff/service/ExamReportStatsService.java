package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;

import java.util.List;

/**
 * Tính thống kê báo cáo kỳ thi từ danh sách thí sinh.
 */
public interface ExamReportStatsService {

    /**
     * Tổng hợp chỉ số báo cáo (số thí sinh, đỗ/trượt, …) theo danh sách đã lọc.
     *
     * @param candidates danh sách thí sinh trong báo cáo
     * @param examId     mã kỳ thi
     * @return DTO thống kê báo cáo
     */
    ExamReportStatsDTO computeStats(List<ExamRegistrationDTO> candidates, int examId);
}
