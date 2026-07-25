package examstaff.controller;

import examstaff.service.StaffCallService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

/**
 * Orchestration ca gọi số ở Presentation: clear session flags + resume CallBoard.
 * Không chứa SQL/CRUD; không thay đổi trạng thái kỳ thi trên DB.
 *
 * Phân biệt “ca gọi” vs “kỳ thi”:
 * - <b>Ca gọi (Call Board):</b> resumeBoard — xóa shiftEnded/examPaused
 *       trên board in-memory + xóa flag session shiftEnded/shiftPaused
 * - <b>Kỳ thi (DB):</b> start/pause/end exam qua ExamControlServlet — cập nhật Status trên SQL
 * Method này chỉ làm nhánh Call Board + session UI.
 */
public final class ExamStaffShiftSupport {

    /** Không khởi tạo. */
    private ExamStaffShiftSupport() {
    }

    /**
     * Bắt đầu / tiếp tục ca gọi số cho một kỳ thi.
     * <p>
     * Luồng: xóa shiftEnded/shiftPaused → resumeBoard.
     * Pause/resume toàn kỳ thi (DB) thực hiện qua ExamControlServlet.
     * @param session   session staff
     * @param ctx       ServletContext để lấy CallBoard DAO
     * @param examId    mã kỳ thi
     * @param staffCall resume CallBoard
     * @return true nếu ca gọi số sẵn sàng tiếp tục
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
