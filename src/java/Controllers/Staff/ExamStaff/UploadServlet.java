package Controllers.Staff.ExamStaff;

import Constants.Db2Mappings;
import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.ProfileDAO;
import DAO.UserDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import DAO.Impl.ProfileDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.ExamRegistration;
import Models.ExamSession;
import Models.Profile;
import Models.User;
import Utils.UsernameGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 15,      // 15MB
                 maxRequestSize = 1024 * 1024 * 30)   // 30MB
public class UploadServlet extends HttpServlet {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        if ("downloadTemplate".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"danh_sach_mau.csv\"");
            
            // Write UTF-8 BOM explicitly
            byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            response.getOutputStream().write(bom);
            
            // Write template data
            String csvData = "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                    + "SBD-000001,Nguyễn Văn A,15/06/2000,012345678901,B2,0987654321,nguyenvana@gmail.com\r\n";
            response.getOutputStream().write(csvData.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }

        if ("downloadTestFile".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"danh_sach_thi_sinh_test.csv\"");
            
            // Write UTF-8 BOM explicitly
            byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            response.getOutputStream().write(bom);
            
            // Write test file data
            String csvData = "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                    + "SBD-202611,Lê Hoàng Long,12/10/1997,038201999991,B2,0912345678,hoanglong@gmail.com\r\n"
                    + "SBD-202612,Phạm Minh Anh,25/08/2002,038202888882,A1,0987654322,minhanh@gmail.com\r\n"
                    + "SBD-202613,Nguyễn Trung Kiên,04/05/1995,038203777773,B2,0901234567,trungkien@gmail.com\r\n"
                    + "SBD-202614,Hoàng Thu Thủy,18/02/1998,038204666664,A1,0934567890,thuthuy@gmail.com\r\n"
                    + "SBD-202615,Trần Đức Thắng,30/11/1996,038205555555,B2,0945678901,ducthang@gmail.com\r\n";
            response.getOutputStream().write(csvData.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
            return;
        }

        // UC-01 Normal Flow Step 6: Confirm & save from preview
        if ("save".equals(action)) {
            List<ExamRegistration> previewList = (List<ExamRegistration>) session.getAttribute("previewCandidates");
            Integer selectedSessionId = (Integer) session.getAttribute("selectedImportSessionId");
            if (selectedSessionId == null) selectedSessionId = 2;

            if (previewList != null && !previewList.isEmpty()) {
                int importedCount = 0;
                int skippedCount = 0;
                for (ExamRegistration reg : previewList) {
                    try {
                        String dupAction = request.getParameter("dupAction_" + reg.getGovIdNo());
                        if (reg.isDuplicate() && "skip".equals(dupAction)) {
                            skippedCount++;
                            continue;
                        }
                        if (reg.isInvalid()) {
                            skippedCount++;
                            continue;
                        }

                        Profile profile = ensureProfileForImport(reg);
                        if (profile == null) {
                            skippedCount++;
                            continue;
                        }

                        Integer existingId = regDAO.findCandidateIdByProfileAndSession(profile.getId(), selectedSessionId);
                        boolean regExists = existingId != null;

                        if (regExists) {
                            int regId = existingId;
                            reg.setId(regId);
                            reg.setPersonId(profile.getId());
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            regDAO.updatePresent(regId, true);
                            regDAO.updatePhoto(regId, null);
                            if (reg.getCandidateNumber() != null && !reg.getCandidateNumber().isBlank()) {
                                regDAO.updateCandidateNumber(regId, reg.getCandidateNumber());
                            }
                            importedCount++;
                        } else {
                            reg.setPersonId(profile.getId());
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            if (regDAO.insert(reg)) {
                                regDAO.updatePhoto(reg.getId(), null);
                                importedCount++;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error importing: " + reg.getFullName() + " - " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }

                session.removeAttribute("previewCandidates");
                List<ExamRegistration> updatedQueue = regDAO.getCandidatesBySession(selectedSessionId);
                CandidatePhotoHelper.normalizeQueue(request.getServletContext().getRealPath("/"), updatedQueue, regDAO);
                session.setAttribute("candidateQueue", updatedQueue);
                session.setAttribute("lastLoadedSessionId", selectedSessionId);
                session.setAttribute("importedCount", importedCount);

                String uploadedFile = (String) session.getAttribute("uploadedFileName");
                if (uploadedFile == null) {
                    uploadedFile = "danh_sach.csv";
                }
                ExamSession importSession = sessionDAO.getById(selectedSessionId);
                String sessionLabel = importSession != null ? importSession.getSessionName() : ("SessionId " + selectedSessionId);
                String auditDetails = "Import CSV \"" + uploadedFile + "\": nhập " + importedCount
                        + " thí sinh vào ca " + sessionLabel + " (SessionId=" + selectedSessionId + ")"
                        + (skippedCount > 0 ? ", bỏ qua " + skippedCount + " dòng" : "");
                addAuditLog(session, "IMPORT Candidates", auditDetails, selectedSessionId);

                response.sendRedirect("upload?importSuccess=true");
                return;
            }
        }

        request.setAttribute("activeSessions", sessionDAO.getActiveSessions());
        request.getRequestDispatcher("/views/staff/examstaff/upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        session.removeAttribute("uploadError");
        session.removeAttribute("hasInvalidRows");

        String sessionParam = request.getParameter("examSessionId");
        int selectedSessionId = 2;
        if (sessionParam != null && !sessionParam.isEmpty()) {
            try { selectedSessionId = Integer.parseInt(sessionParam); } catch (Exception e) { /* ignore */ }
        }
        session.setAttribute("selectedImportSessionId", selectedSessionId);

        try {
            Part filePart = request.getPart("fileInput");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                session.setAttribute("uploadedFileName", fileName);

                if (!fileName.toLowerCase().endsWith(".csv") && !fileName.toLowerCase().endsWith(".txt")) {
                    throw new Exception("Invalid file extension. Only CSV format is supported.");
                }

                byte[] fileBytes = filePart.getInputStream().readAllBytes();
                
                java.nio.charset.Charset charset = StandardCharsets.UTF_8;
                if (!isValidUTF8(fileBytes)) {
                    try {
                        charset = java.nio.charset.Charset.forName("Cp1258");
                    } catch (Exception e) {
                        try {
                            charset = java.nio.charset.Charset.forName("Windows-1258");
                        } catch (Exception e2) {
                            charset = java.nio.charset.Charset.forName("Cp1252");
                        }
                    }
                }
                
                String fileContent = new String(fileBytes, charset);
                if (fileContent.startsWith("\uFEFF")) {
                    fileContent = fileContent.substring(1);
                }
                
                List<ExamRegistration> parsedList = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new java.io.StringReader(fileContent));

                String line;
                boolean isHeader = true;
                boolean hasInvalidRows = false;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length != 7)
                        throw new Exception("Structure mismatch. The imported file must contain exactly 7 columns (SBD, Họ tên, Ngày sinh, CCCD, Hạng GPLX, SĐT, Email).");
                    if (isHeader) { isHeader = false; continue; }

                    String sbdRaw        = parts[0].trim();
                    String fullName    = parts[1].trim();
                    String dobStr      = parts[2].trim();
                    String cccd        = parts[3].trim();
                    String licenseCode = parts[4].trim();
                    String phone       = parts[5].trim();
                    String email       = parts[6].trim();

                    ExamRegistration reg = new ExamRegistration();
                    reg.setFullName(fullName);
                    reg.setGovIdNo(cccd);
                    reg.setLicenseCode(licenseCode.isEmpty() ? "B2" : licenseCode);
                    reg.setPhoneNo(phone);
                    reg.setEmail(email);
                    reg.setRegistrationType("WalkIn");
                    reg.setIsPaymentCompleted(false);
                    reg.setIsPresent(true);

                    // Validate required fields (including SBD, Phone and Email per user request)
                    if (sbdRaw.isEmpty() || fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        reg.setInvalid(true);
                        hasInvalidRows = true;
                        java.util.List<String> missing = new java.util.ArrayList<>();
                        if (sbdRaw.isEmpty())   missing.add("SBD");
                        if (fullName.isEmpty()) missing.add("Họ tên");
                        if (cccd.isEmpty())     missing.add("CCCD");
                        if (phone.isEmpty())    missing.add("SĐT");
                        if (email.isEmpty())    missing.add("Email");
                        reg.setValidationMessage("Thiếu " + String.join(" & ", missing));
                    }

                    reg.setCandidateNumber(sbdRaw);
                    reg.setCandidateNo(Db2Mappings.parseCandidateNo(sbdRaw));

                    // Parse DOB
                    try {
                        Date sqlDob;
                        if (dobStr.contains("/")) {
                            String[] dp = dobStr.split("/");
                            sqlDob = Date.valueOf(dp[2] + "-" + dp[1] + "-" + dp[0]);
                        } else {
                            sqlDob = Date.valueOf(dobStr);
                        }
                        reg.setDateOfBirth(sqlDob);
                    } catch (Exception e) {
                        reg.setDateOfBirth(Date.valueOf("2000-01-01"));
                    }

                    // Duplicate check (only if CCCD is valid)
                    if (!cccd.isEmpty()) {
                        Profile existingProfile = profileDAO.getByGovIdNo(cccd);
                        if (existingProfile != null) {
                            if (regDAO.findCandidateIdByProfileAndSession(existingProfile.getId(), selectedSessionId) != null) {
                                reg.setDuplicate(true);
                            }
                        }
                    }

                    parsedList.add(reg);
                }

                session.setAttribute("previewCandidates", parsedList);
                session.setAttribute("hasInvalidRows", hasInvalidRows);
                response.sendRedirect("upload?preview=true");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("uploadError", "Lỗi xử lý tệp: " + e.getMessage());
        }

        response.sendRedirect("upload");
    }

    private Profile ensureProfileForImport(ExamRegistration reg) {
        Profile profile = profileDAO.getByGovIdNo(reg.getGovIdNo());

        String finalPhone = (reg.getPhoneNo() != null && !reg.getPhoneNo().trim().isEmpty())
                ? reg.getPhoneNo().trim()
                : "09" + (int) (10000000 + Math.random() * 90000000);
        String finalEmail = (reg.getEmail() != null && !reg.getEmail().trim().isEmpty())
                ? reg.getEmail().trim()
                : "candidate" + reg.getGovIdNo() + "@dlem.com";

        if (profile == null) {
            User user = new User();
            user.setUsername(generateUniqueUsername(reg.getFullName()));
            user.setEmail(finalEmail);
            user.setPasswordHash(UsernameGenerator.randomPassword(10));
            user.setIsActive(true);
            user.setRole(Db2Mappings.roleFromName("Registrant"));

            if (!userDAO.insert(user)) {
                return null;
            }

            profile = new Profile();
            profile.setUserId(user.getId());
            profile.setFullName(reg.getFullName());
            profile.setGovIdNo(reg.getGovIdNo());
            profile.setDateOfBirth(reg.getDateOfBirth());
            profile.setGender(true);
            profile.setPhoneNo(finalPhone);
            profile.setAddress("Hà Nội, Việt Nam");

            if (!profileDAO.insert(profile)) {
                return null;
            }
        } else {
            profile.setFullName(reg.getFullName());
            profile.setDateOfBirth(reg.getDateOfBirth());
            if (reg.getPhoneNo() != null && !reg.getPhoneNo().trim().isEmpty()) {
                profile.setPhoneNo(reg.getPhoneNo().trim());
            }
            profileDAO.update(profile);
        }

        return profile;
    }

    private String generateUniqueUsername(String fullName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = UsernameGenerator.generateFromFullName(fullName);
            if (userDAO.getByUsername(username) == null) {
                return username;
            }
        }
        return UsernameGenerator.generateFromFullName(fullName) + System.currentTimeMillis() % 1000;
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        List<java.util.Map<String, String>> sessionAuditLogs = (List<java.util.Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        java.util.Map<String, String> audit = new java.util.HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", action);
        audit.put("details", details);
        sessionAuditLogs.add(0, audit);

        Utils.AuditLogHelper.persist(session, action, details, recordId);
    }

    private boolean isValidUTF8(byte[] bytes) {
        int i = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;
            if (b <= 0x7F) {
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                if (i + 1 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80 || (bytes[i + 3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }
}
