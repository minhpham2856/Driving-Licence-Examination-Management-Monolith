package controller.examiner;


import model.user.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles the main examiner score entry view and actions during an active exam section.
@WebServlet("/views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends BaseExaminerServlet {

    // Handles GET requests for score entry page rendering and interactive actions.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String sbd = request.getParameter("sbd");
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");

        if (sessionId != null && sessionId > 0) {
            if (action != null) {
                if (handleScoreEntryAction(request, response, session, sessionId, action, sbd, user)) {
                    return;
                }
            }

            // Auto-advance score entry logic if no active candidate is set
            if (sbd == null || sbd.isBlank()) {
                if (request.getAttribute("candidate") == null && action == null) {
                    String called = examinerService.autoCallScoreEntryIfNeeded(sessionId, user, session);
                    if (called != null) {
                        viewDataService.attachScoreEntry(request, sessionId, called);
                    }
                }
            } else {
                viewDataService.attachScoreEntry(request, sessionId, sbd);
            }
        }

        forward(request, response, "/views/examiner/score-entry.jsp");
    }

    // Handles POST requests (e.g., finalizeScore).
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chưa có ca thi đang diễn ra.");
            return;
        }

        if ("finalizeScore".equals(request.getParameter("action"))) {
            String sbd = request.getParameter("sbd");
            if (sbd == null || sbd.isBlank()) {
                redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                return;
            }
            if (!examinerService.finalizeScoreEntry(sessionId, sbd, session)) {
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }
            String nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd);
            if (nextSbd != null && !nextSbd.isBlank()) {
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(nextSbd) + "&finalized=1");
            } else {
                redirect(response, request, "/views/examiner/score-entry?finalized=1");
            }
            return;
        }

        doGet(request, response);
    }

    // Handles various interactive actions within the score entry page.
    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, String sbd, User user) throws IOException {
        switch (action) {
            case "call" -> {
                if (sbd == null || sbd.isBlank()) {
                    String called = examinerService.autoCallScoreEntryIfNeeded(sessionId, user, session);
                    if (called == null) {
                        redirect(response, request, "/views/examiner/score-entry?error=noCandidate");
                        return true;
                    }
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(called) + "&scoreCalled=1");
                    return true;
                }
                if (!examinerService.callScoreEntryCandidate(sessionId, sbd, user, session)) {
                    redirect(response, request, "/views/examiner/score-entry?error=callFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                String next = examinerService.deferScoreEntryAbsent(sessionId, sbd, user, session);
                if (next == null) {
                    redirect(response, request, "/views/examiner/score-entry?deferred=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(next) + "&deferred=" + urlEncode(sbd));
                return true;
            }
            case "select" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd));
                return true;
            }
            case "changeVehicle" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                int deviceId;
                try {
                    deviceId = Integer.parseInt(request.getParameter("deviceId"));
                } catch (Exception e) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=invalidDevice");
                    return true;
                }
                if (!examinerService.changeCandidateVehicle(sessionId, sbd, deviceId, session)) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=changeVehicleFailed");
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&vehicleChanged=1");
                return true;
            }
            case "maintenance", "operational" -> {
                int deviceId;
                try {
                    deviceId = Integer.parseInt(request.getParameter("deviceId"));
                } catch (Exception e) {
                    String base = "/views/examiner/score-entry?error=invalidDevice";
                    if (sbd != null && !sbd.isBlank()) base += "&sbd=" + urlEncode(sbd);
                    redirect(response, request, base);
                    return true;
                }

                boolean updated;
                String suffix;
                if ("operational".equals(action)) {
                    updated = examinerService.setDeviceAvailable(deviceId, session);
                    suffix = updated ? "operationalDone=" + deviceId : "error=operationalFailed&deviceId=" + deviceId;
                } else {
                    updated = examinerService.setDeviceMaintenance(deviceId, session);
                    suffix = updated ? "maintenanceDone=" + deviceId : "error=maintenanceFailed&deviceId=" + deviceId;
                }

                String redirectPath = "/views/examiner/score-entry?" + suffix;
                if (sbd != null && !sbd.isBlank()) redirectPath += "&sbd=" + urlEncode(sbd);
                redirect(response, request, redirectPath);
                return true;
            }
            case "adjustDeduction" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                int deductionId;
                int delta;
                try {
                    deductionId = Integer.parseInt(request.getParameter("deductionId"));
                    delta = Integer.parseInt(request.getParameter("delta"));
                } catch (Exception e) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=invalidDeduction");
                    return true;
                }
                if (!examinerService.adjustScoreDeduction(sessionId, sbd, deductionId, delta, session)) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=deductionFailed");
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd));
                return true;
            }
              case "markAbsentScore" -> {
                  if (sbd == null || sbd.isBlank()) {
                      redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                      return true;
                  }
                  examinerService.markAbsent(sessionId, sbd, session);
                  redirect(response, request, "/views/examiner/score-entry?absentDone=" + urlEncode(sbd));
                  return true;
              }
            case "finalizeScore" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                if (!examinerService.finalizeScoreEntry(sessionId, sbd, session)) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=finalizeFailed");
                    return true;
                }
                String nextSbd = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd);
                if (nextSbd != null && !nextSbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(nextSbd) + "&finalized=1");
                } else {
                    redirect(response, request, "/views/examiner/score-entry?finalized=1");
                }
                return true;
            }
            case "printSignature" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                if (!examinerService.printSignatureForm(sessionId, sbd, session)) {
                    redirect(response, request, "/views/examiner/score-entry?error=signaturePrintFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&signatureMarked=1");
                return true;
            }
            case "completeSectionScore" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                String completeError = examinerService.completeCandidateSection(sessionId, sbd, session);
                if ("needSignaturePrint".equals(completeError)) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=needSignaturePrint");
                    return true;
                }
                if (completeError != null) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=completeFailed");
                    return true;
                }
                String nextAfterComplete = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd);
                if (nextAfterComplete != null && !nextAfterComplete.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(nextAfterComplete) + "&completeDone=" + urlEncode(sbd));
                } else {
                    redirect(response, request, "/views/examiner/score-entry?completeDone=" + urlEncode(sbd));
                }
                return true;
            }
            case "suspendInScoreEntry" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?error=noSbd");
                    return true;
                }
                String reasonCode = request.getParameter("reasonCode");
                String reasonDetail = request.getParameter("reasonDetail");
                String[] deductionStrs = request.getParameterValues("deductionId");

                int[] deductionIds = parseDeductionIds(deductionStrs);
                if (!examinerService.recordViolation(sessionId, sbd, reasonCode, reasonDetail, null, deductionIds, session)) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(sbd) + "&error=suspendFailed");
                    return true;
                }
                String nextAfterSuspend = ExaminerScoreEntryQueue.nextInQueueAfter(session, sessionId, sbd);
                ExaminerScoreEntryQueue.setActiveSbd(session, sessionId, null);
                if (nextAfterSuspend != null && !nextAfterSuspend.isBlank()) {
                    redirect(response, request, "/views/examiner/score-entry?sbd=" + urlEncode(nextAfterSuspend) + "&suspended=" + urlEncode(sbd));
                } else {
                    redirect(response, request, "/views/examiner/score-entry?suspended=" + urlEncode(sbd));
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}



