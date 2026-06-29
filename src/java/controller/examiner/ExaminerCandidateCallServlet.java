package controller.examiner;

import java.util.*;

import model.*;


import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles candidate calling logic, absence marking, and signature printing in the call queue.
@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends BaseExaminerServlet {

    // Handles GET actions for candidate call (call, undoAbsent, markAbsent, completeSection, printSignature).
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            // Block access to result edit features for theory sections
            if (isTheorySection(request) && request.getParameter("error") != null && request.getParameter("error").equals("theoryNoResultEdit")) {
                // Keep the error parameter and render
            }

            // Route absence confirmation from the modal
            if ("1".equals(request.getParameter("absenceConfirmed"))) {
                examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                redirect(response, request, "/views/examiner/candidate-call?absentDone=" + urlEncode(sbd));
                return;
            }

            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }

            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }

        forward(request, response, "/views/examiner/candidate-call.jsp");
    }

    // Handles POST requests, specifically batch calling selected candidates.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "ChÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°a cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³ ca thi ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œang diÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¦n ra.");
            return;
        }

        if ("callSelected".equals(request.getParameter("action"))) {
            User user = (User) session.getAttribute("user");
            String[] sbds = request.getParameterValues("sbd");
            int count = examinerService.callSelectedCandidates(sessionId, sbds, user, ((User) session.getAttribute("user")).getUserId(), resolveSectionType(session), resolveSectionName(session), resolveCallDestination(session));
            if (count <= 0) {
                redirect(response, request, "/views/examiner/candidate-call?error=callSelectedFailed");
                return;
            }
            redirect(response, request, "/views/examiner/candidate-call?calledBatch=" + count);
            return;
        }

        doGet(request, response);
    }

    // Processes single-candidate call-related actions.
    private boolean handleCallAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, String sbd) throws IOException {
        User user = (User) session.getAttribute("user");
        switch (action) {
            case "call" -> {
                if (sbd == null || sbd.isBlank()) {
                    String calledSbd = examinerService.callNextCandidate(sessionId, user, ((User) session.getAttribute("user")).getUserId(), resolveSectionType(session), resolveSectionName(session), resolveCallDestination(session));
                    if (calledSbd == null) {
                        redirect(response, request, "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    redirect(response, request, "/views/examiner/candidate-call?called=" + urlEncode(calledSbd));
                    return true;
                }
                if (!examinerService.callCandidate(sessionId, sbd, user, ((User) session.getAttribute("user")).getUserId(), resolveSectionType(session), resolveSectionName(session), resolveCallDestination(session))) {
                    redirect(response, request, "/views/examiner/candidate-call?error=callFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/candidate-call?called=" + urlEncode(sbd));
                return true;
            }
            case "undoAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.undoAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    redirect(response, request, "/views/examiner/candidate-call?error=undoAbsentFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/candidate-call?undoAbsent=" + urlEncode(sbd));
                return true;
            }
            case "markAbsent" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    redirect(response, request, "/views/examiner/candidate-call?error=absentFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/candidate-call?absentDone=" + urlEncode(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.printSignatureForm(sessionId, sbd, ((User) session.getAttribute("user")).getUserId())) {
                    redirect(response, request, "/views/examiner/candidate-call?error=signaturePrintFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/print-documents?sbd=" + urlEncode(sbd) + "&signatureMarked=1");
                return true;
            }
            case "completeSection" -> {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                String completeError = examinerService.completeCandidateSection(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                if ("needSignaturePrint".equals(completeError)) {
                    redirect(response, request, "/views/examiner/candidate-call?error=needSignaturePrint&sbd=" + urlEncode(sbd));
                    return true;
                }
                if (completeError != null) {
                    redirect(response, request, "/views/examiner/candidate-call?error=completeFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/candidate-call?completeDone=" + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}






