package controller.examiner;

import enums.SectionType;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExamViewService;
import dto.CandidateRowDTO;
import service.impl.ExamViewServiceImpl;
import service.CallService;
import service.impl.CallServiceImpl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/examiner/candidate-call")
public class ExaminerCandidateCallServlet extends HttpServlet {
    private final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService callService = new CallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
        if (sessionId != null && sessionId > 0) {
            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);

            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            SectionType examSection = SectionType.fromValue(sectionName);
            candidates = viewDataService.orderCandidateRowsByQueue(candidates, examSection);

            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            request.setAttribute("examSummary", viewDataService.buildCandidateSummary(sessionId, isTheory, sectionName));
        }
        request.getRequestDispatcher("/views/examiner/candidate-call.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing active session");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noAction");
            return;
        }

        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {
        }

        if (handleCallAction(request, response, session, sessionId, action, sbd)) {
            return;
        }

        if ("callSelected".equals(action)) {
            User user = (User) session.getAttribute("user");
            int userId = user.getUserId();
            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);
            SectionType examSection = SectionType.fromValue(sectionName);
            
            String[] rawSbds = request.getParameterValues("selectedSbd");
            int[] sbds = parseSbdParams(rawSbds);
            if (sbds.length == 0) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSelected");
                return;
            }

            Integer countResult = callService.callSelectedCandidates(sessionId, user, userId, examSection, isTheory,
                    sectionName, resolveCallDestination(session), sbds).getData();
            int count = countResult != null ? countResult : 0;
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
        boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
        String sectionName = resolveSectionName(session);
        SectionType examSection = SectionType.fromValue(sectionName);
        String destination = resolveCallDestination(session);

        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer calledSbd = callService.callNextCandidate(sessionId, user, userId, examSection,
                            isTheory, sectionName, destination).getData();
                    if (calledSbd == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                            + urlEncode(calledSbd));
                    return true;
                }
                if (!callService.callCandidate(sessionId, sbd, user, userId, examSection, isTheory, sectionName, destination).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=callFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                        + urlEncode(sbd));
                return true;
            }
            case "undoAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!callService.undoPresent(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=undoAbsentFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?undoAbsent="
                        + urlEncode(sbd));
                return true;
            }
            case "markAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!callService.markPresent(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=absentFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone="
                        + urlEncode(sbd));
                return true;
            }
            case "printSignature" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                if (!callService.printSignatureForm(sessionId, sbd, userId).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=signaturePrintFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/print-documents?sbd="
                        + urlEncode(sbd) + "&signatureMarked=1");
                return true;
            }
            case "completeSection" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noSbd");
                    return true;
                }
                dto.ServiceResult<Void> res = callService.completeCandidateSection(sessionId, sbd, userId, null);
                if (res != null && "needSignaturePrint".equals(res.getMessage())) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=needSignaturePrint&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                if (res != null && !res.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=completeFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?completeDone="
                        + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private int[] parseSbdParams(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        List<Integer> parsed = new ArrayList<>();
        for (String value : values) {
            try {
                parsed.add(Integer.parseInt(value.trim()));
            } catch (Exception e) {}
        }
        int[] result = new int[parsed.size()];
        for (int i = 0; i < parsed.size(); i++) {
            result[i] = parsed.get(i);
        }
        return result;
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute("examSectionName");
        return name != null ? String.valueOf(name) : null;
    }

    private String resolveCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi";
        }
        Object sectionName = session.getAttribute("examSectionName");
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi";
    }
}
