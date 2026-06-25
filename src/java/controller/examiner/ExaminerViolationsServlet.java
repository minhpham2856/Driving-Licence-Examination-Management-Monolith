package controller.examiner;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
public class ExaminerViolationsServlet extends BaseExaminerServlet {

    // Renders the violations list or the specific violation confirm/undo forms.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/violations".equals(path)) {
                viewDataService.attachToRequest(request, sessionId, sbd, search);
            } else {
                if (sbd == null || sbd.isBlank() || request.getAttribute("candidate") == null) {
                    // Try to attach if missing
                    viewDataService.attachViolation(request, sessionId, sbd);
                    if (request.getAttribute("candidate") == null) {
                        redirect(response, request, "/views/examiner/violations?error=noSbd");
                        return;
                    }
                }

                if ("/views/examiner/violation-confirm".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map<?, ?> candidateMap) {
                        if (Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            redirect(response, request, "/views/examiner/violations?error=alreadySuspended");
                            return;
                        }
                    }
                } else if ("/views/examiner/violation-undo".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map<?, ?> candidateMap) {
                        if (!Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            redirect(response, request, "/views/examiner/violations?error=notSuspended");
                            return;
                        }
                    }
                }
            }
        } else {
            redirect(response, request, "/views/examiner/violations?error=noSession");
            return;
        }

        String jsp = switch (path) {
            case "/views/examiner/violations" -> "/views/examiner/violations.jsp";
            case "/views/examiner/violation-confirm" -> "/views/examiner/violation-confirm.jsp";
            case "/views/examiner/violation-undo" -> "/views/examiner/violation-undo.jsp";
            default -> "/views/examiner/violations.jsp";
        };
        forward(request, response, jsp);
    }

    // Handles POST requests for recording violations and undoing suspensions.
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

        String path = stripContextPath(request);
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
            viewDataService.attachViolation(request, sessionId, sbd);
            request.setAttribute("violationError", "Vui lòng chọn lý do vi phạm.");
            forward(request, response, "/views/examiner/violation-confirm.jsp");
            return;
        }

        String evidencePath = null;
        try {
            Part evidencePart = request.getPart("evidenceFile");
            evidencePath = ExaminerViolationUploadHelper.saveUpload(request, evidencePart, sessionId);
        } catch (IOException | ServletException e) {
            viewDataService.attachViolation(request, sessionId, sbd);
            request.setAttribute("violationError", e.getMessage() != null ? e.getMessage() : "Không tải được file minh chứng.");
            forward(request, response, "/views/examiner/violation-confirm.jsp");
            return;
        }

        String returnTo = request.getParameter("returnTo");
        if (returnTo == null || returnTo.isBlank()) {
            returnTo = "/views/examiner/violations";
        }

        String[] deductionParams = request.getParameterValues("deductionId");
        int[] deductionIds = parseDeductionIds(deductionParams);

        boolean saved = examinerService.recordViolation(
                sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds, session);
        if (saved) {
            redirect(response, request, returnTo + "?suspended=" + urlEncode(sbd));
            return;
        }
        redirect(response, request, "/views/examiner/violation-confirm?sbd=" + urlEncode(sbd) + "&error=saveFailed&returnTo=" + urlEncode(returnTo));
    }

    // Reverses a suspension and logs the undo action.
    private void handleUndoSuspension(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        String sbd = request.getParameter("sbd");
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");

        if (reasonCode == null || reasonCode.isBlank()) {
            viewDataService.attachViolation(request, sessionId, sbd);
            request.setAttribute("undoError", "Vui lòng chọn lý do hoàn tác.");
            forward(request, response, "/views/examiner/violation-undo.jsp");
            return;
        }

        boolean undone = examinerService.undoSuspension(sessionId, sbd, reasonCode, reasonDetail, session);
        if (undone) {
            redirect(response, request, "/views/examiner/violations?undoSuspended=" + urlEncode(sbd));
            return;
        }
        redirect(response, request, "/views/examiner/violation-undo?sbd=" + urlEncode(sbd) + "&error=undoFailed");
    }
}

