package examstaff.controller;

import examstaff.service.StaffCallService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

/**
 * Orchestration ca gọi số ở Presentation: clear session flags + resume CallBoard.
 * Không chứa SQL/CRUD; không thay đổi trạng thái kỳ thi trên DB.
 */
public final class ExamStaffShiftSupport {

    /** Không khởi tạo. */
    private ExamStaffShiftSupport() {
    }

    /**
     * Bắt đầu / tiếp tục ca gọi số cho một kỳ thi.
     * <p>
     * Luồng: xóa {@code shiftEnded}/{@code shiftPaused} → {@code resumeBoard}.
     * Pause/resume toàn kỳ thi (DB) thực hiện qua {@code ExamControlServlet}.
     *
     * @param session   session staff
     * @param ctx       ServletContext để lấy CallBoard DAO
     * @param examId    mã kỳ thi
     * @param staffCall resume CallBoard
     * @return {@code true} nếu ca gọi số sẵn sàng tiếp tục
     */
    public static boolean startOrResumeShift(HttpSession session, ServletContext ctx, int examId,
            StaffCallService staffCall) {
        if (examId <= 0 || staffCall == null) {
            return false;
        }
        if (session != null) {
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
        }
        staffCall.resumeBoard(ExamStaffHttpSupport.callBoardDao(ctx), examId);
        return true;
    }
}
