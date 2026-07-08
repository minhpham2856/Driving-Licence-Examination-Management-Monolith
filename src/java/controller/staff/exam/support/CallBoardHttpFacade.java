package controller.staff.exam.support;

import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;
import model.view.CallBoardState;
import repository.CallBoardRepository;
import repository.ServletContextCallBoardRepository;
import service.CallBoardSyncService;
import service.ExamStaffServices;
import util.examstaff.CallQueueRules;

import java.util.List;

/** Cầu nối HTTP → repository → service (CallBoard). Controller chỉ dùng lớp này. */
public final class CallBoardHttpFacade {

    private CallBoardHttpFacade() {
    }

    private static CallBoardSyncService sync() {
        return ExamStaffServices.get().callBoardSync();
    }

    public static CallBoardRepository repository(ServletContext ctx) {
        return new ServletContextCallBoardRepository(ctx);
    }

    public static CallBoardState getState(ServletContext ctx, int examSessionId) {
        if (ctx == null || examSessionId <= 0) {
            return null;
        }
        return sync().getState(repository(ctx), examSessionId);
    }

    public static void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().sync(repository(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void occupyDesk(ServletContext ctx, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().occupyDesk(repository(ctx), examSessionId, deskSbd, queue, shiftEnded);
    }

    public static void releaseDeskAndCall(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().releaseDeskAndCall(repository(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void syncFromSession(ServletContext ctx, int examSessionId, String callingSbd,
            boolean shiftEnded, List<ExamRegistrationDTO> queue) {
        sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void resumeShift(ServletContext ctx, int examSessionId) {
        CallBoardState state = getState(ctx, examSessionId);
        if (state != null) {
            state.setShiftEnded(false);
            repository(ctx).saveState(examSessionId, state);
        }
    }

    public static List<ExamRegistrationDTO> applyQueueOrder(List<ExamRegistrationDTO> queue,
            List<String> orderSbds) {
        return CallQueueRules.applyQueueOrder(queue, orderSbds);
    }

    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }
}
