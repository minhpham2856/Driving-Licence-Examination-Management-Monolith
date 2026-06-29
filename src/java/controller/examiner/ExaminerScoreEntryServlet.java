package controller.examiner;

import java.util.*;

import model.*;

import dto.CandidateEnrollmentDTO;
import enums.SectionType;
import model.User;

import service.AuditLogService;
import service.impl.AuditLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import util.ExaminerUtil;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;

import java.io.IOException;

@WebServlet("/views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    private final AuditLogService auditLogService = new AuditLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) return;

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        String sbd = request.getParameter("sbd");
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");

        if (sessionId != null && sessionId > 0) {
            if (ExaminerUtil.isTheorySection(request) && request.getParameter("error") == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=theoryNoScoreEntry");
                return;
            }

            if (action != null) {
                if (handleScoreEntryAction(request, response, session, sessionId, action, sbd, user)) {
                    return;
                }
            }

            if (sbd == null || sbd.isBlank()) {
                if (request.getAttribute("candidate") == null && action == null) {
                    String called = autoCallScoreEntryIfNeeded(sessionId, user, session, user.getUserId());
                    if (called != null) {
                        Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, called); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            } else {
                Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, sbd); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        request.getRequestDispatcher("/views/examiner/score-entry.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) return;

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if ("finalize".equals(request.getParameter("action"))) {
            String sbd = request.getParameter("sbd");
            if (sbd == null || sbd.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                return;
            }
            if (!examinerService.finalizeScoreEntry(sessionId, sbd, ((User) session.getAttribute("user")).getUserId(), ExaminerUtil.resolveSectionName(session))) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd=" + ExaminerUtil.urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);
            String nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd);
            if (nextSbd != null && !nextSbd.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd=" + ExaminerUtil.urlEncode(nextSbd) + "&finalized=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?finalized=1");
            }
            return;
        }

        doGet(request, response);
    }

    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, String sbd, User user) throws IOException {
        switch (action) {
            case "call" -> {
                if (sbd == null || sbd.isBlank()) {
                    String called = autoCallScoreEntryIfNeeded(sessionId, user, session, user.getUserId());
                    if (called == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd=" + ExaminerUtil.urlEncode(called) + "&scoreCalled=1");
                    return true;
                }
                if (!examinerService.callScoreEntryCandidate(sessionId, sbd, user, user.getUserId(), ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session))) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=callFailed&sbd=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, sbd);
                ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd=" + ExaminerUtil.urlEncode(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                String next = deferScoreEntryAbsent(sessionId, sbd, user, session, user.getUserId());
                if (next == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?deferred=" + ExaminerUtil.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd=" + ExaminerUtil.urlEncode(next) + "&deferred=" + ExaminerUtil.urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private String autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session, Integer actionUserId) {
        String active = ExaminerScoreEntryQueue.getActiveSbd(session, sessionId);
        String called = ExaminerScoreEntryQueue.getCalledSbd(session, sessionId);
        if (active != null && !active.isBlank()) {
            return active;
        }
        if (called != null && !called.isBlank()) {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, called);
            return called;
        }
        String first = ExaminerScoreEntryQueue.firstInQueue(session, sessionId);
        if (first == null || first.isBlank()) {
            return null;
        }
        if (examinerService.callScoreEntryCandidate(sessionId, first, user, actionUserId, ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session))) {
            ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, first);
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, first);
            return first;
        }
        return null;
    }

    private String deferScoreEntryAbsent(int sessionId, String sbd, User user, HttpSession session, Integer actionUserId) {
        CandidateEnrollmentDTO reg = examinerService.findCandidate(sessionId, sbd.trim());
        if (reg == null) return null;
        String nextSbd = ExaminerScoreEntryQueue.moveToBottom(session, sessionId, reg.getSbd());
        auditLogService.persist(actionUserId, "UPDATE ScoreEntryQueue",
                "ChuyÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢n SBD " + reg.getSbd() + " xuÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œng cuÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œi danh sÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ch chÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢m (vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¯ng mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â·t tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡m thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âi)",
                reg.getId());
        if (nextSbd != null && !nextSbd.isBlank()) {
            if (examinerService.callScoreEntryCandidate(sessionId, nextSbd, user, actionUserId, ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session), ExaminerUtil.resolveCallDestination(session))) {
                ExaminerScoreEntryQueue.setCalledSbd(session, sessionId, nextSbd);
                ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, nextSbd);
            }
        } else {
            ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);
        }
        return nextSbd;
    }
}

