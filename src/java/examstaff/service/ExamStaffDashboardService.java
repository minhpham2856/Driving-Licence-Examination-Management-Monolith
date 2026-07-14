package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;

import java.util.List;

/**
 * Xây dựng dữ liệu bảng điều khiển (dashboard) cho nhân viên kỳ thi.
 */
public interface ExamStaffDashboardService {

    /**
     * Ghép view dashboard theo kỳ thi đang chọn.
     *
     * @param allExams danh sách kỳ thi
     * @param examId   mã kỳ đang xem
     * @return DTO dashboard
     */
    ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allExams, int examId);
}
