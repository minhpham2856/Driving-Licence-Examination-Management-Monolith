package controller.examiner;

import java.util.*;

import model.*;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
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

import jakarta.servlet.http.Part;
import util.ExaminerViolationUploadHelper;


import java.io.IOException;
import java.util.Map;

// Handles violation management: viewing violations, confirming new violations (with evidence upload), and undoing suspensions.
@WebServlet(urlPatterns = {
    "/views/examiner/violations",
    "/views/examiner/violation-confirm",
    "/views/examiner/violation-undo"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024)
public class ExaminerViolationsServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    // Renders the violations list or the specific violation confirm/undo forms.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) return;

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        String path = ExaminerUtil.stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/violations".equals(path)) {
                Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            } else {
                if (sbd == null || sbd.isBlank() || request.getAttribute("candidate") == null) {
                    // Try to attach if missing
                    Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    if (request.getAttribute("candidate") == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
                        return;
                    }
                }

                if ("/views/examiner/violation-confirm".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map<?, ?> candidateMap) {
                        if (Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=alreadySuspended");
                            return;
                        }
                    }
                } else if ("/views/examiner/violation-undo".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map<?, ?> candidateMap) {
                        if (!Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=notSuspended");
                            return;
                        }
                    }
                }
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSession");
            return;
        }

        String jsp = switch (path) {
            case "/views/examiner/violations" -> "/views/examiner/violations.jsp";
            case "/views/examiner/violation-confirm" -> "/views/examiner/violation-confirm.jsp";
            case "/views/examiner/violation-undo" -> "/views/examiner/violation-undo.jsp";
            default -> "/views/examiner/violations.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    // Handles POST requests for recording violations and undoing suspensions.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) return;

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "ChÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°a cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ ca thi ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ang diÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦n ra.");
            return;
        }

        String path = ExaminerUtil.stripContextPath(request);
        if ("/views/examiner/violation-confirm".equals(path)) {
            handleRecordViolation(request, response, session, sessionId);
            return;
        }
        if ("/views/examiner/violation-undo".equals(path)) {
            handleUndoSuspension(request, response, session, sessionId);
            return;
        }

        doGet(request, response);
    }

    // Records a new violation, processes evidence upload, and suspends the candidate.
    private void handleRecordViolation(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        String sbd = request.getParameter("sbd");
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");

        if (reasonCode == null || reasonCode.isBlank()) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            request.setAttribute("violationError", "Vui lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â²ng chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Ân lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â½ do vi phÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â¡m.");
            request.getRequestDispatcher("/views/examiner/violation-confirm.jsp").forward(request, response);
            return;
        }

        String evidencePath = null;
        try {
            Part evidencePart = request.getPart("evidenceFile");
            evidencePath = ExaminerViolationUploadHelper.saveUpload(request, evidencePart, sessionId);
        } catch (IOException | ServletException e) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            request.setAttribute("violationError", e.getMessage() != null ? e.getMessage() : "KhÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´ng tÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚ÂºÃƒâ€šÃ‚Â£i ÃƒÆ’Ã¢â‚¬Å¾ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÆ’Ã¢â‚¬Â Ãƒâ€šÃ‚Â°ÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â£c file minh chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Â©ng.");
            request.getRequestDispatcher("/views/examiner/violation-confirm.jsp").forward(request, response);
            return;
        }

        String returnTo = request.getParameter("returnTo");
        if (returnTo == null || returnTo.isBlank()) {
            returnTo = "/views/examiner/violations";
        }

        String[] deductionParams = request.getParameterValues("deductionId");
        int[] deductionIds = ExaminerUtil.parseDeductionIds(deductionParams);

        boolean saved = examinerService.recordViolation(
                sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds, ((User) session.getAttribute("user")).getUserId(), ExaminerUtil.resolveSectionType(session), ExaminerUtil.resolveSectionName(session));
        if (saved) {
            response.sendRedirect(request.getContextPath() + returnTo + "?suspended=" + ExaminerUtil.urlEncode(sbd));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/views/examiner/violation-confirm?sbd=" + ExaminerUtil.urlEncode(sbd) + "&error=saveFailed&returnTo=" + ExaminerUtil.urlEncode(returnTo));
    }

    // Reverses a suspension and logs the undo action.
    private void handleUndoSuspension(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        String sbd = request.getParameter("sbd");
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");

        if (reasonCode == null || reasonCode.isBlank()) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            request.setAttribute("undoError", "Vui lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â²ng chÃƒÆ’Ã‚Â¡Ãƒâ€šÃ‚Â»Ãƒâ€šÃ‚Ân lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â½ do hoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â n tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡c.");
            request.getRequestDispatcher("/views/examiner/violation-undo.jsp").forward(request, response);
            return;
        }

        boolean undone = examinerService.undoSuspension(sessionId, sbd, reasonCode, reasonDetail, ((User) session.getAttribute("user")).getUserId());
        if (undone) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?undoSuspended=" + ExaminerUtil.urlEncode(sbd));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/views/examiner/violation-undo?sbd=" + ExaminerUtil.urlEncode(sbd) + "&error=undoFailed");
    }
}





