package controller.examiner;
import dto.ExaminerSlotDTO;
import filter.ExaminerPortalFilter;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import util.ExamQueue;
import util.ExamQueue.Lane;
import util.ExaminerCandidateSort;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        String action = request.getParameter("action");
        if (sessionId != null && sessionId > 0) {
            if ("1".equals(request.getParameter("absenceConfirmed")) && sbd != null) {
                examinerService.markAbsent(sessionId, sbd, ((User) session.getAttribute("user")).getUserId());
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?absentDone="
                        + urlEncode(sbd));
                return;
            }
            if (action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }
            boolean isTheory = ExaminerPortalFilter.isTheorySession(session);
            String sectionName = resolveSectionName(session);
            List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            Lane lane = ExamQueue.resolveLane(isTheory, sectionName);
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
            candidates = viewDataService.orderCandidateRowsByQueue(candidates, isTheory, sectionName);
            ExaminerCandidateSort.applyCandidateSort(request, candidates);
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
        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if ("callSelected".equals(request.getParameter("action"))) {
            User user = (User) session.getAttribute("user");
            int[] sbds = parseSbdParams(request.getParameterValues("sbd"));
            int count = examinerService.callSelectedCandidates(sessionId, sbds, user,
                    user.getUserId(),
                    ExaminerPortalFilter.isTheorySession(session),
                    resolveSectionName(session),
                    resolveCallDestination(session));
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
        boolean isTheory = ExaminerPortalFilter.isTheorySession(session);
        String sectionName = resolveSectionName(session);
        String destination = resolveCallDestination(session);
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    Integer calledSbd = examinerService.callNextCandidate(sessionId, user, userId,
                            isTheory, sectionName, destination);
                    if (calledSbd == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?called="
                            + urlEncode(calledSbd));
                    return true;
                }
                if (!examinerService.callCandidate(sessionId, sbd, user, userId, isTheory, sectionName, destination)) {
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
                if (!examinerService.undoAbsent(sessionId, sbd, userId)) {
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
                if (!examinerService.markAbsent(sessionId, sbd, userId)) {
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
                if (!examinerService.printSignatureForm(sessionId, sbd, userId)) {
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
                String completeError = examinerService.completeCandidateSection(sessionId, sbd, userId);
                if ("needSignaturePrint".equals(completeError)) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=needSignaturePrint&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                if (completeError != null) {
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
    private int[] parseSbdParams(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        List<Integer> parsed = new ArrayList<>();
        for (String value : values) {
            Integer sbd = parseSbdParam(value);
            if (sbd != null) {
                parsed.add(sbd);
            }
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
        Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
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
