package controller.staff.exam.adapter;

import dao.CallBoardDAO;
import dao.impl.ServletContextCallBoardDAO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateCallBoardStateDTO;
import jakarta.servlet.ServletContext;
import model.view.CallBoardState;
import service.CandidateCallBoardService;
import service.CallBoardSyncService;
import util.examstaff.CallQueueRules;

import java.util.List;

/** HTTP edge -> CallBoardDAO -> CallBoardSyncService. Controllers use this facade only. */
public final class CallBoardHttpFacade {

    private final CallBoardSyncService syncService;
    private final CandidateCallBoardService callBoardService;

    public CallBoardHttpFacade(CallBoardSyncService syncService, CandidateCallBoardService callBoardService) {
        this.syncService = syncService;
        this.callBoardService = callBoardService;
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

    public CandidateCallBoardStateDTO getBoardState(ServletContext ctx, int examSessionId) {
        if (ctx == null || examSessionId <= 0) {
            return null;
        }
        return callBoardService.getState(dao(ctx), examSessionId);
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

    public void syncFromSession(ServletContext ctx, int examSessionId, String callingSbd,
            boolean shiftEnded, List<ExamRegistrationDTO> queue) {
        sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    public void resumeShift(ServletContext ctx, int examSessionId) {
        CallBoardState state = getState(ctx, examSessionId);
        if (state != null) {
            state.setShiftEnded(false);
            dao(ctx).saveState(examSessionId, state);
        }
    }

    public List<ExamRegistrationDTO> applyQueueOrder(List<ExamRegistrationDTO> queue,
            List<String> orderSbds) {
        return CallQueueRules.applyQueueOrder(queue, orderSbds);
    }

    public ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }
}
