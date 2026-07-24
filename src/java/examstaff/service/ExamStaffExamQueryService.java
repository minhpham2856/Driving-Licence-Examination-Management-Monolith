package examstaff.service;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * Truy vấn danh sách và chi tiết kỳ thi phục vụ exam staff.
 */
public interface ExamStaffExamQueryService {

    /**
     * Lấy toàn bộ kỳ thi dạng tóm tắt.
     *
     * @return danh sách kỳ thi
     */
    List<ExamSummaryDTO> listAllExams();

    /**
     * Tìm kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return tóm tắt kỳ thi, hoặc null nếu không có
     */
    ExamSummaryDTO findByExamId(int examId);

    /**
     * Lọc các kỳ thi cùng ngày với kỳ tham chiếu.
     *
     * @param allExams danh sách kỳ nguồn
     * @param examId   mã kỳ tham chiếu
     * @return các kỳ trong cùng ngày
     */
    List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId);
}
