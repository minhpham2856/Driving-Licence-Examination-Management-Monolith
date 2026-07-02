package controller.examiner;
import dto.CandidateEnrollmentDTO;
import dto.ExaminerSlotDTO;
import filter.ExaminerPortalFilter;
import model.User;
import service.AuditLogService;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.AuditLogServiceImpl;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
@WebServlet("/views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");
        boolean isTheory = ExaminerPortalFilter.isTheorySession(session);
        String sectionName = resolveSectionName(session);
        if (sessionId != null && sessionId > 0) {
            if (isTheorySection(request) && request.getParameter("error") == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=theoryNoScoreEntry");
                return;
            }
            if (action != null) {
                if ("adjustDeduction".equals(action)) {
                    if (sbd == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                        return;
                    }
                    int deductionId;
                    int delta;
                    try {
                        deductionId = Integer.parseInt(request.getParameter("deductionId"));
                        delta = Integer.parseInt(request.getParameter("delta"));
                    } catch (Exception e) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                                + urlEncode(sbd) + "&error=invalidDeduction");
                        return;
                    }
                    if (!examinerService.adjustScoreDeduction(sessionId, sbd, deductionId, delta, user.getUserId())) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                                + urlEncode(sbd) + "&error=deductionFailed");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + urlEncode(sbd));
                    return;
                }
                if (handleScoreEntryAction(request, response, session, sessionId, action, sbd, user, isTheory, sectionName)) {
                    return;
                }
            }
            if (sbd == null) {
                if (request.getAttribute("candidate") == null && action == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, isTheory, sectionName, user.getUserId());
                    if (called != null) {
                        Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, called, sectionName);
                        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                }
            } else {
                Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, sbd, sectionName);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
        request.getRequestDispatcher("/views/examiner/score-entry.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        boolean isTheory = ExaminerPortalFilter.isTheorySession(session);
        String sectionName = resolveSectionName(session);
        if ("finalize".equals(request.getParameter("action"))) {
            Integer sbd = parseSbdParam(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                return;
            }
            if (!examinerService.finalizeScoreEntry(sessionId, sbd,
                    ((User) session.getAttribute("user")).getUserId(), sectionName)) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }
            ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, null);
            Integer nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(isTheory, sectionName, sbd);
            if (nextSbd != null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + urlEncode(nextSbd) + "&finalized=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?finalized=1");
            }
            return;
        }
        doGet(request, response);
    }
    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, Integer sbd, User user,
            boolean isTheory, String sectionName) throws IOException {
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, isTheory, sectionName, user.getUserId());
                    if (called == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + urlEncode(called) + "&scoreCalled=1");
                    return true;
                }
                if (!examinerService.callScoreEntryCandidate(sessionId, sbd, user, user.getUserId(), isTheory,
                        sectionName, resolveCallDestination(session))) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=callFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                ExaminerScoreEntryQueue.setCalledSbd(isTheory, sectionName, sbd);
                ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                Integer next = deferScoreEntryAbsent(sessionId, sbd, user, session, isTheory, sectionName, user.getUserId());
                if (next == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?deferred="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + urlEncode(next) + "&deferred=" + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }
    private Integer autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session, boolean isTheory,
            String sectionName, Integer actionUserId) {
        Integer active = ExaminerScoreEntryQueue.getActiveSbd(isTheory, sectionName);
        Integer called = ExaminerScoreEntryQueue.getCalledSbd(isTheory, sectionName);
        if (active != null) {
            return active;
        }
        if (called != null) {
            ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, called);
            return called;
        }
        Integer first = ExaminerScoreEntryQueue.firstInQueue(isTheory, sectionName);
        if (first == null) {
            return null;
        }
        if (examinerService.callScoreEntryCandidate(sessionId, first, user, actionUserId, isTheory, sectionName,
                resolveCallDestination(session))) {
            ExaminerScoreEntryQueue.setCalledSbd(isTheory, sectionName, first);
            ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, first);
            return first;
        }
        return null;
    }
    private Integer deferScoreEntryAbsent(int sessionId, int sbd, User user, HttpSession session,
            boolean isTheory, String sectionName, Integer actionUserId) {
        CandidateEnrollmentDTO reg = examinerService.findCandidate(sessionId, sbd);
        if (reg == null) {
            return null;
        }
        Integer nextSbd = ExaminerScoreEntryQueue.moveToBottom(isTheory, sectionName, reg.getSbd());
        auditLogService.logAction(actionUserId, "UPDATE ScoreEntryQueue",
                "Chuyển SBD " + reg.getSbd() + " xuống cuối hàng nhập điểm",
                reg.getId());
        if (nextSbd != null) {
            if (examinerService.callScoreEntryCandidate(sessionId, nextSbd, user, actionUserId, isTheory, sectionName,
                    resolveCallDestination(session))) {
                ExaminerScoreEntryQueue.setCalledSbd(isTheory, sectionName, nextSbd);
                ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, nextSbd);
            }
        } else {
            ExaminerScoreEntryQueue.setActiveSbd(isTheory, sectionName, null);
        }
        return nextSbd;
    }
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
    private Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }
    private boolean isTheorySection(HttpServletRequest request) {
        return ExaminerPortalFilter.isTheorySession(request.getSession(false));
    }
    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
    private String resolveCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi";
        }
        Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            ExaminerSlotDTO slot = (ExaminerSlotDTO) slotObj;
            if (slot.getAreaName() != null && !slot.getAreaName().isBlank()) {
                return slot.getAreaName();
            }
        }
        Object sectionName = session.getAttribute(ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }
}
