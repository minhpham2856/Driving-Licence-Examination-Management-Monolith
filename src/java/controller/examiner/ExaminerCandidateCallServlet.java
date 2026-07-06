package controller.examiner;
import filter.ExaminerFilter;
import dto.ServiceResult;
import enums.ExamSection;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import util.ExamQueue;
import util.ExamQueue.Lane;
import util.ExaminerCandidateSort;
import util.ExamSessionState;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dto.ExaminerCandidateRowDTO;
@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends BaseExaminerServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        String action = request.getParameter("action");
        if (sessionId != null && sessionId > 0) {
            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }
            boolean isTheory = ExaminerFilter.isTheorySession(session);
            ExamSection examSection = getExamSection(session);
            String sectionName = examSection.getValue();
            List<ExaminerCandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            Lane lane = ExamQueue.laneFor(examSection);
            List<Integer> eligibleSbds = new ArrayList<>();
            for (ExaminerCandidateRowDTO row : candidates) {
                if (row.isCallEligible()) {
                    eligibleSbds.add(row.getSbd());
                }
            }
            ExamQueue.sync(lane, eligibleSbds);
            candidates = viewDataService.orderCandidateRowsByQueue(candidates, examSection);
            enrichDeskState(candidates, sessionId, lane);
            ExaminerCandidateSort.applyCandidateSort(request, candidates);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(Locale.ROOT);
                List<ExaminerCandidateRowDTO> filtered = new ArrayList<>();
                for (ExaminerCandidateRowDTO row : candidates) {
                    String sbdVal = String.valueOf(row.getSbd());
                    String name = row.getFullName() != null ? row.getFullName() : "";
                    String gov = row.getGovernmentId() != null ? row.getGovernmentId() : "";
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
                for (ExaminerCandidateRowDTO row : candidates) {
                    if (row.getSbd() == sbd) {
                        request.setAttribute("candidate", row);
                        break;
                    }
                }
            }
            request.setAttribute("examSummary",
                    viewDataService.buildCandidateSummary(sessionId, isTheory, sectionName));
        }
        request.getRequestDispatcher("/views/examiner/candidate-call.jsp").forward(request, response);
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
        if ("callSelected".equals(request.getParameter("action"))) {
            User user = (User) session.getAttribute("user");
            int[] sbds = parseSbdParams(request.getParameterValues("sbd"));
            ServiceResult<Integer> selectedResult = examinerService.callSelectedCandidates(
                    buildCallCommand(session, user, sessionId, null, sbds, false));
            if (!selectedResult.isSuccess() || selectedResult.getData() == null || selectedResult.getData() <= 0) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callSelectedFailed");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?calledBatch=" + selectedResult.getData());
            return;
        }
        doGet(request, response);
    }
    private boolean handleCallAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, Integer sbd) throws IOException {
        User user = (User) session.getAttribute("user");
        int userId = user.getUserId();
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    ServiceResult<Integer> nextResult = examinerService.callNextCandidate(
                            buildCallCommand(session, user, sessionId, null, null, false));
                    if (!nextResult.isSuccess() || nextResult.getData() == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                            + encodeSbd(nextResult.getData()));
                    return true;
                }
                if (!examinerService.callCandidate(
                        buildCallCommand(session, user, sessionId, sbd, null, false)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                        + encodeSbd(sbd));
                return true;
            }
            case "undoPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.undoPresent(buildSessionCommand(sessionId, sbd, userId)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=undoPresentFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ExamSessionState.clearPresent(getServletContext(), sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?undoPresent="
                        + encodeSbd(sbd));
                return true;
            }
            case "markPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.markPresent(buildSessionCommand(sessionId, sbd, userId)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=presentFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ExamSessionState.markPresent(getServletContext(), sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?presentDone="
                        + encodeSbd(sbd));
                return true;
            }
            case "wrongInfo" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.sendWrongInfoToProcedure(buildSessionCommand(sessionId, sbd, userId)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=wrongInfoFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ExamSessionState.sendToProcedure(getServletContext(), sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?wrongInfoDone="
                        + encodeSbd(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!examinerService.printSignatureForm(buildSessionCommand(sessionId, sbd, userId)).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=signaturePrintFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/examiner/print/docx?type=BB1&sbd="
                        + encodeSbd(sbd));
                return true;
            }
            case "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                ServiceResult<Void> completeResult = examinerService.completeCandidateSection(
                        buildSessionCommand(sessionId, sbd, userId,
                                ExamSessionState.getSectionPassed(getServletContext(), sessionId, sbd)));
                if (!completeResult.isSuccess() && "needSignaturePrint".equals(completeResult.getMessage())) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=needSignaturePrint&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                if (!completeResult.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=completeFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?completeDone="
                        + encodeSbd(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void enrichDeskState(List<ExaminerCandidateRowDTO> candidates, int sessionId, Lane lane) {
        Integer activeSbd = ExamQueue.getActiveSbd(lane);
        Integer calledSbd = ExamQueue.getCalledSbd(lane);
        for (ExaminerCandidateRowDTO row : candidates) {
            int sbd = row.getSbd();
            boolean present = ExamSessionState.isPresent(getServletContext(), sessionId, sbd);
            boolean inProcedure = ExamSessionState.isInProcedureQueue(getServletContext(), sessionId, sbd);
            boolean called = (activeSbd != null && activeSbd == sbd)
                    || (calledSbd != null && calledSbd == sbd);
            row.setPresent(present);
            row.setInProcedure(inProcedure);
            row.setMarkPresentEligible(called && !present && !inProcedure
                    && !row.isSuspended()
                    && !"awaiting".equals(row.getStatus())
                    && !"done".equals(row.getStatus()));
            row.setUndoPresentEligible(present && !inProcedure
                    && !row.isSuspended()
                    && !"awaiting".equals(row.getStatus())
                    && !"done".equals(row.getStatus()));
            row.setWrongInfoEligible(called && !inProcedure
                    && !row.isSuspended()
                    && !"done".equals(row.getStatus()));
            row.setViolationEligible(!row.isSuspended()
                    && !"done".equals(row.getStatus()));
        }
    }
}
