package controller.examiner;

import dto.CandidateEnrollmentDTO;
import enums.AuditAction;
import enums.AuditEntity;
import enums.ExamSection;
import model.User;
import service.AuditLogService;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.AuditLogServiceImpl;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends BaseExaminerServlet {

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
        Integer sessionId = getActiveSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");
        ExamSection examSection = getExamSection(session);
        String sectionName = examSection.getValue();
        if (sessionId != null && sessionId > 0) {
            if (examSection == ExamSection.THEORY && request.getParameter("error") == null) {
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
                    } catch (Exception ex) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                                + encodeSbd(sbd) + "&error=invalidDeduction");
                        return;
                    }
                    if (!examinerService.adjustScoreDeduction(
                            buildAdjustDeductionCommand(sessionId, sbd, deductionId, delta, user.getUserId())).isSuccess()) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                                + encodeSbd(sbd) + "&error=deductionFailed");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + encodeSbd(sbd));
                    return;
                }
                if (handleScoreEntryAction(request, response, session, sessionId, action, sbd, user, examSection, sectionName)) {
                    return;
                }
            }
            if (sbd == null) {
                if (request.getAttribute("candidate") == null && action == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, examSection, user.getUserId());
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
        Integer sessionId = getActiveSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        ExamSection examSection = getExamSection(session);
        String sectionName = examSection.getValue();
        if ("finalize".equals(request.getParameter("action"))) {
            Integer sbd = parseSbdParam(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                return;
            }
            if (!examinerService.finalizeScoreEntry(
                    buildFinalizeCommand(sessionId, sbd, ((User) session.getAttribute("user")).getUserId(), sectionName)).isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + encodeSbd(sbd) + "&error=finalizeFailed");
                return;
            }
            ExaminerScoreEntryQueue.setActiveSbd(examSection, null);
            Integer nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(examSection, sbd);
            if (nextSbd != null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + encodeSbd(nextSbd) + "&finalized=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?finalized=1");
            }
            return;
        }
        doGet(request, response);
    }

    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, Integer sbd, User user,
            ExamSection examSection, String sectionName) throws IOException {
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer called = autoCallScoreEntryIfNeeded(sessionId, user, session, examSection, user.getUserId());
                    if (called == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                            + encodeSbd(called) + "&scoreCalled=1");
                    return true;
                }
                if (!examinerService.callScoreEntryCandidate(
                        buildCallCommand(session, user, sessionId, sbd, null, true)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=callFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ExaminerScoreEntryQueue.setCalledSbd(examSection, sbd);
                ExaminerScoreEntryQueue.setActiveSbd(examSection, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + encodeSbd(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferQueue" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                Integer next = deferScoreEntryCandidate(sessionId, sbd, user, session, examSection, user.getUserId());
                if (next == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?deferred="
                            + encodeSbd(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/score-entry?sbd="
                        + encodeSbd(next) + "&deferred=" + encodeSbd(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private Integer autoCallScoreEntryIfNeeded(int sessionId, User user, HttpSession session,
            ExamSection examSection, Integer actionUserId) {
        Integer active = ExaminerScoreEntryQueue.getActiveSbd(examSection);
        Integer called = ExaminerScoreEntryQueue.getCalledSbd(examSection);
        if (active != null) {
            return active;
        }
        if (called != null) {
            ExaminerScoreEntryQueue.setActiveSbd(examSection, called);
            return called;
        }
        Integer first = ExaminerScoreEntryQueue.firstInQueue(examSection);
        if (first == null) {
            return null;
        }
        if (examinerService.callScoreEntryCandidate(
                buildCallCommand(session, user, sessionId, first, null, true)).isSuccess()) {
            ExaminerScoreEntryQueue.setCalledSbd(examSection, first);
            ExaminerScoreEntryQueue.setActiveSbd(examSection, first);
            return first;
        }
        return null;
    }

    private Integer deferScoreEntryCandidate(int sessionId, int sbd, User user, HttpSession session,
            ExamSection examSection, Integer actionUserId) {
        CandidateEnrollmentDTO reg = examinerService.getRegistration(sessionId, sbd);
        if (reg == null) {
            return null;
        }
        Integer nextSbd = ExaminerScoreEntryQueue.moveToBottom(examSection, reg.getSbd());
        auditLogService.logAction(actionUserId, AuditAction.UPDATE, AuditEntity.EXAM_SCORE,
                "Chuyển SBD " + reg.getSbd() + " xuống cuối hàng nhập điểm",
                reg.getId());
        if (nextSbd != null) {
            if (examinerService.callScoreEntryCandidate(
                    buildCallCommand(session, user, sessionId, nextSbd, null, true)).isSuccess()) {
                ExaminerScoreEntryQueue.setCalledSbd(examSection, nextSbd);
                ExaminerScoreEntryQueue.setActiveSbd(examSection, nextSbd);
            }
        } else {
            ExaminerScoreEntryQueue.setActiveSbd(examSection, null);
        }
        return nextSbd;
    }
}
