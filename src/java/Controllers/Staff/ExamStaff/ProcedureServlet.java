package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.ExamComputerDAO;
import DAO.Impl.ExamComputerDAOImpl;
import DAO.PaymentDAO;
import DAO.Impl.PaymentDAOImpl;
import Models.ExamRegistration;
import Models.ExamComputer;
import Models.Payment;

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

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamComputerDAO compDAO = new ExamComputerDAOImpl();
    private final PaymentDAO payDAO = new PaymentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 1. Retrieve or load queue
        List<ExamRegistration> qList = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        if (qList == null) {
            try {
                qList = regDAO.getCandidatesBySession(2);
            } catch (Exception e) {
                e.printStackTrace();
                qList = new ArrayList<>();
            }
            session.setAttribute("candidateQueue", qList);
        }

        // 2. Find active profile and detect candidate changes based on resolved SBD
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

        ExamRegistration profile = null;
        if (sbdParam != null && !sbdParam.trim().isEmpty() && qList != null) {
            try {
                for (ExamRegistration c : qList) {
                    if (sbdParam.equals(c.getSbd())) {
                        profile = c;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Resolve active step and persist in session
        String stepParam = request.getParameter("step");
        if (sbdChanged) {
            // Reset to step 1 when candidate changes
            stepParam = "1";
            session.setAttribute("procedureStep", "1");
        }
        if (stepParam == null || stepParam.trim().isEmpty()) {
            stepParam = (String) session.getAttribute("procedureStep");
        }
        if (stepParam == null || stepParam.trim().isEmpty()) {
            if (profile != null) {
                if (profile.isPaymentCompleted()) {
                    stepParam = "3";
                } else if (profile.getPhotoUrl() != null && !profile.getPhotoUrl().isEmpty()) {
                    stepParam = "3";
                } else {
                    stepParam = "1";
                }
            } else {
                stepParam = "1";
            }
        }
        session.setAttribute("procedureStep", stepParam);
        request.setAttribute("step", stepParam);

        // 3. Process actions
        String pAction = request.getParameter("action");
        
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
                    // Update session object
                    profile.setFullName(fullName);
                    profile.setDateOfBirth(sqlDob);
                    profile.setGovIdNo(govIdNo);
                    profile.setEmail(email);
                    profile.setPhoneNo(phoneNo);
                    
                    request.setAttribute("profileUpdatedAlert", "true");
                    addAuditLog(session, "UPDATE on Person", "Sửa đổi lý lịch SBD " + sbdParam);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("recapture".equals(pAction) && profile != null) {
            regDAO.updatePhoto(profile.getId(), null);
            profile.setPhotoUrl("");
            addAuditLog(session, "UPDATE on Person", "Yêu cầu chụp lại ảnh SBD " + sbdParam);
        }

        if ("saveCapturedPhoto".equals(pAction) && profile != null) {
            String base64Data = request.getParameter("photoBase64");
            if (base64Data != null && base64Data.startsWith("data:image/png;base64,")) {
                try {
                    String base64Image = base64Data.split(",")[1];
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                    
                    // Define file save location inside the web application directory
                    String uploadDir = request.getServletContext().getRealPath("/") + "assets/imgs/candidates/";
                    java.io.File dir = new java.io.File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    java.io.File file = new java.io.File(dir, sbdParam + "_captured.png");
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        fos.write(imageBytes);
                    }
                    
                    // Update photo in database and update profile object in session
                    String photoPath = "assets/imgs/candidates/" + sbdParam + "_captured.png";
                    boolean updated = regDAO.updatePhoto(profile.getId(), photoPath);
                    if (updated) {
                        profile.setPhotoUrl(photoPath);
                        addAuditLog(session, "UPDATE on Person", "Lưu ảnh chụp từ webcam thực tế SBD " + sbdParam);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Return 200 OK for AJAX fetch request
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String photoCapturedParam = request.getParameter("photoCaptured");
        if ("true".equals(photoCapturedParam) && profile != null) {
            // Keep dynamic database reference sync
            String photoPath = "assets/imgs/candidates/" + sbdParam + "_captured.png";
            profile.setPhotoUrl(photoPath);
        }

        String paymentSuccessParam = request.getParameter("paymentSuccess");
        if ("true".equals(paymentSuccessParam) && profile != null) {
            // Update Payment in DB
            boolean updatedPay = regDAO.updatePayment(profile.getId(), true);
            if (updatedPay) {
                profile.setIsPaymentCompleted(true);

                // Create billing transaction
                Payment payment = new Payment();
                payment.setExamRegistrationId(profile.getId());
                payment.setAmount(200000.00);
                payment.setPaymentStatus("Completed");
                payment.setPaymentMethod("Cash");
                payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1000000);
                payment.setNotes("Thu lệ phí tại bàn thủ tục");
                payDAO.insert(payment);

                // Auto-allocate a free theory exam computer (PC-01 to PC-30)
                String autoPC = "PC-01";
                List<ExamComputer> availableComps = compDAO.getAvailableComputers();
                if (!availableComps.isEmpty()) {
                    autoPC = availableComps.get(0).getComputerCode();
                    // Set computer InUse in DB
                    compDAO.updateStatus(availableComps.get(0).getId(), "InUse");
                }
                regDAO.updateComputer(profile.getId(), autoPC);
                profile.setComputerCode(autoPC);

                addAuditLog(session, "INSERT on Payment", "Thu lệ phí thi 200,000 đ và Tự động cấp máy " + autoPC + " cho SBD " + sbdParam);
                
                // Clear active calling SBD
                session.setAttribute("callingSbd", null);

                // Auto-call next candidate in the queue
                String nextSbd = null;
                for (ExamRegistration c : qList) {
                    boolean isDone = c.isPaymentCompleted() && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty();
                    if (!isDone) {
                        nextSbd = c.getSbd();
                        break;
                    }
                }
                session.setAttribute("callingSbd", nextSbd);

                // Redirect back to candidatecall
                response.sendRedirect("candidatecall");
                return;
            }
        }

        if (profile != null) {
            request.setAttribute("profile", profile);
        }

        request.getRequestDispatcher("/views/staff/examstaff/procedure.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void addAuditLog(HttpSession session, String action, String details) {
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

        try {
            Models.User user = (Models.User) session.getAttribute("user");
            Integer userId = null;
            if (user != null) {
                userId = user.getId();
            }
            
            // Chuẩn hóa action sang giá trị hợp lệ cho CHECK constraint DB
            String upper = action.toUpperCase();
            String dbAction;
            if (upper.contains("INSERT") || upper.contains("IMPORT")) dbAction = "INSERT";
            else if (upper.contains("DELETE"))                         dbAction = "DELETE";
            else if (upper.contains("EXPORT"))                         dbAction = "EXPORT";
            else                                                        dbAction = "UPDATE";
            
            // Xác định tableName từ action gốc
            String tableName;
            if (upper.contains("PAYMENT"))    tableName = "Payment";
            else if (upper.contains("PERSON")) tableName = "Person";
            else                               tableName = "ExamRegistration";
            
            String sql = "insert into AuditLog (tableName, recordId, action, newValue, changedBy, changedAt) values (?, ?, ?, ?, ?, ?)";
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123");
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ps.setInt(2, 0); // recordId NOT NULL — dùng 0 khi không có entity cụ thể
                ps.setString(3, dbAction);
                ps.setString(4, action + " | " + details);
                int uid = (userId != null && userId > 0) ? userId : 3;
                ps.setInt(5, uid);
                ps.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
