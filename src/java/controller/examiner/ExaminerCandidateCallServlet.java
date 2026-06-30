package controller.examiner;

import enums.SectionType;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import util.ExamQueue;
import util.ExamQueue.Lane;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends HttpServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerServletSupport.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerServletSupport.activeSessionId(session);
        Integer sbd = ExaminerServletSupport.parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            if ("1".equals(request.getParameter("absenceConfirmed")) && sbd != null) {
                examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone="
                        + ExaminerServletSupport.urlEncode(sbd));
                return;
            }

            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }

            SectionType sectionType = ExaminerServletSupport.resolveSectionType(session);
            String sectionName = ExaminerServletSupport.resolveSectionName(session);
            List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(sessionId, sectionType, sectionName);
            Lane lane = ExamQueue.resolveLane(sectionType, sectionName);
            List<Integer> eligibleSbds = new ArrayList<>();
            for (Map<String, Object> row : candidates) {
                if (Boolean.TRUE.equals(row.get("callEligible"))) {
                    Object sbdObj = row.get("sbd");
                    if (sbdObj instanceof Number) {
                        eligibleSbds.add(((Number) sbdObj).intValue());
                    }
                }
            }
            ExamQueue.sync(lane, eligibleSbds);
            candidates = viewDataService.orderCandidateRowsByQueue(candidates, sectionType, sectionName);
            ExaminerServletSupport.applyCandidateSort(request, candidates);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(Locale.ROOT);
                List<Map<String, Object>> filtered = new ArrayList<>();
                for (Map<String, Object> row : candidates) {
                    String sbdVal = String.valueOf(row.get("sbd"));
                    String name = String.valueOf(row.get("fullName"));
                    String gov = String.valueOf(row.get("governmentId"));
                    if (sbdVal.toLowerCase(Locale.ROOT).contains(q)
                            || name.toLowerCase(Locale.ROOT).contains(q)
                            || gov.toLowerCase(Locale.ROOT).contains(q)) {
                        filtered.add(row);
                    }
                }
                candidates = filtered;
                request.setAttribute("searchActive", true);
                request.setAttribute("searchQuery", search.trim());
            }
            request.setAttribute("candidates", candidates);

            if (sbd != null) {
                for (Map<String, Object> row : candidates) {
                    Object sbdObj = row.get("sbd");
                    if (sbdObj instanceof Number && ((Number) sbdObj).intValue() == sbd) {
                        request.setAttribute("candidate", row);
                        break;
                    }
                }
            }
            request.setAttribute("examSummary",
                    viewDataService.buildCandidateSummary(sessionId, sectionType, sectionName));
        }

        request.getRequestDispatcher("/views/examiner/candidate-call.jsp").forward(request, response);
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

        if ("callSelected".equals(request.getParameter("action"))) {
            User user = (User) session.getAttribute("user");
            int[] sbds = ExaminerServletSupport.parseSbdParams(request.getParameterValues("sbd"));
            int count = examinerService.callSelectedCandidates(sessionId, sbds, user,
                    user.getUserId(),
                    ExaminerServletSupport.resolveSectionType(session),
                    ExaminerServletSupport.resolveSectionName(session),
                    ExaminerServletSupport.resolveCallDestination(session));
            if (count <= 0) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callSelectedFailed");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?calledBatch=" + count);
            return;
        }

        doGet(request, response);
    }

    private boolean handleCallAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, Integer sbd) throws IOException {
        User user = (User) session.getAttribute("user");
        int userId = user.getUserId();
        SectionType sectionType = ExaminerServletSupport.resolveSectionType(session);
        String sectionName = ExaminerServletSupport.resolveSectionName(session);
        String destination = ExaminerServletSupport.resolveCallDestination(session);

        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer calledSbd = examinerService.callNextCandidate(sessionId, user, userId,
                            sectionType, sectionName, destination);
                    if (calledSbd == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                            + ExaminerServletSupport.urlEncode(calledSbd));
                    return true;
                }
                if (!examinerService.callCandidate(sessionId, sbd, user, userId, sectionType, sectionName, destination)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                        + ExaminerServletSupport.urlEncode(sbd));
                return true;
            }
            case "undoAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.undoAbsent(sessionId, sbd, userId)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=undoAbsentFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?undoAbsent="
                        + ExaminerServletSupport.urlEncode(sbd));
                return true;
            }
            case "markAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.markAbsent(sessionId, sbd, userId)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=absentFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone="
                        + ExaminerServletSupport.urlEncode(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.printSignatureForm(sessionId, sbd, userId)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=signaturePrintFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/print-documents?sbd="
                        + ExaminerServletSupport.urlEncode(sbd) + "&signatureMarked=1");
                return true;
            }
            case "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                String completeError = examinerService.completeCandidateSection(sessionId, sbd, userId);
                if ("needSignaturePrint".equals(completeError)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=needSignaturePrint&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                if (completeError != null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=completeFailed&sbd="
                            + ExaminerServletSupport.urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?completeDone="
                        + ExaminerServletSupport.urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
