package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.CandidateQueueHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;
import examstaff.dto.view.CallBoardState;
import examstaff.enums.ExamStatus;
import examstaff.enums.ExamStaffMessage;
import examstaff.service.CandidateQueueService;
import examstaff.service.CandidatePhotoService;
import examstaff.service.ExamControlService;
import examstaff.service.ExamStaffServices;
import examstaff.service.ProcedureFeeQueryService;
import examstaff.service.ProcedureWorkflowService;
import examstaff.util.Utf8EncodingHelper;
import examstaff.util.ProcedureStepHelper;

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
 * Bàn thủ tục thí sinh (desk): điều phối HTTP/session ↔ ProcedureWorkflow ↔ CallBoard/queue.
 * <p>
 * <b>Action → handler (slide defense)</b>
 * <table>
 *   <tr><th>Trigger</th><th>Handler</th><th>Kết quả</th></tr>
 *   <tr><td>{@code action=startShift}</td><td>{@link #handleStartShift}</td><td>Resume ca, redirect candidatecall</td></tr>
 *   <tr><td>{@code action=nextCandidate}</td><td>{@link #handleNextCandidate}</td><td>Chuyển SBD tiếp, redirect</td></tr>
 *   <tr><td>{@code action=resetProcedure}</td><td>{@link #handleResetProcedure}</td><td>Reset hồ sơ, redirect (nếu OK)</td></tr>
 *   <tr><td>{@code action=saveProfile}</td><td>{@link #handleSaveProfileAction}</td><td>Lưu lý lịch → desk mặc định</td></tr>
 *   <tr><td>{@code action=recapture}</td><td>{@link #handleRecapture}</td><td>Yêu cầu chụp lại → desk mặc định</td></tr>
 *   <tr><td>{@code action=saveCapturedPhoto}</td><td>{@link #handleSaveCapturedPhoto}</td><td>JSON lưu ảnh webcam</td></tr>
 *   <tr><td>{@code action=confirmPayment}</td><td rowspan="2">{@link #handlePayment}</td><td>confirmPayment → {@link #processPayment}</td></tr>
 *   <tr><td>{@code paymentSuccess=true}</td><td>paymentSuccess → nhánh callback sau redirect</td></tr>
 *   <tr><td>(không action / fall-through)</td><td>{@link #showDeskDefault}</td><td>Bind step + forward candidatecall.jsp (deskMode)</td></tr>
 * </table>
 */
@WebServlet("/views/staff/examstaff/procedure")
public class ProcedureServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ProcedureWorkflowService procedureWorkflow = SERVICES.procedures();
    private final CandidatePhotoService photoService = SERVICES.photos();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final ProcedureFeeQueryService procedureFeeService = SERVICES.procedureFees();
    private final ExamControlService examControlService = SERVICES.examControl();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    /** Trạng thái desk sau bước prepare; các handler có thể cập nhật trước khi {@link #showDeskDefault}. */
    private static final class DeskContext {
        String webRoot;
        int examId;
        List<ExamSummaryDTO> allExams;
        List<ExamRegistrationDTO> candidateQueue;
        String requestedSbd;
        boolean sbdChanged;
        ExamRegistrationDTO profile;
        boolean hasValidPhoto;
        String procedureStep;
        boolean examMutationsLocked;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if ("startShift".equals(request.getParameter("action"))) {
            handleStartShift(request, response, session);
            return;
        }

        handleDeskRequest(request, response, session);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String procedureAction = request.getParameter("action");
        if ("saveCapturedPhoto".equals(procedureAction)) {
            HttpSession session = request.getSession();
            String webRoot = request.getServletContext().getRealPath("/");
            List<ExamSummaryDTO> allExams = selectionFacade.loadAllExams();
            int examId = selectionFacade.ensureExamId(request, session, allExams);
            List<ExamRegistrationDTO> candidateQueue = refreshCandidateQueue(session, examId, webRoot, allExams);
            String requestedSbd = resolveRequestedSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, requestedSbd, candidateQueue, webRoot, examId);
            return;
        }
        handleDeskRequest(request, response, request.getSession());
    }

    /** Luồng desk chung cho GET và POST (trừ startShift / saveCapturedPhoto POST). */
    private void handleDeskRequest(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException {

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        DeskContext deskContext = prepareDeskContext(request, session);

        if (dispatchProcedureAction(request, response, session, deskContext)) {
            return;
        }

        showDeskDefault(request, response, session, deskContext);
    }

    /** {@code action=startShift}: resume ca nếu kỳ đang pause, bật lại shift trên board. */
    private void handleStartShift(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws IOException {
        List<ExamSummaryDTO> bootstrapExams = selectionFacade.loadAllExams();
        int boardExamId = selectionFacade.resolveExamId(request, session, bootstrapExams, 0);
        ExamSummaryDTO currentExam = selectionFacade.findExamById(bootstrapExams, boardExamId);
        if (currentExam != null && examstaff.enums.ExamStatus.isPaused(currentExam.getStatus())) {
            ExamControlService.ResumeResult resume = examControlService.resumeExam(boardExamId);
            if (!resume.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
                return;
            }
        }
        if (currentExam != null && ExamStatus.isLockedForStaffMutation(currentExam.getStatus())) {
            session.setAttribute(ExamStaffSessionKeys.EXAM_CONTROL_ERROR,
                    ExamStaffMessage.EXAM_MUTATIONS_LOCKED.getText());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }
        session.removeAttribute(ExamStaffSessionKeys.SHIFT_ENDED);
        session.removeAttribute(ExamStaffSessionKeys.SHIFT_PAUSED);
        callBoardHttp.resumeShift(getServletContext(), boardExamId);
        response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
    }

    /** Chuẩn bị page context, profile, queue và bước thủ tục trước khi dispatch action. */
    private DeskContext prepareDeskContext(HttpServletRequest request, HttpSession session) {
        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, webRoot);
        int examId = pageCtx.getExamId();
        List<ExamSummaryDTO> allExams = pageCtx.getAllExams();
        List<ExamRegistrationDTO> candidateQueue = pageCtx.getCandidates();

        String requestedSbd = resolveRequestedSbd(request, session);
        boolean sbdChanged = trackSbdChange(session, requestedSbd);

        ExamRegistrationDTO profile = procedureWorkflow.findProfile(webRoot, examId, examId, requestedSbd, candidateQueue);
        ProcedureProfilePrepareResultDTO prepared = procedureWorkflow.prepareProfileForDesk(
                webRoot, examId, examId, profile, candidateQueue);
        profile = prepared.getProfile();
        if (prepared.getPhotoStaleMessage() != null) {
            request.setAttribute("photoStaleMsg", prepared.getPhotoStaleMessage());
        }
        publishCandidateQueue(request, session, candidateQueue, examId);

        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        String procedureStep = ProcedureStepHelper.resolveStep(
                request.getParameter("step"), sbdChanged, profile, hasValidPhoto);

        if ("3".equals(procedureStep) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            request.setAttribute("photoRequiredMsg", ProcedureStepHelper.photoRequiredForStep3Message());
        }

        DeskContext ctx = new DeskContext();
        ctx.webRoot = webRoot;
        ctx.examId = examId;
        ctx.allExams = allExams;
        ctx.candidateQueue = candidateQueue;
        ctx.requestedSbd = requestedSbd;
        ctx.sbdChanged = sbdChanged;
        ctx.profile = profile;
        ctx.hasValidPhoto = hasValidPhoto;
        ctx.procedureStep = procedureStep;
        ExamSummaryDTO currentExam = selectionFacade.findExamById(allExams, examId);
        ctx.examMutationsLocked = currentExam != null
                && ExamStatus.isLockedForStaffMutation(currentExam.getStatus());
        return ctx;
    }

    /**
     * Dispatch theo {@code action} / {@code paymentSuccess}; trả {@code true} nếu đã xử lý xong (redirect/response).
     */
    private boolean dispatchProcedureAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, DeskContext ctx) throws ServletException, IOException {

        String procedureAction = request.getParameter("action");

        if (ctx.examMutationsLocked && isProcedureMutationAction(procedureAction, request)) {
            session.setAttribute(ExamStaffSessionKeys.EXAM_CONTROL_ERROR,
                    ExamStaffMessage.EXAM_MUTATIONS_LOCKED.getText());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return true;
        }

        if ("nextCandidate".equals(procedureAction)) {
            handleNextCandidate(request, response, session, ctx);
            return true;
        }

        if ("resetProcedure".equals(procedureAction)) {
            return handleResetProcedure(request, response, session, ctx);
        }

        if ("saveProfile".equals(procedureAction) && ctx.profile != null) {
            handleSaveProfileAction(request, session, ctx);
            return false;
        }

        if ("recapture".equals(procedureAction) && ctx.profile != null) {
            handleRecapture(request, session, ctx);
            return false;
        }

        if ("saveCapturedPhoto".equals(procedureAction)) {
            handleSaveCapturedPhoto(request, response, session, ctx.requestedSbd,
                    ctx.candidateQueue, ctx.webRoot, ctx.examId);
            return true;
        }

        if ("confirmPayment".equals(procedureAction) || "true".equals(request.getParameter("paymentSuccess"))) {
            if (ctx.profile != null) {
                handlePayment(request, response, session, ctx);
                return true;
            }
        }

        return false;
    }

    /** {@code action=nextCandidate}: kết thúc thí sinh hiện tại, gọi SBD tiếp theo. */
    private void handleNextCandidate(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, DeskContext ctx) throws IOException {
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID_SBD);
        String finishedSbd = ctx.requestedSbd;
        if (finishedSbd == null || finishedSbd.isBlank()) {
            finishedSbd = (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
        }
        advanceToNextCandidate(session, ctx.candidateQueue, ctx.webRoot, ctx.examId, ctx.allExams, finishedSbd);
        response.sendRedirect("candidatecall");
    }

    /** {@code action=resetProcedure}: xóa hồ sơ thủ tục; redirect khi thành công. */
    private boolean handleResetProcedure(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, DeskContext ctx) throws IOException {
        if (ctx.requestedSbd == null || ctx.requestedSbd.trim().isEmpty()) {
            return false;
        }
        ProcedureResetOutcomeDTO reset = procedureWorkflow.resetProcedure(
                ctx.requestedSbd.trim(), ctx.examId, ctx.webRoot);
        if (!reset.isSuccess()) {
            return false;
        }
        ctx.candidateQueue = reset.getQueue();
        candidateQueueService.moveCallableCandidateToFront(ctx.candidateQueue, reset.getSbd());
        ExamStaffPageBinder.syncCallQueueOrder(session, ctx.examId, ctx.candidateQueue);
        publishCandidateQueue(request, session, ctx.candidateQueue, ctx.examId);
        session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, reset.getSbd());
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_STEP);
        session.removeAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD);
        addAuditLog(session, "RESET Procedure",
                "Xóa hồ sơ thủ tục SBD " + reset.getSbd(), reset.getCandidateId());
        response.sendRedirect(request.getContextPath()
                + "/views/staff/examstaff/candidatecall?procedureReset="
                + java.net.URLEncoder.encode(reset.getSbd(), java.nio.charset.StandardCharsets.UTF_8));
        return true;
    }

    /** {@code action=saveProfile}: lưu lý lịch form, cập nhật ctx rồi fall-through desk mặc định. */
    private void handleSaveProfileAction(HttpServletRequest request, HttpSession session, DeskContext ctx) {
        ctx.profile = handleSaveProfile(request, session, ctx.profile, ctx.requestedSbd,
                ctx.candidateQueue, ctx.webRoot, ctx.examId);
        ctx.procedureStep = "2";
        ctx.hasValidPhoto = ctx.profile != null && ctx.profile.isValidCapturedPhoto();
    }

    /** {@code action=recapture}: reset ảnh, ép bước 2. */
    private void handleRecapture(HttpServletRequest request, HttpSession session, DeskContext ctx) {
        ctx.profile = procedureWorkflow.recapturePhoto(
                ctx.profile.getId(), ctx.webRoot, ctx.examId, ctx.requestedSbd, ctx.candidateQueue);
        ctx.hasValidPhoto = false;
        ctx.procedureStep = "2";
        publishCandidateQueue(request, session, ctx.candidateQueue, ctx.examId);
        session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "2");
        request.setAttribute("step", "2");
        request.setAttribute("hasValidPhoto", false);
        addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + ctx.requestedSbd);
    }

    /**
     * Thanh toán thống nhất: {@code confirmPayment} gọi {@link #processPayment};
     * {@code paymentSuccess=true} là callback sau redirect (nhánh riêng giữ hành vi cũ).
     */
    private void handlePayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, DeskContext ctx) throws IOException {
        if ("confirmPayment".equals(request.getParameter("action"))) {
            processPayment(request, response, session, ctx.profile, ctx.requestedSbd,
                    ctx.candidateQueue, ctx.webRoot, ctx.allExams, ctx.examId);
            return;
        }
        if ("true".equals(request.getParameter("paymentSuccess"))) {
            if (!ctx.profile.isValidCapturedPhoto()) {
                request.setAttribute("photoRequiredMsg", ProcedureStepHelper.paymentBlockedNoPhotoMessage());
                request.setAttribute("step", "2");
                session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", ctx.profile);
                try {
                    forwardDeskView(request, response, ctx.candidateQueue);
                } catch (ServletException e) {
                    throw new IOException(e);
                }
                return;
            }
            ProcedurePaymentOutcomeDTO outcome = procedureWorkflow.confirmPayment(
                    ctx.profile, ctx.requestedSbd, ctx.examId, ctx.webRoot, ctx.allExams);
            applyPaymentOutcome(request, session, ctx.requestedSbd, outcome, ctx.examId);
            if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
                selectionFacade.syncExamSelection(session, ctx.allExams, ctx.examId);
                showPostPaymentDesk(request, response, session, outcome.getProfile(), ctx.requestedSbd,
                        outcome.getQueue(), false);
            }
        }
    }

    /** Desk mặc định: bind profile/fees/photo, step session và forward JSP. */
    private void showDeskDefault(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, DeskContext ctx) throws ServletException, IOException {
        if (ctx.profile != null) {
            request.setAttribute("profile", ctx.profile);
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(ctx.profile));
            photoService.resolveCapturedPhoto(ctx.webRoot, ctx.profile);
            ctx.hasValidPhoto = ctx.profile.isValidCapturedPhoto();
        }

        session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, ctx.procedureStep);
        request.setAttribute("step", ctx.procedureStep);
        request.setAttribute("hasValidPhoto", ctx.hasValidPhoto);
        request.setAttribute("examMutationsLocked", ctx.examMutationsLocked);

        forwardDeskView(request, response, ctx.candidateQueue);
    }

    /**
     * Lưu lý lịch từ form; set {@code profileUpdatedAlert} và ghi audit khi thành công.
     */
    private ExamRegistrationDTO handleSaveProfile(HttpServletRequest request, HttpSession session,
            ExamRegistrationDTO profile, String requestedSbd, List<ExamRegistrationDTO> candidateQueue,
            String webRoot, int examId) {
        String fullName = request.getParameter("fullName");
        String dobStr = request.getParameter("dateOfBirth");
        String govIdNo = request.getParameter("govIdNo");
        String email = request.getParameter("email");
        String phoneNo = request.getParameter("phoneNo");

        try {
            Date sqlDob = parseDateOfBirth(dobStr);
            boolean updated = procedureWorkflow.saveProfile(
                    profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo);
            if (updated) {
                profile = procedureWorkflow.reloadProfile(webRoot, examId, profile.getId(), requestedSbd, candidateQueue);
                request.setAttribute("profileUpdatedAlert", "true");
                addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + requestedSbd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        publishCandidateQueue(request, session, candidateQueue, examId);
        return profile;
    }

    /** Parse ngày sinh dạng dd/MM/yyyy hoặc yyyy-MM-dd thành {@link Date} SQL. */
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
     * Xử lý confirmPayment: map outcome DTO sang redirect/desk view (thiếu ảnh / đã trả / lỗi / thành công).
     */
    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String requestedSbd,
            List<ExamRegistrationDTO> candidateQueue, String webRoot, List<ExamSummaryDTO> allExams, int examId)
            throws IOException {
        ProcedurePaymentOutcomeDTO outcome = procedureWorkflow.confirmPayment(
                profile, requestedSbd, examId, webRoot, allExams);

        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PROFILE_NOT_FOUND) {
            response.sendRedirect("candidatecall");
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.NO_PHOTO) {
            try {
                request.setAttribute("photoRequiredMsg", ProcedureStepHelper.paymentBlockedNoPhotoMessage());
                request.setAttribute("step", "2");
                session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", outcome.getProfile());
                forwardDeskView(request, response, candidateQueue);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.ALREADY_PAID) {
            boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
            showPostPaymentDesk(request, response, session, outcome.getProfile(), requestedSbd, candidateQueue, openPrint);
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PAYMENT_FAILED) {
            try {
                request.setAttribute("paymentErrorMsg", "Không ghi được thanh toán. Vui lòng thử lại.");
                request.setAttribute("step", "3");
                request.setAttribute("profile", outcome.getProfile());
                request.setAttribute("hasValidPhoto", outcome.getProfile().isValidCapturedPhoto());
                forwardDeskView(request, response, candidateQueue);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }

        applyPaymentOutcome(request, session, requestedSbd, outcome, examId);
        selectionFacade.syncExamSelection(session, allExams, examId);
        session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, outcome.getBoardExamId());

        boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
        showPostPaymentDesk(request, response, session, outcome.getProfile(), requestedSbd, outcome.getQueue(), openPrint);
    }

    /** Sau thanh toán thành công: publish queue + ghi audit payment/allocate. */
    private void applyPaymentOutcome(HttpServletRequest request, HttpSession session, String requestedSbd,
            ProcedurePaymentOutcomeDTO outcome, int examId) {
        if (outcome.getStatus() != ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
            return;
        }
        publishCandidateQueue(request, session, outcome.getQueue(), examId);
        addAuditLog(session, "INSERT on Payment", outcome.getPaymentAuditDetail(), outcome.getProfile().getId());
        if (outcome.isAuditAllocate()) {
            addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ phòng thi cho SBD " + requestedSbd);
        }
    }

    /**
     * Hiển thị desk bước 3 sau thanh toán; set {@code paymentJustCompleted} / {@code openDossierPrint}.
     */
    private void showPostPaymentDesk(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String requestedSbd,
            List<ExamRegistrationDTO> candidateQueue, boolean openPrint) throws IOException {
        try {
            session.setAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD, requestedSbd);
            session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, requestedSbd);
            session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "3");
            session.setAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID_SBD, requestedSbd);
            request.setAttribute("profile", profile);
            request.setAttribute("step", "3");
            request.setAttribute("hasValidPhoto", true);
            request.setAttribute("paymentJustCompleted", Boolean.TRUE);
            if (openPrint) {
                request.setAttribute("openDossierPrint", requestedSbd);
            }
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
            forwardDeskView(request, response, candidateQueue);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    /** Lưu ảnh webcam (JSON response); cập nhật queue/session step khi SUCCESS. */
    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String requestedSbd, List<ExamRegistrationDTO> candidateQueue, String webRoot,
            int examId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);

        ProcedurePhotoSaveOutcomeDTO outcome = procedureWorkflow.saveCapturedPhoto(
                webRoot, requestedSbd, examId, request.getParameter("photoBase64"), candidateQueue);

        switch (outcome.getStatus()) {
            case CANDIDATE_NOT_FOUND, INVALID_IMAGE -> {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"" + outcome.getMessage() + "\"}");
            }
            case SUCCESS -> {
                publishCandidateQueue(request, session, candidateQueue, examId);
                session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "2");
                addAuditLog(session, "UPDATE on Person",
                        "Lưu ảnh chụp từ webcam thực tế SBD " + requestedSbd);
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
     * Forward deskMode lên candidatecall.jsp: refresh queue, sync board/occupy desk, bind call page attrs.
     */
    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> candidateQueue) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
        }
        String webRoot = request.getServletContext().getRealPath("/");
        List<ExamSummaryDTO> allExams = selectionFacade.loadAllExams();
        int examId = selectionFacade.ensureExamId(request, httpSession, allExams);
        int boardExamId = selectionFacade.resolveExamId(request, httpSession, allExams, 0);
        if (boardExamId <= 0) {
            boardExamId = examId;
        }

        candidateQueue = refreshQueueFromDb(httpSession, webRoot, examId, allExams);
        publishCandidateQueue(request, httpSession, candidateQueue, examId);
        bindCandidateCallPageAttributes(request, httpSession, examId, candidateQueue);
        boolean shiftEnded = isShiftEnded(httpSession);
        syncCallingSbd(httpSession, boardExamId, candidateQueue, shiftEnded);
        if (profile != null && profile.getSbd() != null && !profile.getSbd().isBlank()) {
            callBoardHttp.occupyDesk(request.getServletContext(), boardExamId, profile.getSbd(), candidateQueue, shiftEnded);
        }
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        selectionFacade.bindSidebarIfNeeded(request, httpSession);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    /** Đồng bộ chọn kỳ rồi refresh queue từ DB. */
    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examId,
            List<ExamSummaryDTO> allExams) {
        selectionFacade.syncExamSelection(session, allExams, examId);
        List<ExamRegistrationDTO> candidateQueue = refreshCandidateQueue(session, examId, webRoot, allExams);
        session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID,
                selectionFacade.resolvePrimaryExamId(allExams, examId));
        return candidateQueue;
    }

    /** SBD từ query {@code sbd} hoặc fallback session {@link ExamStaffSessionKeys#CALLING_SBD}. */
    private String resolveRequestedSbd(HttpServletRequest request, HttpSession session) {
        String requestedSbd = request.getParameter("sbd");
        if (requestedSbd == null || requestedSbd.trim().isEmpty()) {
            requestedSbd = (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
        }
        return requestedSbd;
    }

    /**
     * Theo dõi đổi SBD trên session ({@link ExamStaffSessionKeys#LAST_SELECTED_SBD}/
     * {@link ExamStaffSessionKeys#CALLING_SBD}); trả true nếu vừa đổi.
     */
    private boolean trackSbdChange(HttpSession session, String requestedSbd) {
        boolean sbdChanged = false;
        String prevSbd = (String) session.getAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD);
        if (requestedSbd != null && !requestedSbd.trim().isEmpty()) {
            if (prevSbd == null || !prevSbd.equals(requestedSbd)) {
                sbdChanged = true;
                session.setAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD, requestedSbd);
                session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, requestedSbd);
            }
        } else {
            session.setAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD, null);
        }
        return sbdChanged;
    }

    /**
     * Sau nextCandidate: reset procedure state, chọn SBD tiếp theo và releaseDesk/call trên board.
     */
    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> candidateQueue,
            String webRoot, int examId, List<ExamSummaryDTO> allExams, String finishedSbd) {
        session.setAttribute(ExamStaffSessionKeys.LAST_SELECTED_SBD, null);
        session.setAttribute(ExamStaffSessionKeys.PROCEDURE_STEP, "1");
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID);
        session.removeAttribute(ExamStaffSessionKeys.PROCEDURE_JUST_PAID_SBD);

        candidateQueue = refreshCandidateQueue(session, examId, webRoot, allExams);
        int boardExamId = selectionFacade.resolvePrimaryExamId(allExams, examId);
        publishCandidateQueue(null, session, candidateQueue, examId);
        selectionFacade.syncExamSelection(session, allExams, examId);
        session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, boardExamId);

        String nextSbd = candidateQueueService.resolveNextCallingSbd(candidateQueue, finishedSbd);
        session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, nextSbd);
        callBoardHttp.releaseDeskAndCall(getServletContext(), boardExamId, nextSbd, candidateQueue, false);
    }

    /** Ghi audit kèm session feed (recordId = 0). */
    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    /** Ghi audit kèm session feed qua {@link StaffAuditLogSupport}. */
    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        auditLogSupport.persistWithSessionFeed(session, action, details, recordId);
    }

    /** Refresh queue theo {@link ExamStaffSessionKeys#SELECTED_EXAM_ID} trên session (fallback examId). */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<ExamSummaryDTO> allExams) {
        int selectedExamId = 0;
        if (session != null) {
            Integer picked = (Integer) session.getAttribute(ExamStaffSessionKeys.SELECTED_EXAM_ID);
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
     * Refresh queue theo queueExamId; publish snapshot vào session qua binder.
     */
    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int queueExamId,
            String webRoot, List<ExamSummaryDTO> allExams) {
        return CandidateQueueHttpSupport.refreshAndPublish(null, session, candidateQueueService,
                examId, queueExamId, webRoot, allExams);
    }

    /** Publish full/active/procedure-done queue lên request + session. */
    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> candidateQueue, int examId) {
        CandidateQueueHttpSupport.publishLists(request, session, candidateQueueService,
                selectionFacade, candidateQueue, examId);
    }

    /** Bind thuộc tính trang gọi (calling candidate, exam chip, số đình chỉ). */
    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> candidateQueue) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, candidateQueue);
        int resolvedExamId = selectionFacade.resolveExamId(request, session, null, 0);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        ExamSummaryDTO current = selectionFacade.findExamById(
                selectionFacade.loadAllExams(), resolvedExamId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeExam(
                    selectionFacade.loadAllExams(), examId);
        }
        int suspendedCount = candidateQueueService.listSuspendedInExam(candidateQueue).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    /** Resolve thí sinh đang gọi từ session SBD; sửa/xóa session nếu SBD lệch hoặc không còn. */
    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> candidateQueue) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD);
        ExamRegistrationDTO calling = candidateQueueService.resolveCallingCandidate(callingSbd, candidateQueue);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
        }
        return calling;
    }

    /** Đồng bộ callingSbd session với CallBoard rồi publish state lên board. */
    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> candidateQueue, boolean shiftEnded) {
        String httpCalling = session != null ? (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD) : null;
        CallBoardState callBoard = callBoardHttp.getState(getServletContext(), boardExamId);
        String callingSbd = candidateQueueService.resolveSyncedCallingSbd(httpCalling, callBoard, candidateQueue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, callingSbd);
            } else {
                session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
            }
        }
        callBoardHttp.sync(getServletContext(), boardExamId, callingSbd, candidateQueue, shiftEnded);
    }

    /** Đọc flag session {@link ExamStaffSessionKeys#SHIFT_ENDED}. */
    private static boolean isShiftEnded(HttpSession session) {
        return session != null && ExamStaffSessionKeys.FLAG_TRUE.equals(session.getAttribute(ExamStaffSessionKeys.SHIFT_ENDED));
    }

    /** Action sửa hồ sơ / ảnh / phí / reset khi kỳ đã kết thúc. */
    private static boolean isProcedureMutationAction(String action, HttpServletRequest request) {
        if ("true".equals(request.getParameter("paymentSuccess"))) {
            return true;
        }
        if (action == null || action.isBlank()) {
            return false;
        }
        return switch (action) {
            case "resetProcedure", "saveProfile", "recapture", "saveCapturedPhoto",
                    "confirmPayment", "nextCandidate" -> true;
            default -> false;
        };
    }
}
