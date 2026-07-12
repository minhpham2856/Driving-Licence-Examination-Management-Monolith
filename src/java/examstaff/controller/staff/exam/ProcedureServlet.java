package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ProcedurePaymentOutcomeDTO;
import examstaff.dto.ProcedurePhotoSaveOutcomeDTO;
import examstaff.dto.ProcedureProfilePrepareResultDTO;
import examstaff.dto.ProcedureResetOutcomeDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.dto.view.CallBoardState;
import examstaff.service.CandidateCallingService;
import examstaff.service.CandidateQueueService;
import examstaff.service.CandidatePhotoService;
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

@WebServlet("/views/staff/examstaff/procedure")
public class ProcedureServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ProcedureWorkflowService procedureWorkflow = SERVICES.procedures();
    private final CandidatePhotoService photoService = SERVICES.photos();
    private final CandidateCallingService callingService = SERVICES.calling();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final ProcedureFeeQueryService procedureFeeService = SERVICES.procedureFees();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        if ("startShift".equals(request.getParameter("action"))) {
            List<ExamSummaryDTO> bootstrapSessions = selectionFacade.loadAllExams();
            int boardExamId = selectionFacade.resolveExamId(request, session, bootstrapSessions, 0);
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            callBoardHttp.resumeShift(getServletContext(), boardExamId);
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, webRoot);
        int examId = pageCtx.getExamId();
        List<ExamSummaryDTO> allSessions = pageCtx.getAllSessions();
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
        String stepParam = ProcedureStepHelper.resolveStep(request.getParameter("step"), sbdChanged, profile, hasValidPhoto);

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
            advanceToNextCandidate(session, qList, webRoot, examId, allSessions, finishedSbd);
            response.sendRedirect("candidatecall");
            return;
        }

        if ("resetProcedure".equals(pAction) && sbdParam != null && !sbdParam.trim().isEmpty()) {
            ProcedureResetOutcomeDTO reset = procedureWorkflow.resetProcedure(sbdParam.trim(), examId, webRoot);
            if (reset.isSuccess()) {
                qList = reset.getQueue();
                candidateQueueService.moveCallableCandidateToFront(qList, reset.getSbd());
                ExamStaffPageBinder.syncCallQueueOrder(session, examId, qList);
                publishCandidateQueue(request, session, qList, examId);
                session.setAttribute("callingSbd", reset.getSbd());
                session.removeAttribute("procedureStep");
                session.removeAttribute("lastSelectedSbd");
                addAuditLog(session, "RESET Procedure",
                        "XÃ³a há»“ sÆ¡ thá»§ tá»¥c SBD " + reset.getSbd(), reset.getCandidateId());
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
            hasValidPhoto = false;
            stepParam = "2";
            publishCandidateQueue(request, session, qList, examId);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            addAuditLog(session, "UPDATE on Person", "YÃªu cáº§u chá»¥p láº¡i áº£nh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot, allSessions, examId);
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
                    profile, sbdParam, examId, webRoot, allSessions);
            applyPaymentOutcome(request, session, sbdParam, outcome, examId);
            if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
                selectionFacade.syncExamSelection(session, allSessions, examId);
                showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), false);
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
            photoService.resolveCapturedPhoto(webRoot, profile);
            hasValidPhoto = profile.isValidCapturedPhoto();
        }

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);

        forwardDeskView(request, response, qList);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
            String webRoot = request.getServletContext().getRealPath("/");
            List<ExamSummaryDTO> allSessions = selectionFacade.loadAllExams();
            int examId = selectionFacade.ensureExamId(request, session, allSessions);
            List<ExamRegistrationDTO> qList = refreshCandidateQueue(session, examId, webRoot, allSessions);
            String sbdParam = resolveSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId);
            return;
        }
        if ("confirmPayment".equals(action)) {
            doGet(request, response);
            return;
        }
        doGet(request, response);
    }

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
            boolean updated = procedureWorkflow.saveProfile(
                    profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo);
            if (updated) {
                profile = procedureWorkflow.reloadProfile(webRoot, examId, profile.getId(), sbdParam, qList);
                request.setAttribute("profileUpdatedAlert", "true");
                addAuditLog(session, "UPDATE on Person", "Sá»­a Ä‘á»•i lÃ½ lá»‹ch SBD " + sbdParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        publishCandidateQueue(request, session, qList, examId);
        return profile;
    }

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

    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
            List<ExamRegistrationDTO> qList, String webRoot, List<ExamSummaryDTO> allSessions, int examId)
            throws IOException {
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
            boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
            showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, qList, openPrint);
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PAYMENT_FAILED) {
            try {
                request.setAttribute("paymentErrorMsg", "KhÃ´ng ghi Ä‘Æ°á»£c thanh toÃ¡n. Vui lÃ²ng thá»­ láº¡i.");
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
        selectionFacade.syncExamSelection(session, allSessions, examId);
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
        addAuditLog(session, "INSERT on Payment", outcome.getPaymentAuditDetail(), outcome.getProfile().getId());
        if (outcome.isAuditAllocate()) {
            addAuditLog(session, "ALLOCATE Candidates", "Tá»± Ä‘á»™ng phÃ¢n bá»• phÃ²ng thi cho SBD " + sbdParam);
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
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
            forwardDeskView(request, response, qList);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<ExamRegistrationDTO> qList, String webRoot,
            int examId) throws IOException {
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
                addAuditLog(session, "UPDATE on Person",
                        "LÆ°u áº£nh chá»¥p tá»« webcam thá»±c táº¿ SBD " + sbdParam);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"photoUrl\":\"" + outcome.getPhotoPath() + "\"}");
            }
            default -> {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = outcome.getMessage() != null ? outcome.getMessage().replace("\"", "'") : "Lá»—i lÆ°u áº£nh";
                response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
            }
        }
    }

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> qList) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            ExamStaffPageBinder.bindProcedureFees(request, procedureFeeService.resolveProcedureFees(profile));
        }
        String webRoot = request.getServletContext().getRealPath("/");
        List<ExamSummaryDTO> allSessions = selectionFacade.loadAllExams();
        int examId = selectionFacade.ensureExamId(request, httpSession, allSessions);
        int boardExamId = selectionFacade.resolveExamId(request, httpSession, allSessions, 0);
        if (boardExamId <= 0) {
            boardExamId = examId;
        }

        qList = refreshQueueFromDb(httpSession, webRoot, examId, allSessions);
        publishCandidateQueue(request, httpSession, qList, examId);
        bindCandidateCallPageAttributes(request, httpSession, examId, qList);
        boolean shiftEnded = isShiftEnded(httpSession);
        syncCallingSbd(httpSession, boardExamId, qList, shiftEnded);
        if (profile != null && profile.getSbd() != null && !profile.getSbd().isBlank()) {
            callBoardHttp.occupyDesk(request.getServletContext(), boardExamId, profile.getSbd(), qList, shiftEnded);
        }
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute("callingSbd");
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        selectionFacade.bindSidebarIfNeeded(request, httpSession);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examId,
            List<ExamSummaryDTO> allSessions) {
        selectionFacade.syncExamSelection(session, allSessions, examId);
        List<ExamRegistrationDTO> qList = refreshCandidateQueue(session, examId, webRoot, allSessions);
        session.setAttribute("lastLoadedExamId",
                selectionFacade.resolvePrimaryExamId(allSessions, examId));
        return qList;
    }

    private String resolveSbdParam(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

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

    private String resolveSbd(HttpServletRequest request, HttpSession session) {
        return resolveSbdParam(request, session);
    }

    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> qList,
            String webRoot, int examId, List<ExamSummaryDTO> allSessions, String finishedSbd) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");

        qList = refreshCandidateQueue(session, examId, webRoot, allSessions);
        int boardExamId = selectionFacade.resolvePrimaryExamId(allSessions, examId);
        publishCandidateQueue(null, session, qList, examId);
        selectionFacade.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedExamId", boardExamId);

        String nextSbd = candidateQueueService.resolveNextCallingSbd(qList, finishedSbd);
        session.setAttribute("callingSbd", nextSbd);
        callBoardHttp.releaseDeskAndCall(getServletContext(), boardExamId, nextSbd, qList, false);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        auditLogSupport.persistWithSessionFeed(session, action, details, recordId);
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<ExamSummaryDTO> allSessions) {
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
        return refreshCandidateQueue(session, examId, selectedExamId, webRoot, allSessions);
    }

    private List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int queueExamId,
            String webRoot, List<ExamSummaryDTO> allSessions) {
        if (session == null) {
            return List.of();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(queueExamId > 0 ? queueExamId : examId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = candidateQueueService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId) {
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(qList, examId, examId);
        ExamSummaryDTO current = selectionFacade.findExamById(selectionFacade.loadAllExams(), examId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeSessionForExam(
                    selectionFacade.loadAllExams(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> qList) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, qList);
        int resolvedExamId = selectionFacade.resolveExamId(request, session, null, 0);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        ExamSummaryDTO current = selectionFacade.findExamById(
                selectionFacade.loadAllExams(), resolvedExamId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeSessionForExam(
                    selectionFacade.loadAllExams(), examId);
        }
        int suspendedCount = candidateQueueService.listSuspendedInSession(qList).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> qList) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = callingService.resolveCallingCandidate(callingSbd, qList);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> qList, boolean shiftEnded) {
        String sessionCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        CallBoardState callBoard = callBoardHttp.getState(getServletContext(), boardExamId);
        String callingSbd = callingService.resolveSyncedCallingSbd(sessionCalling, callBoard, qList);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        callBoardHttp.sync(getServletContext(), boardExamId, callingSbd, qList, shiftEnded);
    }

    private static boolean isShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }
}

