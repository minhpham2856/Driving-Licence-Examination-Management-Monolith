package controller.staff.exam.page;

import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.binder.ExamStaffPageBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffPageContextDTO;
import dto.examstaff.ExamStaffPagePrepareInput;
import dto.examstaff.ExamStaffPageTransitionInput;
import dto.examstaff.ExamStaffPageTransitionStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.ExamStaffPageService;
import service.ExamStaffSelectionService;

import java.util.List;

/**
 * Prepares exam-staff page context and binds the initial page model.
 */
public final class ExamStaffPageFacade {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffSelectionFacade SELECTION_FACADE = MODULE.selectionFacade();

    private ExamStaffPageFacade() {
    }

    private static ExamStaffPageService page() {
        return MODULE.services().page();
    }

    private static ExamStaffSelectionService selection() {
        return MODULE.services().selection();
    }

    public static final class ExamStaffPageContext {
        private final int examId;
        private final int sessionId;
        private final List<SessionDTO> allSessions;
        private final List<ExamRegistrationDTO> candidates;

        public ExamStaffPageContext(int examId, int sessionId, List<SessionDTO> allSessions,
                List<ExamRegistrationDTO> candidates) {
            this.examId = examId;
            this.sessionId = sessionId;
            this.allSessions = allSessions != null ? allSessions : List.of();
            this.candidates = candidates != null ? candidates : List.of();
        }

        public int getExamId() {
            return examId;
        }

        public int getSessionId() {
            return sessionId;
        }

        public List<SessionDTO> getAllSessions() {
            return allSessions;
        }

        public List<ExamRegistrationDTO> getCandidates() {
            return candidates;
        }
    }

    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot) {
        return prepareExamStaffPage(request, session, webRoot, true);
    }

    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates) {
        applyUtf8Request(request);
        int urlSessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
        if (urlSessionId > 0 && session != null) {
            ExamStaffPageTransitionStateDTO transition = selection().preparePageTransition(
                    buildPageTransitionInput(session, urlSessionId));
            if (transition.isClearCandidateCache()) {
                SELECTION_FACADE.clearCandidateCache(session);
            }
            if (transition.isClearProcedureState()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(session,
                        transition.getExamId(), transition.getSessionId());
            }
            if (transition.isPersistSelection()) {
                ExamStaffPageBinder.persistExamSelection(session, transition.getSessionId(), transition.getExamId());
            }
        }

        ExamStaffPagePrepareInput input = buildPagePrepareInput(request, session, webRoot, loadCandidates, urlSessionId);
        ExamStaffPageContextDTO ctx = page().preparePageContext(input);

        if (ExamStaffHttpSupport.parseSessionIdParam(request) > 0 && ctx.getExamId() <= 0 && request != null) {
            request.setAttribute("sessionSelectError",
                    "Khong tim thay ky thi (sessionId=" + urlSessionId + ").");
        }

        if (ctx.getExamId() > 0 && ctx.getSessionId() > 0 && session != null) {
            ExamStaffPageBinder.persistExamSelection(session, ctx.getSessionId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            ExamStaffPageBinder.bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = MODULE.services().candidateQueue().buildSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getSessionId());
        ExamStaffPageBinder.publishQueue(request, session, snapshot);

        return new ExamStaffPageContext(ctx.getExamId(), ctx.getSessionId(),
                ctx.getAllSessions(), ctx.getCandidates());
    }

    private static ExamStaffPagePrepareInput buildPagePrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlSessionId) {
        ExamStaffPagePrepareInput input = new ExamStaffPagePrepareInput();
        input.setUrlSessionId(urlSessionId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setHasSessionIdParam(ExamStaffHttpSupport.parseSessionIdParam(request) > 0);
        input.setAllSessions(SELECTION_FACADE.loadAllSessions());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId((Integer) session.getAttribute("selectedExamId"));
            input.setPreviousSessionId((Integer) session.getAttribute("selectedSessionId"));
            input.setSelectedExamId((Integer) session.getAttribute("selectedExamId"));
            input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
            input.setLoadedExamId((Integer) session.getAttribute("examStaffLoadedExamId"));
            input.setLoadedSessionId((Integer) session.getAttribute("examStaffLoadedSessionId"));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order = (List<String>) session.getAttribute("callQueueOrder");
            input.setCallQueueOrder(order);
            input.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));
        }
        return input;
    }

    private static ExamStaffPageTransitionInput buildPageTransitionInput(HttpSession session, int urlSessionId) {
        ExamStaffPageTransitionInput input = new ExamStaffPageTransitionInput();
        input.setUrlSessionId(urlSessionId);
        input.setAllSessions(SELECTION_FACADE.loadAllSessions());
        if (session != null) {
            input.setPreviousExamId((Integer) session.getAttribute("selectedExamId"));
            input.setPreviousSessionId((Integer) session.getAttribute("selectedSessionId"));
            input.setLoadedSessionId((Integer) session.getAttribute("examStaffLoadedSessionId"));
        }
        return input;
    }

    private static void applyUtf8Request(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (Exception ignored) {
        }
    }
}
