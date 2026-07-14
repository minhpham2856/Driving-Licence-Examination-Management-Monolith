package examstaff.controller.staff.exam.adapter;

import examstaff.dao.CallBoardDAO;
import examstaff.dao.impl.ServletContextCallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CallBoardSyncService;

import java.util.List;

/**
 * Biên HTTP → {@link CallBoardDAO} → {@link CallBoardSyncService}.
 * Controller chỉ dùng facade này, không tạo DAO ServletContext trực tiếp.
 */
public final class CallBoardHttpFacade {

    private final CallBoardSyncService syncService;

    /**
     * @param syncService service đồng bộ bảng gọi (BLL)
     */
    public CallBoardHttpFacade(CallBoardSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Tạo DAO gắn {@link ServletContext} của request hiện tại.
     *
     * @param ctx servlet context
     * @return DAO in-memory trên context
     */
    public CallBoardDAO dao(ServletContext ctx) {
        return new ServletContextCallBoardDAO(ctx);
    }

    /**
     * Đọc CallBoardState của kỳ thi từ ServletContext.
     *
     * @param ctx    servlet context
     * @param examId mã kỳ thi
     * @return state hoặc null nếu thiếu ctx/examId
     */
    public CallBoardState getState(ServletContext ctx, int examId) {
        if (ctx == null || examId <= 0) {
            return null;
        }
        return syncService.getState(dao(ctx), examId);
    }

    /**
     * Đồng bộ bảng gọi (số đang gọi / next / queue order).
     *
     * @param ctx        servlet context
     * @param examId     mã kỳ thi
     * @param callingSbd SBD đang gọi
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     */
    public void sync(ServletContext ctx, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.sync(dao(ctx), examId, callingSbd, queue, shiftEnded);
    }

    /**
     * Đánh dấu bàn thủ tục bận.
     *
     * @param ctx        servlet context
     * @param examId     mã kỳ thi
     * @param deskSbd    SBD ở bàn
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     */
    public void occupyDesk(ServletContext ctx, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.occupyDesk(dao(ctx), examId, deskSbd, queue, shiftEnded);
    }

    /**
     * Giải phóng bàn rồi gắn số đang gọi mới.
     *
     * @param ctx        servlet context
     * @param examId     mã kỳ thi
     * @param callingSbd SBD gọi tiếp
     * @param queue      hàng đợi
     * @param shiftEnded ca đã đóng
     */
    public void releaseDeskAndCall(ServletContext ctx, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.releaseDeskAndCall(dao(ctx), examId, callingSbd, queue, shiftEnded);
    }

    /**
     * Tiếp tục ca: xóa cờ shiftEnded / examPaused trên board.
     *
     * @param ctx    servlet context
     * @param examId mã kỳ thi
     */
    public void resumeShift(ServletContext ctx, int examId) {
        CallBoardState state = getState(ctx, examId);
        if (state != null) {
            state.setShiftEnded(false);
            state.setExamPaused(false);
            dao(ctx).saveState(examId, state);
        }
    }

    /**
     * Tạm dừng ca gọi trên bảng (giữ thứ tự queue).
     *
     * @param ctx    servlet context
     * @param examId mã kỳ thi
     * @param queue  hàng đợi
     */
    public void pauseShift(ServletContext ctx, int examId, List<ExamRegistrationDTO> queue) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.pauseShift(dao(ctx), examId, queue);
    }
}
