package examstaff.controller;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ServiceResult;
import examstaff.enums.ExamStatus;
import examstaff.service.ExamControlService;
import examstaff.service.ExamStaffViewService;
import examstaff.service.StaffCallService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.List;

/**
 * Orchestration ca thi ở Presentation: resume kỳ paused + clear session flags + resume CallBoard.
 * Không chứa SQL/CRUD.
 */
public final class ExamStaffShiftSupport {

    /** Không khởi tạo. */
    private ExamStaffShiftSupport() {
    }

    /**
     * Bắt đầu / tiếp tục ca gọi số cho một kỳ thi.
     * <p>
     * Luồng: kiểm tra kỳ paused → {@code resumeExam} (nếu cần) → xóa
     * {@code shiftEnded}/{@code shiftPaused} → {@code resumeBoard}.
     *
     * @param session            session staff (ghi flash lỗi/thành công nếu resume DB)
     * @param ctx                ServletContext để lấy CallBoard DAO
     * @param examId             mã kỳ thi
     * @param viewService        tra cứu trạng thái kỳ
     * @param examControlService resume kỳ trên DB
     * @param staffCall          resume CallBoard
     * @return {@code false} nếu resume DB thất bại (caller nên redirect kèm flash);
     *         {@code true} nếu ca sẵn sàng gọi số
     */
    public static boolean startOrResumeShift(HttpSession session, ServletContext ctx, int examId,
            ExamStaffViewService viewService, ExamControlService examControlService,
            StaffCallService staffCall) {
        if (examId <= 0 || viewService == null || examControlService == null || staffCall == null) {
            return false;
        }
        // 1) Nếu kỳ đang paused trên DB → gọi resume
        List<ExamSummaryDTO> exams = viewService.listAllExams();
        ExamSummaryDTO currentExam = viewService.findExamById(examId, exams);
        if (currentExam != null && ExamStatus.isPaused(currentExam.getStatus())) {
            ServiceResult<String> resume = examControlService.resumeExam(examId);
            if (!resume.isSuccess()) {
                if (session != null) {
                    session.setAttribute("examControlError", resume.getMessage());
                }
                return false;
            }
            if (session != null) {
                session.setAttribute("examControlMsg", resume.getMessage());
            }
        }
        // 2) Xóa cờ ca trên session rồi mở lại CallBoard
        if (session != null) {
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
        }
        staffCall.resumeBoard(ExamStaffHttpSupport.callBoardDao(ctx), examId);
        return true;
    }
}
