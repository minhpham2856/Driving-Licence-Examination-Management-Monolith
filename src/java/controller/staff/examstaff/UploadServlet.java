package controller.staff.examstaff;

import dto.ServiceResult;
import dto.UploadRowDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import service.RegistrationService;
import service.impl.RegistrationServiceImpl;

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
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 15,      // 15MB
                 maxRequestSize = 1024 * 1024 * 30)   // 30MB
public class UploadServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final RegistrationService registrationService = new RegistrationServiceImpl();

    // Default target session, matching the branch's default.
    private static final int DEFAULT_SESSION_ID = 2;
    // Auto SBD counters (mirroring the branch's per-license counters).
    private static final int A1_START = 24;
    private static final int B2_START = 145;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

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
                    + "SBD-202612,Phạm Minh Anh,25/08/2002,038202888882,A1,0987654322,minhanh@gmail.com\r\n"
                    + "SBD-202613,Nguyễn Trung Kiên,04/05/1995,038203777773,B2,0901234567,trungkien@gmail.com\r\n"
                    + "SBD-202614,Hoàng Thu Thủy,18/02/1998,038204666664,A1,0934567890,thuthuy@gmail.com\r\n"
                    + "SBD-202615,Trần Đức Thắng,30/11/1996,038205555555,B2,0945678901,ducthang@gmail.com\r\n");
            return;
        }

        // UC-01 Normal Flow Step 6: Confirm & save from preview.
        if ("save".equals(action)) {
            savePreview(request, response, session);
            return;
        }

        // Default: render the upload page. The active-exam dropdown previously came from
        // SessionService (now removed); it is intentionally dropped here.
        request.getRequestDispatcher("/views/staff/examstaff/upload.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        session.removeAttribute("uploadError");
        session.removeAttribute("hasInvalidRows");

        int selectedExamId = DEFAULT_SESSION_ID;
        String sessionParam = request.getParameter("examExamId");
        if (sessionParam != null && !sessionParam.isEmpty()) {
            try {
                selectedExamId = Integer.parseInt(sessionParam);
            } catch (NumberFormatException e) {
                // Keep the default when the param is not a valid number.
            }
        }
        session.setAttribute("selectedImportExamId", selectedExamId);

        try {
            Part filePart = request.getPart("fileInput");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                session.setAttribute("uploadedFileName", fileName);

                if (!fileName.toLowerCase().endsWith(".csv")
                        && !fileName.toLowerCase().endsWith(".txt")) {
                    throw new Exception("Định dạng tệp không hợp lệ. Chỉ hỗ trợ tệp CSV.");
                }

                byte[] fileBytes = filePart.getInputStream().readAllBytes();

                // Detect charset; fall back to a Vietnamese codepage if not valid UTF-8.
                Charset charset = StandardCharsets.UTF_8;
                if (!isValidUtf8(fileBytes)) {
                    charset = detectFallbackCharset();
                }

                String fileContent = new String(fileBytes, charset);
                if (fileContent.startsWith("﻿")) {
                    fileContent = fileContent.substring(1);
                }

                List<UploadRowDTO> parsedList = parseCsv(fileContent, selectedExamId, session);
                session.setAttribute("previewCandidates", parsedList);
                response.sendRedirect("upload?preview=true");
                return;
            }
        } catch (Exception e) {
            session.setAttribute("uploadError", "Lỗi xử lý tệp: " + e.getMessage());
        }

        response.sendRedirect("upload");
    }

    // Parse the CSV content into a list of upload rows (preview candidates).
    private List<UploadRowDTO> parseCsv(String fileContent, int selectedExamId, HttpSession session)
            throws Exception {
        List<UploadRowDTO> parsedList = new ArrayList<>();
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

            UploadRowDTO row = new UploadRowDTO();
            row.setFullName(fullName);
            row.setGovIdNo(cccd);
            row.setLicenseCode(licenseCode.isEmpty() ? "B2" : licenseCode);
            row.setPhoneNo(phone);
            row.setEmail(email);
            row.setRegistrationType("WalkIn");
            row.setIsPaymentCompleted(false);
            row.setIsPresent(true);

            // Validate required fields (Họ tên, CCCD, SĐT, Email).
            if (fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                row.setInvalid(true);
                hasInvalidRows = true;
                List<String> missing = new ArrayList<>();
                if (fullName.isEmpty()) missing.add("Họ tên");
                if (cccd.isEmpty()) missing.add("CCCD");
                if (phone.isEmpty()) missing.add("SĐT");
                if (email.isEmpty()) missing.add("Email");
                StringBuilder sb = new StringBuilder("Thiếu ");
                for (int i = 0; i < missing.size(); i++) {
                    if (i > 0) sb.append(" & ");
                    sb.append(missing.get(i));
                }
                row.setValidationMessage(sb.toString());
            }

            // Parse date of birth (DD/MM/YYYY or YYYY-MM-DD).
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

            // Auto-generate SBD counter per license.
            if ("A1".equalsIgnoreCase(licenseCode)) {
                row.setCandidateNo(a1Count++);
            } else {
                row.setCandidateNo(b2Count++);
            }

            // Duplicate check: already enrolled in the target session.
            if (!cccd.isEmpty()) {
                Integer existingId =
                        registrationService.findCandidateIdByGovIdAndExam(cccd, selectedExamId);
                if (existingId != null) {
                    row.setDuplicate(true);
                }
            }

            parsedList.add(row);
        }

        session.setAttribute("hasInvalidRows", hasInvalidRows);
        return parsedList;
    }

    // Confirm the preview list and persist each row via the service layer.
    private void savePreview(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {

        List<UploadRowDTO> previewList = (List<UploadRowDTO>) session.getAttribute("previewCandidates");
        int selectedExamId = DEFAULT_SESSION_ID;
        Integer stored = (Integer) session.getAttribute("selectedImportExamId");
        if (stored != null) {
            selectedExamId = stored;
        }

        if (previewList != null && !previewList.isEmpty()) {
            int importedCount = 0;
            int skippedCount = 0;

            for (UploadRowDTO row : previewList) {
                // Skip rows with missing required data.
                if (row.isInvalid()) {
                    skippedCount++;
                    continue;
                }

                // Handle duplicates: skip if the user chose "skip".
                if (row.isDuplicate()) {
                    String dupAction = request.getParameter("dupAction_" + row.getGovIdNo());
                    if ("skip".equals(dupAction)) {
                        skippedCount++;
                        continue;
                    }
                    // Duplicate not skipped: ensure the existing candidate is marked present.
                    Integer existingId = registrationService.findCandidateIdByGovIdAndExam(
                            row.getGovIdNo(), selectedExamId);
                    if (existingId != null) {
                        registrationService.updatePresent(existingId, true);
                        importedCount++;
                    }
                    continue;
                }

                // Normal row: persist candidate + enrollment via the service.
                row.setExamId(selectedExamId);
                ServiceResult<Void> result = registrationService.insert(row);
                if (result.isSuccess()) {
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }

            session.removeAttribute("previewCandidates");
            session.setAttribute("importedCount", importedCount);

            String uploadedFile = (String) session.getAttribute("uploadedFileName");
            if (uploadedFile == null) {
                uploadedFile = "danh_sach.csv";
            }

            response.sendRedirect("upload?importSuccess=true");
            return;
        }

        response.sendRedirect("upload");
    }

    // Write a UTF-8 CSV response with a BOM (so Excel reads Vietnamese correctly).
    private void writeCsv(HttpServletResponse response, String fileName, String csvData) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        response.getOutputStream().write(bom);
        response.getOutputStream().write(csvData.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    // Best-effort fallback charset detection for legacy Vietnamese exports.
    private Charset detectFallbackCharset() {
        String[] names = { "Cp1258", "Windows-1258", "Cp1252" };
        for (String name : names) {
            try {
                return Charset.forName(name);
            } catch (Exception e) {
                // Try the next candidate.
            }
        }
        return StandardCharsets.UTF_8;
    }

    // Validate that the bytes form a well-formed UTF-8 sequence.
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
                if (i + 1 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80
                        || (bytes[i + 2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80
                        || (bytes[i + 2] & 0xC0) != 0x80 || (bytes[i + 3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }
}
