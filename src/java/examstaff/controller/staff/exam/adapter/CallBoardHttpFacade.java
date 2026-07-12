package examstaff.controller.staff.exam.adapter;

import examstaff.dao.CallBoardDAO;
import examstaff.dao.impl.ServletContextCallBoardDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import examstaff.model.view.CallBoardState;
import examstaff.service.CallBoardSyncService;

import java.util.List;

/** HTTP edge -> CallBoardDAO -> CallBoardSyncService. Controllers use this facade only. */
public final class CallBoardHttpFacade {

    private final CallBoardSyncService syncService;

    public CallBoardHttpFacade(CallBoardSyncService syncService) {
        this.syncService = syncService;
    }

    public CallBoardDAO dao(ServletContext ctx) {
        return new ServletContextCallBoardDAO(ctx);
    }

    public CallBoardState getState(ServletContext ctx, int examId) {
        if (ctx == null || examId <= 0) {
            return null;
        }
        return syncService.getState(dao(ctx), examId);
    }

    public void sync(ServletContext ctx, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.sync(dao(ctx), examId, callingSbd, queue, shiftEnded);
    }

    public void occupyDesk(ServletContext ctx, int examId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.occupyDesk(dao(ctx), examId, deskSbd, queue, shiftEnded);
    }

    public void releaseDeskAndCall(ServletContext ctx, int examId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.releaseDeskAndCall(dao(ctx), examId, callingSbd, queue, shiftEnded);
    }

    public void resumeShift(ServletContext ctx, int examId) {
        CallBoardState state = getState(ctx, examId);
        if (state != null) {
            state.setShiftEnded(false);
            state.setExamPaused(false);
            dao(ctx).saveState(examId, state);
        }
    }

    public void pauseShift(ServletContext ctx, int examId, List<ExamRegistrationDTO> queue) {
        if (ctx == null || examId <= 0) {
            return;
        }
        syncService.pauseShift(dao(ctx), examId, queue);
    }
}
