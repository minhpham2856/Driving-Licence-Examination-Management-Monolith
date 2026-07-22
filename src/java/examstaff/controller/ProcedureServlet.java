package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ProcedureActionOutcome;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.CallBoardState;
import examstaff.dto.ServiceResult;
import examstaff.dto.SePayProcedureCheckoutDTO;
import examstaff.service.AuditService;
import examstaff.service.ExamControlService;
import examstaff.service.ExamStaffViewService;
import examstaff.service.ProcedureService;
import examstaff.service.StaffCallService;
import examstaff.service.impl.AuditServiceImpl;
import examstaff.service.impl.ExamControlServiceImpl;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.service.impl.ProcedureServiceImpl;
import examstaff.service.impl.StaffCallServiceImpl;
import examstaff.service.impl.support.procedure.ProcedureStepHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Bàn thủ tục thí sinh (wizard lý lịch / ảnh / phí): prepare → actions → bind desk → candidatecall.jsp (deskMode).
 *
 * Vai trò:
 * Wizard 3 bước tại bàn tiếp nhận: xác nhận lý lịch, chụp/lưu ảnh, thu phí (SePay/cash).
 * Đồng bộ {@link examstaff.dao.CallBoardDAO}, session {@code callingSbd}, queue và audit.
 * Render {@code candidatecall.jsp} ở chế độ desk (không phải trang gọi số riêng).
 *
 * Luồng GET:
 * - {@code action=startShift} → {@link ExamStaffShiftSupport} → redirect candidatecall
 * - Prepare page + resolve SBD → {@code findProfile} / {@code prepareProfileForDesk}
 * - Phân nhánh action: next/reset/save/photo/payment/SePay finalize
 * - Bind step/fees/board → {@code forwardDeskView} (candidatecall.jsp deskMode)
 *
 * Ai gọi:
 * Redirect từ {@link CandidateCallServlet} ({@code view=desk}); sidebar exam staff;
 * link sau gọi số với {@code ?sbd=}.
 */
@WebServlet("/examstaff/procedure")
public class ProcedureServlet extends HttpServlet {

    private final ProcedureService procedureService = new ProcedureServiceImpl();
    private final StaffCallService staffCall = new StaffCallServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();
    private final ExamControlService examControlService = new ExamControlServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    /**
     * GET: startShift | prepare desk | xử lý action (next/reset/save/photo/payment) | forward desk view.
     * <p>
     * Luồng: resolve SBD → find/prepare profile → phân nhánh action → bind step/fees → {@link #forwardDeskView}.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi redirect / JSON
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        // Bắt đầu / resume ca → quay lại trang gọi
        if ("startShift".equals(request.getParameter("action"))) {
            List<ExamSummaryDTO> bootstrapExams = viewService.listAllExams();
            int boardExamId = ExamStaffPageSupport.resolveExamId(request, session, bootstrapExams, 0, viewService);
            ExamStaffShiftSupport.startOrResumeShift(session, getServletContext(), boardExamId, staffCall);
            response.sendRedirect(request.getContextPath() + "/examstaff/candidatecall");
            return;
        }

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                request, session, webRoot, true, viewService);
        int examId = pageCtx.getExamId();
        List<ExamSummaryDTO> allExams = pageCtx.getAllExams();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

        // SBD hiện tại + phát hiện đổi thí sinh (reset bước wizard)
        String sbdParam = resolveSbdParam(request, session);
        boolean sbdChanged = trackSbdChange(session, sbdParam);

        ExamRegistrationDTO profile = procedureService.findProfile(webRoot, examId, examId, sbdParam, qList);
        ServiceResult<ExamRegistrationDTO> prepared = procedureService.prepareProfileForDesk(
                webRoot, examId, examId, profile, qList);
        profile = prepared.getData();
        if (prepared.getMessage() != null) {
            request.setAttribute("photoStaleMsg", prepared.getMessage());
        }
        publishCandidateQueue(request, session, qList, examId);

        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        String stepParam = ProcedureStepHelper.resolveStep(request.getParameter("step"), sbdChanged, profile, hasValidPhoto);

        if ("3".equals(stepParam) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            request.setAttribute("photoRequiredMsg", ProcedureStepHelper.photoRequiredForStep3Message());
        }

        String pAction = request.getParameter("action");

        // --- SePay: server-side finalize khi đang chờ IPN ---
        // Mục tiêu: giảm JS polling fetch; khi UI reload (meta refresh), servlet sẽ tự finalize nếu IPN đã ghi Payment.
        if ((pAction == null || pAction.isBlank())
                && "3".equals(stepParam)
                && profile != null
                && sbdParam != null
                && !profile.isPaymentCompleted()) {
            String awaitingSbd = session.getAttribute("sePayAwaitingSbd") instanceof String
                    ? (String) session.getAttribute("sePayAwaitingSbd")
                    : null;
            if (awaitingSbd != null && awaitingSbd.equals(sbdParam)) {
                ServiceResult<ProcedureActionOutcome> finalizeResult =
                        procedureService.finalizeAfterSePayPayment(profile, sbdParam, examId, webRoot, allExams);
                ProcedureActionOutcome outcome = finalizeResult.getData();
                if (outcome != null) {
                    switch (outcome.getPaymentStatus()) {
                        case SUCCESS -> {
                            applyPaymentOutcome(request, session, sbdParam, outcome, examId);
                            session.removeAttribute("sePayAwaitingSbd");
                            session.removeAttribute("sePayAwaitingInvoice");
                            showPostPaymentDesk(request, response, session,
                                    outcome.getProfile(), sbdParam, outcome.getQueue(), false);
                            return;
                        }
                        case ALREADY_PAID -> {
                            session.removeAttribute("sePayAwaitingSbd");
                            session.removeAttribute("sePayAwaitingInvoice");
                            showPostPaymentDesk(request, response, session,
                                    outcome.getProfile(), sbdParam, outcome.getQueue(), false);
                            return;
                        }
                        case NO_PHOTO -> {
                            request.setAttribute("photoRequiredMsg",
                                    ProcedureStepHelper.paymentBlockedNoPhotoMessage());
                            request.setAttribute("step", "2");
                            session.setAttribute("procedureStep", "2");
                            request.setAttribute("hasValidPhoto", false);
                            request.setAttribute("profile", outcome.getProfile() != null ? outcome.getProfile() : profile);
                            session.removeAttribute("sePayAwaitingSbd");
                            session.removeAttribute("sePayAwaitingInvoice");
                            List<ExamRegistrationDTO> fallbackQ = qList != null ? qList : List.of();
                            List<ExamRegistrationDTO> nextQ = outcome.getQueue() != null ? outcome.getQueue() : fallbackQ;
                            forwardDeskView(request, response, nextQ);
                            return;
                        }
                        default -> {
                        }
                    }
                }
            }
        }

        // --- Các nhánh action ---
        if ("nextCandidate".equals(pAction)) {
            session.removeAttribute("procedureJustPaidSbd");
            String finishedSbd = sbdParam;
            if (finishedSbd == null || finishedSbd.isBlank()) {
                finishedSbd = (String) session.getAttribute("callingSbd");
            }
            advanceToNextCandidate(session, qList, webRoot, examId, allExams, finishedSbd);
            response.sendRedirect("candidatecall");
            return;
        }

        if ("resetProcedure".equals(pAction) && sbdParam != null && !sbdParam.trim().isEmpty()) {
            ServiceResult<ProcedureActionOutcome> resetResult =
                    procedureService.resetProcedure(sbdParam.trim(), examId, webRoot);
            ProcedureActionOutcome reset = resetResult.getData();
            if (resetResult.isSuccess() && reset != null) {
                qList = reset.getQueue();
                staffCall.moveCallableCandidateToFront(qList, reset.getSbd());
                ExamStaffPageBinder.syncCallQueueOrder(session, examId, qList);
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("callingSbd", reset.getSbd());
                session.removeAttribute("procedureStep");
                session.removeAttribute("lastSelectedSbd");
                addAuditLog(session, "RESET Procedure",
                        "Xóa hồ sơ thủ tục SBD " + reset.getSbd(), reset.getCandidateId());
                response.sendRedirect(request.getContextPath()
                        + "/examstaff/candidatecall?procedureReset="
                        + java.net.URLEncoder.encode(reset.getSbd(), java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
        }

        if ("saveProfile".equals(pAction) && profile != null) {
            profile = handleSaveProfile(request, session, profile, sbdParam, qList, webRoot, examId);
            stepParam = "2";
            hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        }

        if ("recapture".equals(pAction) && profile != null) {
            profile = procedureService.recapturePhoto(profile.getId(), webRoot, examId, sbdParam, qList);
            hasValidPhoto = false;
            stepParam = "2";
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot, allExams, examId);
            return;
        }

        // --- SePay: tạo checkout (HTML form auto-submit) hoặc kiểm tra đã IPN ---
        if ("createSePayCheckout".equals(pAction) && profile != null) {
            processSePayCheckout(request, response, session, profile, sbdParam, webRoot, examId);
            return;
        }

        if ("checkSePayPayment".equals(pAction) && profile != null) {
            processSePayCheck(request, response, session, profile, sbdParam, webRoot, allExams, examId);
            return;
        }

        // Callback sau cổng thanh toán (paymentSuccess=true)
        if ("true".equals(request.getParameter("paymentSuccess")) && profile != null) {
            if (!profile.isValidCapturedPhoto()) {
                request.setAttribute("photoRequiredMsg", ProcedureStepHelper.paymentBlockedNoPhotoMessage());
                request.setAttribute("step", "2");
                session.setAttribute("procedureStep", "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", profile);
                forwardDeskView(request, response, qList);
                return;
            }
            ProcedureActionOutcome outcome = procedureService.confirmPayment(
                    profile, sbdParam, examId, webRoot, allExams).getData();
            if (outcome == null) {
                response.sendRedirect("candidatecall");
                return;
            }
            applyPaymentOutcome(request, session, sbdParam, outcome, examId);
            if (outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.SUCCESS) {
                ExamStaffPageSupport.syncExamSelection(session, allExams, examId, viewService);
                showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), false);
                return;
            }
        }

        // Bind hồ sơ + phí + bước wizard rồi mở desk
        if (profile != null) {
            request.setAttribute("profile", profile);
            ExamStaffPageBinder.bindProcedureFees(request, procedureService.resolveProcedureFees(profile));
            viewService.resolveCapturedPhoto(profile);
            hasValidPhoto = profile.isValidCapturedPhoto();
        }

        // Cờ UI SePay cho procedure.jsp (nút enable/disable, sandbox hint)
        request.setAttribute("sePayConfigured", procedureService.isSePayConfigured());
        request.setAttribute("sePaySandbox", procedureService.isSePaySandbox());
        request.setAttribute("sePayIpnUrl", procedureService.sePayIpnCallbackUrl());

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);

        forwardDeskView(request, response, qList);
    }

    /**
     * POST: saveCapturedPhoto (JSON nhanh) / confirmPayment / còn lại ủy quyền GET.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
            String webRoot = request.getServletContext().getRealPath("/");
            List<ExamSummaryDTO> allExams = viewService.listAllExams();
            int examId = ExamStaffPageSupport.ensureExamId(request, session, allExams, viewService);
            List<ExamRegistrationDTO> qList = refreshCandidateQueue(session, examId, webRoot, allExams);
            String sbdParam = resolveSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId);
            return;
        }
        if ("confirmPayment".equals(action)) {
            doGet(request, response);
            return;
        }
        if ("createSePayCheckout".equals(action) || "checkSePayPayment".equals(action)) {
            doGet(request, response);
            return;
        }
        doGet(request, response);
    }

    /**
     * Mở cổng SePay: tạo form signed + HTML auto-submit.
     * Thành công → ghi session {@code sePayAwaitingSbd} để desk poll/Kiểm tra biết đang chờ IPN.
     * Lỗi nghiệp vụ → JSON 400 cho JS hiển thị message.
     */
    private void processSePayCheckout(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam, String webRoot, int examId)
            throws IOException {
        SePayProcedureCheckoutDTO result = procedureService.startSePayCheckout(
                profile, sbdParam, examId, webRoot);
        if (result.getStatus() == SePayProcedureCheckoutDTO.Status.READY
                && result.getCheckoutHtml() != null) {
            // Tab SePay + poll trên desk dùng các attribute này
            session.setAttribute("sePayAwaitingSbd", sbdParam);
            session.setAttribute("sePayAwaitingInvoice", result.getInvoiceNumber());
            addAuditLog(session, "SEPAY Checkout",
                    "Mở SePay QR SBD " + sbdParam + " invoice " + result.getInvoiceNumber(),
                    profile.getId());
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(result.getCheckoutHtml());
            return;
        }
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String msg = result.getMessage() != null ? result.getMessage().replace("\"", "'") : "Không tạo được SePay";
        response.getWriter().write("{\"success\":false,\"status\":\"" + result.getStatus()
                + "\",\"message\":\"" + msg + "\"}");
    }

    /**
     * Nút “Kiểm tra đã thanh toán” / poll JS.
     * Không gọi SePay API — chỉ xem DB đã có Payment (do IPN ghi) chưa rồi finalize thủ tục.
     * Trả JSON: {@code paid=true/false} + message chờ nếu chưa có IPN.
     */
    private void processSePayCheck(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
            String webRoot, List<ExamSummaryDTO> allExams, int examId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);

        ServiceResult<ProcedureActionOutcome> result = procedureService.finalizeAfterSePayPayment(
                profile, sbdParam, examId, webRoot, allExams);
        ProcedureActionOutcome outcome = result.getData();

        if (outcome != null
                && outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.SUCCESS) {
            // IPN đã ghi Payment → đánh dấu có mặt / phân ca giống thu tiền mặt
            applyPaymentOutcome(request, session, sbdParam, outcome, examId);
            ExamStaffPageSupport.syncExamSelection(session, allExams, examId, viewService);
            session.setAttribute("procedureJustPaidSbd", sbdParam);
            session.setAttribute("lastSelectedSbd", sbdParam);
            session.setAttribute("callingSbd", sbdParam);
            session.setAttribute("procedureStep", "3");
            addAuditLog(session, "SEPAY Paid",
                    outcome.getPaymentAuditDetail() != null ? outcome.getPaymentAuditDetail()
                            : ("Xác nhận SePay SBD " + sbdParam),
                    outcome.getProfile() != null ? outcome.getProfile().getId() : 0);
            response.getWriter().write("{\"success\":true,\"paid\":true,\"finalized\":true}");
            return;
        }
        if (outcome != null
                && outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.ALREADY_PAID) {
            response.getWriter().write("{\"success\":true,\"paid\":true,\"finalized\":false}");
            return;
        }
        if (outcome != null
                && outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.NO_PHOTO) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"paid\":false,\"message\":\"Thiếu ảnh thủ tục.\"}");
            return;
        }
        String waitMsg = result.getMessage() != null ? result.getMessage().replace("\"", "'")
                : "Đang chờ IPN SePay.";
        response.getWriter().write("{\"success\":true,\"paid\":false,\"message\":\"" + waitMsg + "\"}");
    }

    /**
     * Lưu lý lịch từ form → reload profile → audit.
     * @return profile sau khi lưu (hoặc gốc nếu lỗi)
     */
    private ExamRegistrationDTO handleSaveProfile(HttpServletRequest request, HttpSession session,
            ExamRegistrationDTO profile, String sbdParam, List<ExamRegistrationDTO> qList,
            String webRoot, int examId) {
        String fullName = request.getParameter("fullName");
        String dobStr = request.getParameter("dateOfBirth");
        String govIdNo = request.getParameter("govIdNo");
        String email = request.getParameter("email");
        String phoneNo = request.getParameter("phoneNo");

        try {
            Date sqlDob = parseDateOfBirth(dobStr);
            ServiceResult<Boolean> saveResult = procedureService.saveProfile(
                    profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo);
            if (saveResult.isSuccess()) {
                profile = procedureService.reloadProfile(webRoot, examId, profile.getId(), sbdParam, qList);
                request.setAttribute("profileUpdatedAlert", "true");
                addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + sbdParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        publishCandidateQueue(request, session, qList, examId);
        return profile;
    }

    /**
     * Parse ngày sinh từ {@code dd/MM/yyyy} hoặc {@code yyyy-MM-dd}.
     * @return java.sql.Date hoặc null nếu trống
     */
    private static Date parseDateOfBirth(String dobStr) {
        if (dobStr == null || dobStr.trim().isEmpty()) {
            return null;
        }
        if (dobStr.contains("/")) {
            String[] parts = dobStr.split("/");
            return Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
        }
        return Date.valueOf(dobStr.trim());
    }

    /**
     * Thu phí: confirmPayment → xử lý từng PaymentStatus → desk sau thanh toán hoặc lỗi.
     * @throws IOException lỗi redirect / forward
     */
    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
            List<ExamRegistrationDTO> qList, String webRoot, List<ExamSummaryDTO> allExams, int examId)
            throws IOException {
        ProcedureActionOutcome outcome = procedureService.confirmPayment(
                profile, sbdParam, examId, webRoot, allExams).getData();
        if (outcome == null) {
            response.sendRedirect("candidatecall");
            return;
        }

        if (outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.PROFILE_NOT_FOUND) {
            response.sendRedirect("candidatecall");
            return;
        }
        if (outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.NO_PHOTO) {
            try {
                request.setAttribute("photoRequiredMsg", ProcedureStepHelper.paymentBlockedNoPhotoMessage());
                request.setAttribute("step", "2");
                session.setAttribute("procedureStep", "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", outcome.getProfile());
                forwardDeskView(request, response, qList);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        if (outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.ALREADY_PAID) {
            boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
            showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, qList, openPrint);
            return;
        }
        if (outcome.getPaymentStatus() == ProcedureActionOutcome.PaymentStatus.PAYMENT_FAILED) {
            try {
                request.setAttribute("paymentErrorMsg", "Không ghi được thanh toán. Vui lòng thử lại.");
                request.setAttribute("step", "3");
                request.setAttribute("profile", outcome.getProfile());
                request.setAttribute("hasValidPhoto", outcome.getProfile().isValidCapturedPhoto());
                forwardDeskView(request, response, qList);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }

        applyPaymentOutcome(request, session, sbdParam, outcome, examId);
        ExamStaffPageSupport.syncExamSelection(session, allExams, examId, viewService);
        session.setAttribute("lastLoadedExamId", outcome.getBoardExamId());

        boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
        showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), openPrint);
    }

    /**
     * Sau thanh toán thành công: publish queue + audit (payment / auto-allocate).
     */
    private void applyPaymentOutcome(HttpServletRequest request, HttpSession session, String sbdParam,
            ProcedureActionOutcome outcome, int examId) {
        if (outcome.getPaymentStatus() != ProcedureActionOutcome.PaymentStatus.SUCCESS) {
            return;
        }
        publishCandidateQueue(request, session, outcome.getQueue(), examId);
        addAuditLog(session, "INSERT on Payment", outcome.getPaymentAuditDetail(), outcome.getProfile().getId());
        if (outcome.isAuditAllocate()) {
            addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ phòng thi cho SBD " + sbdParam);
        }
    }

    /**
     * Hiển thị bàn bước 3 sau khi thu phí (cờ just-paid + optional mở in).
     * @param openPrint true → set openDossierPrint
     * @throws IOException lỗi forward
     */
    private void showPostPaymentDesk(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
            List<ExamRegistrationDTO> qList, boolean openPrint) throws IOException {
        try {
            session.setAttribute("lastSelectedSbd", sbdParam);
            session.setAttribute("callingSbd", sbdParam);
            session.setAttribute("procedureStep", "3");
            session.setAttribute("procedureJustPaidSbd", sbdParam);
            request.setAttribute("profile", profile);
            request.setAttribute("step", "3");
            request.setAttribute("hasValidPhoto", true);
            request.setAttribute("paymentJustCompleted", Boolean.TRUE);
            if (openPrint) {
                request.setAttribute("openDossierPrint", sbdParam);
            }
            ExamStaffPageBinder.bindProcedureFees(request, procedureService.resolveProcedureFees(profile));
            forwardDeskView(request, response, qList);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    /**
     * Lưu ảnh webcam (JSON response): service saveCapturedPhoto → status code + body.
     * @throws IOException lỗi ghi JSON
     */
    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<ExamRegistrationDTO> qList, String webRoot,
            int examId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);

        ProcedureActionOutcome outcome = procedureService.saveCapturedPhoto(
                webRoot, sbdParam, examId, request.getParameter("photoBase64"), qList).getData();
        if (outcome == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Lỗi lưu ảnh\"}");
            return;
        }

        switch (outcome.getPhotoStatus()) {
            case CANDIDATE_NOT_FOUND, INVALID_IMAGE -> {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"" + outcome.getMessage() + "\"}");
            }
            case SUCCESS -> {
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("procedureStep", "2");
                addAuditLog(session, "UPDATE on Person",
                        "Lưu ảnh chụp từ webcam thực tế SBD " + sbdParam);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"photoUrl\":\"" + outcome.getPhotoPath() + "\"}");
            }
            default -> {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = outcome.getMessage() != null ? outcome.getMessage().replace("\"", "'") : "Lỗi lưu ảnh";
                response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
            }
        }
    }

    /**
     * Forward desk view: refresh queue → bind calling → occupyDesk → candidatecall.jsp (deskMode).
     * <p>
     * Luồng: fees nếu thiếu → ensureExamId → refresh DB → publish → sync calling → occupy → sidebar → JSP.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi I/O
     */
    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> qList) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            ExamStaffPageBinder.bindProcedureFees(request, procedureService.resolveProcedureFees(profile));
        }
        String webRoot = request.getServletContext().getRealPath("/");
        List<ExamSummaryDTO> allExams = viewService.listAllExams();
        int examId = ExamStaffPageSupport.ensureExamId(request, httpSession, allExams, viewService);
        int boardExamId = ExamStaffPageSupport.resolveExamId(request, httpSession, allExams, 0, viewService);
        if (boardExamId <= 0) {
            boardExamId = examId;
        }

        // Đồng bộ queue + trạng thái gọi trước khi mở bàn
        qList = refreshQueueFromDb(httpSession, webRoot, examId, allExams);
        publishCandidateQueue(request, httpSession, qList, examId);
        bindCandidateCallPageAttributes(request, httpSession, examId, qList);
        boolean shiftEnded = isShiftEnded(httpSession);
        syncCallingSbd(httpSession, boardExamId, qList, shiftEnded);
        if (profile != null && profile.getSbd() != null && !profile.getSbd().isBlank()) {
            staffCall.occupyDesk(ExamStaffHttpSupport.callBoardDao(request.getServletContext()),
                    boardExamId, profile.getSbd(), qList, shiftEnded);
        }
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute("callingSbd");
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        ExamStaffPageSupport.bindSidebarIfNeeded(request, httpSession, viewService);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    /**
     * Sync selection + refresh queue từ DB; cập nhật lastLoadedExamId.
     * @return queue sau refresh
     */
    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examId,
            List<ExamSummaryDTO> allExams) {
        ExamStaffPageSupport.syncExamSelection(session, allExams, examId, viewService);
        List<ExamRegistrationDTO> qList = refreshCandidateQueue(session, examId, webRoot, allExams);
        session.setAttribute("lastLoadedExamId",
                viewService.resolvePrimaryExamId(allExams, examId));
        return qList;
    }

    /**
     * SBD từ param {@code sbd}; fallback {@code callingSbd} session.
     * @return SBD hoặc null
     */
    private String resolveSbdParam(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

    /**
     * Theo dõi đổi SBD: cập nhật lastSelectedSbd/callingSbd; trả true nếu đổi thí sinh.
     * @return true nếu SBD khác lần trước (reset bước wizard)
     */
    private boolean trackSbdChange(HttpSession session, String sbdParam) {
        boolean sbdChanged = false;
        String prevSbd = (String) session.getAttribute("lastSelectedSbd");
        if (sbdParam != null && !sbdParam.trim().isEmpty()) {
            if (prevSbd == null || !prevSbd.equals(sbdParam)) {
                sbdChanged = true;
                session.setAttribute("lastSelectedSbd", sbdParam);
                session.setAttribute("callingSbd", sbdParam);
            }
        } else {
            session.setAttribute("lastSelectedSbd", null);
        }
        return sbdChanged;
    }

    /** Alias {@link #resolveSbdParam} (nhánh POST saveCapturedPhoto). */
    private String resolveSbd(HttpServletRequest request, HttpSession session) {
        return resolveSbdParam(request, session);
    }

    /**
     * Kết thúc bàn hiện tại: reset procedure step → refresh queue → gọi SBD kế → releaseDeskAndCall.
     * @param finishedSbd SBD vừa xong thủ tục
     */
    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> qList,
            String webRoot, int examId, List<ExamSummaryDTO> allExams, String finishedSbd) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");

        qList = refreshCandidateQueue(session, examId, webRoot, allExams);
        int boardExamId = viewService.resolvePrimaryExamId(allExams, examId);
        publishCandidateQueue(null, session, qList, examId);
        ExamStaffPageSupport.syncExamSelection(session, allExams, examId, viewService);
        session.setAttribute("lastLoadedExamId", boardExamId);

        String nextSbd = viewService.resolveNextCallingSbd(qList, finishedSbd);
        session.setAttribute("callingSbd", nextSbd);
        staffCall.releaseDeskAndCall(ExamStaffHttpSupport.callBoardDao(getServletContext()),
                boardExamId, nextSbd, qList, false);
    }

    /** Ghi audit UI feed + DB với recordId = 0. */
    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    /**
     * Ghi audit UI feed session + AuditService (userId từ session).
     * @param recordId id bản ghi liên quan (0 nếu không có)
     */
    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        ExamStaffPageBinder.appendExamAuditFeed(session, action, details);
        auditService.logAction(SessionUserHelper.resolveUserId(session), action, details, recordId);
    }

    /**
     * Refresh queue theo selectedExamId session (fallback examId).
     * @return full queue
     */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<ExamSummaryDTO> allExams) {
        int selectedExamId = 0;
        if (session != null) {
            Integer picked = (Integer) session.getAttribute("selectedExamId");
            if (picked != null && picked > 0) {
                selectedExamId = picked;
            }
        }
        if (selectedExamId <= 0) {
            selectedExamId = examId;
        }
        return refreshCandidateQueue(session, examId, selectedExamId, webRoot, allExams);
    }

    /**
     * Refresh queue theo queueExamId cụ thể rồi publish vào session.
     * @param queueExamId mã kỳ nạp queue
     * @return full queue (có thể null từ snapshot)
     */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int queueExamId,
            String webRoot, List<ExamSummaryDTO> allExams) {
        if (session == null) {
            return List.of();
        }
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setExamId(queueExamId > 0 ? queueExamId : examId);
        input.setWebRoot(webRoot);
        input.setAllExams(allExams);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = viewService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    /**
     * Publish snapshot queue + currentExam lên request/session.
     */
    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId) {
        CandidateQueueSnapshotDTO snapshot = viewService.buildQueueSnapshot(qList, examId, examId);
        ExamSummaryDTO current = viewService.findExamById(examId, viewService.listAllExams());
        if (current == null && examId > 0) {
            current = viewService.representativeExam(viewService.listAllExams(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    /**
     * Bind calling/suspended/currentExam cho desk (dùng lại binder candidate-call).
     */
    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> qList) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, qList);
        int resolvedExamId = ExamStaffPageSupport.resolveExamId(request, session, null, 0, viewService);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        ExamSummaryDTO current = viewService.findExamById(resolvedExamId, viewService.listAllExams());
        if (current == null && examId > 0) {
            current = viewService.representativeExam(viewService.listAllExams(), examId);
        }
        int suspendedCount = viewService.listSuspendedInExam(qList).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    /**
     * Resolve thí sinh đang gọi; đồng bộ callingSbd session nếu lệch/null.
     * @return DTO hoặc null
     */
    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> qList) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = staffCall.resolveCallingCandidate(callingSbd, qList);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    /**
     * Đồng bộ callingSbd session ↔ CallBoard rồi syncBoard.
     */
    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> qList,
            boolean shiftEnded) {
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        String httpCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        CallBoardState callBoard = staffCall.getBoardState(dao, boardExamId);
        String callingSbd = staffCall.resolveSyncedCallingSbd(httpCalling, callBoard, qList);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        staffCall.syncBoard(dao, boardExamId, callingSbd, qList, shiftEnded);
    }

    /** true nếu session có {@code shiftEnded=true}. */
    private static boolean isShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }
}
