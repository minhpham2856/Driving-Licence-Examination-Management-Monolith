package examstaff.service.impl.support.shared;

import examstaff.dao.ExamViewDAO;
import examstaff.dao.impl.ExamViewDAOImpl;
import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/** Implementation: truy vấn danh sách kỳ thi qua {@link ExamViewDAO}. */
public class ExamStaffExamQueryServiceImpl {

    private final ExamViewDAO examViewDAO = new ExamViewDAOImpl();

    /**
     * Lấy toàn bộ kỳ thi dạng tóm tắt.
     *
     * @return danh sách kỳ thi
     */
    public List<ExamSummaryDTO> listAllExams() {
        return examViewDAO.findAllOrdered();
    }

    /**
     * Tìm kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return tóm tắt kỳ thi, hoặc null nếu không có
     */
    public ExamSummaryDTO findByExamId(int examId) {
        return examViewDAO.findByExamId(examId);
    }

    /**
     * Lọc các kỳ thi cùng ngày với kỳ tham chiếu.
     *
     * @param allExams danh sách kỳ nguồn
     * @param examId   mã kỳ tham chiếu
     * @return các kỳ trong cùng ngày
     */
    public List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId) {
        return ExamStaffExamRules.examsForExam(allExams, examId);
    }
}
