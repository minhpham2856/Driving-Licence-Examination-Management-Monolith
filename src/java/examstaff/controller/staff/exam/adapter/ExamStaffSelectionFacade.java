package examstaff.controller.staff.exam.adapter;

import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;

import java.util.List;

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
        ExamStaffPageBinder.persistExamSelection(session, examId, resolvedExamId);
        return resolvedExamId;
    }

    public int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allExams, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allExams, defaultId));
    }

    public int ensureExamId(HttpServletRequest request, HttpSession session, List<ExamSummaryDTO> allExams) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allExams, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int primaryExamId = page().resolvePrimaryExamId(input.getAllExams(), examId);
            ExamStaffPageBinder.persistExamSelection(session, primaryExamId, examId);
        }
        return examId;
    }

    public void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allExams, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentExamId = ExamStaffPageBinder.readSelectedExamId(session);
        ExamStaffSelectionStateDTO state = selection().syncExamSelection(examId, currentExamId, allExams);
        session.setAttribute("selectedExamId", state.getExamId() > 0 ? state.getExamId() : examId);
    }

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
            ExamStaffPageBinder.persistExamSelection(httpSession, examId, picked.getExamId());
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
