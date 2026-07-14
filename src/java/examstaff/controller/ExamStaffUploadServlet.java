package examstaff.controller;

import examstaff.util.StaffAuditLogSupport;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.impl.ExamStaffSessionQueryServiceImpl;
import examstaff.service.impl.StaffAuditLogServiceImpl;
import examstaff.util.ExamStaffPageSupport;
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
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/staff/examstaff/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 15,
        maxRequestSize = 1024 * 1024 * 30)
public class ExamStaffUploadServlet extends HttpServlet {

    private static final int A1_START = 24;
    private static final int B2_START = 145;

    private final ExamStaffSessionQueryServiceImpl sessionQueryService = new ExamStaffSessionQueryServiceImpl();
    private final ExamRegistrationServiceImpl registrationService = new ExamRegistrationServiceImpl();
    private final StaffAuditLogSupport auditLogSupport = new StaffAuditLogSupport(new StaffAuditLogServiceImpl());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        if ("downloadTemplate".equals(action)) {
            writeCsv(response, "danh_sach_mau.csv",
                    "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                            + "SBD-000001,Nguyễn Văn A,15/06/2000,012345678901,B2,0987654321,nguyenvana@gmail.com\r\n");
            return;
        }

        if ("downloadTestFile".equals(action)) {
            writeCsv(response, "danh_sach_thi_sinh_test.csv",
                    "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                            + "SBD-202611,Lê Hoàng Long,12/10/1997,038201999991,B2,0912345678,hoanglong@gmail.com\r\n"
                            + "SBD-202612,Phạm Minh Anh,25/08/2002,038202888882,A1,0987654322,minhanh@gmail.com\r\n");
            return;
        }

        if ("save".equals(action)) {
            savePreview(request, response, session);
            return;
        }

        ExamStaffPageSupport.preparePage(request, false);
        request.setAttribute("activeExams", sessionQueryService.listAllSessions());
        Integer selectedImportExamId = (Integer) session.getAttribute("selectedImportExamId");
        if (selectedImportExamId == null || selectedImportExamId <= 0) {
            int examId = ExamStaffPageSupport.ensureExamId(request, session, ExamStaffPageSupport.loadAllExams());
            session.setAttribute("selectedImportExamId", examId);
        }

        request.getRequestDispatcher("/views/staff/examstaff/upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("uploadError");
        session.removeAttribute("hasInvalidRows");

        int selectedExamId = resolveImportExamId(request, session);

        try {
            Part filePart = request.getPart("fileInput");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                session.setAttribute("uploadedFileName", fileName);

                if (fileName == null
                        || (!fileName.toLowerCase().endsWith(".csv") && !fileName.toLowerCase().endsWith(".txt"))) {
                    throw new Exception("Định dạng tệp không hợp lệ. Chỉ hỗ trợ tệp CSV.");
                }

                byte[] fileBytes = filePart.getInputStream().readAllBytes();
                Charset charset = StandardCharsets.UTF_8;
                if (!isValidUtf8(fileBytes)) {
                    charset = detectFallbackCharset();
                }

                String fileContent = new String(fileBytes, charset);
                if (fileContent.startsWith("\uFEFF")) {
                    fileContent = fileContent.substring(1);
                }

                List<ExamRegistrationDTO> parsedList = parseCsv(fileContent, selectedExamId, session);
                session.setAttribute("previewCandidates", parsedList);
                response.sendRedirect(request.getContextPath() + "/staff/examstaff/upload?preview=true");
                return;
            }
        } catch (Exception e) {
            session.setAttribute("uploadError", "Lỗi xử lý tệp: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/staff/examstaff/upload");
    }

    private void savePreview(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> previewList = (List<ExamRegistrationDTO>) session.getAttribute("previewCandidates");
        int selectedExamId = resolveImportExamId(request, session);

        if (previewList != null && !previewList.isEmpty()) {
            int importedCount = 0;
            int skippedCount = 0;

            for (ExamRegistrationDTO row : previewList) {
                if (row.isInvalid()) {
                    skippedCount++;
                    continue;
                }
                if (row.isDuplicate()) {
                    String dupAction = request.getParameter("dupAction_" + row.getGovIdNo());
                    if ("skip".equals(dupAction)) {
                        skippedCount++;
                        continue;
                    }
                    Integer existingId = registrationService.findCandidateIdByGovIdAndSession(
                            row.getGovIdNo(), selectedExamId);
                    if (existingId != null) {
                        registrationService.updatePresent(existingId, true);
                        importedCount++;
                    }
                    continue;
                }

                row.setExamId(selectedExamId);
                if (registrationService.insert(row)) {
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }

            session.removeAttribute("previewCandidates");
            session.setAttribute("importedCount", importedCount);
            session.setAttribute("importSkippedCount", skippedCount);

            String uploadedFile = (String) session.getAttribute("uploadedFileName");
            if (uploadedFile == null) {
                uploadedFile = "danh_sach.csv";
            }
            auditLogSupport.persist(session, "IMPORT Candidates",
                    "Import CSV \"" + uploadedFile + "\": nhập " + importedCount
                            + " thí sinh vào ca #" + selectedExamId
                            + (skippedCount > 0 ? ", bỏ qua " + skippedCount + " dòng" : ""));

            response.sendRedirect(request.getContextPath() + "/staff/examstaff/upload?importSuccess=true");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/staff/examstaff/upload");
    }

    private int resolveImportExamId(HttpServletRequest request, HttpSession session) {
        String sessionParam = request.getParameter("examExamId");
        if (sessionParam != null && !sessionParam.isBlank()) {
            try {
                int parsed = Integer.parseInt(sessionParam.trim());
                if (parsed > 0) {
                    session.setAttribute("selectedImportExamId", parsed);
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Integer stored = (Integer) session.getAttribute("selectedImportExamId");
        if (stored != null && stored > 0) {
            return stored;
        }
        int examId = ExamStaffPageSupport.ensureExamId(request, session, ExamStaffPageSupport.loadAllExams());
        session.setAttribute("selectedImportExamId", examId);
        return examId;
    }

    private List<ExamRegistrationDTO> parseCsv(String fileContent, int selectedExamId, HttpSession session)
            throws Exception {
        List<ExamRegistrationDTO> parsedList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(fileContent));

        String line;
        boolean isHeader = true;
        int a1Count = A1_START;
        int b2Count = B2_START;
        boolean hasInvalidRows = false;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length != 7) {
                throw new Exception("Cấu trúc không khớp. Tệp phải có đúng 7 cột "
                        + "(SBD, Họ tên, Ngày sinh, CCCD, Hạng GPLX, SĐT, Email).");
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

            ExamRegistrationDTO row = new ExamRegistrationDTO();
            row.setFullName(fullName);
            row.setGovIdNo(cccd);
            row.setLicenseCode(licenseCode.isEmpty() ? "B2" : licenseCode);
            row.setPhoneNo(phone);
            row.setEmail(email);
            row.setRegistrationType("WalkIn");
            row.setIsPaymentCompleted(false);
            row.setIsPresent(true);

            if (fullName.isEmpty() || cccd.isEmpty()) {
                row.setInvalid(true);
                hasInvalidRows = true;
                List<String> missing = new ArrayList<>();
                if (fullName.isEmpty()) {
                    missing.add("Họ tên");
                }
                if (cccd.isEmpty()) {
                    missing.add("CCCD");
                }
                StringBuilder sb = new StringBuilder("Thiếu ");
                for (int i = 0; i < missing.size(); i++) {
                    if (i > 0) {
                        sb.append(" & ");
                    }
                    sb.append(missing.get(i));
                }
                row.setValidationMessage(sb.toString());
            }

            Date sqlDob;
            try {
                if (dobStr.contains("/")) {
                    String[] dp = dobStr.split("/");
                    sqlDob = Date.valueOf(dp[2] + "-" + dp[1] + "-" + dp[0]);
                } else {
                    sqlDob = Date.valueOf(dobStr);
                }
            } catch (Exception e) {
                sqlDob = Date.valueOf("2000-01-01");
            }
            row.setDateOfBirth(sqlDob);

            if ("A1".equalsIgnoreCase(licenseCode)) {
                row.setCandidateNo(a1Count++);
            } else {
                row.setCandidateNo(b2Count++);
            }

            if (!cccd.isEmpty()) {
                Integer existingId = registrationService.findCandidateIdByGovIdAndSession(cccd, selectedExamId);
                if (existingId != null) {
                    row.setDuplicate(true);
                }
            }

            parsedList.add(row);
        }

        session.setAttribute("hasInvalidRows", hasInvalidRows);
        return parsedList;
    }

    private void writeCsv(HttpServletResponse response, String fileName, String csvData) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        response.getOutputStream().write(bom);
        response.getOutputStream().write(csvData.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    private Charset detectFallbackCharset() {
        String[] names = {"Cp1258", "Windows-1258", "Cp1252"};
        for (String name : names) {
            try {
                return Charset.forName(name);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }

    private boolean isValidUtf8(byte[] bytes) {
        int i = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
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
                if (i + 2 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80
                        || (bytes[i + 2] & 0xC0) != 0x80) {
                    return false;
                }
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80
                        || (bytes[i + 2] & 0xC0) != 0x80 || (bytes[i + 3] & 0xC0) != 0x80) {
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
