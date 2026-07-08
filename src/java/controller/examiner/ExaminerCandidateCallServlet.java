package controller.examiner;
import filter.ExaminerFilter;
import dto.ServiceResult;
import enums.ExamSection;
import model.User;
import service.CallService;
import service.ExamViewService;
import service.SessionService;
import service.impl.CallServiceImpl;
import service.impl.ExamViewServiceImpl;
import service.impl.SessionServiceImpl;
import util.ExamQueue;
import util.ExamQueue.Lane;
import util.ExaminerCandidateSort;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dto.CandidateRowDTO;
import dto.SessionViewDTO;
@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends BaseExaminerServlet {
    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService ScheduleService = new CallServiceImpl();
    private final SessionService sessionControlService = new SessionServiceImpl();
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
            SessionViewDTO examSession = sessionControlService.getSessionById(sessionId);
            boolean sessionEnded = examSession != null && isSessionEnded(examSession.getStatus());
            request.setAttribute("sessionEnded", sessionEnded);
            if (sessionEnded && action != null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=sessionEnded");
                return;
            }
            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }
            boolean isTheory = ExaminerFilter.isTheorySession(session);
            ExamSection examSection = getExamSection(session);
            String sectionName = examSection.getValue();
            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            Lane lane = ExamQueue.laneFor(examSection);
            List<Integer> eligibleSbds = new ArrayList<>();
            for (CandidateRowDTO row : candidates) {
                if (row.isCallEligible()) {
                    eligibleSbds.add(row.getSbd());
                }
            }
            ExamQueue.sync(lane, eligibleSbds);
            candidates = viewDataService.orderCandidateRowsByQueue(candidates, examSection);
            enrichDeskState(candidates, sessionId, lane);
            applyCandidateSort(request, candidates);
            if (search != null && !search.isBlank()) {
                String q = search.trim().toLowerCase(Locale.ROOT);
                List<CandidateRowDTO> filtered = new ArrayList<>();
                for (CandidateRowDTO row : candidates) {
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
                for (CandidateRowDTO row : candidates) {
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
            ExamSection examSection = getExamSection(session);
            int[] sbds = parseSbdParams(request.getParameterValues("sbd"));
            ServiceResult<Integer> selectedResult = ScheduleService.callSelectedCandidates(
                    sessionId, user, user.getUserId(), examSection, examSection == ExamSection.THEORY,
                    examSection.getValue(), getCallDestination(session), sbds);
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
        ExamSection examSection = getExamSection(session);
        boolean isTheory = examSection == ExamSection.THEORY;
        String sectionName = examSection.getValue();
        String callDestination = getCallDestination(session);
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    ServiceResult<Integer> nextResult = ScheduleService.callNextCandidate(
                            sessionId, user, userId, examSection, isTheory, sectionName, callDestination);
                    if (!nextResult.isSuccess() || nextResult.getData() == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                            + encodeSbd(nextResult.getData()));
                    return true;
                }
                if (!ScheduleService.callCandidate(sessionId, sbd, user, userId, examSection, isTheory,
                        sectionName, callDestination).isSuccess()) {
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
                if (!ScheduleService.undoPresent(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=undoPresentFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ScheduleService.clearPresent(sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?undoPresent="
                        + encodeSbd(sbd));
                return true;
            }
            case "markPresent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!ScheduleService.markPresent(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=presentFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ScheduleService.markPresent(sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?presentDone="
                        + encodeSbd(sbd));
                return true;
            }
            case "wrongInfo" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!ScheduleService.sendWrongInfoToProcedure(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=wrongInfoFailed&sbd="
                            + encodeSbd(sbd));
                    return true;
                }
                ScheduleService.sendToProcedure(sessionId, sbd);
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?wrongInfoDone="
                        + encodeSbd(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!ScheduleService.printSignatureForm(sessionId, sbd, userId).isSuccess()) {
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
                ServiceResult<Void> completeResult = ScheduleService.completeCandidateSection(
                        sessionId, sbd, userId, null);
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

    private void enrichDeskState(List<CandidateRowDTO> candidates, int sessionId, Lane lane) {
        Integer activeSbd = ExamQueue.getActiveSbd(lane);
        Integer calledSbd = ExamQueue.getCalledSbd(lane);
        for (CandidateRowDTO row : candidates) {
            int sbd = row.getSbd();
            boolean present = ScheduleService.isPresent(sessionId, sbd);
            boolean inProcedure = ScheduleService.isInProcedureQueue(sessionId, sbd);
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
