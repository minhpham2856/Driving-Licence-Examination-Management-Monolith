package examstaff.controller.staff.exam.page;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;

import java.util.List;

/**
 * Prepares exam-staff page context and binds the initial page examstaff.model.
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
        private final List<ExamSummaryDTO> allSessions;
        private final List<ExamRegistrationDTO> candidates;

        public ExamStaffPageContext(int examId, int fallbackExamId, List<ExamSummaryDTO> allSessions,
                List<ExamRegistrationDTO> candidates) {
            this.examId = examId > 0 ? examId : fallbackExamId;
            this.allSessions = allSessions != null ? allSessions : List.of();
            this.candidates = candidates != null ? candidates : List.of();
        }

        public int getExamId() {
            return examId;
        }

        public List<ExamSummaryDTO> getAllSessions() {
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
        int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (urlExamId > 0 && session != null) {
            ExamStaffPageTransitionStateDTO transition = selection().preparePageTransition(
                    buildPageTransitionInput(session, urlExamId));
            if (transition.isClearCandidateCache()) {
                SELECTION_FACADE.clearCandidateCache(session);
            }
            if (transition.isClearProcedureState()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(session,
                        transition.getExamId(), transition.getExamId());
            }
            if (transition.isPersistSelection()) {
                ExamStaffPageBinder.persistExamSelection(session, transition.getExamId(), transition.getExamId());
            }
        }

        ExamStaffPagePrepareInput input = buildPagePrepareInput(request, session, webRoot, loadCandidates, urlExamId);
        ExamStaffPageContextDTO ctx = page().preparePageContext(input);

        if (ExamStaffHttpSupport.parseExamIdParam(request) > 0 && ctx.getExamId() <= 0 && request != null) {
            request.setAttribute("sessionSelectError",
                    "Không tìm thấy kỳ thi (mã " + urlExamId + ").");
        }

        if (ctx.getExamId() > 0 && session != null) {
            ExamStaffPageBinder.persistExamSelection(session, ctx.getExamId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            ExamStaffPageBinder.bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = MODULE.services().candidateQueue().buildSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getExamId());
        ExamStaffPageBinder.publishQueue(request, session, snapshot);

        return new ExamStaffPageContext(ctx.getExamId(), ctx.getExamId(),
                ctx.getAllSessions(), ctx.getCandidates());
    }

    private static ExamStaffPagePrepareInput buildPagePrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlExamId) {
        ExamStaffPagePrepareInput input = new ExamStaffPagePrepareInput();
        input.setUrlExamId(urlExamId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setHasExamIdParam(ExamStaffHttpSupport.parseExamIdParam(request) > 0);
        input.setAllSessions(SELECTION_FACADE.loadAllExams());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order = (List<String>) session.getAttribute("callQueueOrder");
            input.setCallQueueOrder(order);
            input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));
        }
        return input;
    }

    private static ExamStaffPageTransitionInput buildPageTransitionInput(HttpSession session, int urlExamId) {
        ExamStaffPageTransitionInput input = new ExamStaffPageTransitionInput();
        input.setUrlExamId(urlExamId);
        input.setAllSessions(SELECTION_FACADE.loadAllExams());
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
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
