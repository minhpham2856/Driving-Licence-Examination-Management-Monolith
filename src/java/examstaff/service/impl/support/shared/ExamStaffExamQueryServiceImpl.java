package examstaff.service.impl.support.shared;

import examstaff.dao.ExamViewDAO;
import examstaff.dao.impl.ExamViewDAOImpl;
import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * Implementation truy vấn danh sách kỳ thi ExamStaff — facade mỏng trên ExamViewDAO.
 * Cung cấp listAllExams, findByExamId và lọc kỳ cùng ngày cho sidebar/dashboard.
 *
 * Vai trò trong luồng examstaff:
 * Mọi màn staff cần dropdown/sidebar ca thi đều cần danh sách ExamSummaryDTO đã sắp.
 * Lớp này tách JDBC khỏi ExamStaffViewServiceImpl và filter — inject một điểm đọc exam
 * dùng chung bởi ExamStaffSidebarFilter khi apply examId từ URL.
 *
 * Cách hoạt động:
 * - listAllExams — examViewDAO.findAllOrdered().
 * - findByExamId — lookup một kỳ theo id.
 * - listExamsForDay — ủy quyền ExamStaffExamRules.examsForExam trên list đã có.
 *
 * Ai gọi:
 * ExamStaffViewServiceImpl, ExamStaffSidebarFilter, ExamStaffPageSupport,
 * DashboardServlet, ExamSelectServlet — nạp context ca thi request/session.
 */
public class ExamStaffExamQueryServiceImpl {

    private final ExamViewDAO examViewDAO = new ExamViewDAOImpl();

    /**
     * Lấy toàn bộ kỳ thi dạng tóm tắt.
     * @return danh sách kỳ thi
     */
    public List<ExamSummaryDTO> listAllExams() {
        return examViewDAO.findAllOrdered();
    }

    /**
     * Tìm kỳ thi theo mã.
     * @param examId mã kỳ thi
     * @return tóm tắt kỳ thi, hoặc null nếu không có
     */
    public ExamSummaryDTO findByExamId(int examId) {
        return examViewDAO.findByExamId(examId);
    }

    /**
     * Lọc các kỳ thi cùng ngày với kỳ tham chiếu.
     * @param allExams danh sách kỳ nguồn
     * @param examId   mã kỳ tham chiếu
     * @return các kỳ trong cùng ngày
     */
    public List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId) {
        return ExamStaffExamRules.examsForExam(allExams, examId);
    }
}
