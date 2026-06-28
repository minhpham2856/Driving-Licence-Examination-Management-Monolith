package service.impl;

import enums.SectionType;
import dto.examiner.ExaminerSlotDTO;
import dao.AuditLogDAO;
import dao.ExaminerSessionDataDAO;
import dao.impl.AuditLogDAOImpl;
import dao.impl.ExaminerSessionDataDAOImpl;

import model.user.AuditRecordModel;

import dto.candidate.CandidateDTO;

import dao.CandidateDAO;
import dao.impl.CandidateDAOImpl;

import dto.examiner.ExaminerExportContext;
import dto.examiner.ExaminerExportPayload;
import service.ExaminerExportService;
import dto.xml.XmlExportTable;
import service.AuditLogService;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import service.ExaminerDataService;

// Implementation of {@link ExaminerExportService}.
public class ExaminerExportServiceImpl implements ExaminerExportService {
    private final service.AuditLogService auditLogService = new service.impl.AuditLogServiceImpl();

    // Date formatter for Vietnamese date display (dd/MM/yyyy) — shared, thread-unsafe, sync on use
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    // Time formatter for Vietnamese time display (HH:mm) — shared, thread-unsafe, sync on use
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    // Date+time formatter for audit log timestamps — shared, thread-unsafe, sync on use
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    // Maximum number of audit log entries to include in a single export
    private static final int AUDIT_LIMIT = 5000;

    // XML field element names for the candidate export table (aligned with CANDIDATE_HEADERS)
    private static final List<String> CANDIDATE_FIELDS = List.of(
            "stt", "sbd", "hoVaTen", "ngaySinh", "gioiTinh", "cccd", "email", "soDienThoai",
            "diaChi", "hangGplx", "lyDoThi", "ngayThi", "vangThi", "tinhTrangThi",
            "dung", "sai", "khongTraLoi", "diemLyThuyet", "ketQuaLt",
            "diemThucHanh", "diemDuongTruong");
    // Vietnamese column headers for the candidate export table (aligned with CANDIDATE_FIELDS)
    private static final List<String> CANDIDATE_HEADERS = List.of(
            "STT", "SBD", "Ho va ten", "Ngay sinh", "Gioi tinh", "So can cuoc", "Email", "So dien thoai",
            "Dia chi", "Hang GPLX", "Ly do thi", "Ngay thi", "Vang thi", "Tinh trang thi",
            "Dung", "Sai", "Khong TL", "Diem ly thuyet", "Ket qua LT",
            "Diem thuc hanh", "Diem duong truong");

    // Service used to load candidate rows and build summary statistics
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    // DAO for session-level export metadata (session name, exam code, dates)
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    // DAO for audit log entries (violations and general audit trail)

    // DAO for candidate data (used to build SBD lookup maps)
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();

    // Builds the candidate-list export payload (danh sach thi sinh).
    @Override
    public ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx) {
        // Load candidate rows from the view-data service, filtered by section
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
        // Build the data rows list — each row is a List<Object> aligned with CANDIDATE_HEADERS
        List<List<Object>> rows = new ArrayList<>();
        // Row counter for the STT (ordinal number) column
        int index = 1;
        // Convert each candidate map into an ordered row of cell values
        for (Map<String, Object> c : candidates) {
            rows.add(Arrays.asList(
                    index++, // STT
                    c.get("sbd"), // SBD (candidate number)
                    c.get("fullName"), // Full name
                    c.get("dob"), // Date of birth
                    c.get("sex"), // Gender
                    c.get("governmentId"), // Government ID (CCCD)
                    c.get("email"), // Email
                    c.get("phoneNo"), // Phone number
                    c.get("address"), // Address
                    c.get("licenceClass"), // Licence class
                    c.get("reasonForTaking"), // Reason for taking exam
                    c.get("examDate"), // Exam date
                    Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong", // Absent (yes/no)
                    c.get("statusLabel"), // Status label
                    c.get("correct"), // Theory correct count
                    c.get("wrong"), // Theory wrong count
                    c.get("unanswered"), // Theory unanswered count
                    c.get("scoreTheory"), // Theory score
                    c.get("resultLabel"), // Theory result
                    c.get("scorePractical"), // Practical score
                    c.get("scoreOnRoad")));                                           // On-road score
        }

        // Wrap the rows in an XmlExportTable with candidate-specific element names
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", CANDIDATE_FIELDS, CANDIDATE_HEADERS, rows);
        // Return the payload with no metadata and no preamble (simple candidate list)
        return new ExaminerExportPayload(
                "Danh sach thi sinh", "danhSachThiSinh", Map.of(), List.of(table), null);
    }

    // Builds the results export payload (ket qua sat hach).
    @Override
    public ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx) {
        // Load candidate rows filtered by section type and name
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
        // Declare column definitions — these differ based on section type
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        // Row counter for the STT column
        int index = 1;

        // Branch based on section type: score-based (practical/road) vs theory
        if (ctx.sectionType() == SectionType.SCORE_BASED) {
            // Score-based sections: simpler layout with a single score column
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Diem", "Ket qua", "Tinh trang", "Vang thi");
            for (Map<String, Object> c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("examScore"), // Single score column
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        } else {
            // Theory sections: include correct/wrong/unanswered breakdown columns
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Dung", "Sai", "Khong TL", "Ket qua", "Tinh trang",
                    "Vang thi");
            for (Map<String, Object> c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("correct"), // Correct count
                        c.get("wrong"), // Wrong count
                        c.get("unanswered"), // Unanswered count
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        }

        // Wrap the rows in an XmlExportTable with results-specific element names
        XmlExportTable table = new XmlExportTable("ketQuaThi", "ketQua", fields, headers, rows);
        return new ExaminerExportPayload("Ket qua thi", "ketQuaThi", Map.of(), List.of(table), null);
    }

    // Builds the exam minutes export payload (bien ban sat hach).
    @Override
    public ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx) {
        // Load session metadata (name, exam code, date, times) from the DAO
        Map<String, Object> meta = sessionDataDAO.findSessionExportMeta(ctx.sessionId());
        // Build candidate summary statistics (total, done, testing, pending, passed, failed)
        Map<String, Object> summary = viewDataService.buildCandidateSummary(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
        // Load candidate rows for the data table
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());

        // Build the XML metadata map (session info + statistics)
        Map<String, Object> metadata = buildMinutesMetadata(meta, summary, ctx.slot(), ctx.sectionType(),
                ctx.sectionName());
        // Build the Excel preamble rows (header rows before the main data table)
        List<List<Object>> preamble = buildMinutesPreamble(meta, summary, ctx.slot(), ctx.sectionType(),
                ctx.sectionName());

        // Define column layout based on section type (same as results export)
        List<String> fields;
        List<String> headers;
        if (ctx.sectionType() == SectionType.SCORE_BASED) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Diem", "Ket qua", "Tinh trang", "Vang thi");
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Dung", "Sai", "Khong TL", "Ket qua", "Tinh trang",
                    "Vang thi");
        }

        // Build the candidate data rows for the minutes table
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.sectionType()));
        // Return payload with metadata, tables, and preamble for Excel
        return new ExaminerExportPayload(
                "Bien ban thi", "bienBanThi", metadata, List.of(table), preamble);
    }

    // Builds the violations export payload (bien ban vi pham).
    @Override
    public ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx) {
        // Load session metadata for the export header
        Map<String, Object> meta = sessionDataDAO.findSessionExportMeta(ctx.sessionId());
        // Load violation audit log entries (regulatory violations from the audit trail)
        List<AuditRecordModel> auditViolations = auditLogService.getViolationLogsForSession(ctx.sessionId(), AUDIT_LIMIT);
        // Load score violation rows (deductions applied to candidate scores)
        List<Map<String, Object>> scoreViolations = sessionDataDAO.findScoreViolationRows(ctx.sessionId());

        // Build the metadata map with title, session name, and exam code
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIEN BAN VI PHAM");
        metadata.put("caThi", nullToDash(meta.get("sessionName")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));

        // --- Build Table 1: Audit violation rows ---
        List<List<Object>> auditRows = new ArrayList<>();
        // Row counter for the STT column
        int index = 1;
        for (AuditRecordModel log : auditViolations) {
            // Format the timestamp to Vietnamese date+time format
            String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "-";
            auditRows.add(Arrays.asList(
                    index++,
                    nullToDash(log.getChangerName()), // Person who recorded the violation
                    nullToDash(log.getNewValue()), // Violation description/content
                    nullToDash(log.getReason()), // Reason for the violation
                    time));                               // Timestamp
        }

        // --- Build Table 2: Score deduction rows ---
        List<List<Object>> scoreRows = new ArrayList<>();
        // Reset row counter for the second table
        index = 1;
        for (Map<String, Object> row : scoreViolations) {
            scoreRows.add(Arrays.asList(
                    index++,
                    row.get("sbd"), // Candidate SBD
                    row.get("fullName"), // Candidate name
                    row.get("sectionName"), // Exam section
                    row.get("violationReason"), // Deduction reason
                    row.get("deductionPoints"), // Points deducted
                    Boolean.TRUE.equals(row.get("critical")) ? "Co" : "Khong", // Critical flag
                    row.get("currentScore")));                                          // Current score after deduction
        }

        // Create the audit violations XML table structure
        XmlExportTable auditTable = new XmlExportTable(
                "viPhamQuyChe",
                "viPham",
                List.of("stt", "nguoiGhi", "noiDung", "lyDo", "thoiGian"),
                List.of("STT", "Nguoi ghi", "Noi dung", "Ly do", "Thoi gian"),
                auditRows);
        // Create the score deductions XML table structure
        XmlExportTable scoreTable = new XmlExportTable(
                "truDiemThi",
                "banTruDiem",
                List.of("stt", "sbd", "hoVaTen", "phanThi", "lyDoTruDiem", "diemTru", "loiNghiemTrong", "diemHienTai"),
                List.of("STT", "SBD", "Ho va ten", "Phan thi", "Ly do tru diem", "Diem tru",
                        "Loi nghiem trong", "Diem hien tai"),
                scoreRows);

        // Build the combined Excel rows (preamble + both tables merged)
        List<List<Object>> excelRows = buildViolationsExcelRows(meta, auditViolations, scoreViolations);
        // Return payload with both tables and the merged Excel preamble
        return new ExaminerExportPayload(
                "Bien ban vi pham", "bienBanViPham", metadata, List.of(auditTable, scoreTable), excelRows);
    }

    // Builds the audit-log export payload (nhat ky).
    @Override
    public ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery) {
        // Load paginated audit log entries for the session (up to AUDIT_LIMIT)
        List<AuditRecordModel> logs = auditLogService.getLogsForSessionPaginated(ctx.sessionId(), 1, AUDIT_LIMIT, searchQuery);
        // Build a lookup map of candidate record ID -> SBD for display
        Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.sessionId());
        // Build the data rows from the audit log entries
        List<List<Object>> rows = new ArrayList<>();
        for (AuditRecordModel log : logs) {
            // Convert each audit log entry into one or more display rows
            for (Map<String, Object> viewRow : auditLogService.toViewRows(log, sbdByRecordId)) {
                // Format the timestamp for display
                String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "";
                // Extract the reason, converting the dash placeholder to empty string
                Object reason = viewRow.get("reason");
                rows.add(Arrays.asList(
                        viewRow.get("username"), // User who performed the action
                        log.getAction(), // Action type
                        viewRow.get("entityName"), // Entity affected
                        viewRow.get("sbd"), // Candidate SBD
                        viewRow.get("info"), // Additional info
                        viewRow.get("oldValue") != null ? viewRow.get("oldValue") : "",// Old value (before change)
                        viewRow.get("newValue"), // New value (after change)
                        "-".equals(reason) ? "" : reason, // Reason for change
                        time));                                                        // Timestamp
            }
        }

        // Create the XML table structure for audit log entries
        XmlExportTable table = new XmlExportTable(
                "nhatKy",
                "banGhi",
                List.of("nguoiDung", "thaoTac", "doiTuong", "maBanGhi", "thongTin", "cu", "moi", "lyDo", "thoiGian"),
                List.of("Nguoi dung", "Thao tac", "Doi tuong", "SBD", "Thong tin", "Cu", "Moi", "Ly do",
                        "Thoi gian"),
                rows);
        // Include the search keyword in metadata if provided
        Map<String, Object> metadata = Map.of();
        if (searchQuery != null && !searchQuery.isBlank()) {
            metadata = Map.of("tuKhoa", searchQuery.trim());
        }
        return new ExaminerExportPayload("Nhat ky", "nhatKyHeThong", metadata, List.of(table), null);
    }

    // Builds the metadata map for the minutes (bien ban) export.
    private Map<String, Object> buildMinutesMetadata(Map<String, Object> meta, Map<String, Object> summary,
            ExaminerSlotDTO slot, SectionType sectionType, String sectionName) {
        // Use LinkedHashMap to preserve the insertion order of metadata entries
        Map<String, Object> metadata = new LinkedHashMap<>();
        // Add the document title
        metadata.put("tieuDe", "BIEN BAN TO CHUC THI");
        // Add session identification fields
        metadata.put("caThi", nullToDash(meta.get("sessionName")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));
        // Add date and time fields
        metadata.put("ngayThi", formatDate(meta.get("examDate")));
        metadata.put("gioBatDau", formatTime(meta.get("startTime")));
        metadata.put("gioKetThuc", formatTime(meta.get("endTime")));
        // Add examiner and area info if a slot is available
        if (slot != null) {
            metadata.put("khuVucPhong", nullToDash(slot.getAreaName()));
            metadata.put("giamThi", nullToDash(slot.getExaminerName()));
        }
        // Add the section name, using "Ly thuyet" for theory sections
        metadata.put("phanThi",
                sectionType == SectionType.SCORE_BASED ? nullToDash(sectionName) : "Ly thuyet");

        // Build the statistics sub-map with candidate counts
        Map<String, Object> thongKe = new LinkedHashMap<>();
        thongKe.put("tongThiSinh", summary.get("total"));     // Total candidates
        thongKe.put("daThi", summary.get("done"));             // Completed
        thongKe.put("dangThi", summary.get("testing"));        // Currently testing
        thongKe.put("chuaThi", summary.get("pending"));       // Not yet started
        thongKe.put("dat", summary.get("passed"));             // Passed
        thongKe.put("truot", summary.get("failed"));           // Failed
        metadata.put("thongKe", thongKe);
        return metadata;
    }

    // Builds Excel preamble rows for the minutes (bien ban) export.
    private List<List<Object>> buildMinutesPreamble(Map<String, Object> meta, Map<String, Object> summary,
            ExaminerSlotDTO slot, SectionType sectionType, String sectionName) {
        List<List<Object>> preamble = new ArrayList<>();
        // Title row
        preamble.add(Arrays.asList("BIEN BAN TO CHUC THI"));
        // Session identification rows
        preamble.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        preamble.add(Arrays.asList("Ma dot thi", nullToDash(meta.get("examCode"))));
        // Date and time rows
        preamble.add(Arrays.asList("Ngay thi", formatDate(meta.get("examDate"))));
        preamble.add(Arrays.asList("Gio bat dau", formatTime(meta.get("startTime"))));
        preamble.add(Arrays.asList("Gio ket thuc", formatTime(meta.get("endTime"))));
        // Examiner and area rows (if slot is available)
        if (slot != null) {
            preamble.add(Arrays.asList("Khu vuc / Phong", nullToDash(slot.getAreaName())));
            preamble.add(Arrays.asList("Giam thi", nullToDash(slot.getExaminerName())));
        }
        // Section name row
        preamble.add(Arrays.asList("Phan thi",
                sectionType == SectionType.SCORE_BASED ? nullToDash(sectionName) : "Ly thuyet"));
        // Empty separator row before statistics
        preamble.add(Arrays.asList());
        // Statistics rows
        preamble.add(Arrays.asList("Tong thi sinh", summary.get("total")));
        preamble.add(Arrays.asList("Da thi", summary.get("done")));
        preamble.add(Arrays.asList("Dang thi", summary.get("testing")));
        preamble.add(Arrays.asList("Chua thi", summary.get("pending")));
        preamble.add(Arrays.asList("Dat", summary.get("passed")));
        preamble.add(Arrays.asList("Truot", summary.get("failed")));
        // Empty separator row before the main data table
        preamble.add(Arrays.asList());
        return preamble;
    }

    // Builds candidate data rows for the minutes export.
    private static List<List<Object>> buildMinutesRows(List<Map<String, Object>> candidates,
            SectionType sectionType) {
        List<List<Object>> rows = new ArrayList<>();
        // Row counter for the STT column
        int index = 1;
        for (Map<String, Object> c : candidates) {
            if (sectionType == SectionType.SCORE_BASED) {
                // Score-based row layout: STT, SBD, name, score, result, status, absent
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("examScore"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            } else {
                // Theory row layout: STT, SBD, name, correct, wrong, unanswered, result, status, absent
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("correct"),
                        c.get("wrong"),
                        c.get("unanswered"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        }
        return rows;
    }

    // Builds the full Excel row set (preamble + data) for the violations export.
    private List<List<Object>> buildViolationsExcelRows(Map<String, Object> meta, List<AuditRecordModel> auditViolations,
            List<Map<String, Object>> scoreViolations) {
        List<List<Object>> rows = new ArrayList<>();
        // Document title and session identification
        rows.add(Arrays.asList("BIEN BAN VI PHAM"));
        rows.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        rows.add(Arrays.asList("Ma dot thi", nullToDash(meta.get("examCode"))));
        // Empty separator row
        rows.add(Arrays.asList());

        // --- Section I: Regulatory violations from the audit trail ---
        rows.add(Arrays.asList("I. Vi pham quy che thi (nhat ky)"));
        // Column headers for the audit violations table
        rows.add(Arrays.asList("STT", "Nguoi ghi", "Noi dung", "Ly do", "Thoi gian"));
        int index = 1;
        for (AuditRecordModel log : auditViolations) {
            // Format the timestamp for each audit entry
            String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "-";
            rows.add(Arrays.asList(
                    index++,
                    nullToDash(log.getChangerName()),
                    nullToDash(log.getNewValue()),
                    nullToDash(log.getReason()),
                    time));
        }
        // Add a placeholder row if no audit violations exist
        if (auditViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "Khong co vi pham", "-", "-"));
        }

        // Empty separator row between sections
        rows.add(Arrays.asList());
        // --- Section II: Score deductions applied to candidates ---
        rows.add(Arrays.asList("II. Tru diem thi"));
        // Column headers for the score deductions table
        rows.add(Arrays.asList("STT", "SBD", "Ho va ten", "Phan thi", "Ly do tru diem", "Diem tru",
                "Loi nghiem trong", "Diem hien tai"));
        // Reset row counter for the second section
        index = 1;
        for (Map<String, Object> row : scoreViolations) {
            rows.add(Arrays.asList(
                    index++,
                    row.get("sbd"),
                    row.get("fullName"),
                    row.get("sectionName"),
                    row.get("violationReason"),
                    row.get("deductionPoints"),
                    Boolean.TRUE.equals(row.get("critical")) ? "Co" : "Khong",
                    row.get("currentScore")));
        }
        // Add a placeholder row if no score deductions exist
        if (scoreViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "Khong co tru diem", "-", "-", "-", "-", "-"));
        }
        return rows;
    }

    // Formats a value as dd/MM/yyyy, or "-" if null.
    private static String formatDate(Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            // Synchronise because SimpleDateFormat is not thread-safe
            synchronized (DATE_FMT) {
                return DATE_FMT.format(date);
            }
        }
        // Fall back to nullToDash for non-Date values
        return nullToDash(value);
    }

    // Formats a value as HH:mm, or "-" if null.
    private static String formatTime(Object value) {
        if (value instanceof Time) {
            Time time = (Time) value;
            // Synchronise because SimpleDateFormat is not thread-safe
            synchronized (TIME_FMT) {
                return TIME_FMT.format(time);
            }
        }
        // Handle java.util.Date values (from SQL CAST) as well
        if (value instanceof Date) {
            Date date = (Date) value;
            synchronized (TIME_FMT) {
                return TIME_FMT.format(date);
            }
        }
        // Fall back to nullToDash for non-temporal values
        return nullToDash(value);
    }

    // Returns the string representation of a value, or "-" if null/blank.
    private static String nullToDash(Object value) {
        // Return dash for null values
        if (value == null) {
            return "-";
        }
        // Trim the string representation and return dash if empty
        String text = value.toString().trim();
        return text.isEmpty() ? "-" : text;
    }

    // Builds a lookup map of candidate record ID -> SBD.
    private Map<Integer, String> buildSbdLookup(int sessionId) {
        // Use LinkedHashMap to preserve insertion order (matches candidate display order)
        Map<Integer, String> lookup = new LinkedHashMap<>();
        // Load all candidate registrations for the session and extract ID -> SBD pairs
        for (CandidateDTO reg : candidateDAO.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), reg.getSbd());
        }
        return lookup;
    }
}
