package managingstaff.controller;

import managingstaff.dao.LicenceDAO;
import managingstaff.dao.impl.LicenceDAOImpl;
import managingstaff.dao.ExamSessionDAO;
import managingstaff.dao.impl.ExamSessionDAOImpl;
import managingstaff.dto.SessionDTO;
import managingstaff.dto.ExamRegistrationDTO;
import managingstaff.dto.ExamSessionImportDraft;
import shared.model.Licence;
import auth.dto.UserDTO;
import managingstaff.service.ExamSessionImportService;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import managingstaff.service.EmailService;
import managingstaff.service.impl.EmailServiceImpl;

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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/manager/exam-schedules/create")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 15,
        maxRequestSize = 1024 * 1024 * 30)
public class ExamSessionImportServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/exam-session-create.jsp";
    private static final String ROUTE = "/manager/exam-schedules/create";
    private static final String PREVIEW_CANDIDATES = "previewCandidates";
    private static final String IMPORT_DRAFT = "examSessionImportDraft";
    private static final String HAS_INVALID_ROWS = "hasInvalidRows";
    private static final Set<String> ALLOWED_LICENCES = Set.of("A1", "A", "B1");
    private static final Set<String> ALLOWED_SECTION_TYPES = Set.of(
            "Lý thuyết", "Thực hành trong hình");
    private static final DateTimeFormatter VI_DATE = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern TRAILING_NUMBER = Pattern.compile("(\\d+)$");

    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ExamSessionImportService importService = new ExamSessionImportService();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("downloadTemplate".equals(action)) {
            writeCsv(response, "danh_sach_chinh_thuc_mau.csv",
                    "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                    + "101,Nguyễn Văn A,15/06/2000,012345678901,A1,0987654321,nguyenvana@gmail.com\r\n");
            return;
        }
        if ("downloadTestFile".equals(action)) {
            writeCsv(response, "danh_sach_a1_test.csv",
                    "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Số điện thoại,Email\r\n"
                    + "111,Lê Chi A1,13/03/1999,079900000103,A1,0909000103,demo.a1.approved@laivui.local\r\n");
            return;
        }
        if ("cancel".equals(action)) {
            clearPreview(request.getSession());
            response.sendRedirect(request.getContextPath() + ROUTE);
            return;
        }

        moveFlash(request, "uploadError");
        moveFlash(request, "uploadSuccess");
        bindPageData(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String action = trim(request.getParameter("action"));
        if ("confirm".equals(action)) {
            confirmCreate(request, response);
        } else {
            preview(request, response);
        }
    }

    private void preview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        clearPreview(session);

        try {
            ExamSessionImportDraft draft = readAndValidateDraft(request);
            session.setAttribute(IMPORT_DRAFT, draft);

            Part filePart = request.getPart("fileInput");
            if (filePart == null || filePart.getSize() == 0) {
                throw new IllegalArgumentException("Vui lòng chọn tệp CSV danh sách chính thức.");
            }
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || (!fileName.toLowerCase(Locale.ROOT).endsWith(".csv")
                    && !fileName.toLowerCase(Locale.ROOT).endsWith(".txt"))) {
                throw new IllegalArgumentException("Chỉ chấp nhận tệp .csv hoặc .txt.");
            }

            byte[] fileBytes = filePart.getInputStream().readAllBytes();
            List<ExamRegistrationDTO> candidates = parseCandidates(fileBytes, draft);
            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("Tệp không có dòng thí sinh nào.");
            }

            List<String> databaseErrors = importService.validateApprovedCandidates(
                    candidates, draft.getLicenceId());
            for (int i = 0; i < candidates.size(); i++) {
                if (databaseErrors.get(i) != null) {
                    addInvalid(candidates.get(i), databaseErrors.get(i));
                }
            }

            boolean hasInvalidRows = candidates.stream().anyMatch(ExamRegistrationDTO::isInvalid);
            long validCount = candidates.stream().filter(candidate -> !candidate.isInvalid()).count();
            session.setAttribute(PREVIEW_CANDIDATES, candidates);
            session.setAttribute(HAS_INVALID_ROWS, hasInvalidRows);
            session.setAttribute("validCandidateCount", validCount);
            session.setAttribute("uploadedFileName", fileName);

            response.sendRedirect(request.getContextPath() + ROUTE + "?preview=true");
        } catch (Exception ex) {
            session.setAttribute("uploadError", safeMessage(ex));
            response.sendRedirect(request.getContextPath() + ROUTE);
        }
    }

    @SuppressWarnings("unchecked")
    private void confirmCreate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ExamSessionImportDraft draft = (ExamSessionImportDraft) session.getAttribute(IMPORT_DRAFT);
        List<ExamRegistrationDTO> candidates = (List<ExamRegistrationDTO>) session.getAttribute(PREVIEW_CANDIDATES);
        boolean hasInvalidRows = Boolean.TRUE.equals(session.getAttribute(HAS_INVALID_ROWS));

        if (draft == null || candidates == null || candidates.isEmpty()) {
            session.setAttribute("uploadError", "Bản xem trước đã hết hạn. Vui lòng chọn lại tệp danh sách.");
            response.sendRedirect(request.getContextPath() + ROUTE);
            return;
        }
        if (hasInvalidRows) {
            session.setAttribute("uploadError", "Danh sách còn dòng chưa hợp lệ nên chưa thể tạo phiên thi.");
            response.sendRedirect(request.getContextPath() + ROUTE + "?preview=true");
            return;
        }

        try {
            ExamSessionImportService.ImportResult result = importService
                    .importCandidatesIntoSession(draft, candidates);
            int emailed = sendScheduleEmails(draft, candidates);
            String uploadedFile = (String) session.getAttribute("uploadedFileName");

            session.setAttribute("createdSessionId", result.getSessionId());
            session.setAttribute("createdExamId", result.getExamId());
            session.setAttribute("createdSessionName", draft.getSessionName());
            session.setAttribute("importedCount", result.getImportedCount());
            session.setAttribute("uploadSuccess", "Đã nhập danh sách thành công; gửi được " + emailed
                    + "/" + candidates.size() + " email lịch thi.");

            String details = "Tạo phiên " + draft.getSessionName()
                    + " (SessionId=" + result.getSessionId() + ", hạng " + draft.getLicenceClass()
                    + ") từ tệp " + (uploadedFile == null ? "danh_sach.csv" : uploadedFile)
                    + ": " + result.getImportedCount() + " thí sinh, trạng thái Pending";
            AuditLogHelper.persist(session, "CREATE SESSION ROSTER", details, result.getSessionId());

            clearPreview(session);
            response.sendRedirect(request.getContextPath() + ROUTE + "?importSuccess=true");
        } catch (Exception ex) {
            session.setAttribute("uploadError", safeMessage(ex));
            response.sendRedirect(request.getContextPath() + ROUTE + "?preview=true");
        }
    }

    private int sendScheduleEmails(ExamSessionImportDraft draft,List<ExamRegistrationDTO> candidates){
        EmailService mail=new EmailServiceImpl();if(!mail.isConfigured())return 0;int sent=0;
        String subject="Thông báo lịch sát hạch lái xe - "+draft.getSessionName();
        for(ExamRegistrationDTO c:candidates){
            String body="Xin chào "+c.getFullName()+",\n\nBạn có lịch sát hạch hạng "+draft.getLicenceClass()
                    +" vào ngày "+draft.getExamDateValue()+" tại "+draft.getCentreName()+"."
                    +"\nSố báo danh: "+c.getSbd()+".\nVui lòng có mặt theo hướng dẫn của đơn vị tổ chức.";
            if(mail.sendTextEmail(c.getEmail(),subject,body))sent++;
        }return sent;
    }

    private ExamSessionImportDraft readAndValidateDraft(HttpServletRequest request) {
        int examId = parsePositiveInt(request.getParameter("sessionId"), "Phiên thi");
        SessionDTO selected = sessionDAO.findById(examId);
        if (selected == null || !selected.isEditable()) {
            throw new IllegalArgumentException("Chỉ được import vào phiên thi chưa diễn ra.");
        }
        ExamSessionImportDraft draft = new ExamSessionImportDraft();
        draft.setExamId(examId);
        draft.setSessionName(selected.getSessionName());
        draft.setCentreName(selected.getCentreName());
        draft.setLicenceId(selected.getLicenceId());
        draft.setLicenceClass(selected.getLicenseCode());
        draft.setSectionType(selected.getExamTypeName());
        draft.setStartTime(Timestamp.valueOf(selected.getExamDate().toLocalDate().atTime(selected.getShiftStartTime().toLocalTime())));
        draft.setEndTime(Timestamp.valueOf(selected.getExamDate().toLocalDate().atTime(23, 59)));
        return draft;
    }

    private List<ExamRegistrationDTO> parseCandidates(byte[] fileBytes, ExamSessionImportDraft draft)
            throws IOException {
        Charset charset = isValidUtf8(fileBytes)
                ? StandardCharsets.UTF_8 : Charset.forName("Windows-1258");
        String content = new String(fileBytes, charset);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        List<ExamRegistrationDTO> candidates = new ArrayList<>();
        Set<String> seenGovIds = new HashSet<>();
        Set<Integer> seenCandidateNumbers = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new java.io.StringReader(content))) {
            String line;
            int lineNumber = 0;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                List<String> columns = parseCsvLine(line);
                if (!headerSkipped) {
                    headerSkipped = true;
                    if (columns.size() != 7) {
                        throw new IllegalArgumentException("Dòng tiêu đề phải có đúng 7 cột.");
                    }
                    continue;
                }
                if (columns.size() != 7) {
                    throw new IllegalArgumentException("Dòng " + lineNumber
                            + " không có đúng 7 cột dữ liệu.");
                }

                ExamRegistrationDTO candidate = new ExamRegistrationDTO();
                String rawCandidateNumber = normalizeSpreadsheetNumber(columns.get(0), 0);
                String fullName = columns.get(1).trim();
                String dob = columns.get(2).trim();
                String govId = normalizeSpreadsheetNumber(columns.get(3), 12);
                String licenceClass = columns.get(4).trim().toUpperCase(Locale.ROOT);
                String phoneNo = normalizeSpreadsheetNumber(columns.get(5), 10);
                String email = columns.get(6).trim();

                candidate.setFullName(fullName);
                candidate.setGovIdNo(govId);
                candidate.setLicenseCode(licenceClass);
                candidate.setPhoneNo(phoneNo);
                candidate.setEmail(email);
                candidate.setRegistrationType("OfficialList");
                candidate.setIsPresent(false);
                candidate.setIsPaymentCompleted(false);

                int candidateNo = parseCandidateNumber(rawCandidateNumber);
                candidate.setCandidateNo(candidateNo);
                if (candidateNo <= 0) {
                    addInvalid(candidate, "SBD phải kết thúc bằng một số lớn hơn 0");
                } else if (!seenCandidateNumbers.add(candidateNo)) {
                    candidate.setDuplicate(true);
                    addInvalid(candidate, "Trùng SBD trong tệp");
                }
                if (fullName.isEmpty()) {
                    addInvalid(candidate, "Thiếu họ tên");
                }
                if (!govId.matches("\\d{12}")) {
                    addInvalid(candidate, "CCCD phải gồm đúng 12 chữ số");
                } else if (!seenGovIds.add(govId)) {
                    candidate.setDuplicate(true);
                    addInvalid(candidate, "Trùng CCCD trong tệp");
                }
                if (licenceClass.isEmpty()) {
                    addInvalid(candidate, "Thiếu hạng GPLX");
                } else if (!ALLOWED_LICENCES.contains(licenceClass)) {
                    addInvalid(candidate, "Hạng GPLX chỉ chấp nhận A1, A hoặc B1");
                } else if (!draft.getLicenceClass().equals(licenceClass)) {
                    addInvalid(candidate, "Hạng trong tệp không khớp hạng " + draft.getLicenceClass());
                }
                if (!phoneNo.matches("0\\d{9}")) {
                    addInvalid(candidate, "Số điện thoại bắt buộc và phải gồm 10 chữ số, bắt đầu bằng 0");
                }
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    addInvalid(candidate, "Email bắt buộc và không đúng định dạng");
                }
                try {
                    LocalDate parsedDate = dob.contains("/")
                            ? LocalDate.parse(dob, VI_DATE) : LocalDate.parse(dob);
                    candidate.setDateOfBirth(Date.valueOf(parsedDate));
                } catch (DateTimeParseException ex) {
                    addInvalid(candidate, "Ngày sinh không hợp lệ");
                }

                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Tệp CSV có dấu ngoặc kép chưa đóng.");
        }
        values.add(current.toString());
        return values;
    }

    private int parseCandidateNumber(String value) {
        Matcher matcher = TRAILING_NUMBER.matcher(value == null ? "" : value.trim());
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizeSpreadsheetNumber(String raw, int expectedLength) {
        String value = raw == null ? "" : raw.trim();
        if (value.startsWith("=")) value = value.substring(1).trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        if (value.startsWith("'")) value = value.substring(1).trim();
        if (value.matches("(?i)[+-]?\\d+(?:\\.\\d+)?E[+-]?\\d+")) {
            try { value = new BigDecimal(value).toPlainString(); }
            catch (NumberFormatException ignored) { return value; }
        }
        if (value.endsWith(".0") && value.substring(0, value.length() - 2).matches("\\d+")) {
            value = value.substring(0, value.length() - 2);
        }
        if (expectedLength > 0 && value.matches("\\d+") && value.length() < expectedLength) {
            value = "0".repeat(expectedLength - value.length()) + value;
        }
        return value;
    }

    private void bindPageData(HttpServletRequest request) {
        List<Licence> licences = licenceDAO.findAll().stream()
                .filter(licence -> licence.getLicenceClass() != null
                && ALLOWED_LICENCES.contains(licence.getLicenceClass().toUpperCase(Locale.ROOT)))
                .toList();
        request.setAttribute("licences", licences);
        request.setAttribute("sectionTypes", ALLOWED_SECTION_TYPES);
        request.setAttribute("importSessions", sessionDAO.getAllSessions());
        request.setAttribute("today", LocalDate.now().toString());
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void writeCsv(HttpServletResponse response, String fileName, String data) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        response.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
    }

    private void addInvalid(ExamRegistrationDTO candidate, String message) {
        candidate.setInvalid(true);
        String current = candidate.getValidationMessage();
        candidate.setValidationMessage(current == null || current.isBlank()
                ? message : current + "; " + message);
    }

    private int parsePositiveInt(String raw, String fieldName) {
        try {
            int value = Integer.parseInt(trim(raw));
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException(fieldName + " không hợp lệ.");
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? "Không thể xử lý yêu cầu." : message;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void moveFlash(HttpServletRequest request, String name) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute(name);
        if (value != null) {
            request.setAttribute(name, value);
            session.removeAttribute(name);
        }
    }

    private void clearPreview(HttpSession session) {
        clearPreviewOnly(session);
        session.removeAttribute(IMPORT_DRAFT);
    }

    private void clearPreviewOnly(HttpSession session) {
        session.removeAttribute(PREVIEW_CANDIDATES);
        session.removeAttribute(HAS_INVALID_ROWS);
        session.removeAttribute("validCandidateCount");
        session.removeAttribute("uploadedFileName");
    }

    private boolean isValidUtf8(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes));
            return true;
        } catch (java.nio.charset.CharacterCodingException ex) {
            return false;
        }
    }
}
