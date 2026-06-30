package controller.staff.examstaff;

import dto.EnrollmentDTO;
import dto.ServiceResult;
import enums.PaymentStatus;
import model.Exam;
import model.Payment;
import service.ExamService;
import service.RegistrationService;
import service.impl.ExamServiceImpl;
import service.impl.RegistrationServiceImpl;

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
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;

@WebServlet("/staff/examstaff/procedure")
public class ProcedureServlet extends HttpServlet {

    // Branch labelled this "Bàn làm thủ tục"; keep the Vietnamese procedure-desk label.
    private static final String PROCEDURE_FEE = "200,000 đ";

    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final ExamService examService = new ExamServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 1. Always load the queue from the Service layer (no legacy in-session queue).
        int examId = resolveExamId(session);
        List<EnrollmentDTO> qList = registrationService.getCandidatesByExam(examId);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedExamId", examId);

        // 2. Resolve SBD and load the profile through the Service layer.
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

        EnrollmentDTO profile = loadProfile(examId, sbdParam, qList);

        // Mark the candidate present the first time the desk opens their file.
        if (profile != null && !profile.isPresent()) {
            ServiceResult<Void> presentResult = registrationService.updatePresent(profile.getId(), true);
            if (presentResult.isSuccess()) {
                profile = reloadProfile(profile.getId());
                qList = registrationService.getCandidatesByExam(examId);
                session.setAttribute("candidateQueue", qList);
            }
        }

        // 3. Resolve the active step from the profile state (no legacy session step).
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

        // 4. Process actions.
        String pAction = request.getParameter("action");

        if ("nextCandidate".equals(pAction)) {
            advanceToNextCandidate(session, examId);
            response.sendRedirect("candidate-call");
            return;
        }

        if ("saveProfile".equals(pAction) && profile != null) {
            String fullName = request.getParameter("fullName");
            String dobStr = request.getParameter("dateOfBirth");
            String govIdNo = request.getParameter("govIdNo");
            String phoneNo = request.getParameter("phoneNo");
            // NOTE: branch also captured "email", but main's Candidate profile has no email
            // column, so it is displayed only and not persisted here.

            Date sqlDob = parseDob(dobStr);
            if (sqlDob == null) {
                sqlDob = profile.getDob();
            }
            ServiceResult<Void> updateResult = registrationService.updateProfile(
                    profile.getId(), fullName, sqlDob, govIdNo, phoneNo);
            if (updateResult.isSuccess()) {
                profile = reloadProfile(profile.getId());
                hasValidPhoto = profile != null && profile.isValidCapturedPhoto();
                qList = registrationService.getCandidatesByExam(examId);
                session.setAttribute("candidateQueue", qList);
                request.setAttribute("profileUpdatedAlert", "true");
            } else {
                request.setAttribute("profileUpdateError", updateResult.getMessage());
            }
        }

        if ("recapture".equals(pAction) && profile != null) {
            registrationService.updatePhoto(profile.getId(), null);
            profile = reloadProfile(profile.getId());
            hasValidPhoto = false;
            session.setAttribute("procedureStep", "2");
            request.setAttribute("step", "2");
            request.setAttribute("hasValidPhoto", false);
        }

        if ("saveCapturedPhoto".equals(pAction)) {
            handleSaveCapturedPhoto(request, response, session, sbdParam, examId);
            return;
        }

        if ("confirmPayment".equals(pAction) && profile != null) {
            processPayment(request, response, session, profile, examId);
            return;
        }

        // Legacy "paymentSuccess" GET flag — routed through the same guarded routine so a
        // candidate already marked paid is never charged twice.
        String paymentSuccessParam = request.getParameter("paymentSuccess");
        if ("true".equals(paymentSuccessParam) && profile != null) {
            processPayment(request, response, session, profile, examId);
            return;
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
        }
        request.setAttribute("currentExam", examService.getById(examId));
        request.getRequestDispatcher("/views/staff/examstaff/procedure.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("saveCapturedPhoto".equals(action)) {
            HttpSession session = request.getSession();
            int examId = resolveExamId(session);
            String sbdParam = resolveSbd(request, session);
            handleSaveCapturedPhoto(request, response, session, sbdParam, examId);
            return;
        }
        if ("confirmPayment".equals(action)) {
            doGet(request, response);
            return;
        }
        doGet(request, response);
    }

    private void processPayment(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, EnrollmentDTO profile, int examId) throws IOException {
        profile = reloadProfile(profile.getId());
        if (profile == null) {
            response.sendRedirect("candidate-call");
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
                request.setAttribute("currentExam", examService.getById(examId));
                request.getRequestDispatcher("/views/staff/examstaff/procedure.jsp").forward(request, response);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }
        if (profile.isPaymentCompleted()) {
            advanceToNextCandidate(session, examId);
            response.sendRedirect("candidate-call");
            return;
        }

        // Record the procedure fee. Prefer an explicit Payment row carrying the 200,000 amount;
        // fall back to the service's payment flag if the row cannot be inserted.
        Payment payment = new Payment();
        int enrollmentId = (profile.getEnrollment() != null)
                ? profile.getEnrollment().getExamEnrollmentId() : 0;
        payment.setExamEnrollmentId(enrollmentId);
        payment.setTotalAmount(200000.00);
        payment.setPaymentStatus(PaymentStatus.COMPLETED.getValue());
        payment.setPaymentMethod("Cash");
        payment.setTransactionReference("REF-" + (System.currentTimeMillis() % 1000000));
        payment.setPaidAt(new Timestamp(System.currentTimeMillis()));

        boolean updatedPay = registrationService.insertPayment(payment);
        if (!updatedPay) {
            updatedPay = registrationService.updatePayment(profile.getId(), true).isSuccess();
        }
        if (!updatedPay) {
            try {
                request.setAttribute("paymentErrorMsg", "Không ghi được thanh toán. Vui lòng thử lại.");
                request.setAttribute("step", "3");
                request.setAttribute("profile", profile);
                request.setAttribute("hasValidPhoto", profile.isValidCapturedPhoto());
                request.setAttribute("currentExam", examService.getById(examId));
                request.getRequestDispatcher("/views/staff/examstaff/procedure.jsp").forward(request, response);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            return;
        }

        profile.setIsPaymentCompleted(true);
        profile.setIsPresent(true);
        registrationService.updatePresent(profile.getId(), true);
        if ("Absent".equalsIgnoreCase(profile.getNotes())) {
            registrationService.clearAbsentMarking(profile.getId());
        }

        // Branch auto-allocated the candidate to a theory room here via ExamAutoAllocator,
        // which has no main-branch equivalent in the ported exam-staff flow; allocation is
        // handled separately by the examiner allocation screens, so it is intentionally omitted.

        List<EnrollmentDTO> fresh = registrationService.getCandidatesByExam(examId);
        session.setAttribute("candidateQueue", fresh);
        session.setAttribute("lastLoadedExamId", examId);
        session.setAttribute("selectedExamId", examId);
        session.setAttribute("procedureJustPaid", "true");
        advanceToNextCandidate(session, examId);
        response.sendRedirect("candidate-call");
    }

    private void handleSaveCapturedPhoto(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String sbdParam, int examId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        EnrollmentDTO profile = loadProfile(examId, sbdParam, null);
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
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                throw new IllegalArgumentException("Ảnh rỗng");
            }

            String uploadDir = request.getServletContext().getRealPath("/") + "assets/imgs/candidates/";
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new java.io.IOException("Không tạo được thư mục lưu ảnh: " + uploadDir);
            }

            String safeSbd = sbdParam.replaceAll("[^A-Za-z0-9\\-]", "_");
            String fileName = safeSbd + "_captured." + ext;
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }

            String photoPath = "assets/imgs/candidates/" + fileName;
            ServiceResult<Void> photoResult = registrationService.updatePhoto(profile.getId(), photoPath);
            if (!photoResult.isSuccess()) {
                throw new java.io.IOException("Không cập nhật được photoUrl trong DB");
            }

            profile = reloadProfile(profile.getId());
            if (profile != null) {
                profile.setValidCapturedPhoto(true);
            }
            List<EnrollmentDTO> fresh = registrationService.getCandidatesByExam(examId);
            session.setAttribute("candidateQueue", fresh);
            session.setAttribute("procedureStep", "2");
            session.setAttribute("lastLoadedExamId", examId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\":true,\"photoUrl\":\"" + photoPath + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Lỗi lưu ảnh";
            response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
        }
    }

    private EnrollmentDTO loadProfile(int examId, String sbdParam, List<EnrollmentDTO> qList) {
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            return null;
        }
        int sbd = parseSbd(sbdParam);
        if (sbd <= 0) {
            return null;
        }
        EnrollmentDTO profile = registrationService.getByExamAndSbd(examId, sbd);
        if (profile == null && qList != null) {
            for (EnrollmentDTO c : qList) {
                if (c.getCandidateNumber() == sbd) {
                    profile = registrationService.getById(c.getId());
                    break;
                }
            }
        }
        return profile;
    }

    private EnrollmentDTO reloadProfile(int candidateId) {
        return registrationService.getById(candidateId);
    }

    private void advanceToNextCandidate(HttpSession session, int examId) {
        session.setAttribute("lastSelectedSbd", null);
        session.setAttribute("procedureStep", "1");
        session.removeAttribute("procedureJustPaid");

        List<EnrollmentDTO> fresh = registrationService.getCandidatesByExam(examId);
        session.setAttribute("candidateQueue", fresh);
        session.setAttribute("lastLoadedExamId", examId);
        session.setAttribute("selectedExamId", examId);

        String nextSbd = null;
        for (EnrollmentDTO c : fresh) {
            if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                nextSbd = String.valueOf(c.getCandidateNumber());
                break;
            }
        }
        session.setAttribute("callingSbd", nextSbd);
        // Branch synced the call board (CandidateCallBoard) here; main has no equivalent, so omitted.
    }

    private int resolveExamId(HttpSession session) {
        Integer selected = (Integer) session.getAttribute("selectedExamId");
        if (selected != null) {
            return selected;
        }
        return 2; // mirrors CandidateCallServlet default exam session.
    }

    private String resolveSbd(HttpServletRequest request, HttpSession session) {
        String sbdParam = request.getParameter("sbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            sbdParam = (String) session.getAttribute("callingSbd");
        }
        return sbdParam;
    }

    private int parseSbd(String sbdParam) {
        if (sbdParam == null) {
            return 0;
        }
        try {
            return Integer.parseInt(sbdParam.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Date parseDob(String dobStr) {
        if (dobStr == null || dobStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dobStr.contains("/")) {
                String[] parts = dobStr.split("/");
                return Date.valueOf(parts[2] + "-" + parts[1] + "-" + parts[0]);
            }
            return Date.valueOf(dobStr);
        } catch (Exception e) {
            return null;
        }
    }
}
