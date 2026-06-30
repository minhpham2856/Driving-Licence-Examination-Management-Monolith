package controller.staff.exam;

import dto.AutoAllocateResultDTO;
import dto.CandidateCallBoardStateDTO;
import dto.CandidateEnrollmentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Payment;
import model.User;
import service.AuditLogService;
import service.CandidateCallBoardService;
import service.CandidatePhotoService;
import service.ExamRegistrationService;
import service.ExaminerAllocationService;
import service.impl.AuditLogServiceImpl;
import service.impl.CandidateCallBoardServiceImpl;
import service.impl.CandidatePhotoServiceImpl;
import service.impl.ExamRegistrationServiceImpl;
import service.impl.ExaminerAllocationServiceImpl;

@WebServlet("/views/staff/exam/procedure")
public class ProcedureServlet extends HttpServlet {

    private static final double EXAM_FEE = 200_000.00;
    private static final String MSG_PHOTO_REQUIRED = "Vui lòng chụp ảnh chân dung trước khi thu phí.";
    private static final String MSG_PAYMENT_FAILED = "Không thể ghi nhận thanh toán. Vui lòng thử lại.";

    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    private final CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();
    private final ExaminerAllocationService allocator = new ExaminerAllocationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        int examSessionId = resolveSessionId(session, null, null);
        List<CandidateEnrollmentDTO> qList = refreshQueueFromDb(session, webRoot, examSessionId);

        String sbdParam = resolveSbd(request, session);
        boolean sbdChanged = trackSbdChange(session, sbdParam);

        CandidateEnrollmentDTO profile = loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
        session.setAttribute("candidateQueue", qList);

        if (profile != null && !profile.isPresent()) {
            if (regService.updatePresent(profile.getId(), true)) {
                profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
                session.setAttribute("candidateQueue", qList);
            }
        }

        String stepParam = resolveStep(request, session, profile, sbdChanged);
        boolean hasValidPhoto = profile != null && profile.isValidCapturedPhoto();

        if ("3".equals(stepParam) && profile != null && !hasValidPhoto && !profile.isPaymentCompleted()) {
            stepParam = "2";
            request.setAttribute("photoRequiredMsg", MSG_PHOTO_REQUIRED);
        }

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);

        String action = request.getParameter("action");
        if ("nextCandidate".equals(action)) {
            advanceToNextCandidate(session, qList, webRoot, resolveSessionId(session, profile, qList));
            response.sendRedirect("candidatecall");
            return;
        }

        if ("saveProfile".equals(action) && profile != null) {
            handleSaveProfile(request, session, webRoot, examSessionId, sbdParam, qList, profile);
            profile = (CandidateEnrollmentDTO) request.getAttribute("profile");
            if (profile == null) {
                profile = loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
            }
            hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
            request.setAttribute("hasValidPhoto", hasValidPhoto);
        }

        if ("recapture".equals(action) && profile != null) {
            regService.updatePhoto(profile.getId(), null);
            profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
            hasValidPhoto = false;
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            request.setAttribute("profile", profile);
            addAuditLog(session, "UPDATE on Person", "Xóa ảnh chân dung để chụp lại SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(action)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot);
            return;
        }

        if ("confirmPayment".equals(action) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot);
            return;
        }

        if ("true".equals(request.getParameter("paymentSuccess")) && profile != null) {
            if (handlePaymentSuccess(request, response, session, profile, sbdParam, qList, webRoot)) {
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
        }
        forwardDeskView(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
            String webRoot = request.getServletContext().getRealPath("/");
            List<CandidateEnrollmentDTO> qList = loadQueue(session, webRoot);
            handleSaveCapturedPhoto(request, response, session, resolveSbd(request, session), qList, webRoot);
            return;
        }
        if ("confirmPayment".equals(action)) {
            doGet(request, response);
            return;
        }
        doGet(request, response);
    }

    private void handleSaveProfile(HttpServletRequest request, HttpSession session, String webRoot,
            int examSessionId, String sbdParam, List<CandidateEnrollmentDTO> qList,
            CandidateEnrollmentDTO profile) {
        String fullName = request.getParameter("fullName");
        String dobStr = request.getParameter("dateOfBirth");
        String govIdNo = request.getParameter("govIdNo");
        String email = request.getParameter("email");
        String phoneNo = request.getParameter("phoneNo");

        if (dobStr == null || dobStr.trim().isEmpty()) {
            return;
        }

        try {
            Date sqlDob;
            if (dobStr.contains("/")) {
                String[] parts = dobStr.split("/");
                if (parts.length != 3) {
                    return;
                }
                sqlDob = Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
            } else {
                sqlDob = Date.valueOf(dobStr);
            }

            if (regService.updateProfile(profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo)) {
                profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
                session.setAttribute("candidateQueue", qList);
                request.setAttribute("profileUpdatedAlert", "true");
                request.setAttribute("profile", profile);
                addAuditLog(session, "UPDATE on Person",
                        "Cập nhật thông tin nhân thân SBD " + sbdParam, profile.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean handlePaymentSuccess(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, CandidateEnrollmentDTO profile, String sbdParam,
            List<CandidateEnrollmentDTO> qList, String webRoot) throws IOException, ServletException {
        if (!profile.isValidCapturedPhoto()) {
            request.setAttribute("photoRequiredMsg", MSG_PHOTO_REQUIRED);
            request.setAttribute("step", "2");
            session.setAttribute("procedureStep", "2");
            request.setAttribute("hasValidPhoto", false);
            request.setAttribute("profile", profile);
            forwardDeskView(request, response);
            return true;
        }
        if (completePayment(session, profile, sbdParam, webRoot)) {
            session.setAttribute("procedureJustPaid", "true");
            advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
            response.sendRedirect("candidatecall");
            return true;
        }
        return false;
    }

    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, CandidateEnrollmentDTO profile, String sbdParam,
            List<CandidateEnrollmentDTO> qList, String webRoot) throws IOException, ServletException {
        int examSessionId = resolveSessionId(session, profile, qList);
        profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
        if (profile == null) {
            response.sendRedirect("candidatecall");
            return;
        }
        if (!profile.isValidCapturedPhoto()) {
            request.setAttribute("photoRequiredMsg", MSG_PHOTO_REQUIRED);
            request.setAttribute("step", "2");
            session.setAttribute("procedureStep", "2");
            request.setAttribute("hasValidPhoto", false);
            request.setAttribute("profile", profile);
            forwardDeskView(request, response);
            return;
        }
        if (profile.isPaymentCompleted()) {
            advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
            response.sendRedirect("candidatecall");
            return;
        }
        if (!completePayment(session, profile, sbdParam, webRoot)) {
            request.setAttribute("paymentErrorMsg", MSG_PAYMENT_FAILED);
            request.setAttribute("step", "3");
            request.setAttribute("profile", profile);
            request.setAttribute("hasValidPhoto", profile.isValidCapturedPhoto());
            forwardDeskView(request, response);
            return;
        }
        advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
        response.sendRedirect("candidatecall");
    }

    private boolean completePayment(HttpSession session, CandidateEnrollmentDTO profile,
            String sbdParam, String webRoot) {
        Payment payment = new Payment();
        payment.setCandidateId(profile.getId());
        if (profile.getEnrollment() != null) {
            payment.setExamEnrollmentId(profile.getEnrollment().getExamEnrollmentId());
        }
        payment.setTotalAmount(EXAM_FEE);
        payment.setPaymentStatus("Completed");
        payment.setPaymentMethod("Cash");
        payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1_000_000);

        boolean updatedPay = regService.insertPayment(payment);
        if (!updatedPay) {
            updatedPay = regService.updatePayment(profile.getId(), true);
        }
        if (!updatedPay) {
            return false;
        }

        profile.setIsPaymentCompleted(true);
        profile.setIsPresent(true);
        regService.updatePresent(profile.getId(), true);
        if (profile.isAbsent()) {
            clearAbsentMarking(profile);
        }

        AutoAllocateResultDTO allocResult = allocator.autoAllocateCandidate(
                profile.getExamSessionId(), profile.getId());

        List<CandidateEnrollmentDTO> qList = regService.getCandidatesBySession(profile.getExamSessionId());
        photoService.normalizeQueue(webRoot, qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", profile.getExamSessionId());
        session.setAttribute("selectedSessionId", profile.getExamSessionId());

        String allocDetail = allocResult.allocatedCount > 0
                ? "Phân bổ " + allocResult.allocatedCount + " giám khảo"
                : "Không phân bổ được giám khảo tự động";
        addAuditLog(session, "INSERT on Payment",
                "Thu phí thi " + allocDetail + " cho SBD " + sbdParam, profile.getId());
        if (allocResult.allocatedCount > 0) {
            addAuditLog(session, "ALLOCATE Candidates",
                    "Tự động phân bổ giám khảo cho SBD " + sbdParam, profile.getId());
        }
        return true;
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<CandidateEnrollmentDTO> qList, String webRoot)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int examSessionId = resolveSessionId(session, null, qList);
        CandidateEnrollmentDTO profile = loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
        if (profile == null) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    false, "Không tìm thấy thí sinh.", null);
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
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    false, "Định dạng ảnh không hợp lệ.", null);
            return;
        }

        try {
            String base64Image = base64Data.substring(base64Data.indexOf(',') + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("Ảnh rỗng");
            }

            String uploadDir = webRoot + "assets/imgs/candidates/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Không tạo được thư mục lưu ảnh: " + uploadDir);
            }

            String safeSbd = sbdParam.replaceAll("[^A-Za-z0-9\\-]", "_");
            String fileName = safeSbd + "_captured." + ext;
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }

            String photoPath = "assets/imgs/candidates/" + fileName;
            if (!regService.updatePhoto(profile.getId(), photoPath)) {
                throw new IOException("Không cập nhật được photoUrl trong DB");
            }

            profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
            if (profile != null) {
                profile.setValidCapturedPhoto(true);
            }
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("procedureStep", "2");
            addAuditLog(session, "UPDATE on Person",
                    "Lưu ảnh chụp từ webcam thực tế SBD " + sbdParam, profile != null ? profile.getId() : 0);

            writeJson(response, HttpServletResponse.SC_OK, true, null, photoPath);
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi lưu ảnh";
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, msg, null);
        }
    }

    private void writeJson(HttpServletResponse response, int status, boolean success,
            String message, String photoUrl) throws IOException {
        response.setStatus(status);
        StringBuilder json = new StringBuilder("{\"success\":").append(success);
        if (message != null) {
            json.append(",\"message\":\"").append(escapeJson(message)).append("\"");
        }
        if (photoUrl != null) {
            json.append(",\"photoUrl\":\"").append(escapeJson(photoUrl)).append("\"");
        }
        json.append("}");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String resolveStep(HttpServletRequest request, HttpSession session,
            CandidateEnrollmentDTO profile, boolean sbdChanged) {
        String stepParam = request.getParameter("step");
        if (sbdChanged) {
            return "1";
        }
        if (stepParam != null && !stepParam.trim().isEmpty()) {
            return stepParam;
        }
        if (profile == null) {
            return "1";
        }
        if (profile.isPaymentCompleted()) {
            return "3";
        }
        if (profile.isValidCapturedPhoto()) {
            return "2";
        }
        return "1";
    }

    private boolean trackSbdChange(HttpSession session, String sbdParam) {
        String prevSbd = (String) session.getAttribute("lastSelectedSbd");
        if (sbdParam != null && !sbdParam.trim().isEmpty()) {
            boolean changed = prevSbd == null || !prevSbd.equals(sbdParam);
            session.setAttribute("lastSelectedSbd", sbdParam);
            session.setAttribute("callingSbd", sbdParam);
            return changed;
        }
        session.setAttribute("lastSelectedSbd", null);
        return false;
    }

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("deskMode", Boolean.TRUE);
        request.getRequestDispatcher("/views/staff/exam/candidatecall.jsp").forward(request, response);
    }

    private List<CandidateEnrollmentDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examSessionId) {
        List<CandidateEnrollmentDTO> qList;
        try {
            qList = regService.getCandidatesBySession(examSessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        photoService.normalizeQueue(webRoot, qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", examSessionId);
        session.setAttribute("selectedSessionId", examSessionId);
        return qList;
    }

    private CandidateEnrollmentDTO loadProfileFromDb(String webRoot, int examSessionId, String sbdParam,
            List<CandidateEnrollmentDTO> qList) {
        Integer sbd = parseSbdParam(sbdParam);
        if (sbd == null) {
            return null;
        }
        CandidateEnrollmentDTO profile = regService.getBySessionAndSbd(examSessionId, sbd);
        if (profile == null && qList != null) {
            for (CandidateEnrollmentDTO c : qList) {
                if (c.getSbd() == sbd) {
                    profile = regService.getById(c.getId());
                    break;
                }
            }
        }
        if (profile != null) {
            photoService.normalizeQueue(webRoot, Collections.singletonList(profile));
            syncProfileInQueue(qList, profile);
        }
        return profile;
    }

    private CandidateEnrollmentDTO reloadProfileAfterMutation(String webRoot, int examSessionId, int candidateId,
            String sbdParam, List<CandidateEnrollmentDTO> qList) {
        CandidateEnrollmentDTO fresh = regService.getById(candidateId);
        if (fresh == null) {
            return loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
        }
        photoService.normalizeQueue(webRoot, Collections.singletonList(fresh));
        syncProfileInQueue(qList, fresh);
        return fresh;
    }

    private List<CandidateEnrollmentDTO> loadQueue(HttpSession session, String webRoot) {
        int examSessionId = resolveSessionId(session, null, null);
        return refreshQueueFromDb(session, webRoot, examSessionId);
    }

    private String resolveSbd(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

    private void syncProfileInQueue(List<CandidateEnrollmentDTO> qList, CandidateEnrollmentDTO refreshed) {
        for (int i = 0; i < qList.size(); i++) {
            if (qList.get(i).getId() == refreshed.getId()) {
                qList.set(i, refreshed);
                return;
            }
        }
    }

    private void clearAbsentMarking(CandidateEnrollmentDTO profile) {
        regService.clearAbsentMarking(profile.getId());
        profile.setAbsent(false);
        profile.setTheoryPassed("none");
        profile.setPracticalPassed("none");
        profile.setRoadTestPassed("none");
        profile.setTheoryScore(null);
        profile.setPracticalScore(null);
        profile.setRoadTestScore(null);
    }

    private int resolveSessionId(HttpSession session, CandidateEnrollmentDTO profile,
            List<CandidateEnrollmentDTO> qList) {
        if (profile != null && profile.getExamSessionId() > 0) {
            return profile.getExamSessionId();
        }
        Integer selected = (Integer) session.getAttribute("selectedSessionId");
        if (selected != null) {
            return selected;
        }
        if (qList != null && !qList.isEmpty()) {
            return qList.get(0).getExamSessionId();
        }
        return 2;
    }

    private void advanceToNextCandidate(HttpSession session, List<CandidateEnrollmentDTO> qList,
            String webRoot, int examSessionId) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");

        qList = regService.getCandidatesBySession(examSessionId);
        photoService.normalizeQueue(webRoot, qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", examSessionId);
        session.setAttribute("selectedSessionId", examSessionId);

        String nextSbd = null;
        for (CandidateEnrollmentDTO c : qList) {
            if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                nextSbd = String.valueOf(c.getSbd());
                break;
            }
        }
        session.setAttribute("callingSbd", nextSbd);
        CandidateCallBoardStateDTO state = callBoardService.getState(getServletContext(), examSessionId);
        if (state != null) {
            state.setCallingSbd(nextSbd);
            state.setShiftEnded(false);
        }
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        List<Map<String, String>> sessionAuditLogs
                = (List<Map<String, String>>) session.getAttribute("sessionAuditLogs");
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

        int userId = 3;
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            userId = ((User) userObj).getUserId();
        }
        auditLogService.logAction(userId, action, details, recordId);
    }

    private static Integer parseSbdParam(String raw) {
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
}
