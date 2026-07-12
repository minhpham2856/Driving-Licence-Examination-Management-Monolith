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

    public CallBoardState getState(ServletContext ctx, int examSessionId) {
        if (ctx == null || examSessionId <= 0) {
            return null;
        }
        return syncService.getState(dao(ctx), examSessionId);
    }

    public void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        syncService.sync(dao(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public void occupyDesk(ServletContext ctx, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        syncService.occupyDesk(dao(ctx), examSessionId, deskSbd, queue, shiftEnded);
    }

    public void releaseDeskAndCall(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        syncService.releaseDeskAndCall(dao(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public void resumeShift(ServletContext ctx, int examSessionId) {
        CallBoardState state = getState(ctx, examSessionId);
        if (state != null) {
            state.setShiftEnded(false);
            state.setExamPaused(false);
            dao(ctx).saveState(examSessionId, state);
        }
    }

    public void pauseShift(ServletContext ctx, int examSessionId, List<ExamRegistrationDTO> queue) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        syncService.pauseShift(dao(ctx), examSessionId, queue);
    }
}
