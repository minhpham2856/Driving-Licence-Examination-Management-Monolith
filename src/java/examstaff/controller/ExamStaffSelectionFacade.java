package examstaff.controller;

import examstaff.controller.ExamStaffPageBinder;
import examstaff.controller.ExamStaffHttpSupport;
import examstaff.controller.ExamStaffSessionKeys;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;

import java.util.List;

/**
 * Facade Presentation chọn kỳ thi: wiring request/session ↔ selection/page service ↔ binder.
 * <p>
 * Hai API chính dễ giải thích miệng:
 * <ul>
 *   <li>{@link #resolveExamId} — URL → session → default (không ghi)</li>
 *   <li>{@link #rememberExamId} — ghi {@code selectedExamId} vào session</li>
 * </ul>
 * Commit đầy đủ khi đổi kỳ (clear cache/procedure) nằm ở
 * {@link examstaff.controller.ExamStaffPageFacade#prepareExamStaffPage}.
 */
public final class ExamStaffSelectionFacade {
    private final ExamStaffPageService pageService;
    private final ExamStaffSelectionService selectionService;

    public ExamStaffSelectionFacade(ExamStaffPageService pageService, ExamStaffSelectionService selectionService) {
        this.pageService = pageService;
        this.selectionService = selectionService;
    }

    private ExamStaffPageService page() {
        return pageService;
    }

    private ExamStaffSelectionService selection() {
        return selectionService;
    }

    public List<ExamSummaryDTO> loadAllExams() {
        return page().listAllExams();
    }

    public void clearCandidateCache(HttpSession session) {
        ExamStaffPageBinder.clearCandidateCache(session);
    }

    /**
     * Áp examId từ URL vào session nếu hợp lệ; không có URL thì resolve theo session/default.
     *
     * @return examId đã resolve, hoặc 0 nếu URL không khớp kỳ nào
     */
    public int applyExamIdFromRequest(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return resolveExamId(request, session, allExams, 0);
        }
        int resolvedExamId = selection().resolveExamFromUrl(examId, allExams);
        if (resolvedExamId <= 0) {
            return 0;
        }
        rememberExamId(session, examId, resolvedExamId);
        return resolvedExamId;
    }

    /** Resolve examId từ URL/session/default (không ghi session). */
    public int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allExams, defaultId));
    }

    /**
     * Ghi nhớ kỳ đang chọn vào session ({@code selectedExamId}).
     * Alias tường minh của persist — dùng khi giải thích “commit kỳ” với hội đồng.
     */
    public void rememberExamId(HttpSession session, int fallbackExamId, int examId) {
        ExamStaffPageBinder.persistExamSelection(session, fallbackExamId, examId);
    }

    /**
     * Đảm bảo có examId hợp lệ; remember selection khi resolve thành công.
     */
    public int ensureExamId(HttpServletRequest request, HttpSession session, List<ExamSummaryDTO> allExams) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allExams, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int primaryExamId = page().resolvePrimaryExamId(input.getAllExams(), examId);
            rememberExamId(session, primaryExamId, examId);
        }
        return examId;
    }

    /** Đồng bộ {@code selectedExamId} session theo examId hiện tại. */
    public void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allExams, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentExamId = ExamStaffPageBinder.readSelectedExamId(session);
        ExamStaffSelectionStateDTO state = selection().syncExamSelection(examId, currentExamId, allExams);
        session.setAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID,
                state.getExamId() > 0 ? state.getExamId() : examId);
    }

    /** Alias bind sidebar picker — chỉ bind UI, không phải commit kỳ. */
    public void ensureExamPickerBound(HttpServletRequest request, HttpSession session) {
        bindSidebarIfNeeded(request, session);
    }

    /** Bind picker sidebar nếu request chưa có {@code examOptions}. */
    public void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<ExamSummaryDTO> allExams = loadAllExams();
        int examId = resolveExamId(request, session, allExams, 0);
        ExamStaffPageBinder.bindPickerView(request, page().buildPickerView(allExams, examId, 0));
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<ExamSummaryDTO> options = (List<ExamSummaryDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    public ExamSummaryDTO findExamById(List<ExamSummaryDTO> allExams, int examId) {
        return page().findExamById(examId, allExams);
    }

    public ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId) {
        return page().representativeExam(allExams, examId);
    }

    public int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId) {
        return page().resolvePrimaryExamId(allExams, examId);
    }

    public ExamSummaryDTO resolveExamFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<ExamSummaryDTO> allExams) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return null;
        }
        ExamSummaryDTO picked = findExamById(allExams, examId);
        if (picked != null && picked.getExamId() > 0) {
            rememberExamId(httpSession, examId, picked.getExamId());
            return picked;
        }
        return null;
    }

    private ExamStaffSelectionResolveInput buildSelectionInput(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, int defaultExamId) {
        ExamStaffSelectionResolveInput input = new ExamStaffSelectionResolveInput();
        input.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
        input.setAllExams(allExams);
        input.setDefaultExamId(defaultExamId);
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        }
        return input;
    }
}
