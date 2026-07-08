package controller.staff.exam;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ProcedurePaymentOutcomeDTO;
import dto.examstaff.ProcedurePhotoSaveOutcomeDTO;
import dto.examstaff.ProcedureProfilePrepareResultDTO;
import dto.examstaff.ProcedureResetOutcomeDTO;
import service.CandidatePhotoService;
import service.ProcedureWorkflowService;
import service.impl.CandidatePhotoServiceImpl;
import service.impl.ProcedureWorkflowServiceImpl;
import util.Utf8EncodingUtil;
import util.examstaff.ProcedureStepUtil;

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

    private final ProcedureWorkflowService procedureWorkflow = new ProcedureWorkflowServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        if ("startShift".equals(request.getParameter("action"))) {
            List<SessionDTO> bootstrapSessions = BaseExamStaffServlet.loadAllSessions();
            int boardSessionId = BaseExamStaffServlet.resolveSessionId(request, session, bootstrapSessions, 0);
            BaseExamStaffServlet.resumeCallShift(getServletContext(), session, boardSessionId);
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }

        BaseExamStaffServlet.applyNoCacheHeaders(response);
        BaseExamStaffServlet.ExamStaffPageContext pageCtx = BaseExamStaffServlet.prepareExamStaffPage(
                request, session, webRoot);
        int examId = pageCtx.getExamId();
        int sessionId = pageCtx.getSessionId();
        List<SessionDTO> allSessions = pageCtx.getAllSessions();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

        String sbdParam = resolveSbdParam(request, session);
        boolean sbdChanged = trackSbdChange(session, sbdParam);

        ExamRegistrationDTO profile = procedureWorkflow.findProfile(webRoot, examId, sessionId, sbdParam, qList);
        ProcedureProfilePrepareResultDTO prepared = procedureWorkflow.prepareProfileForDesk(
                webRoot, examId, sessionId, profile, qList);
        profile = prepared.getProfile();
        if (prepared.getPhotoStaleMessage() != null) {
            request.setAttribute("photoStaleMsg", prepared.getPhotoStaleMessage());
        }
        BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);

        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        String stepParam = ProcedureStepUtil.resolveStep(request.getParameter("step"), sbdChanged, profile, hasValidPhoto);

        if ("3".equals(stepParam) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            request.setAttribute("photoRequiredMsg", ProcedureStepUtil.photoRequiredForStep3Message());
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
                BaseExamStaffServlet.moveCallableCandidateToFront(qList, reset.getSbd());
                BaseExamStaffServlet.syncCallQueueOrderFromQueue(session, sessionId, qList);
                BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("callingSbd", reset.getSbd());
                session.removeAttribute("procedureStep");
                session.removeAttribute("lastSelectedSbd");
                addAuditLog(session, "RESET Procedure",
                        "Xóa hồ sơ thủ tục SBD " + reset.getSbd(), reset.getCandidateId());
                response.sendRedirect(request.getContextPath()
                        + "/views/staff/examstaff/candidatecall?procedureReset="
                        + java.net.URLEncoder.encode(reset.getSbd(), java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
        }

        if ("saveProfile".equals(pAction) && profile != null) {
            profile = handleSaveProfile(request, session, profile, sbdParam, qList, webRoot, examId, sessionId);
            stepParam = "2";
            hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
        }

        if ("recapture".equals(pAction) && profile != null) {
            profile = procedureWorkflow.recapturePhoto(profile.getId(), webRoot, examId, sbdParam, qList);
            hasValidPhoto = false;
            stepParam = "2";
            BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId, sessionId);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot, allSessions, examId);
            return;
        }

        if ("true".equals(request.getParameter("paymentSuccess")) && profile != null) {
            if (!profile.isValidCapturedPhoto()) {
                request.setAttribute("photoRequiredMsg", ProcedureStepUtil.paymentBlockedNoPhotoMessage());
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
                BaseExamStaffServlet.syncExamSelection(session, allSessions, examId);
                showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), false);
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
            BaseExamStaffServlet.bindProcedureFeeAttributes(request, profile);
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
            List<SessionDTO> allSessions = BaseExamStaffServlet.loadAllSessions();
            int examId = BaseExamStaffServlet.ensureExamId(request, session, allSessions);
            int sessionId = BaseExamStaffServlet.resolveSessionId(request, session, allSessions, 0);
            List<ExamRegistrationDTO> qList = BaseExamStaffServlet.refreshCandidateQueue(
                    session, examId, webRoot, allSessions);
            String sbdParam = resolveSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId, sessionId);
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
            String webRoot, int examId, int sessionId) {
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
                addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + sbdParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
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
            List<ExamRegistrationDTO> qList, String webRoot, List<SessionDTO> allSessions, int examId)
            throws IOException {
        ProcedurePaymentOutcomeDTO outcome = procedureWorkflow.confirmPayment(
                profile, sbdParam, examId, webRoot, allSessions);

        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.PROFILE_NOT_FOUND) {
            response.sendRedirect("candidatecall");
            return;
        }
        if (outcome.getStatus() == ProcedurePaymentOutcomeDTO.Status.NO_PHOTO) {
            try {
                request.setAttribute("photoRequiredMsg", ProcedureStepUtil.paymentBlockedNoPhotoMessage());
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
        BaseExamStaffServlet.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedSessionId", outcome.getBoardSessionId());

        boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
        showPostPaymentDesk(request, response, session, outcome.getProfile(), sbdParam, outcome.getQueue(), openPrint);
    }

    private void applyPaymentOutcome(HttpServletRequest request, HttpSession session, String sbdParam,
            ProcedurePaymentOutcomeDTO outcome, int examId) {
        if (outcome.getStatus() != ProcedurePaymentOutcomeDTO.Status.SUCCESS) {
            return;
        }
        BaseExamStaffServlet.publishCandidateQueue(request, session, outcome.getQueue(), examId, outcome.getBoardSessionId());
        addAuditLog(session, "INSERT on Payment", outcome.getPaymentAuditDetail(), outcome.getProfile().getId());
        if (outcome.isAuditAllocate()) {
            addAuditLog(session, "ALLOCATE Candidates", "Tự động phân bổ phòng thi cho SBD " + sbdParam);
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
            BaseExamStaffServlet.bindProcedureFeeAttributes(request, profile);
            forwardDeskView(request, response, qList);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<ExamRegistrationDTO> qList, String webRoot,
            int examId, int sessionId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingUtil.applyResponse(response);

        ProcedurePhotoSaveOutcomeDTO outcome = procedureWorkflow.saveCapturedPhoto(
                webRoot, sbdParam, examId, request.getParameter("photoBase64"), qList);

        switch (outcome.getStatus()) {
            case CANDIDATE_NOT_FOUND, INVALID_IMAGE -> {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"" + outcome.getMessage() + "\"}");
            }
            case SUCCESS -> {
                BaseExamStaffServlet.publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("procedureStep", "3");
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

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> qList) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            BaseExamStaffServlet.bindProcedureFeeAttributes(request, profile);
        }
        String webRoot = request.getServletContext().getRealPath("/");
        List<SessionDTO> allSessions = BaseExamStaffServlet.loadAllSessions();
        int examId = BaseExamStaffServlet.ensureExamId(request, httpSession, allSessions);
        int sessionId = BaseExamStaffServlet.resolveSessionId(request, httpSession, allSessions, 0);

        qList = refreshQueueFromDb(httpSession, webRoot, examId, allSessions);
        BaseExamStaffServlet.publishCandidateQueue(request, httpSession, qList, examId, sessionId);
        BaseExamStaffServlet.bindCandidateCallPageAttributes(request, httpSession, examId, qList);
        boolean shiftEnded = BaseExamStaffServlet.isCallShiftEnded(httpSession);
        BaseExamStaffServlet.syncCallingSbd(httpSession, request.getServletContext(), sessionId, qList, shiftEnded);
        if (profile != null && profile.getSbd() != null && !profile.getSbd().isBlank()) {
            BaseExamStaffServlet.occupyDesk(request.getServletContext(), sessionId, profile.getSbd(), qList, shiftEnded);
        }
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute("callingSbd");
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        BaseExamStaffServlet.bindSidebarIfNeeded(request, httpSession);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examId,
            List<SessionDTO> allSessions) {
        BaseExamStaffServlet.syncExamSelection(session, allSessions, examId);
        List<ExamRegistrationDTO> qList = BaseExamStaffServlet.refreshCandidateQueue(session, examId, webRoot, allSessions);
        session.setAttribute("lastLoadedSessionId",
                BaseExamStaffServlet.resolvePrimarySessionId(allSessions, examId));
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
            String webRoot, int examId, List<SessionDTO> allSessions, String finishedSbd) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");

        qList = BaseExamStaffServlet.refreshCandidateQueue(session, examId, webRoot, allSessions);
        int boardSessionId = BaseExamStaffServlet.resolvePrimarySessionId(allSessions, examId);
        BaseExamStaffServlet.publishCandidateQueue(null, session, qList, examId, boardSessionId);
        BaseExamStaffServlet.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedSessionId", boardSessionId);

        String nextSbd = BaseExamStaffServlet.resolveNextCallingSbd(qList, finishedSbd);
        session.setAttribute("callingSbd", nextSbd);
        BaseExamStaffServlet.releaseDeskAndCall(getServletContext(), boardSessionId, nextSbd, qList, false);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        BaseExamStaffServlet.persistWithSessionFeed(session, action, details, recordId);
    }
}
