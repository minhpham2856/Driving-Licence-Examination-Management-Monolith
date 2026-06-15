package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.PersonDAO;
import DAO.WalkInCandidateDAO;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.WalkInCandidateDAOImpl;
import Models.ExamRegistration;
import Models.Person;
import Models.ExamSession;
import DAO.ExamSessionDAO;
import DAO.Impl.ExamSessionDAOImpl;

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
import java.util.LinkedHashMap;
import java.util.List;

@WebServlet("/views/staff/examstaff/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 15,      // 15MB
                 maxRequestSize = 1024 * 1024 * 30)   // 30MB
public class UploadServlet extends HttpServlet {

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final WalkInCandidateDAO walkInCandidateDAO = new WalkInCandidateDAOImpl();
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
                    + "SBD-000001,Nguyễn Văn A,15/06/2000,012345678901,B,0987654321,nguyenvana@gmail.com\r\n";
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

                        Person p = personDAO.getByGovIdNo(reg.getGovIdNo());
                        if (p == null) {
                            p = new Person();
                            p.setFullName(reg.getFullName());
                            p.setGovIdNo(reg.getGovIdNo());
                            p.setDateOfBirth(reg.getDateOfBirth());
                            p.setGender(true);
                            
                            // Use imported Phone and Email if provided, else fallback to dynamic defaults
                            String finalPhone = (reg.getPhoneNo() != null && !reg.getPhoneNo().trim().isEmpty())
                                    ? reg.getPhoneNo().trim()
                                    : "09" + (int)(10000000 + Math.random() * 90000000);
                            String finalEmail = (reg.getEmail() != null && !reg.getEmail().trim().isEmpty())
                                    ? reg.getEmail().trim()
                                    : "candidate" + reg.getGovIdNo() + "@dlem.com";
                                    
                            p.setPhoneNo(finalPhone);
                            p.setEmail(finalEmail);
                            p.setAddress("Hà Nội, Việt Nam");
                            p.setIsWalkIn("WalkIn".equals(reg.getRegistrationType()));
                            p.setApprovalStatus("Approved");
                            if (!walkInCandidateDAO.insertWalkIn(p)) {
                                skippedCount++;
                                continue;
                            }
                        } else {
                            p.setFullName(reg.getFullName());
                            p.setDateOfBirth(reg.getDateOfBirth());
                            // Update existing phone/email if new ones are provided
                            if (reg.getPhoneNo() != null && !reg.getPhoneNo().trim().isEmpty()) {
                                p.setPhoneNo(reg.getPhoneNo().trim());
                            }
                            if (reg.getEmail() != null && !reg.getEmail().trim().isEmpty()) {
                                p.setEmail(reg.getEmail().trim());
                            }
                            personDAO.update(p);
                        }

                        Integer existingId = regDAO.findCandidateIdByProfileAndSession(p.getId(), selectedSessionId);
                        boolean regExists = existingId != null;

                        if (regExists) {
                            int regId = existingId;
                            reg.setId(regId);
                            reg.setPersonId(p.getId());
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            regDAO.updatePresent(regId, true);
                            regDAO.updatePhoto(regId, null);
                            importedCount++;
                        } else {
                            reg.setPersonId(p.getId());
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
                ExamSession importSessionForQueue = sessionDAO.getById(selectedSessionId);
                int importExamId = (importSessionForQueue != null && importSessionForQueue.getExamId() > 0)
                        ? importSessionForQueue.getExamId() : selectedSessionId;
                List<ExamRegistration> updatedQueue = regDAO.getCandidatesByExamId(importExamId);
                CandidatePhotoHelper.normalizeQueue(request.getServletContext().getRealPath("/"), updatedQueue, regDAO);
                session.setAttribute("candidateQueue", updatedQueue);
                session.setAttribute("lastLoadedExamId", importExamId);
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

        List<ExamSession> activeSessions = sessionDAO.getActiveSessions();
        request.setAttribute("activeSessions", activeSessions);

        LinkedHashMap<Integer, ExamSession> examOptionMap = new LinkedHashMap<>();
        for (ExamSession s : activeSessions) {
            if (s.getExamId() > 0 && !examOptionMap.containsKey(s.getExamId())) {
                examOptionMap.put(s.getExamId(), s);
            }
        }
        request.setAttribute("examOptions", new ArrayList<>(examOptionMap.values()));

        Integer importSessId = (Integer) session.getAttribute("selectedImportSessionId");
        if (importSessId != null) {
            ExamSession importSession = sessionDAO.getById(importSessId);
            if (importSession != null) {
                request.setAttribute("selectedImportExamId", importSession.getExamId());
                request.setAttribute("importExamLicense", importSession.getLicenseCode());
            }
        }

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
        ExamSession importSession = sessionDAO.getById(selectedSessionId);
        String examLicense = (importSession != null && importSession.getLicenseCode() != null)
                ? importSession.getLicenseCode().trim()
                : "";
        session.setAttribute("selectedImportExamLicense", examLicense);

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
                int a1Count = 24;
                int b2Count = 145;
                boolean hasInvalidRows = false;

                if (examLicense.isEmpty()) {
                    throw new Exception("Không xác định được hạng bằng của kỳ thi đã chọn. Vui lòng chọn lại kỳ thi.");
                }

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length != 7)
                        throw new Exception("Structure mismatch. The imported file must contain exactly 7 columns (SBD, Họ tên, Ngày sinh, CCCD, Hạng GPLX, SĐT, Email).");
                    if (isHeader) { isHeader = false; continue; }

                    String legacySbd   = parts[0].trim();
                    String fullName    = parts[1].trim();
                    String dobStr      = parts[2].trim();
                    String cccd        = parts[3].trim();
                    String csvLicense  = parts[4].trim();
                    String phone       = parts[5].trim();
                    String email       = parts[6].trim();

                    ExamRegistration reg = new ExamRegistration();
                    reg.setFullName(fullName);
                    reg.setGovIdNo(cccd);
                    reg.setLicenseCode(csvLicense);
                    reg.setPhoneNo(phone);
                    reg.setEmail(email);
                    reg.setRegistrationType("WalkIn");
                    reg.setIsPaymentCompleted(false);
                    reg.setIsPresent(true);

                    List<String> errors = collectImportErrors(
                            legacySbd, fullName, dobStr, cccd, csvLicense, phone, email, examLicense);
                    if (!errors.isEmpty()) {
                        reg.setInvalid(true);
                        hasInvalidRows = true;
                        reg.setValidationMessage(String.join("; ", errors));
                    } else {
                        reg.setDateOfBirth(parseDateOfBirth(dobStr));
                        reg.setLicenseCode(examLicense);
                        if ("A1".equalsIgnoreCase(examLicense)) {
                            reg.setCandidateNo(a1Count++);
                        } else {
                            reg.setCandidateNo(b2Count++);
                        }

                        Person existingP = personDAO.getByGovIdNo(cccd);
                        if (existingP != null
                                && regDAO.findCandidateIdByProfileAndSession(existingP.getId(), selectedSessionId) != null) {
                            reg.setDuplicate(true);
                            reg.setValidationMessage("CCCD đã đăng ký kỳ thi này");
                        }
                    }

                    parsedList.add(reg);
                }

                session.setAttribute("previewCandidates", parsedList);
                session.setAttribute("hasInvalidRows", hasInvalidRows);
                session.setAttribute("selectedImportExamLicense", examLicense);
                response.sendRedirect("upload?preview=true");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("uploadError", "Lỗi xử lý tệp: " + e.getMessage());
        }

        response.sendRedirect("upload");
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

    private static List<String> collectImportErrors(String legacySbd, String fullName, String dobStr,
            String cccd, String csvLicense, String phone, String email, String examLicense) {
        List<String> errors = new ArrayList<>();
        if (legacySbd.isEmpty()) {
            errors.add("Thiếu SBD/ID đăng ký (cột 1)");
        }
        if (fullName.isEmpty()) {
            errors.add("Thiếu Họ và tên");
        }
        if (dobStr.isEmpty()) {
            errors.add("Thiếu Ngày sinh");
        } else if (!isParseableDateOfBirth(dobStr)) {
            errors.add("Ngày sinh không đúng định dạng (DD/MM/YYYY)");
        }
        if (cccd.isEmpty()) {
            errors.add("Thiếu CCCD");
        }
        if (csvLicense.isEmpty()) {
            errors.add("Thiếu Hạng GPLX");
        } else if (!licenseMatchesExam(csvLicense, examLicense)) {
            errors.add("Hạng GPLX \"" + csvLicense + "\" không khớp kỳ thi (hạng " + examLicense + ")");
        }
        if (phone.isEmpty()) {
            errors.add("Thiếu Số điện thoại");
        }
        if (email.isEmpty()) {
            errors.add("Thiếu Email");
        }
        return errors;
    }

    private static boolean isParseableDateOfBirth(String dobStr) {
        try {
            parseDateOfBirth(dobStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Date parseDateOfBirth(String dobStr) {
        if (dobStr.contains("/")) {
            String[] dp = dobStr.split("/");
            if (dp.length != 3) {
                throw new IllegalArgumentException("Invalid date");
            }
            return Date.valueOf(dp[2] + "-" + dp[1] + "-" + dp[0]);
        }
        return Date.valueOf(dobStr);
    }

    /** So khớp hạng CSV với hạng kỳ thi (B/B1/B2 cùng nhóm ô tô). */
    private static boolean licenseMatchesExam(String csvLicense, String examLicense) {
        String csv = csvLicense.toUpperCase().trim();
        String exam = examLicense.toUpperCase().trim();
        if (csv.equals(exam)) {
            return true;
        }
        return isCarLicenseFamily(csv) && isCarLicenseFamily(exam);
    }

    private static boolean isCarLicenseFamily(String license) {
        return "B".equals(license) || "B1".equals(license) || "B2".equals(license);
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
