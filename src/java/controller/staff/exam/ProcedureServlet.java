package controller.staff.exam;

import controller.staff.exam.CandidateCallBoard;
import service.ExamRegistrationService;

import service.impl.ExamRegistrationServiceImpl;

import dao.PaymentDAO;

import dao.ExamSessionDAO;

import dao.impl.ExamSessionDAOImpl;

import dao.impl.PaymentDAOImpl;

import dto.exam.ExamRegistrationDTO;
import dto.SessionDTO;

import model.Payment;

import service.ExaminerAllocationService;
import service.impl.ExaminerAllocationServiceImpl;

import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;

import dto.AutoAllocateResultDTO;

import util.Utf8EncodingHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/staff/examstaff/procedure")
public class ProcedureServlet extends HttpServlet {

    private final ExamRegistrationService regDAO = new ExamRegistrationServiceImpl();
    private final PaymentDAO payDAO = new PaymentDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        if ("startShift".equals(request.getParameter("action"))) {
            List<SessionDTO> bootstrapSessions = sessionDAO.getAllSessions();
            int examId = ExamStaffViewHelper.ensureExamId(request, session, bootstrapSessions);
            int boardSessionId = ExamStaffViewHelper.resolveSessionId(request, session, bootstrapSessions, 0);
            ExamStaffViewHelper.resumeCallShift(getServletContext(), session, boardSessionId);
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }

        ExamStaffViewHelper.applyNoCacheHeaders(response);
        ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                request, session, sessionDAO, webRoot);
        int examId = pageCtx.getExamId();
        int sessionId = pageCtx.getSessionId();
        List<SessionDTO> allSessions = pageCtx.getAllSessions();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }

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

        ExamRegistrationDTO profile = loadProfileFromDb(request, session, webRoot, sbdParam, qList);
        ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);

        if (profile != null && CandidatePhotoHelper.hasPhotoRecord(profile)
                && !CandidatePhotoHelper.hasCapturedPhoto(webRoot, profile)) {
            regDAO.updatePhoto(profile.getId(), null);
            profile.setPhotoUrl(null);
            profile.setValidCapturedPhoto(false);
            request.setAttribute("photoStaleMsg",
                    "Ảnh trong hồ sơ không tìm thấy trên máy chủ — vui lòng chụp lại ảnh chân dung.");
        }

        if (profile != null && !profile.isPresent()) {
            boolean updatedPresent = regDAO.updatePresent(profile.getId(), true);
            if (updatedPresent) {
                profile = reloadProfileAfterMutation(webRoot, examId, profile.getId(), sbdParam, qList);
                ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
            }
        }

        String stepParam = request.getParameter("step");
        if (sbdChanged && (stepParam == null || stepParam.trim().isEmpty())) {
            stepParam = "1";
        }
        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();

        if (stepParam == null || stepParam.trim().isEmpty()) {
            if (profile != null) {
                if (profile.isPaymentCompleted()) {
                    stepParam = "3";
                } else if (hasValidPhoto) {
                    stepParam = "2";
                } else {
                    stepParam = "1";
                }
            } else {
                stepParam = "1";
            }
        }

        if ("3".equals(stepParam) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            stepParam = "2";
            request.setAttribute("photoRequiredMsg", "Bắt buộc chụp ảnh chân dung trước khi thu lệ phí và in hồ sơ.");
        }

        String pAction = request.getParameter("action");

        if ("nextCandidate".equals(pAction)) {
            // advance to next candidate
            session.removeAttribute("procedureJustPaidSbd");
            advanceToNextCandidate(session, qList, webRoot, examId, allSessions);
            response.sendRedirect("candidatecall");
            return;
        }

        if ("resetProcedure".equals(pAction) && sbdParam != null && !sbdParam.trim().isEmpty()) {
            ExamRegistrationDTO target = loadProfileFromDb(request, session, webRoot, sbdParam.trim(), qList);
            if (target != null) {
                regDAO.updatePhoto(target.getId(), null);
                regDAO.clearCompletedPayments(target.getId());
                qList = refreshQueueFromDb(session, webRoot, examId, allSessions);
                ExamStaffViewHelper.moveCallableCandidateToFront(qList, sbdParam.trim());
                ExamStaffViewHelper.syncCallQueueOrderFromQueue(session, sessionId, qList);
                ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
                session.setAttribute("callingSbd", sbdParam.trim());
                session.removeAttribute("procedureStep");
                session.removeAttribute("lastSelectedSbd");
                addAuditLog(session, "RESET Procedure", "Xóa hồ sơ thủ tục SBD " + sbdParam.trim(), target.getId());
                response.sendRedirect(request.getContextPath()
                        + "/views/staff/examstaff/candidatecall?procedureReset="
                        + java.net.URLEncoder.encode(sbdParam.trim(), java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
        }

        if ("saveProfile".equals(pAction) && profile != null) {
            String fullName = request.getParameter("fullName");
            String dobStr = request.getParameter("dateOfBirth");
            String govIdNo = request.getParameter("govIdNo");
            String email = request.getParameter("email");
            String phoneNo = request.getParameter("phoneNo");

            try {
                Date sqlDob = null;
                if (dobStr != null && !dobStr.trim().isEmpty()) {
                    if (dobStr.contains("/")) {
                        String[] parts = dobStr.split("/");
                        sqlDob = Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
                    } else {
                        sqlDob = Date.valueOf(dobStr.trim());
                    }
                }

                boolean updated = regDAO.updateProfile(profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo);
                if (updated) {
                    // add audit log
                    profile = reloadProfileAfterMutation(webRoot, examId, profile.getId(), sbdParam, qList);
                    request.setAttribute("profileUpdatedAlert", "true");
                    addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + sbdParam);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stepParam = "2";
            hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
            ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
        }

        if ("recapture".equals(pAction) && profile != null) {
            regDAO.updatePhoto(profile.getId(), null);
            profile = reloadProfileAfterMutation(webRoot, examId, profile.getId(), sbdParam, qList);
            hasValidPhoto = false;
            stepParam = "2";
            ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
            // add audit log
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            // handle save captured photo
            addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            // process payment
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot, examId, sessionId);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot);
            return;
        }

        String paymentSuccessParam = request.getParameter("paymentSuccess");
        if ("true".equals(paymentSuccessParam) && profile != null) {
                // forward desk view
            if (!profile.isValidCapturedPhoto()) {
                request.setAttribute("photoRequiredMsg", "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.");
                request.setAttribute("step", "2");
                session.setAttribute("procedureStep", "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", profile);
                forwardDeskView(request, response, qList);
                return;
            }
            Payment payment = new Payment();
            int enrollmentId = profile.getExamEnrollmentId();
            if (enrollmentId <= 0) {
                enrollmentId = payDAO.resolveEnrollmentId(profile.getId());
            }
            payment.setExamEnrollmentId(enrollmentId);
            payment.setTotalAmount(200000.00);
            payment.setPaymentStatus("Completed");
            payment.setPaymentMethod("Cash");
            payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1000000);
            boolean updatedPay = payDAO.insert(payment);
            if (!updatedPay) {
                updatedPay = regDAO.updatePayment(profile.getId(), true);
                    // Huy danh dau vang
            }
            if (updatedPay) {
                profile.setIsPaymentCompleted(true);
                profile.setIsPresent(true);
                regDAO.updatePresent(profile.getId(), true);

                if (profile.isAbsent()) {
                    clearAbsentMarking(profile);
                }

                ExaminerAllocationService allocator = new ExaminerAllocationServiceImpl();
                AutoAllocateResultDTO allocResult = allocator.autoAllocateCandidate(
                        profile.getExamSessionId(), profile.getId());

                qList = regDAO.getCandidatesByExam(examId);
                CandidatePhotoHelper.normalizeQueue(webRoot, qList);
                ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
                ExamStaffViewHelper.syncExamSelection(session, allSessions, examId);

                String allocDetail = formatAutoAllocateDetail(allocResult);
                addAuditLog(session, "INSERT on Payment",
                        "Thu lệ phí thi 200,000 đ" + allocDetail + " cho SBD " + sbdParam, profile.getId());
                if (allocResult.allocatedCount > 0) {
                    addAuditLog(session, "ALLOCATE Candidates",
                            "Tự động phân bổ phòng thi cho SBD " + sbdParam);
                }

                showPostPaymentDesk(request, response, session, profile, sbdParam, qList, false);
                return;
            }
        }

        // forward desk view
        if (profile != null) {
            request.setAttribute("profile", profile);
    // forward desk view
            ExamStaffViewHelper.bindProcedureFeeAttributes(request, profile);
        }

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);

        forwardDeskView(request, response, qList);
    }

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response,
            List<ExamRegistrationDTO> qList) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();
        ExamRegistrationDTO profile = (ExamRegistrationDTO) request.getAttribute("profile");
        if (profile != null && request.getAttribute("feeLines") == null) {
            ExamStaffViewHelper.bindProcedureFeeAttributes(request, profile);
        }
        String webRoot = request.getServletContext().getRealPath("/");
        List<SessionDTO> allSessions = sessionDAO.getAllSessions();
        int examId = ExamStaffViewHelper.ensureExamId(request, httpSession, allSessions, sessionDAO);
        int sessionId = ExamStaffViewHelper.resolveSessionId(request, httpSession, allSessions, 0);

        qList = refreshQueueFromDb(httpSession, webRoot, examId, allSessions);
        ExamStaffViewHelper.publishCandidateQueue(request, httpSession, qList, examId, sessionId);
        ExamStaffViewHelper.bindCandidateCallPageAttributes(request, sessionDAO, httpSession, examId, qList);
        boolean shiftEnded = ExamStaffViewHelper.isCallShiftEnded(httpSession);
        ExamStaffViewHelper.syncCallingSbd(httpSession, request.getServletContext(), sessionId, qList, shiftEnded);
    // Xu ly yeu cau POST
        if (request.getAttribute("callingCandidate") == null && profile != null) {
            String callingSbd = (String) httpSession.getAttribute("callingSbd");
            if (callingSbd != null && callingSbd.equals(profile.getSbd())) {
                request.setAttribute("callingCandidate", profile);
            }
        }
        request.setAttribute("deskMode", Boolean.TRUE);
        ExamStaffViewHelper.bindSidebarIfNeeded(request, httpSession);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    @Override
            // handle save captured photo
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
            // Xu ly yeu cau GET
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
        // Xu ly yeu cau GET
            String webRoot = request.getServletContext().getRealPath("/");
            List<SessionDTO> allSessions = sessionDAO.getAllSessions();
    // process payment
            int examId = ExamStaffViewHelper.ensureExamId(request, session, allSessions, sessionDAO);
            int sessionId = ExamStaffViewHelper.resolveSessionId(request, session, allSessions, 0);
            List<ExamRegistrationDTO> qList = ExamStaffViewHelper.refreshCandidateQueue(
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

    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, ExamRegistrationDTO profile, String sbdParam,
                // forward desk view
            List<ExamRegistrationDTO> qList, String webRoot) throws IOException {
                // ioexception
        List<SessionDTO> allSessions = sessionDAO.getAllSessions();
        int examId = ExamStaffViewHelper.ensureExamId(request, session, allSessions, sessionDAO);
        profile = reloadProfileAfterMutation(webRoot, examId, profile.getId(), sbdParam, qList);
        if (profile == null) {
            // show post payment desk
            response.sendRedirect("candidatecall");
            return;
        }
        if (!profile.isValidCapturedPhoto()) {
            try {
                request.setAttribute("photoRequiredMsg",
                        "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.");
                request.setAttribute("step", "2");
                session.setAttribute("procedureStep", "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", profile);
                forwardDeskView(request, response, qList);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        if (profile.isPaymentCompleted()) {
                // forward desk view
            showPostPaymentDesk(request, response, session, profile, sbdParam, qList, false);
                // ioexception
            return;
        }
        Payment payment = new Payment();
        int enrollmentId = profile.getExamEnrollmentId();
        if (enrollmentId <= 0) {
            enrollmentId = payDAO.resolveEnrollmentId(profile.getId());
        }
        payment.setExamEnrollmentId(enrollmentId);
        payment.setTotalAmount(200000.00);
        payment.setPaymentStatus("Completed");
        payment.setPaymentMethod("Cash");
            // Huy danh dau vang
        payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1000000);
        boolean updatedPay = payDAO.insert(payment);
        if (!updatedPay) {
            updatedPay = regDAO.updatePayment(profile.getId(), true);
        }
        if (!updatedPay) {
            try {
                request.setAttribute("paymentErrorMsg", "Không ghi được thanh toán. Vui lòng thử lại.");
                request.setAttribute("step", "3");
                request.setAttribute("profile", profile);
                request.setAttribute("hasValidPhoto", profile.isValidCapturedPhoto());
                forwardDeskView(request, response, qList);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        profile.setIsPaymentCompleted(true);
        profile.setIsPresent(true);
        regDAO.updatePresent(profile.getId(), true);
        if (profile.isAbsent()) {
            clearAbsentMarking(profile);
        }
        // show post payment desk
        ExaminerAllocationService allocator = new ExaminerAllocationServiceImpl();
        AutoAllocateResultDTO allocResult = allocator.autoAllocateCandidate(
    // show post payment desk
                profile.getExamSessionId(), profile.getId());
        qList = regDAO.getCandidatesByExam(examId);
        CandidatePhotoHelper.normalizeQueue(webRoot, qList);
        int boardSessionId = profile.getExamSessionId() > 0
                ? profile.getExamSessionId()
                : ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId);
        ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, boardSessionId);
        ExamStaffViewHelper.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedSessionId",
                ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId));
        String allocDetail = formatAutoAllocateDetail(allocResult);
        addAuditLog(session, "INSERT on Payment",
                "Thu lệ phí thi 200,000 đ" + allocDetail + " cho SBD " + sbdParam, profile.getId());
            // forward desk view
        if (allocResult.allocatedCount > 0) {
            // ioexception
            addAuditLog(session, "ALLOCATE Candidates",
                    "Tự động phân bổ phòng thi cho SBD " + sbdParam);
        }
    // handle save captured photo
        boolean openPrint = "true".equals(request.getParameter("printAfterPayment"));
        showPostPaymentDesk(request, response, session, profile, sbdParam, qList, openPrint);
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
            ExamStaffViewHelper.bindProcedureFeeAttributes(request, profile);
            forwardDeskView(request, response, qList);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<ExamRegistrationDTO> qList, String webRoot,
            int examId, int sessionId) throws IOException {
                // illegal argument exception
        response.setContentType("application/json;charset=UTF-8");
        Utf8EncodingHelper.applyResponse(response);

        ExamRegistrationDTO profile = loadProfileFromDb(request, session, webRoot, sbdParam, qList);
        if (profile == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Không tìm thấy thí sinh.\"}");
            return;
        }

        String base64Data = request.getParameter("photoBase64");
        String ext = null;
        if (base64Data != null && base64Data.startsWith("data:image/png;base64,")) {
            ext = "png";
        } else if (base64Data != null && base64Data.startsWith("data:image/jpeg;base64,")) {
            ext = "jpg";
        }
        if (ext == null) {
            // add audit log
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"Dữ liệu ảnh không hợp lệ.\"}");
            return;
        }

        try {
            String base64Image = base64Data.substring(base64Data.indexOf(',') + 1);
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("Ảnh rỗng");
            }
    // Lam moi queue from db

            String safeSbd = sbdParam.replaceAll("[^A-Za-z0-9\\-]", "_");
            String fileName = safeSbd + "_captured." + ext;
            CandidatePhotoHelper.writePhotoFile(request.getServletContext(), fileName, imageBytes);

            String photoPath = CandidatePhotoHelper.toWebPhotoPath(fileName);
            boolean updated = regDAO.updatePhoto(profile.getId(), photoPath);
            if (!updated) {
    // Tai profile from db
                throw new java.io.IOException("Không cập nhật được photoUrl trong DB");
            }

            profile = reloadProfileAfterMutation(webRoot, examId, profile.getId(), sbdParam, qList);
            if (profile != null) {
                profile.setValidCapturedPhoto(true);
            }
            ExamStaffViewHelper.publishCandidateQueue(request, session, qList, examId, sessionId);
            // sync profile in queue
            session.setAttribute("procedureStep", "2");
            addAuditLog(session, "UPDATE on Person", "Lưu ảnh chụp từ webcam thực tế SBD " + sbdParam);

            response.setStatus(HttpServletResponse.SC_OK);
    // reload profile after mutation
            response.getWriter().write("{\"success\":true,\"photoUrl\":\"" + photoPath + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Lỗi lưu ảnh";
            response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
        }
    }

    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examId,
            List<SessionDTO> allSessions) {
        ExamStaffViewHelper.syncExamSelection(session, allSessions, examId);
        List<ExamRegistrationDTO> qList = ExamStaffViewHelper.refreshCandidateQueue(session, examId, webRoot, allSessions);
        // sync profile in queue
        session.setAttribute("lastLoadedSessionId",
                ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId));
        return qList;
    // Xac dinh sbd
    }

    private ExamRegistrationDTO loadProfileFromDb(HttpServletRequest request, HttpSession session,
            String webRoot, String sbdParam, List<ExamRegistrationDTO> qList) {
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            return null;
        }
    // sync profile in queue
        ExamRegistrationDTO profile = ExamStaffViewHelper.resolveCandidateBySbd(request, session, sbdParam.trim());
        if (profile != null) {
            CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
            photoService.normalizeQueue(webRoot, java.util.Collections.singletonList(profile));
            syncProfileInQueue(qList, profile);
        }
        return profile;
    }

    private ExamRegistrationDTO reloadProfileAfterMutation(String webRoot, int examId, int candidateId,
            String sbdParam, List<ExamRegistrationDTO> qList) {
    // Huy danh dau vang
        ExamRegistrationDTO fresh = regDAO.getById(candidateId);
        if (fresh == null) {
            if (sbdParam == null || sbdParam.isBlank()) {
                return null;
            }
            fresh = regDAO.getByExamAndSbd(examId, sbdParam.trim());
            if (fresh == null) {
                return null;
            }
        }
    // add audit log
        // add audit log
        CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
        photoService.normalizeQueue(webRoot, java.util.Collections.singletonList(fresh));
    // advance to next candidate
        syncProfileInQueue(qList, fresh);
        return fresh;
    }

    private String resolveSbd(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

    private void syncProfileInQueue(List<ExamRegistrationDTO> qList, ExamRegistrationDTO refreshed) {
        if (qList == null || refreshed == null) {
            return;
        }
        for (int i = 0; i < qList.size(); i++) {
            if (qList.get(i).getId() == refreshed.getId()) {
                qList.set(i, refreshed);
                return;
            }
        }
    }
    // add audit log

    private void clearAbsentMarking(ExamRegistrationDTO profile) {
        regDAO.clearAbsentMarking(profile.getId());
        profile.setAbsent(false);
        profile.setTheoryPassed("none");
        profile.setPracticalPassed("none");
        profile.setRoadTestPassed("none");
        profile.setTheoryScore(null);
        profile.setPracticalScore(null);
        profile.setRoadTestScore(null);
    }

    private static String formatAutoAllocateDetail(AutoAllocateResultDTO allocResult) {
        if (allocResult != null && allocResult.allocatedCount > 0) {
            return " và tự động phân bổ vào phòng thi";
        }
        if (allocResult != null && allocResult.errorMsg != null && !allocResult.errorMsg.isBlank()) {
            return " (" + allocResult.errorMsg.trim() + ")";
        }
        return " (chưa phân được phòng - kiểm tra sức chứa phòng thi)";
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> qList,
            String webRoot, int examId, List<SessionDTO> allSessions) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");

        qList = regDAO.getCandidatesByExam(examId);
        CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
        photoService.normalizeQueue(webRoot, qList);
        int boardSessionId = ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId);
        ExamStaffViewHelper.publishCandidateQueue(null, session, qList, examId, boardSessionId);
        ExamStaffViewHelper.syncExamSelection(session, allSessions, examId);
        session.setAttribute("lastLoadedSessionId", boardSessionId);

        String currentSbd = (String) session.getAttribute("callingSbd");
        String nextSbd = ExamStaffViewHelper.findNextPendingSbd(qList, currentSbd);
        if (nextSbd == null || nextSbd.equals(currentSbd)) {
            nextSbd = ExamStaffViewHelper.findNextPendingSbd(qList, null);
        }
        session.setAttribute("callingSbd", nextSbd);
        CandidateCallBoard.sync(getServletContext(), boardSessionId, nextSbd, qList, false);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        List<Map<String, String>> sessionAuditLogs = (List<Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        Map<String, String> audit = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", action);
        audit.put("details", details);
        sessionAuditLogs.add(0, audit);

        util.AuditLogHelper.persist(session, action, details, recordId);
    }
}
