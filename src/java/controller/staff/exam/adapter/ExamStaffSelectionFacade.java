package controller.staff.exam.adapter;

import controller.staff.exam.binder.ExamStaffPageBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import dto.SessionDTO;
import dto.examstaff.ExamStaffSelectionResolveInput;
import dto.examstaff.ExamStaffSelectionStateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import service.ExamStaffPageService;
import service.ExamStaffSelectionService;

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

    public List<SessionDTO> loadAllSessions() {
        return page().listAllSessions();
    }

    public void clearCandidateCache(HttpSession session) {
        ExamStaffPageBinder.clearCandidateCache(session);
    }

    public int applySessionIdFromRequest(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions) {
        int sessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
        if (sessionId <= 0) {
            return resolveExamId(request, session, allSessions, 0);
        }
        int examId = selection().resolveExamFromSessionUrl(sessionId, allSessions);
        if (examId <= 0) {
            return 0;
        }
        ExamStaffPageBinder.persistExamSelection(session, sessionId, examId);
        return examId;
    }

    public int resolveExamId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allSessions, defaultId, 0));
    }

    public int ensureExamId(HttpServletRequest request, HttpSession session, List<SessionDTO> allSessions) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allSessions, 0, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int sessionId = page().resolvePrimarySessionId(input.getAllSessions(), examId);
            ExamStaffPageBinder.persistExamSelection(session, sessionId, examId);
        }
        return examId;
    }

    public int resolveSessionId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        return selection().resolveSessionId(
                buildSelectionInput(request, session, allSessions, 0, defaultId));
    }

    public void syncExamSelection(HttpSession session, List<SessionDTO> allSessions, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentSession = (Integer) session.getAttribute("selectedSessionId");
        ExamStaffSelectionStateDTO state = selection().syncExamSelection(examId, currentSession, allSessions);
        session.setAttribute("selectedExamId", state.getExamId());
        if (state.getSessionId() > 0) {
            session.setAttribute("selectedSessionId", state.getSessionId());
        }
    }

    public void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<SessionDTO> allSessions = loadAllSessions();
        int examId = resolveExamId(request, session, allSessions, 0);
        ExamStaffPageBinder.bindPickerView(request, page().buildPickerView(allSessions, examId, 0));
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<SessionDTO> options = (List<SessionDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    public SessionDTO findSessionById(List<SessionDTO> allSessions, int sessionId) {
        return page().findSessionById(sessionId, allSessions);
    }

    public SessionDTO representativeSessionForExam(List<SessionDTO> allSessions, int examId) {
        return page().representativeSessionForExam(allSessions, examId);
    }

    public int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        return page().resolvePrimarySessionId(allSessions, examId);
    }

    public SessionDTO resolveSessionFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<SessionDTO> allSessions) {
        int sessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
        if (sessionId <= 0) {
            return null;
        }
        SessionDTO picked = findSessionById(allSessions, sessionId);
        if (picked != null && picked.getExamId() > 0) {
            ExamStaffPageBinder.persistExamSelection(httpSession, sessionId, picked.getExamId());
            return picked;
        }
        return null;
    }

    private ExamStaffSelectionResolveInput buildSelectionInput(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultExamId, int defaultSessionId) {
        ExamStaffSelectionResolveInput input = new ExamStaffSelectionResolveInput();
        input.setUrlSessionId(ExamStaffHttpSupport.parseSessionIdParam(request));
        input.setAllSessions(allSessions);
        input.setDefaultExamId(defaultExamId);
        input.setDefaultSessionId(defaultSessionId);
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setSelectedExamId((Integer) session.getAttribute("selectedExamId"));
            input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
        }
        return input;
    }
}
