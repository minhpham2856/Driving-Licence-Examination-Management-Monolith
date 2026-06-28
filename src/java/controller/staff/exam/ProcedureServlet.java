package controller.staff.exam;

import service.ExamRegistrationService;

import service.impl.ExamRegistrationServiceImpl;

import dao.PaymentDAO;
import dao.impl.PaymentDAOImpl;

import dto.exam.ExamRegistrationDTO;

import model.payment.Payment;

import service.ExaminerAllocationService;
import service.impl.ExaminerAllocationServiceImpl;

import service.CandidateCallBoardService;
import service.impl.CandidateCallBoardServiceImpl;

import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;

import dto.examiner.AutoAllocateResultDTO;

import dto.candidate.CandidateCallBoardStateDTO;


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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        // 1. Luôn tải hàng đợi từ DB (không dùng candidateQueue cũ trong session)
        int examSessionId = resolveSessionId(session, null, null);
        List<ExamRegistrationDTO> qList = refreshQueueFromDb(session, webRoot, examSessionId);

        // 2. Resolve SBD và load profile trực tiếp từ DB
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

        ExamRegistrationDTO profile = loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
        session.setAttribute("candidateQueue", qList);

        if (profile != null && !profile.isPresent()) {
            boolean updatedPresent = regDAO.updatePresent(profile.getId(), true);
            if (updatedPresent) {
                profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
                session.setAttribute("candidateQueue", qList);
            }
        }

        // 3. Resolve active step từ DB (không dùng procedureStep cũ trong session)
        String stepParam = request.getParameter("step");
        if (sbdChanged) {
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

        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);
        request.setAttribute("hasValidPhoto", hasValidPhoto);

        // 3. Process actions
        String pAction = request.getParameter("action");

        if ("nextCandidate".equals(pAction)) {
            advanceToNextCandidate(session, qList, webRoot, resolveSessionId(session, profile, qList));
            response.sendRedirect("candidatecall");
            return;
        }
        
        if ("saveProfile".equals(pAction) && profile != null) {
            String fullName = request.getParameter("fullName");
            String dobStr = request.getParameter("dateOfBirth");
            String govIdNo = request.getParameter("govIdNo");
            String email = request.getParameter("email");
            String phoneNo = request.getParameter("phoneNo");

            try {
                // Parse date dobStr (expected format dd/MM/yyyy or yyyy-MM-dd)
                Date sqlDob = null;
                if (dobStr.contains("/")) {
                    String[] parts = dobStr.split("/");
                    sqlDob = Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
                } else {
                    sqlDob = Date.valueOf(dobStr);
                }

                // Update database
                boolean updated = regDAO.updateProfile(profile.getId(), fullName, sqlDob, govIdNo, email, phoneNo);
                if (updated) {
                    profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
                    hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
                    session.setAttribute("candidateQueue", qList);
                    request.setAttribute("profileUpdatedAlert", "true");
                    addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + sbdParam);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("recapture".equals(pAction) && profile != null) {
            regDAO.updatePhoto(profile.getId(), null);
            profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
            hasValidPhoto = false;
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
            addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, sbdParam, qList, webRoot);
            return;
        }

        String paymentSuccessParam = request.getParameter("paymentSuccess");
        if ("true".equals(paymentSuccessParam) && profile != null) {
            if (!profile.isValidCapturedPhoto()) {
                request.setAttribute("photoRequiredMsg", "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.");
                request.setAttribute("step", "2");
                session.setAttribute("procedureStep", "2");
                request.setAttribute("hasValidPhoto", false);
                request.setAttribute("profile", profile);
                forwardDeskView(request, response);
                return;
            }
            Payment payment = new Payment();
            payment.setCandidateId(profile.getId());
            payment.setTotalAmount(200000.00);
            payment.setPaymentStatus("Completed");
            payment.setPaymentMethod("Cash");
            payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1000000);
            boolean updatedPay = payDAO.insert(payment);
            if (!updatedPay) {
                updatedPay = regDAO.updatePayment(profile.getId(), true);
            }
            if (updatedPay) {
                profile.setIsPaymentCompleted(true);
                profile.setIsPresent(true);
                regDAO.updatePresent(profile.getId(), true);

                if (profile.isAbsent()) {
                    clearAbsentMarking(profile);
                }

                // Tự động phân bổ phòng thi lý thuyết ngay sau khi hoàn tất hồ sơ (non-UI; thiết bị do Examiner quản lý)
                ExaminerAllocationService allocator = new ExaminerAllocationServiceImpl();
                AutoAllocateResultDTO allocResult = allocator.autoAllocateCandidate(
                        profile.getExamSessionId(), profile.getId());

                qList = regDAO.getCandidatesBySession(profile.getExamSessionId());
                session.setAttribute("candidateQueue", qList);
                session.setAttribute("lastLoadedSessionId", profile.getExamSessionId());
                session.setAttribute("selectedSessionId", profile.getExamSessionId());

                String allocDetail = allocResult.allocatedCount > 0
                        ? " và tự động phân bổ vào phòng thi"
                        : " (chưa phân được phòng - kiểm tra sức chứa phòng thi)";
                addAuditLog(session, "INSERT on Payment",
                        "Thu lệ phí thi 200,000 đ" + allocDetail + " cho SBD " + sbdParam, profile.getId());
                if (allocResult.allocatedCount > 0) {
                    addAuditLog(session, "ALLOCATE Candidates",
                            "Tự động phân bổ phòng thi cho SBD " + sbdParam);
                }

                session.setAttribute("procedureJustPaid", "true");
                advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
                response.sendRedirect("candidatecall");
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
        }

        forwardDeskView(request, response);
    }

    private void forwardDeskView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("deskMode", Boolean.TRUE);
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
            String webRoot = request.getServletContext().getRealPath("/");
            List<ExamRegistrationDTO> qList = loadQueue(session, webRoot);
            String sbdParam = resolveSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, sbdParam, qList, webRoot);
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
            List<ExamRegistrationDTO> qList, String webRoot) throws IOException {
        int examSessionId = resolveSessionId(session, profile, qList);
        profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
        if (profile == null) {
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
                forwardDeskView(request, response);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        if (profile.isPaymentCompleted()) {
            advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
            response.sendRedirect("candidatecall");
            return;
        }
        Payment payment = new Payment();
        payment.setCandidateId(profile.getId());
        payment.setTotalAmount(200000.00);
        payment.setPaymentStatus("Completed");
        payment.setPaymentMethod("Cash");
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
                forwardDeskView(request, response);
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
        ExaminerAllocationService allocator = new ExaminerAllocationServiceImpl();
        AutoAllocateResultDTO allocResult = allocator.autoAllocateCandidate(
                profile.getExamSessionId(), profile.getId());
        qList = regDAO.getCandidatesBySession(profile.getExamSessionId());
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", profile.getExamSessionId());
        session.setAttribute("selectedSessionId", profile.getExamSessionId());
        String allocDetail = allocResult.allocatedCount > 0
                ? " và tự động phân bổ vào phòng thi"
                : " (chưa phân được phòng - kiểm tra sức chứa phòng thi)";
        addAuditLog(session, "INSERT on Payment",
                "Thu lệ phí thi 200,000 đ" + allocDetail + " cho SBD " + sbdParam, profile.getId());
        if (allocResult.allocatedCount > 0) {
            addAuditLog(session, "ALLOCATE Candidates",
                    "Tự động phân bổ phòng thi cho SBD " + sbdParam);
        }
        advanceToNextCandidate(session, qList, webRoot, profile.getExamSessionId());
        response.sendRedirect("candidatecall");
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, List<ExamRegistrationDTO> qList, String webRoot) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        int examSessionId = resolveSessionId(session, null, qList);
        ExamRegistrationDTO profile = loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
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

            String uploadDir = request.getServletContext().getRealPath("/") + "assets/imgs/candidates/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new java.io.IOException("Không tạo được thư mục lưu ảnh: " + uploadDir);
            }

            String safeSbd = sbdParam.replaceAll("[^A-Za-z0-9\\-]", "_");
            String fileName = safeSbd + "_captured." + ext;
            java.io.File file = new java.io.File(dir, fileName);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                fos.write(imageBytes);
            }

            String photoPath = "assets/imgs/candidates/" + fileName;
            boolean updated = regDAO.updatePhoto(profile.getId(), photoPath);
            if (!updated) {
                throw new java.io.IOException("Không cập nhật được photoUrl trong DB");
            }

            profile = reloadProfileAfterMutation(webRoot, examSessionId, profile.getId(), sbdParam, qList);
            if (profile != null) {
                profile.setValidCapturedPhoto(true);
            }
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("procedureStep", "2");
            addAuditLog(session, "UPDATE on Person", "Lưu ảnh chụp từ webcam thực tế SBD " + sbdParam);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\":true,\"photoUrl\":\"" + photoPath + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Lỗi lưu ảnh";
            response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
        }
    }

    private List<ExamRegistrationDTO> refreshQueueFromDb(HttpSession session, String webRoot, int examSessionId) {
        List<ExamRegistrationDTO> qList;
        try {
            qList = regDAO.getCandidatesBySession(examSessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
        photoService.normalizeQueue(webRoot, qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", examSessionId);
        session.setAttribute("selectedSessionId", examSessionId);
        return qList;
    }

    private ExamRegistrationDTO loadProfileFromDb(String webRoot, int examSessionId, String sbdParam,
            List<ExamRegistrationDTO> qList) {
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            return null;
        }
        ExamRegistrationDTO profile = regDAO.getBySessionAndSbd(examSessionId, sbdParam);
        if (profile == null && qList != null) {
            for (ExamRegistrationDTO c : qList) {
                if (sbdParam.equals(c.getSbd())) {
                    profile = regDAO.getById(c.getId());
                    break;
                }
            }
        }
        if (profile != null) {
            CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
            photoService.normalizeQueue(webRoot, java.util.Collections.singletonList(profile));
            syncProfileInQueue(qList, profile);
        }
        return profile;
    }

    private ExamRegistrationDTO reloadProfileAfterMutation(String webRoot, int examSessionId, int candidateId,
            String sbdParam, List<ExamRegistrationDTO> qList) {
        ExamRegistrationDTO fresh = regDAO.getById(candidateId);
        if (fresh == null) {
            return loadProfileFromDb(webRoot, examSessionId, sbdParam, qList);
        }
        CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
        photoService.normalizeQueue(webRoot, java.util.Collections.singletonList(fresh));
        syncProfileInQueue(qList, fresh);
        return fresh;
    }

    private List<ExamRegistrationDTO> loadQueue(HttpSession session, String webRoot) {
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

    private void syncProfileInQueue(List<ExamRegistrationDTO> qList, ExamRegistrationDTO refreshed) {
        for (int i = 0; i < qList.size(); i++) {
            if (qList.get(i).getId() == refreshed.getId()) {
                qList.set(i, refreshed);
                return;
            }
        }
    }

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

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private int resolveSessionId(HttpSession session, ExamRegistrationDTO profile, List<ExamRegistrationDTO> qList) {
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

    private void advanceToNextCandidate(HttpSession session, List<ExamRegistrationDTO> qList,
            String webRoot, int examSessionId) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");

        qList = regDAO.getCandidatesBySession(examSessionId);
        CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
        photoService.normalizeQueue(webRoot, qList);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedSessionId", examSessionId);
        session.setAttribute("selectedSessionId", examSessionId);

        String nextSbd = null;
        for (ExamRegistrationDTO c : qList) {
            if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                nextSbd = c.getSbd();
                break;
            }
        }
        session.setAttribute("callingSbd", nextSbd);
        CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();
        CandidateCallBoardStateDTO state = callBoardService.getState(getServletContext(), examSessionId);
        if (state != null) {
            state.setCallingSbd(nextSbd);
            state.setShiftEnded(false);
        }
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






