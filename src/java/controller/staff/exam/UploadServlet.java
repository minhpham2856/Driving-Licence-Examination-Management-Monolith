package controller.staff.exam;
import dto.*;
import model.*;
import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import model.*;
import service.*;
import service.impl.*;
import service.ExamRegistrationService;
import service.ExamSessionControlService;
import service.impl.ExamRegistrationServiceImpl;
import service.impl.ExamSessionControlServiceImpl;
import dto.CandidateEnrollmentDTO;
import dto.UploadRecordDTO;
import dto.SessionDTO;
import model.Profile;
import model.User;
import util.UsernameGenerator;
import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;
import service.RoleService;
import service.impl.RoleServiceImpl;
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
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
@WebServlet("/views/staff/exam/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 15, // 15MB
        maxRequestSize = 1024 * 1024 * 30)   // 30MB
public class UploadServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final ExamSessionControlService sessionService = new ExamSessionControlServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    private final RoleService roleService = new RoleServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        if ("cancel".equals(action)) {
            session.removeAttribute("previewCandidates");
            session.removeAttribute("hasInvalidRows");
            session.removeAttribute("uploadedFileName");
            response.sendRedirect("upload");
            return;
        }
        if ("downloadTemplate".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"danh_sach_mau.csv\"");
            // Write UTF-8 BOM explicitly
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
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
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
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
            List<UploadRecordDTO> previewList = (List<UploadRecordDTO>) session.getAttribute("previewCandidates");
            Integer selectedSessionId = (Integer) session.getAttribute("selectedImportSessionId");
            if (selectedSessionId == null) {
                selectedSessionId = 2;
            }
            if (previewList != null && !previewList.isEmpty()) {
                int importedCount = 0;
                int skippedCount = 0;
                for (UploadRecordDTO reg : previewList) {
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
                        Integer existingId = regService.findCandidateIdByProfileAndSession(profile.getId(), selectedSessionId);
                        boolean regExists = existingId != null;
                        if (regExists) {
                            int regId = existingId;
                            reg.setId(regId);
                            reg.setPersonId(profile.getId());
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            regService.updatePresent(regId, true);
                            regService.updatePhoto(regId, null);
                            importedCount++;
                        } else {
                            reg.setPersonId(profile.getId());
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            if (regService.insert(reg)) {
                                regService.updatePhoto(reg.getId(), null);
                                importedCount++;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error importing: " + reg.getFullName() + " - " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                session.removeAttribute("previewCandidates");
                List<CandidateEnrollmentDTO> updatedQueue = regService.getCandidatesBySession(selectedSessionId);
                photoService.normalizeQueue(request.getServletContext().getRealPath("/"), updatedQueue);
                session.setAttribute("candidateQueue", updatedQueue);
                session.setAttribute("lastLoadedSessionId", selectedSessionId);
                session.setAttribute("importedCount", importedCount);
                String uploadedFile = (String) session.getAttribute("uploadedFileName");
                if (uploadedFile == null) {
                    uploadedFile = "danh_sach.csv";
                }
                SessionDTO importSession = sessionService.getSessionById(selectedSessionId);
                String sessionLabel = importSession != null ? importSession.getSessionName() : ("SessionId " + selectedSessionId);
                String auditDetails = "Import CSV \"" + uploadedFile + "\": nhập " + importedCount
                        + " thí sinh vào ca " + sessionLabel + " (SessionId=" + selectedSessionId + ")"
                        + (skippedCount > 0 ? ", bỏ qua " + skippedCount + " dòng" : "");
                addAuditLog(session, "IMPORT Candidates", auditDetails, selectedSessionId);
                response.sendRedirect("upload?importSuccess=true");
                return;
            }
        }
        request.setAttribute("activeSessions", sessionService.getActiveSessions());
        request.getRequestDispatcher("/views/staff/exam/upload.jsp").forward(request, response);
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
            try {
                selectedSessionId = Integer.parseInt(sessionParam);
            } catch (Exception e) {
                /* ignore */ }
        }
        session.setAttribute("selectedImportSessionId", selectedSessionId);
        Part filePart = request.getPart("fileInput");
        try {
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                session.setAttribute("uploadedFileName", fileName);
                if (!fileName.toLowerCase().endsWith(".csv") && !fileName.toLowerCase().endsWith(".txt")) {
                    throw new Exception("Invalid file extension. Only CSV format is supported.");
                }
                byte[] fileBytes = filePart.getInputStream().readAllBytes();
                Charset charset = StandardCharsets.UTF_8;
                if (!isValidUTF8(fileBytes)) {
                    try {
                        charset = Charset.forName("Cp1258");
                    } catch (Exception e) {
                        try {
                            charset = Charset.forName("Windows-1258");
                        } catch (Exception e2) {
                            charset = Charset.forName("Cp1252");
                        }
                    }
                }
                String fileContent = new String(fileBytes, charset);
                if (fileContent.startsWith("\uFEFF")) {
                    fileContent = fileContent.substring(1);
                }
                List<UploadRecordDTO> parsedList = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new StringReader(fileContent));
                String line;
                boolean isHeader = true;
                int a1Count = 24;
                int b2Count = 145;
                boolean hasInvalidRows = false;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length != 7) {
                        throw new Exception("Structure mismatch. The imported file must contain exactly 7 columns (SBD, Họ và tên, Ngày sinh, CCCD, Hạng GPLX, SĐT, Email).");
                    }
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }
                    String fullName = parts[1].trim();
                    String dobStr = parts[2].trim();
                    String cccd = parts[3].trim();
                    String licenseCode = parts[4].trim();
                    String phone = parts[5].trim();
                    String email = parts[6].trim();
                    UploadRecordDTO reg = new UploadRecordDTO();
                    reg.setFullName(fullName);
                    reg.setGovIdNo(cccd);
                    reg.setLicenseCode(licenseCode.isEmpty() ? "B2" : licenseCode);
                    reg.setPhoneNo(phone);
                    reg.setEmail(email);
                    reg.setRegistrationType("WalkIn");
                    reg.setIsPaymentCompleted(false);
                    reg.setIsPresent(true);
                    // Validate required fields (including Phone and Email per user request)
                    if (fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        reg.setInvalid(true);
                        hasInvalidRows = true;
                        List<String> missing = new ArrayList<>();
                        if (fullName.isEmpty()) {
                            missing.add("Họ và tên");
                        }
                        if (cccd.isEmpty()) {
                            missing.add("CCCD");
                        }
                        if (phone.isEmpty()) {
                            missing.add("SĐT");
                        }
                        if (email.isEmpty()) {
                            missing.add("Email");
                        }
                        reg.setValidationMessage("Thiếu " + String.join(" & ", missing));
                    }
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
                    // Auto-generate SBD
                    if ("A1".equalsIgnoreCase(licenseCode)) {
                        reg.setCandidateNo(a1Count++);
                    } else {
                        reg.setCandidateNo(b2Count++);
                    }
                    // Duplicate check (only if CCCD is valid)
                    if (!cccd.isEmpty()) {
                        Profile existingProfile = regService.getProfileByGovId(cccd);
                        if (existingProfile != null) {
                            if (regService.findCandidateIdByProfileAndSession(existingProfile.getId(), selectedSessionId) != null) {
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
            if (session.getAttribute("uploadError") == null) {
                session.setAttribute("uploadError", "Vui lòng chọn tệp CSV để tải lên.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("uploadError", "Lỗi xử lý tập: " + e.getMessage());
        }
        response.sendRedirect("upload");
    }
    private Profile ensureProfileForImport(UploadRecordDTO reg) {
        Profile profile = regService.getProfileByGovId(reg.getGovIdNo());
        String finalPhone = (reg.getPhoneNo() != null && !reg.getPhoneNo().isBlank())
                ? reg.getPhoneNo().trim()
                : "09" + (int) (10000000 + Math.random() * 90000000);
        String finalEmail = (reg.getEmail() != null && !reg.getEmail().isBlank())
                ? reg.getEmail().trim()
                : "candidate" + reg.getGovIdNo() + "@dlem.com";
        if (profile == null) {
            User user = new User();
            user.setUsername(generateUniqueUsername(reg.getFullName()));
            user.setEmail(finalEmail);
            user.setPasswordHash(UsernameGenerator.randomPassword(10));
            user.setActive(true);
            user.setRoleId(roleService.getRoleIdByName(enums.UserRole.NGUOI_DANG_KY_THI.getDisplayName()));
            if (!regService.insertUser(user)) {
                return null;
            }
            profile = new Profile();
            profile.setUserId(user.getUserId());
            profile.setFullName(reg.getFullName());
            profile.setGovernmentIdNumber(reg.getGovIdNo());
            profile.setDateOfBirth(reg.getDateOfBirth() != null ? new Timestamp(reg.getDateOfBirth().getTime()) : null);
            profile.setSex(true);
            profile.setPhoneNumber(finalPhone);
            profile.setAddress("Hà Nội, Việt Nam");
            if (!regService.insertProfile(profile)) {
                return null;
            }
        } else {
            profile.setFullName(reg.getFullName());
            profile.setDateOfBirth(reg.getDateOfBirth() != null ? new Timestamp(reg.getDateOfBirth().getTime()) : null);
            if (reg.getPhoneNo() != null && !reg.getPhoneNo().isBlank()) {
                profile.setPhoneNumber(reg.getPhoneNo().trim());
            }
            regService.updateProfile(profile);
        }
        return profile;
    }
    private String generateUniqueUsername(String fullName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = UsernameGenerator.generateFromFullName(fullName);
            if (regService.getUserByUsername(username) == null) {
                return username;
            }
        }
        return UsernameGenerator.generateFromFullName(fullName) + System.currentTimeMillis() % 1000;
    }
    private void addAuditLog(HttpSession session, String action, String details) {
        addAuditLog(session, action, details, 0);
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
        auditLogService.logAction(((User) session.getAttribute("user")).getUserId(), action, details, recordId);
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
                if (i + 1 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80) {
                    return false;
                }
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80) {
                    return false;
                }
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80 || (bytes[i + 3] & 0xC0) != 0x80) {
                    return false;
                }
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }
}
