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
        return page().listAllSessions();
    }

    public void clearCandidateCache(HttpSession session) {
        ExamStaffPageBinder.clearCandidateCache(session);
    }

    public int applyExamIdFromRequest(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allSessions) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return resolveExamId(request, session, allSessions, 0);
        }
        int resolvedExamId = selection().resolveExamFromSessionUrl(examId, allSessions);
        if (resolvedExamId <= 0) {
            return 0;
        }
        ExamStaffPageBinder.persistExamSelection(session, examId, resolvedExamId);
        return resolvedExamId;
    }

    public int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allSessions, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allSessions, defaultId));
    }

    public int ensureExamId(HttpServletRequest request, HttpSession session, List<ExamSummaryDTO> allSessions) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allSessions, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int primaryExamId = page().resolvePrimaryExamId(input.getAllSessions(), examId);
            ExamStaffPageBinder.persistExamSelection(session, primaryExamId, examId);
        }
        return examId;
    }

    public void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allSessions, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentExamId = ExamStaffPageBinder.readSelectedExamId(session);
        ExamStaffSelectionStateDTO state = selection().syncExamSelection(examId, currentExamId, allSessions);
        session.setAttribute("selectedExamId", state.getExamId() > 0 ? state.getExamId() : examId);
        session.removeAttribute("selectedSessionId");
    }

    public void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<ExamSummaryDTO> allSessions = loadAllExams();
        int examId = resolveExamId(request, session, allSessions, 0);
        ExamStaffPageBinder.bindPickerView(request, page().buildPickerView(allSessions, examId, 0));
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<ExamSummaryDTO> options = (List<ExamSummaryDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    public ExamSummaryDTO findExamById(List<ExamSummaryDTO> allSessions, int examId) {
        return page().findExamById(examId, allSessions);
    }

    public ExamSummaryDTO representativeSessionForExam(List<ExamSummaryDTO> allSessions, int examId) {
        return page().representativeSessionForExam(allSessions, examId);
    }

    public int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId) {
        return page().resolvePrimaryExamId(allSessions, examId);
    }

    public ExamSummaryDTO resolveSessionFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<ExamSummaryDTO> allSessions) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId <= 0) {
            return null;
        }
        ExamSummaryDTO picked = findExamById(allSessions, examId);
        if (picked != null && picked.getExamId() > 0) {
            ExamStaffPageBinder.persistExamSelection(httpSession, examId, picked.getExamId());
            return picked;
        }
        return null;
    }

    private ExamStaffSelectionResolveInput buildSelectionInput(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allSessions, int defaultExamId) {
        ExamStaffSelectionResolveInput input = new ExamStaffSelectionResolveInput();
        input.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
        input.setAllSessions(allSessions);
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
