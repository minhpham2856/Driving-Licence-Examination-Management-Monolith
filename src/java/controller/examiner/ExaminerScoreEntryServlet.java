package controller.examiner;

import dto.CandidateEnrollmentDTO;
import enums.SectionType;
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
import java.util.Map;

@WebServlet("/views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends HttpServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    private final AuditLogService auditLogService = new AuditLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerServletSupport.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerServletSupport.activeSessionId(session);
        Integer sbd = ExaminerServletSupport.parseSbdParam(request.getParameter("sbd"));
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");
        SectionType sectionType = ExaminerServletSupport.resolveSectionType(session);
        String sectionName = ExaminerServletSupport.resolveSectionName(session);

        if (sessionId != null && sessionId > 0) {
            if (ExaminerServletSupport.isTheorySection(request) && request.getParameter("error") == null) {
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
                                + ExaminerServletSupport.urlEncode(sbd) + "&error=invalidDeduction");
                        return;
                    }
                    if (!examinerService.adjustScoreDeduction(sessionId, sbd, deductionId, delta, user.getUserId())) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                                + ExaminerServletSupport.urlEncode(sbd) + "&error=deductionFailed");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return;
                }
                if (handleScoreEntryAction(request, response, session, sessionId, action, sbd, user, sectionType, sectionName)) {
                    return;
                }
            }

            if (sbd == null) {
                if (request.getAttribute("candidate") == null && action == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, sectionType, sectionName, user.getUserId());
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
        HttpSession session = ExaminerServletSupport.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerServletSupport.activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        SectionType sectionType = ExaminerServletSupport.resolveSectionType(session);
        String sectionName = ExaminerServletSupport.resolveSectionName(session);

        if ("finalize".equals(request.getParameter("action"))) {
            Integer sbd = ExaminerServletSupport.parseSbdParam(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                return;
            }
            if (!examinerService.finalizeScoreEntry(sessionId, sbd,
                    ((User) session.getAttribute("user")).getUserId(), sectionName)) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + ExaminerServletSupport.urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }
            ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, null);
            Integer nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(sectionType, sectionName, sbd);
            if (nextSbd != null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + ExaminerServletSupport.urlEncode(nextSbd) + "&finalized=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?finalized=1");
            }
            return;
        }

        doGet(request, response);
    }

    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, Integer sbd, User user,
            SectionType sectionType, String sectionName) throws IOException {
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, sectionType, sectionName, user.getUserId());
                    if (called == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + ExaminerServletSupport.urlEncode(called) + "&scoreCalled=1");
                    return true;
                }
                if (!examinerService.callScoreEntryCandidate(sessionId, sbd, user, user.getUserId(), sectionType,
                        sectionName, ExaminerServletSupport.resolveCallDestination(session))) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=callFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                ExaminerScoreEntryQueue.setCalledSbd(sectionType, sectionName, sbd);
                ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + ExaminerServletSupport.urlEncode(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                Integer next = deferScoreEntryAbsent(sessionId, sbd, user, session, sectionType, sectionName, user.getUserId());
                if (next == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?deferred="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + ExaminerServletSupport.urlEncode(next) + "&deferred=" + ExaminerServletSupport.urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private Integer autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session, SectionType sectionType,
            String sectionName, Integer actionUserId) {
        Integer active = ExaminerScoreEntryQueue.getActiveSbd(sectionType, sectionName);
        Integer called = ExaminerScoreEntryQueue.getCalledSbd(sectionType, sectionName);
        if (active != null) {
            return active;
        }
        if (called != null) {
            ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, called);
            return called;
        }
        Integer first = ExaminerScoreEntryQueue.firstInQueue(sectionType, sectionName);
        if (first == null) {
            return null;
        }
        if (examinerService.callScoreEntryCandidate(sessionId, first, user, actionUserId, sectionType, sectionName,
                ExaminerServletSupport.resolveCallDestination(session))) {
            ExaminerScoreEntryQueue.setCalledSbd(sectionType, sectionName, first);
            ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, first);
            return first;
        }
        return null;
    }

    private Integer deferScoreEntryAbsent(int sessionId, int sbd, User user, HttpSession session,
            SectionType sectionType, String sectionName, Integer actionUserId) {
        CandidateEnrollmentDTO reg = examinerService.findCandidate(sessionId, sbd);
        if (reg == null) {
            return null;
        }
        Integer nextSbd = ExaminerScoreEntryQueue.moveToBottom(sectionType, sectionName, reg.getSbd());
        auditLogService.logAction(actionUserId, "UPDATE ScoreEntryQueue",
                "Chuyển SBD " + reg.getSbd() + " xuống cuối hàng nhập điểm",
                reg.getId());
        if (nextSbd != null) {
            if (examinerService.callScoreEntryCandidate(sessionId, nextSbd, user, actionUserId, sectionType, sectionName,
                    ExaminerServletSupport.resolveCallDestination(session))) {
                ExaminerScoreEntryQueue.setCalledSbd(sectionType, sectionName, nextSbd);
                ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, nextSbd);
            }
        } else {
            ExaminerScoreEntryQueue.setActiveSbd(sectionType, sectionName, null);
        }
        return nextSbd;
    }
}
