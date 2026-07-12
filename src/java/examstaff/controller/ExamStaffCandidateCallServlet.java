package examstaff.controller;

import examstaff.util.CallBoardHttpSupport;
import examstaff.util.ExamStaffHttpSupport;
import examstaff.util.StaffAuditLogSupport;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateDossierViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.CallBoardSyncServiceImpl;
import examstaff.service.impl.CandidateCallPageServiceImpl;
import examstaff.service.impl.CandidateCallingServiceImpl;
import examstaff.service.impl.CandidateDossierServiceImpl;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ProcedureFeeQueryServiceImpl;
import examstaff.service.impl.ProcedureWorkflowServiceImpl;
import examstaff.service.impl.StaffAuditLogServiceImpl;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.ExamStaffPageSupport.PageContext;
import examstaff.util.ProcedureStepHelper;
import examstaff.util.SessionUserHelper;
import examstaff.util.Utf8EncodingHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/views/staff/examstaff/candidatecall",
        "/views/staff/examstaff/procedure",
        "/views/staff/examstaff/candidate-dossier"
})
public class ExamStaffCandidateCallServlet extends HttpServlet {

    private final CandidateCallPageServiceImpl pageService = new CandidateCallPageServiceImpl();
    private final CandidateCallingServiceImpl callingService = new CandidateCallingServiceImpl();
    private final CandidateQueueServiceImpl candidateQueueService = new CandidateQueueServiceImpl();
    private final ProcedureWorkflowServiceImpl procedureWorkflow = new ProcedureWorkflowServiceImpl();
    private final ProcedureFeeQueryServiceImpl procedureFeeService = new ProcedureFeeQueryServiceImpl();
    private final CandidateDossierServiceImpl dossierService = new CandidateDossierServiceImpl();
    private final CallBoardHttpSupport callBoardHttp = new CallBoardHttpSupport(new CallBoardSyncServiceImpl());
    private final StaffAuditLogSupport auditLogSupport = new StaffAuditLogSupport(new StaffAuditLogServiceImpl());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.endsWith("candidate-dossier")) {
            handleDossier(request, response);
            return;
        }
        if (servletPath != null && servletPath.endsWith("procedure")) {
            handleProcedure(request, response);
            return;
        }
        handleCandidateCall(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.endsWith("procedure")) {
            String action = request.getParameter("action");
            if ("saveCapturedPhoto".equals(action)) {
                handleSaveCapturedPhoto(request, response);
                return;
            }
        }
        doGet(request, response);
    }

    private void handleCandidateCall(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if ("desk".equals(request.getParameter("view"))) {
            String deskSbd = request.getParameter("sbd");
            if (deskSbd == null || deskSbd.isBlank()) {
                deskSbd = (String) session.getAttribute("callingSbd");
            }
            if (deskSbd != null && !deskSbd.isBlank()) {
                response.sendRedirect("procedure?sbd=" + deskSbd);
            } else {
                response.sendRedirect("procedure");
            }
            return;
        }

        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, true);

        CandidateCallPageCommand command = buildCommand(request, session, pageCtx, webRoot);
        CandidateCallPageViewDTO view = pageService.preparePage(command);

        if (view.isResumeShift()) {
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            callBoardHttp.resumeShift(getServletContext(), pageCtx.getExamId());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }
        if (view.getRedirectPath() != null) {
            response.sendRedirect(request.getContextPath() + view.getRedirectPath());
            return;
        }

        applySessionSideEffects(session, view);
        applyBoardOp(pageCtx.getExamId(), view);
        bindCandidateCallPageAttributes(request, session, view.getPublishExamId(), view.getFullQueue());
        publishCandidateQueue(request, session, view.getFullQueue(), view.getPublishExamId());
        bindActionAlert(request, view);

        if (view.isShowSuspended()) {
            request.setAttribute("suspendedList", view.getSuspendedList());
            request.getRequestDispatcher("/views/staff/examstaff/candidate-suspended.jsp")
                    .forward(request, response);
            return;
        }

        request.setAttribute("nextCallingCandidate", view.getNextCallingCandidate());
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private void handleProcedure(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        if ("startShift".equals(request.getParameter("action"))) {
            int boardExamId = ExamStaffPageSupport.ensureExamId(request, session,
                    ExamStaffPageSupport.loadAllExams());
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            callBoardHttp.resumeShift(getServletContext(), boardExamId);
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, true);
        int examId = pageCtx.getExamId();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

        String sbdParam = resolveSbdParam(request, session);
        boolean sbdChanged = trackSbdChange(session, sbdParam);

        ExamRegistrationDTO profile = procedureWorkflow.findProfile(webRoot, examId, examId, sbdParam, qList);
        ProcedureProfilePrepareResultDTO prepared = procedureWorkflow.prepareProfileForDesk(
                webRoot, examId, examId, profile, qList);
        profile = prepared.getProfile();
        if (prepared.getPhotoStaleMessage() != null) {
            request.setAttribute("photoStaleMsg", prepared.getPhotoStaleMessage());
        }
        publishCandidateQueue(request, session, qList, examId);

        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        String stepParam = ProcedureStepHelper.resolveStep(request.getParameter("step"), sbdChanged, profile,
                hasValidPhoto);

        if ("3".equals(stepParam) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            request.setAttribute("photoRequiredMsg", ProcedureStepHelper.photoRequiredForStep3Message());
        }

        String pAction = request.getParameter("action");

        if ("nextCandidate".equals(pAction)) {
            session.removeAttribute("procedureJustPaidSbd");
            String finishedSbd = sbdParam;
            if (finishedSbd == null || finishedSbd.isBlank()) {
                finishedSbd = (String) session.getAttribute("callingSbd");
            }
            advanceToNextCandidate(session, qList, webRoot, examId, pageCtx.getAllSessions(), finishedSbd);
            response.sendRedirect("candidatecall");
            return;
        }

        if ("resetProcedure".equals(pAction) && sbdParam != null && !sbdParam.isBlank()) {
            ProcedureResetOutcomeDTO reset = procedureWorkflow.resetProcedure(sbdParam.trim(), examId, webRoot);
            if (reset.isSuccess()) {
                qList = reset.getQueue();
                candidateQueueService.moveCallableCandidateToFront(qList, reset.getSbd());
                ExamStaffPageSupport.syncCallQueueOrder(session, examId, qList);
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("callingSbd", reset.getSbd());
                session.removeAttribute("procedureStep");
                session.removeAttribute("lastSelectedSbd");
                auditLogSupport.persistWithSessionFeed(session, "RESET Procedure",
                        "Xóa hồ sơ thủ tục SBD " + reset.getSbd(), reset.getCandidateId());
                response.sendRedirect(request.getContextPath()
                        + "/views/staff/examstaff/candidatecall?procedureReset="
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
            profile = procedureWorkflow.recapturePhoto(profile.getId(), webRoot, examId, sbdParam, qList);
            stepParam = "2";
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            auditLogSupport.persistWithSessionFeed(session, "UPDATE on Person",
                    "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot, pageCtx.getAllSessions(),
                    examId);
            return;
        }

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
            ProcedurePaymentOutcomeDTO outcome = procedureWorkflow.confirmPayment(
                    profile, sbdParam, examId, webRoot, pageCtx.getAllSessions());
            applyPaymentOutcome(request, session, sbdParam, outcome, examId);
            if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
                ExamStaffPageSupport.syncExamSelection(session, pageCtx.getAllSessions(), examId);
                showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(),
                        false);
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
            ExamStaffPageSupport.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
            hasValidPhoto = profile.isValidCapturedPhoto();
        }

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);
        forwardDeskView(request, response, qList);
    }

    private void handleDossier(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.isBlank()) {
            response.sendRedirect("candidatecall");
            return;
        }

        int examId = ExamStaffPageSupport.ensureExamId(request, request.getSession(),
                ExamStaffPageSupport.loadAllExams());
        CandidateDossierViewDTO view = dossierService.loadDossier(
                examId, sbd, request.getServletContext().getRealPath("/"));
        if (view.getProfile() == null) {
            response.sendRedirect("candidatecall");
            return;
        }

        boolean autoPrint = "true".equalsIgnoreCase(request.getParameter("print"));
        bindDossierView(request, view, autoPrint);
        request.getRequestDispatcher("/views/staff/examstaff/candidate-dossier.jsp").forward(request, response);
    }

    private void bindDossierView(HttpServletRequest request, CandidateDossierViewDTO view, boolean autoPrint) {
        if (view == null || view.getProfile() == null) {
            return;
        }
        request.setAttribute("profile", view.getProfile());
        request.setAttribute("examSession", view.getExamSession());
        request.setAttribute("hasPhotoFile", view.isHasPhotoFile());
        request.setAttribute("payment", null);
        if (view.getFees() != null) {
            ExamStaffPageSupport.bindProcedureFees(request, view.getFees());
        }
        request.setAttribute("dossierTitle", view.getDossierTitle());
        request.setAttribute("dossierSubtitle", view.getDossierSubtitle());
        request.setAttribute("autoPrint", autoPrint);
    }

    private CandidateCallPageCommand buildCommand(HttpServletRequest request, HttpSession session,
            PageContext pageCtx, String webRoot) {
        CandidateCallPageCommand command = new CandidateCallPageCommand();
        command.setAction(request.getParameter("action"));
        command.setSbd(request.getParameter("sbd"));
        command.setView(request.getParameter("view"));
        command.setReturnView(request.getParameter("returnView"));
        command.setExamId(pageCtx.getExamId());
        command.setBoardExamId(pageCtx.getExamId());
        command.setCalledByStaffId(SessionUserHelper.resolveUserId(session));
        command.setWebRoot(webRoot);
        command.setShiftEnded(isShiftEnded(session));
        command.setShiftPaused("true".equals(session.getAttribute("shiftPaused")));
        command.setCallingSbd((String) session.getAttribute("callingSbd"));
        command.setLastLoadedExamId((Integer) session.getAttribute("lastLoadedExamId"));
        @SuppressWarnings("unchecked")
        List<String> callQueueOrder = (List<String>) session.getAttribute("callQueueOrder");
        command.setCallQueueOrder(callQueueOrder);
        command.setCallQueueOrderExamId(ExamStaffPageSupport.readCallQueueOrderExamId(session));

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> permanentAbsents =
                (List<ExamRegistrationDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        command.setPermanentAbsents(permanentAbsents);

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> cached =
                (List<ExamRegistrationDTO>) session.getAttribute(Attributes.ExamStaff.CANDIDATE_QUEUE);
        command.setCachedQueue(cached);
        command.setBoard(callBoardHttp.getState(getServletContext(), pageCtx.getExamId()));
        return command;
    }

    private void applySessionSideEffects(HttpSession session, CandidateCallPageViewDTO view) {
        if (view.isClearCallingSbd()) {
            session.removeAttribute("callingSbd");
        } else if (view.getCallingSbd() != null) {
            session.setAttribute("callingSbd", view.getCallingSbd());
        }
        if (view.isShiftEnded()) {
            session.setAttribute("shiftEnded", "true");
        }
        if (view.isShiftPaused()) {
            session.setAttribute("shiftPaused", "true");
        }
        if (view.isClearProcedureJustPaidSbd()) {
            session.removeAttribute("procedureJustPaidSbd");
        }
        if (view.isPersistQueueOrder()) {
            ExamStaffPageSupport.syncCallQueueOrder(session, view.getPublishExamId(), view.getFullQueue());
        }
    }

    private void applyBoardOp(int boardExamId, CandidateCallPageViewDTO view) {
        if (view.isReleaseDesk()) {
            callBoardHttp.releaseDeskAndCall(
                    getServletContext(), boardExamId, view.getReleaseDeskCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
        if (view.isSyncBoard()) {
            callBoardHttp.sync(
                    getServletContext(), boardExamId, view.getBoardCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
        if (view.isPauseBoard()) {
            callBoardHttp.pauseShift(getServletContext(), boardExamId, view.getFullQueue());
        }
    }

    private void bindCandidateCallPageAttributes(HttpServletRequest request, HttpSession session,
            int examId, List<ExamRegistrationDTO> queue) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, queue);
        int suspendedCount = candidateQueueService.listSuspendedInSession(queue).size();
        var current = ExamStaffPageSupport.findExamById(ExamStaffPageSupport.loadAllExams(), examId);
        if (current == null && examId > 0) {
            current = ExamStaffPageSupport.representativeSessionForExam(
                    ExamStaffPageSupport.loadAllExams(), examId);
        }
        ExamStaffPageSupport.bindCandidateCallPage(request, examId, calling, examId, suspendedCount, current);
    }

    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> queue) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = callingService.resolveCallingCandidate(callingSbd, queue);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> queue, int examId) {
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(queue, examId, examId);
        var current = ExamStaffPageSupport.findExamById(ExamStaffPageSupport.loadAllExams(), examId);
        if (current == null && examId > 0) {
            current = ExamStaffPageSupport.representativeSessionForExam(
                    ExamStaffPageSupport.loadAllExams(), examId);
        }
        ExamStaffPageSupport.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    private static void bindActionAlert(HttpServletRequest request, CandidateCallPageViewDTO view) {
        if (view.getAlertType() == CandidateCallActionResultDTO.AlertType.NONE
                || view.getAlertSbd() == null) {
            return;
        }
        switch (view.getAlertType()) {
            case AUTO_ABSENT -> request.setAttribute("autoAbsentAlert", view.getAlertSbd());
            case ABSENT -> request.setAttribute("absentAlert", view.getAlertSbd());
            case PERMANENT_ABSENT -> request.setAttribute("permanentAbsentAlert", view.getAlertSbd());
            case UNDO -> request.setAttribute("undoAlert", view.getAlertSbd());
            default -> {
            }
        }
    }

    private ExamRegistrationDTO handleSaveProfile(HttpServletRequest request, HttpSession session,
            ExamRegistrationDTO profile, String sbdParam, List<ExamRegistrationDTO> qList,
            String webRoot, int examId) {
        try {
            Date sqlDob = parseDateOfBirth(request.getParameter("dateOfBirth"));
            boolean updated = procedureWorkflow.saveProfile(
                    profile.getId(),
                    request.getParameter("fullName"),
                    sqlDob,
                    request.getParameter("govIdNo"),
                    request.getParameter("email"),
                    request.getParameter("phoneNo"));
            if (updated) {
                profile = procedureWorkflow.reloadProfile(webRoot, examId, profile.getId(), sbdParam, qList);
                request.setAttribute("profileUpdatedAlert", "true");
                auditLogSupport.persistWithSessionFeed(session, "UPDATE on Person",
                        "Sửa đổi lý lịch SBD " + sbdParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        publishCandidateQueue(request, session, qList, examId);
        return profile;
    }

    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
            List<ExamRegistrationDTO> qList, String webRoot, List<examstaff.dto.ExamSummaryDTO> allSessions,
            int examId) throws IOException {
        ProcedurePaymentOutcomeDTO outcome = procedureWorkflow.confirmPayment(
                profile, sbdParam, examId, webRoot, allSessions);

        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PROFILE_NOT_FOUND) {
            response.sendRedirect("candidatecall");
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.NO_PHOTO) {
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
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.ALREADY_PAID) {
            showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, qList, false);
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PAYMENT_FAILED) {
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
        ExamStaffPageSupport.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedExamId", outcome.getBoardExamId());

        boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
        showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), openPrint);
    }

    private void applyPaymentOutcome(HttpServletRequest request, HttpSession session, String sbdParam,
            ProcedurePaymentOutcomeDTO outcome, int examId) {
        if (outcome.getStatus() != ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
            return;
        }
        publishCandidateQueue(request, session, outcome.getQueue(), examId);
        auditLogSupport.persistWithSessionFeed(session, "INSERT on Payment",
                outcome.getPaymentAuditDetail(), outcome.getProfile().getId());
        if (outcome.isAuditAllocate()) {
            auditLogSupport.persistWithSessionFeed(session, "ALLOCATE Candidates",
                    "Tự động phân bổ phòng thi cho SBD " + sbdParam);
        }
    }

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
            ExamStaffPageSupport.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
            forwardDeskView(request, response, qList);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");
        int examId = ExamStaffPageSupport.ensureExamId(request, session, ExamStaffPageSupport.loadAllExams());
        List<ExamRegistrationDTO> qList = refreshCandidateQueue(session, examId, webRoot);
        String sbdParam = resolveSbdParam(request, session);

        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);

        ProcedurePhotoSaveOutcomeDTO outcome = procedureWorkflow.saveCapturedPhoto(
                webRoot, sbdParam, examId, request.getParameter("photoBase64"), qList);

        switch (outcome.getStatus()) {
            case CANDIDATE_NOT_FOUND, INVALID_IMAGE -> {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"" + outcome.getMessage() + "\"}");
            }
            case SUCCESS -> {
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("procedureStep", "2");
                auditLogSupport.persistWithSessionFeed(session, "UPDATE on Person",
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

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> qList) throws ServletException, IOException {

        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            ExamStaffPageSupport.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
        }
        String webRoot = request.getServletContext().getRealPath("/");
        int examId = ExamStaffPageSupport.ensureExamId(request, httpSession, ExamStaffPageSupport.loadAllExams());

        qList = refreshCandidateQueue(httpSession, examId, webRoot);
        publishCandidateQueue(request, httpSession, qList, examId);
        bindCandidateCallPageAttributes(request, httpSession, examId, qList);

        boolean shiftEnded = isShiftEnded(httpSession);
        syncCallingSbd(httpSession, examId, qList, shiftEnded);
        if (profile != null && profile.getSbd() != null && !profile.getSbd().isBlank()) {
            callBoardHttp.occupyDesk(getServletContext(), examId, profile.getSbd(), qList, shiftEnded);
        }
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute("callingSbd");
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        ExamStaffPageSupport.bindSidebarIfNeeded(request, httpSession);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot) {
        return refreshCandidateQueue(session, examId, webRoot, ExamStaffPageSupport.loadAllExams());
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<examstaff.dto.ExamSummaryDTO> allSessions) {
        if (session == null) {
            return List.of();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        input.setSelectedExamId(ExamStaffPageSupport.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageSupport.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageSupport.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> qList,
            String webRoot, int examId, List<examstaff.dto.ExamSummaryDTO> allSessions, String finishedSbd) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");

        qList = refreshCandidateQueue(session, examId, webRoot, allSessions);
        publishCandidateQueue(null, session, qList, examId);
        ExamStaffPageSupport.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedExamId", examId);

        String nextSbd = candidateQueueService.resolveNextCallingSbd(qList, finishedSbd);
        session.setAttribute("callingSbd", nextSbd);
        callBoardHttp.releaseDeskAndCall(getServletContext(), examId, nextSbd, qList, false);
    }

    private void syncCallingSbd(HttpSession session, int examId, List<ExamRegistrationDTO> qList, boolean shiftEnded) {
        String sessionCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        var callBoard = callBoardHttp.getState(getServletContext(), examId);
        String callingSbd = callingService.resolveSyncedCallingSbd(sessionCalling, callBoard, qList);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        callBoardHttp.sync(getServletContext(), examId, callingSbd, qList, shiftEnded);
    }

    private static String resolveSbdParam(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.isBlank()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

    private static boolean trackSbdChange(HttpSession session, String sbdParam) {
        boolean sbdChanged = false;
        String prevSbd = (String) session.getAttribute("lastSelectedSbd");
        if (sbdParam != null && !sbdParam.isBlank()) {
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

    private static Date parseDateOfBirth(String dobStr) {
        if (dobStr == null || dobStr.isBlank()) {
            return null;
        }
        if (dobStr.contains("/")) {
            String[] parts = dobStr.split("/");
            return Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
        }
        return Date.valueOf(dobStr.trim());
    }

    private static boolean isShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }
}
