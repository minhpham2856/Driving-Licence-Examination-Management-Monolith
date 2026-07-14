package examstaff.controller.staff.exam.page;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
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
 * Facade Presentation: chuẩn bị context trang exam staff (chọn kỳ, picker, queue)
 * rồi bind model ban đầu qua {@link ExamStaffPageBinder}. Không chứa nghiệp vụ sâu.
 * <p>
 * <b>{@link #prepareExamStaffPage} — điểm commit kỳ duy nhất trên hầu hết trang staff</b>
 * <ol>
 *   <li>URL có {@code examId} → transition (clear cache/procedure nếu đổi kỳ)</li>
 *   <li>Service dựng page context (picker + queue)</li>
 *   <li>{@code remember}/{@code persist} {@code selectedExamId}</li>
 *   <li>Bind picker + publish queue</li>
 * </ol>
 * Ưu tiên resolve: URL → session → default trong danh sách kỳ.
 */
public final class ExamStaffPageFacade {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffSelectionFacade SELECTION_FACADE = MODULE.selectionFacade();

    private ExamStaffPageFacade() {
    }

    /** Shortcut page service từ module. */
    private static ExamStaffPageService page() {
        return MODULE.services().page();
    }

    /** Shortcut selection service từ module. */
    private static ExamStaffSelectionService selection() {
        return MODULE.services().selection();
    }

    /** Context tối thiểu sau prepare: examId, danh sách kỳ, danh sách thí sinh. */
    public static final class ExamStaffPageContext {
        private final int examId;
        private final List<ExamSummaryDTO> allExams;
        private final List<ExamRegistrationDTO> candidates;

        public ExamStaffPageContext(int examId, int fallbackExamId, List<ExamSummaryDTO> allExams,
                List<ExamRegistrationDTO> candidates) {
            this.examId = examId > 0 ? examId : fallbackExamId;
            this.allExams = allExams != null ? allExams : List.of();
            this.candidates = candidates != null ? candidates : List.of();
        }

        /** Mã kỳ đã resolve (fallback nếu primary ≤ 0). */
        public int getExamId() {
            return examId;
        }

        /** Danh sách kỳ thi cho picker/sidebar. */
        public List<ExamSummaryDTO> getAllExams() {
            return allExams;
        }

        /** Queue thí sinh đã load (có thể rỗng nếu không loadCandidates). */
        public List<ExamRegistrationDTO> getCandidates() {
            return candidates;
        }
    }

    /**
     * Chuẩn bị trang: load candidates = true.
     *
     * @see #prepareExamStaffPage(HttpServletRequest, HttpSession, String, boolean)
     */
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot) {
        return prepareExamStaffPage(request, session, webRoot, true);
    }

    /**
     * Điều phối transition đổi kỳ → service preparePageContext → bind picker + publish queue.
     *
     * @param loadCandidates false khi chỉ cần picker (vd. examiner-allocation)
     */
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
            request.setAttribute("examSelectError",
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
                ctx.getAllExams(), ctx.getCandidates());
    }

    /** Đóng gói session/request thành input prepare cho service. */
    private static ExamStaffPagePrepareInput buildPagePrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlExamId) {
        ExamStaffPagePrepareInput input = new ExamStaffPagePrepareInput();
        input.setUrlExamId(urlExamId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setAllExams(SELECTION_FACADE.loadAllExams());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached =
                    (List<ExamRegistrationDTO>) session.getAttribute(ExamStaffSessionKeys.CANDIDATE_QUEUE);
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order =
                    (List<String>) session.getAttribute(ExamStaffSessionKeys.CALL_QUEUE_ORDER);
            input.setCallQueueOrder(order);
            input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));
        }
        return input;
    }

    /** Đóng gói input transition khi URL đổi examId. */
    private static ExamStaffPageTransitionInput buildPageTransitionInput(HttpSession session, int urlExamId) {
        ExamStaffPageTransitionInput input = new ExamStaffPageTransitionInput();
        input.setUrlExamId(urlExamId);
        input.setAllExams(SELECTION_FACADE.loadAllExams());
        if (session != null) {
            input.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(session));
            input.setLoadedExamId(ExamStaffPageBinder.readLoadedExamId(session));
        }
        return input;
    }

    /** Set UTF-8 cho request (bỏ qua lỗi nếu container không cho). */
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
