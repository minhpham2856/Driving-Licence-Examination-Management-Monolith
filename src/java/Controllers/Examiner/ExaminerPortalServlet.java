package Controllers.Examiner;

import Models.User;
import Services.ExaminerCrudService;
import Services.ExaminerSessionContextService;
import Services.ExaminerViewDataService;
import Services.Impl.ExaminerCrudServiceImpl;
import Services.Impl.ExaminerViewDataServiceImpl;
import Utils.ExaminerViolationUploadHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/views/examiner/dashboard",
    "/views/examiner/candidate-call",
    "/views/examiner/confirmation",
    "/views/examiner/candidate-details",
    "/views/examiner/candidate-details-edit",
    "/views/examiner/candidate-paper",
    "/views/examiner/result-details",
    "/views/examiner/result-details-edit",
    "/views/examiner/export",
    "/views/examiner/audit",
    "/views/examiner/score-entry",
    "/views/examiner/violations",
    "/views/examiner/violation-confirm",
    "/views/examiner/violation-undo",
    "/views/examiner/devices",
    "/views/examiner/print-documents"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024)
public class ExaminerPortalServlet extends HttpServlet {

    private final ExaminerViewDataService viewDataService = new ExaminerViewDataServiceImpl();
    private final ExaminerCrudService crudService = new ExaminerCrudServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            String action = request.getParameter("action");
            if ("/views/examiner/candidate-call".equals(path) && action != null) {
                if (handleCallAction(request, response, session, sessionId, action, sbd)) {
                    return;
                }
            }
            if ("/views/examiner/devices".equals(path) && action != null
                    && ("maintenance".equals(action) || "operational".equals(action))) {
                handleDeviceStatusChange(request, response, session, action);
                return;
            }
            if ("/views/examiner/candidate-call".equals(path)
                    && "1".equals(request.getParameter("absenceConfirmed"))) {
                crudService.markAbsent(sessionId, sbd, session);
                redirect(response, request, "/views/examiner/candidate-call?absentDone=" + urlEncode(sbd));
                return;
            }

            loadPageData(request, sessionId, sbd, search, path);

            if (isViolationDetailPath(path)
                    && (sbd == null || sbd.isBlank() || request.getAttribute("candidate") == null)) {
                redirect(response, request, "/views/examiner/violations?error=noSbd");
                return;
            }
            if ("/views/examiner/violation-confirm".equals(path)) {
                Object candidateObj = request.getAttribute("candidate");
                if (candidateObj instanceof Map<?, ?>) {
                    Map<?, ?> candidateMap = (Map<?, ?>) candidateObj;
                    if (Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                        redirect(response, request, "/views/examiner/violations?error=alreadySuspended");
                        return;
                    }
                }
            }
            if ("/views/examiner/violation-undo".equals(path)) {
                Object candidateObj = request.getAttribute("candidate");
                if (candidateObj instanceof Map<?, ?>) {
                    Map<?, ?> candidateMap = (Map<?, ?>) candidateObj;
                    if (!Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                        redirect(response, request, "/views/examiner/violations?error=notSuspended");
                        return;
                    }
                }
            }
        } else if (isViolationPath(path)) {
            redirect(response, request, "/views/examiner/violations?error=noSession");
            return;
        }

        if ("/views/examiner/result-details-edit".equals(path) && sessionId != null && sessionId > 0) {
            if (sbd == null || sbd.isBlank() || request.getAttribute("candidate") == null) {
                redirect(response, request, "/views/examiner/result-details");
                return;
            }
            request.setAttribute("theoryMaxScore", viewDataService.theoryMaxQuestions());
            request.setAttribute("theoryPassScore", viewDataService.theoryPassThreshold());
        }

        forward(request, response, jspForPath(path));
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chưa có ca thi đang diễn ra.");
            return;
        }

        String path = stripContextPath(request);
        if ("/views/examiner/candidate-call".equals(path)
                && "callSelected".equals(request.getParameter("action"))) {
            handleCallSelected(request, response, session, sessionId);
            return;
        }
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            handleUpdateProfile(request, response, session, sessionId);
            return;
        }
        if ("/views/examiner/result-details-edit".equals(path)) {
            handleUpdateScore(request, response, session, sessionId);
            return;
        }
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

    private void loadPageData(HttpServletRequest request, int sessionId, String sbd, String search, String path) {
        if ("/views/examiner/score-entry".equals(path)) {
            viewDataService.attachScoreEntry(request, sessionId, sbd);
            return;
        }
        if ("/views/examiner/violations".equals(path)) {
            viewDataService.attachToRequest(request, sessionId, sbd, search);
            return;
        }
        if (isViolationDetailPath(path)) {
            viewDataService.attachViolation(request, sessionId, sbd);
            return;
        }
        if ("/views/examiner/devices".equals(path)) {
            viewDataService.attachDevices(request, sessionId, search);
            return;
        }
        if ("/views/examiner/audit".equals(path)) {
            viewDataService.attachAuditLogs(request, sessionId, request.getParameter("page"), search);
            return;
        }
        if ("/views/examiner/candidate-paper".equals(path)) {
            viewDataService.attachToRequest(request, sessionId, sbd, search);
            viewDataService.attachPaperAnswers(request, sessionId, sbd, request.getContextPath());
            return;
        }
        viewDataService.attachToRequest(request, sessionId, sbd, search);
    }

    private boolean handleCallAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId, String action, String sbd) throws IOException {
        User user = (User) session.getAttribute("user");
        switch (action) {
            case "call" -> {
                if (sbd == null || sbd.isBlank()) {
                    String calledSbd = crudService.callNextCandidate(sessionId, user, session);
                    if (calledSbd == null) {
                        redirect(response, request, "/views/examiner/candidate-call?error=noCandidate");
                        return true;
                    }
                    redirect(response, request, "/views/examiner/candidate-call?called=" + urlEncode(calledSbd));
                    return true;
                }
                if (!crudService.callCandidate(sessionId, sbd, user, session)) {
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
                if (!crudService.undoAbsent(sessionId, sbd, session)) {
                    redirect(response, request,
                            "/views/examiner/candidate-call?error=undoAbsentFailed&sbd=" + urlEncode(sbd));
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
                if (!crudService.markAbsent(sessionId, sbd, session)) {
                    redirect(response, request,
                            "/views/examiner/candidate-call?error=absentFailed&sbd=" + urlEncode(sbd));
                    return true;
                }
                redirect(response, request, "/views/examiner/candidate-call?absentDone=" + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void handleCallSelected(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException {
        User user = (User) session.getAttribute("user");
        String[] sbds = request.getParameterValues("sbd");
        int count = crudService.callSelectedCandidates(sessionId, sbds, user, session);
        if (count <= 0) {
            redirect(response, request, "/views/examiner/candidate-call?error=callSelectedFailed");
            return;
        }
        redirect(response, request, "/views/examiner/candidate-call?calledBatch=" + count);
    }

    private void handleDeviceStatusChange(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String action) throws IOException {
        int deviceId;
        try {
            deviceId = Integer.parseInt(request.getParameter("deviceId"));
        } catch (Exception e) {
            redirect(response, request, "/views/examiner/devices?error=invalidDevice");
            return;
        }

        boolean updated;
        String redirectParam;
        if ("operational".equals(action)) {
            updated = crudService.setDeviceAvailable(deviceId, session);
            redirectParam = updated
                    ? "/views/examiner/devices?operationalDone=" + deviceId
                    : "/views/examiner/devices?error=operationalFailed&deviceId=" + deviceId;
        } else if ("maintenance".equals(action)) {
            updated = crudService.setDeviceMaintenance(deviceId, session);
            redirectParam = updated
                    ? "/views/examiner/devices?maintenanceDone=" + deviceId
                    : "/views/examiner/devices?error=maintenanceFailed&deviceId=" + deviceId;
        } else {
            return;
        }
        redirect(response, request, redirectParam);
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        String sbd = request.getParameter("sbd");
        boolean updated = crudService.updateCandidateProfile(
                sessionId,
                sbd,
                request.getParameter("fullName"),
                request.getParameter("dateOfBirth"),
                request.getParameter("govIdNo"),
                request.getParameter("email"),
                request.getParameter("phoneNo"),
                request.getParameter("address"),
                request.getParameter("sex"),
                request.getParameter("reasonForTaking"),
                session);

        if (updated) {
            redirect(response, request,
                    "/views/examiner/candidate-details-edit?sbd=" + urlEncode(sbd) + "&saved=1");
            return;
        }

        viewDataService.attachToRequest(request, sessionId, sbd, null);
        request.setAttribute("profileError", "Không lưu được thông tin. Kiểm tra lại dữ liệu nhập.");
        forward(request, response, "/views/examiner/candidate-details-edit.jsp");
    }

    private void handleUpdateScore(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        String sbd = request.getParameter("sbd");
        User user = (User) session.getAttribute("user");
        String password = request.getParameter("password");
        String reason = request.getParameter("reason");
        String reasonDetail = request.getParameter("reasonDetail");
        String newScoreParam = request.getParameter("newScore");
        int maxScore = viewDataService.theoryMaxQuestions();

        if (reason == null || reason.isBlank()) {
            forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                    "Vui lòng chọn lý do điều chỉnh.", maxScore);
            return;
        }

        int newScore;
        try {
            newScore = Integer.parseInt(newScoreParam.trim());
        } catch (Exception e) {
            forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                    "Điểm mới không hợp lệ.", maxScore);
            return;
        }

        if (newScore < 0 || newScore > maxScore) {
            forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                    "Điểm phải từ 0 đến " + maxScore + ".", maxScore);
            return;
        }

        if (password == null || password.isBlank()) {
            forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                    "Vui lòng nhập mật khẩu để xác nhận.", maxScore);
            return;
        }

        if (!crudService.verifyPassword(user, password)) {
            forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                    "Mật khẩu không chính xác.", maxScore);
            return;
        }

        boolean updated = crudService.updateTheoryScore(
                sessionId, sbd, newScore, reason, reasonDetail, user, password, session);

        if (updated) {
            redirect(response, request,
                    "/views/examiner/result-details-edit?sbd=" + urlEncode(sbd) + "&saved=1");
            return;
        }

        forwardScoreFormError(request, response, sessionId, sbd, newScoreParam, reason, reasonDetail,
                "Không lưu được điểm. Kiểm tra thí sinh và ca thi lý thuyết.", maxScore);
    }

    private void forwardScoreFormError(HttpServletRequest request, HttpServletResponse response,
            int sessionId, String sbd, String newScore, String reason, String reasonDetail,
            String errorMessage, int maxScore) throws ServletException, IOException {
        viewDataService.attachToRequest(request, sessionId, sbd, null);
        request.setAttribute("scoreError", errorMessage);
        request.setAttribute("formNewScore", newScore);
        request.setAttribute("formReason", reason);
        request.setAttribute("formReasonDetail", reasonDetail);
        request.setAttribute("theoryMaxScore", maxScore);
        request.setAttribute("theoryPassScore", viewDataService.theoryPassThreshold());
        forward(request, response, "/views/examiner/result-details-edit.jsp");
    }

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
            evidencePath = ExaminerViolationUploadHelper.saveEvidence(request, sessionId, sbd);
        } catch (IOException | ServletException e) {
            viewDataService.attachViolation(request, sessionId, sbd);
            request.setAttribute("violationError",
                    e.getMessage() != null ? e.getMessage() : "Không tải được file minh chứng.");
            forward(request, response, "/views/examiner/violation-confirm.jsp");
            return;
        }

        String[] deductionParams = request.getParameterValues("deductionId");
        int[] deductionIds = parseDeductionIds(deductionParams);
        boolean saved = crudService.recordViolation(
                sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds, session);
        if (saved) {
            redirect(response, request,
                    "/views/examiner/violations?suspended=" + urlEncode(sbd));
            return;
        }
        redirect(response, request,
                "/views/examiner/violation-confirm?sbd=" + urlEncode(sbd) + "&error=saveFailed");
    }

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

        boolean undone = crudService.undoSuspension(sessionId, sbd, reasonCode, reasonDetail, session);
        if (undone) {
            redirect(response, request,
                    "/views/examiner/violations?undoSuspended=" + urlEncode(sbd));
            return;
        }
        redirect(response, request,
                "/views/examiner/violation-undo?sbd=" + urlEncode(sbd) + "&error=undoFailed");
    }

    private static int[] parseDeductionIds(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        int[] ids = new int[values.length];
        int count = 0;
        for (String value : values) {
            try {
                int id = Integer.parseInt(value.trim());
                if (id > 0) {
                    ids[count++] = id;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (count == ids.length) {
            return ids;
        }
        int[] trimmed = new int[count];
        System.arraycopy(ids, 0, trimmed, 0, count);
        return trimmed;
    }

    private static boolean isViolationPath(String path) {
        return "/views/examiner/violations".equals(path)
                || isViolationDetailPath(path);
    }

    private static boolean isViolationDetailPath(String path) {
        return "/views/examiner/violation-confirm".equals(path)
                || "/views/examiner/violation-undo".equals(path);
    }

    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập.");
        }
        return session;
    }

    private static Integer activeSessionId(HttpSession session) {
        return (Integer) session.getAttribute(ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID);
    }

    private static String jspForPath(String path) {
        return switch (path) {
            case "/views/examiner/dashboard" ->
                "/views/examiner/dashboard.jsp";
            case "/views/examiner/candidate-call" ->
                "/views/examiner/candidate-call.jsp";
            case "/views/examiner/confirmation" ->
                "/views/examiner/confirmation.jsp";
            case "/views/examiner/candidate-details" ->
                "/views/examiner/candidate-details.jsp";
            case "/views/examiner/candidate-details-edit" ->
                "/views/examiner/candidate-details-edit.jsp";
            case "/views/examiner/candidate-paper" ->
                "/views/examiner/candidate-paper.jsp";
            case "/views/examiner/result-details" ->
                "/views/examiner/result-details.jsp";
            case "/views/examiner/result-details-edit" ->
                "/views/examiner/result-details-edit.jsp";
            case "/views/examiner/export" ->
                "/views/examiner/export.jsp";
            case "/views/examiner/audit" ->
                "/views/examiner/audit.jsp";
            case "/views/examiner/score-entry" ->
                "/views/examiner/score-entry.jsp";
            case "/views/examiner/violations" ->
                "/views/examiner/violations.jsp";
            case "/views/examiner/violation-confirm" ->
                "/views/examiner/violation-confirm.jsp";
            case "/views/examiner/violation-undo" ->
                "/views/examiner/violation-undo.jsp";
            case "/views/examiner/devices" ->
                "/views/examiner/devices.jsp";
            case "/views/examiner/print-documents" ->
                "/views/examiner/print-documents.jsp";
            default ->
                "/views/examiner/dashboard.jsp";
        };
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws ServletException, IOException {
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    private void redirect(HttpServletResponse response, HttpServletRequest request, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    private static String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
