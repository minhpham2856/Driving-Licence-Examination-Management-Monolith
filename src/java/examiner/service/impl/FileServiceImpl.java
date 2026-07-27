package examiner.service.impl;

import examiner.dto.CandidateRowDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExamStatsDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.ExportPayloadDTO;
import examiner.dto.PrintPreviewDTO;
import examiner.dto.XmlExportTable;
import examiner.dao.CandidateAnswerDAO;
import examiner.dao.CandidateViolationDAO;
import examiner.util.CandidatePhotoFiles;
import shared.enums.FileType;
import shared.enums.SectionType;
import shared.model.Audit;
import shared.model.CandidateAnswer;
import shared.model.CandidateViolation;
import shared.model.Exam;
import shared.model.ExamResult;
import shared.model.ExaminerSchedule;
import shared.model.TheoryPaper;
import examiner.dao.DeductionRecordViewDAO;
import examiner.dao.ExamDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.TheoryPaperDAO;
import examiner.dao.impl.CandidateAnswerDAOImpl;
import examiner.dao.impl.CandidateViolationDAOImpl;
import examiner.dao.impl.DeductionRecordViewDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.TheoryPaperDAOImpl;
import examiner.service.AuditService;
import examiner.service.ExamViewService;
import static examiner.util.FormatUtil.formatBbPrintTitle;
import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.formatPrintTitle;
import static examiner.util.FormatUtil.formatSbdFilter;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.isSessionDocumentType;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import examiner.service.FileService;
import examiner.service.EnrollmentService;

// Builds examiner file exports and JSP print preview models.
public class FileServiceImpl implements FileService {

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();
    private final ExamViewService viewService = new ExamViewServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final CandidateViolationDAO candidateViolationDAO = new CandidateViolationDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final int AUDIT_LIMIT = 5000;
    private static final int BLOCK_A_FROM = 1;
    private static final int BLOCK_A_TO = 12;
    private static final int BLOCK_B_FROM = 13;
    private static final int BLOCK_B_TO = 25;
    private static final List<String> CANDIDATE_FIELDS = List.of(
            "sbd", "hoVaTen", "ngaySinh", "gioiTinh", "cccd", "email", "soDienThoai",
            "diaChi", "hangGplx", "lyDoThi", "ngayThi", "vangThi", "tinhTrangThi");
    private static final List<String> CANDIDATE_HEADERS = List.of(
            "SBD", "Họ và tên", "Ngày sinh", "Giới tính", "Số căn cước", "Email", "Số điện thoại",
            "Địa chỉ", "Hạng GPLX", "Lý do thi", "Ngày thi", "Vắng thi", "Tình trạng thi");

    // Loads exam code and shift times for export preamble metadata.
    private Map<String, Object> getExamExportMeta(int examId) {
        Map<String, Object> meta = new LinkedHashMap<>();
        Exam e = examDAO.get(examId);
        if (e != null) {
            meta.put("shiftLabel", false);
            meta.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : "");
            meta.put("endTime", e.getEndTime() != null ? e.getEndTime().toString() : "");
            meta.put("examCode", e.getExamCode());
        }
        return meta;
    }

    // Builds candidates export from session data.
    private ExportPayloadDTO buildCandidatesExport(ExportContextDTO ctx) {
        List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                ctx.examId(), ctx.section(), null);
        List<List<Object>> rows = new ArrayList<>();
        for (CandidateRowDTO c : candidates) {
            rows.add(candidateInfoRow(c));
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", CANDIDATE_FIELDS, CANDIDATE_HEADERS, rows);
        return new ExportPayloadDTO(
                "Danh sách thí sinh", "danhSachThiSinh", Map.of(), List.of(table), exportTimestampPreamble());
    }

    // Builds results export from session data.
    private ExportPayloadDTO buildResultsExport(ExportContextDTO ctx) {
        List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                ctx.examId(), ctx.section(), null);
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        if (ctx.isTheory() == false) {
            fields = List.of("sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("SBD", "Họ và tên", "Điểm", "Kết quả", "Tình trạng", "Vắng thi");
            for (CandidateRowDTO c : candidates) {
                rows.add(Arrays.asList(
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getExamScore(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        c.isAbsent() ? "Có" : "Không"));
            }
        } else {
            fields = List.of("sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("SBD", "Họ và tên", "Đúng", "Sai", "Không TL", "Kết quả", "Tình trạng",
                    "Vắng thi");
            for (CandidateRowDTO c : candidates) {
                rows.add(Arrays.asList(
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getCorrect(),
                        c.getWrong(),
                        c.getUnanswered(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        c.isAbsent() ? "Có" : "Không"));
            }
        }
        XmlExportTable table = new XmlExportTable("ketQuaThi", "ketQua", fields, headers, rows);
        return new ExportPayloadDTO(
                "Tổng hợp kết quả thi", "tongHopKetQuaThi", Map.of(), List.of(table), exportTimestampPreamble());
    }

    // Builds minutes export from session data.
    public ExportPayloadDTO buildMinutesExport(ExportContextDTO ctx) {
        Map<String, Object> meta = getExamExportMeta(ctx.examId());
        ExamStatsDTO summary = viewService.getStatsByExam(
                ctx.examId(), ctx.section());
        List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                ctx.examId(), ctx.section(), null);
        Map<String, Object> metadata = buildMinutesMetadata(meta, summary, ctx.schedule(), ctx.isTheory(),
                ctx.section() != null ? ctx.section().getValue() : SectionType.LAYOUT.getValue());
        List<List<Object>> preamble = buildMinutesPreamble(meta, summary, ctx.schedule(), ctx.isTheory(),
                ctx.section() != null ? ctx.section().getValue() : SectionType.LAYOUT.getValue());
        preamble.add(0, Arrays.asList("Thời gian xuất", nowExportTimestamp()));
        List<String> fields;
        List<String> headers;
        if (ctx.isTheory() == false) {
            fields = List.of("sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("SBD", "Họ và tên", "Điểm", "Kết quả", "Tình trạng", "Vắng thi");
        } else {
            fields = List.of("sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("SBD", "Họ và tên", "Đúng", "Sai", "Không TL", "Kết quả", "Tình trạng",
                    "Vắng thi");
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.isTheory()));
        return new ExportPayloadDTO(
                "Biên bản thi", "bienBanThi", metadata, List.of(table), preamble);
    }

    // Builds violations export payload without SBD filter.
    private ExportPayloadDTO buildViolationsExport(ExportContextDTO ctx) {
        return buildViolationsExport(ctx, null);
    }

    // Builds violations export payload, optionally filtered to one SBD.
    private ExportPayloadDTO buildViolationsExport(ExportContextDTO ctx, String sbdFilterRaw) {
        Integer sbdFilter = formatSbdFilter(sbdFilterRaw);
        java.util.LinkedHashSet<Integer> violationSbds = collectViolationSbds(ctx.examId());
        if (sbdFilter != null) {
            java.util.LinkedHashSet<Integer> filtered = new java.util.LinkedHashSet<>();
            if (violationSbds.contains(sbdFilter)) {
                filtered.add(sbdFilter);
            }
            violationSbds = filtered;
        }

        List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                ctx.examId(), ctx.section(), null);
        List<List<Object>> rows = new ArrayList<>();
        for (CandidateRowDTO c : candidates) {
            if (violationSbds.contains(c.getCandidateNumber())) {
                rows.add(candidateInfoRow(c));
            }
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinhViPham", "thiSinh", CANDIDATE_FIELDS, CANDIDATE_HEADERS, rows);
        return new ExportPayloadDTO(
                "Danh sách thí sinh vi phạm",
                "danhSachThiSinhViPham",
                Map.of(),
                List.of(table),
                exportTimestampPreamble());
    }

    // Builds audit export from session data.
    private ExportPayloadDTO buildAuditExport(ExportContextDTO ctx, String searchQuery) {
        List<Audit> logs = auditService.getAllByExam(ctx.examId(), 1, AUDIT_LIMIT, searchQuery);
        Map<Long, String> changerNames = auditService.getAllChangerNamesByAudit(logs);
        Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.examId());
        List<List<Object>> rows = new ArrayList<>();
        for (Audit log : logs) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            for (Map<String, Object> viewRow : auditService.toViewRows(log, changerName, sbdByRecordId)) {
                String time = log.getCreatedAt() != null ? AUDIT_DATE_FMT.format(log.getCreatedAt()) : "";
                Object reason = viewRow.get("reason");
                rows.add(Arrays.asList(
                        viewRow.get("username"),
                        viewRow.get("actionLabel"),
                        viewRow.get("entityName"),
                        viewRow.get("sbd"),
                        viewRow.get("info"),
                        viewRow.get("oldValue") != null ? viewRow.get("oldValue") : "",
                        viewRow.get("newValue"),
                        "-".equals(reason) ? "" : reason,
                        time));
            }
        }
        XmlExportTable table = new XmlExportTable(
                "nhatKy",
                "banGhi",
                List.of("nguoiDung", "thaoTac", "doiTuong", "maBanGhi", "thongTin", "cu", "moi", "lyDo", "thoiGian"),
                List.of("Người dùng", "Thao tác", "Đối tượng", "SBD", "Thông tin", "Cũ", "Mới", "Lý do",
                        "Thời gian"),
                rows);
        Map<String, Object> metadata = Map.of();
        if (searchQuery != null && !searchQuery.isBlank()) {
            metadata = Map.of("tuKhoa", searchQuery.trim());
        }
        return new ExportPayloadDTO("Nhật ký", "nhatKyHeThong", metadata, List.of(table), exportTimestampPreamble());
    }

    // Private helper: build minutes metadata.
    private Map<String, Object> buildMinutesMetadata(Map<String, Object> meta, ExamStatsDTO summary,
            ExaminerSchedule schedule, boolean isTheory, String sectionType) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÊN BẢN TỔ CHỨC THI");
        metadata.put("caThi", nullToDash(meta.get("shiftLabel")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));
        metadata.put("ngayThi", formatDate(meta.get("examDate")));
        metadata.put("gioBatDau", formatTime(meta.get("startTime")));
        metadata.put("gioKetThuc", formatTime(meta.get("endTime")));
        if (schedule != null && schedule.getExamArea() != null) {
            metadata.put("khuVucPhong", nullToDash(schedule.getExamArea().getAreaName()));
        }
        metadata.put("phanThi",
                !isTheory ? nullToDash(sectionType) : "Lý thuyết");
        Map<String, Object> thongKe = new LinkedHashMap<>();
        thongKe.put("tongThiSinh", summary.getTotal());
        thongKe.put("daThi", summary.getDone());
        thongKe.put("dangThi", summary.getTesting());
        thongKe.put("chuaThi", summary.getPending());
        thongKe.put("dat", summary.getPassed());
        thongKe.put("truot", summary.getFailed());
        metadata.put("thongKe", thongKe);
        return metadata;
    }

    // Private helper: build minutes preamble.
    private List<List<Object>> buildMinutesPreamble(Map<String, Object> meta, ExamStatsDTO summary,
            ExaminerSchedule schedule, boolean isTheory, String sectionType) {
        List<List<Object>> preamble = new ArrayList<>();
        preamble.add(Arrays.asList("BIÊN BẢN TỔ CHỨC THI"));
        preamble.add(Arrays.asList("Ca thi", nullToDash(meta.get("shiftLabel"))));
        preamble.add(Arrays.asList("Mã đợt thi", nullToDash(meta.get("examCode"))));
        preamble.add(Arrays.asList("Ngày thi", formatDate(meta.get("examDate"))));
        preamble.add(Arrays.asList("Giờ bắt đầu", formatTime(meta.get("startTime"))));
        preamble.add(Arrays.asList("Giờ kết thúc", formatTime(meta.get("endTime"))));
        if (schedule != null && schedule.getExamArea() != null) {
            preamble.add(Arrays.asList("Khu vực / Phòng", nullToDash(schedule.getExamArea().getAreaName())));
        }
        preamble.add(Arrays.asList("Phần thi",
                !isTheory ? nullToDash(sectionType) : "Lý thuyết"));
        preamble.add(Arrays.asList());
        preamble.add(Arrays.asList("Tổng thí sinh", summary.getTotal()));
        preamble.add(Arrays.asList("Đã thi", summary.getDone()));
        preamble.add(Arrays.asList("Đang thi", summary.getTesting()));
        preamble.add(Arrays.asList("Chưa thi", summary.getPending()));
        preamble.add(Arrays.asList("Đạt", summary.getPassed()));
        preamble.add(Arrays.asList("Trượt", summary.getFailed()));
        preamble.add(Arrays.asList());
        return preamble;
    }

    // Private helper: build minutes rows.
    private static List<List<Object>> buildMinutesRows(List<CandidateRowDTO> candidates,
            boolean isTheory) {
        List<List<Object>> rows = new ArrayList<>();
        for (CandidateRowDTO c : candidates) {
            if (!isTheory) {
                rows.add(Arrays.asList(
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getExamScore(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        c.isAbsent() ? "Có" : "Không"));
            } else {
                rows.add(Arrays.asList(
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getCorrect(),
                        c.getWrong(),
                        c.getUnanswered(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        c.isAbsent() ? "Có" : "Không"));
            }
        }
        return rows;
    }

    // Private helper: candidate info row.
    private static List<Object> candidateInfoRow(CandidateRowDTO c) {
        return Arrays.asList(
                c.getCandidateNumber(),
                c.getFullName(),
                c.getDob(),
                c.getSex() != null ? c.getSex().getValue() : "",
                c.getGovernmentId(),
                c.getEmail(),
                c.getPhoneNo(),
                c.getAddress(),
                c.getLicenceClass(),
                c.getReasonForTaking(),
                c.getExamDate(),
                c.isAbsent() ? "Có" : "Không",
                c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "");
    }

    // Private helper: collect violation sbds.
    private java.util.LinkedHashSet<Integer> collectViolationSbds(int examId) {
        java.util.LinkedHashSet<Integer> sbds = new java.util.LinkedHashSet<>();
        List<Map<String, Object>> scoreViolations = deductionRecordViewDAO.getViolationRowsForExam(examId);
        for (Map<String, Object> row : scoreViolations) {
            Integer sbd = toInteger(row.get("sbd"));
            if (sbd != null && sbd > 0) {
                sbds.add(sbd);
            }
        }
        List<Audit> auditViolations = auditService.getAllViolationsByExam(examId, AUDIT_LIMIT);
        Map<Integer, String> sbdByRecordId = buildSbdLookup(examId);
        for (Audit log : auditViolations) {
            Integer sbd = toInteger(auditService.extractSbdForDisplay(log, sbdByRecordId));
            if (sbd != null && sbd > 0) {
                sbds.add(sbd);
            }
        }
        return sbds;
    }

    // Adds export timestamp row used as sheet preamble in payloads.
    private static List<List<Object>> exportTimestampPreamble() {
        List<List<Object>> preamble = new ArrayList<>();
        preamble.add(Arrays.asList("Thời gian xuất", nowExportTimestamp()));
        return preamble;
    }

    // Returns current timestamp formatted for export preambles.
    private static String nowExportTimestamp() {
        synchronized (AUDIT_DATE_FMT) {
            return AUDIT_DATE_FMT.format(new Date());
        }
    }

    // Private helper: format date.
    private static String formatDate(Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            synchronized (DATE_FMT) {
                return DATE_FMT.format(date);
            }
        }
        return nullToDash(value);
    }

    // Private helper: format time.
    private static String formatTime(Object value) {
        if (value instanceof Time) {
            Time time = (Time) value;
            synchronized (TIME_FMT) {
                return TIME_FMT.format(time);
            }
        }
        if (value instanceof Date) {
            Date date = (Date) value;
            synchronized (TIME_FMT) {
                return TIME_FMT.format(date);
            }
        }
        return nullToDash(value);
    }

    // Converts null or blank values to dash for display fields.
    private static String nullToDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "-" : text;
    }

    // Maps candidate record id to SBD string for audit export rows.
    private Map<Integer, String> buildSbdLookup(int examId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (EnrollmentDTO enrollment : enrollmentService.getAllByExam(examId)) {
            lookup.put(enrollment.getCandidateId(), String.valueOf(enrollment.getCandidateNumber()));
        }
        return lookup;
    }

    // Private helper: to integer.
    private static Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    // Private helper: build payload.
    private ExportPayloadDTO buildPayload(ExportContextDTO ctx, String documentType, String searchQuery)
            throws IOException {
        String normalized = documentType == null ? "" : documentType.trim().toLowerCase();
        return switch (normalized) {
            case "candidates" ->
                buildCandidatesExport(ctx);
            case "result" ->
                buildResultsExport(ctx);
            case "violations" ->
                buildViolationsExport(ctx, searchQuery);
            case "audit" ->
                buildAuditExport(ctx, searchQuery);
            default ->
                throw new IOException("Loại tài liệu xuất không được hỗ trợ: " + documentType);
        };
    }

    // === Rendering (from XmlServiceImpl) ===
    // Writes a simple Excel sheet with headers and data rows.
    private void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException {
        exportToExcel(sheetName, null, headers, rows, out);
    }

    // Writes an Excel sheet with optional preamble rows, headers, and data rows.
    private void exportToExcel(String sheetName, List<List<Object>> preambleRows, List<String> headers,
            List<List<Object>> rows, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet((sheetName == null || sheetName.isBlank()) ? "newSheet" : sheetName);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));
            int rowIndex = 0;
            if (preambleRows != null) {
                for (List<Object> preambleRow : preambleRows) {
                    Row row = sheet.createRow(rowIndex++);
                    if (preambleRow == null) {
                        continue;
                    }
                    for (int col = 0; col < preambleRow.size(); col++) {
                        writeCell(row.createCell(col), preambleRow.get(col), dateStyle);
                    }
                }
            }
            int headerColCount = 0;
            if (headers != null && !headers.isEmpty()) {
                Row headerRow = sheet.createRow(rowIndex++);
                headerColCount = headers.size();
                for (int col = 0; col < headers.size(); col++) {
                    Cell headerCell = headerRow.createCell(col);
                    headerCell.setCellValue(headers.get(col));
                    headerCell.setCellStyle(headerStyle);
                }
            }
            if (rows != null) {
                for (List<Object> rowData : rows) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int col = 0; col < rowData.size(); col++) {
                        writeCell(row.createCell(col), rowData.get(col), dateStyle);
                    }
                    headerColCount = Math.max(headerColCount, rowData.size());
                }
            }
            for (int col = 0; col < headerColCount; col++) {
                sheet.autoSizeColumn(col);
            }
            workbook.write(out);
        }
    }

    // Private helper: write cell.
    private void writeCell(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    // === FileService ===

    // Exports examiner session reports to Excel.
    @Override
    public void export(ExportContextDTO ctx, String documentType, FileType format,
            String searchQuery, int sbd, OutputStream out) throws IOException {
        validateExport(documentType, format, sbd);
        ExportPayloadDTO payload = buildPayload(ctx, documentType, searchQuery);
        if (payload.excelPreambleRows() != null) {
            exportToExcel(payload.excelSheetName(), payload.excelPreambleRows(),
                    payload.tables().get(0).headers(), payload.tables().get(0).rows(), out);
        } else {
            exportToExcel(payload.excelSheetName(), payload.tables().get(0).headers(),
                    payload.tables().get(0).rows(), out);
        }
    }

    // Builds print preview for session tables or per-candidate THEORY/LAYOUT/VIOLATION JSP pages.
    @Override
    public PrintPreviewDTO print(ExportContextDTO ctx, String documentType,
            int sbd, String searchQuery) throws IOException {
        String normalized = formatDocumentType(documentType);
        if (isSessionDocumentType(normalized) && !isCandidateResultDocument(normalized, sbd)) {
            ExportPayloadDTO payload = buildPayload(ctx, normalized, searchQuery);
            return new PrintPreviewDTO(
                    "/views/examiner/print/table.jsp",
                    payload,
                    null,
                    formatPrintTitle(normalized, sbd));
        }
        if (sbd <= 0) {
            throw new IOException("Thiếu số báo danh.");
        }
        Map<String, Object> model = buildCandidatePrintModel(ctx, normalized, sbd);
        String form = model.get("_FORM") == null ? "THEORY" : model.get("_FORM").toString();
        String jspPath;
        if ("LAYOUT".equalsIgnoreCase(form)) {
            jspPath = "/views/examiner/print/layout.jsp";
        } else if ("VIOLATION".equalsIgnoreCase(form)) {
            jspPath = "/views/examiner/print/violation.jsp";
        } else {
            jspPath = "/views/examiner/print/theory.jsp";
        }
        return new PrintPreviewDTO(
                jspPath,
                null,
                model,
                formatBbPrintTitle(normalized, sbd));
    }

    // Builds flattened candidate model consumed by JSP print templates.
    private Map<String, Object> buildCandidatePrintModel(ExportContextDTO ctx, String documentType, int sbd)
            throws IOException {
        String normalized = formatDocumentType(documentType);

        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String form;
        Map<String, Object> data;
        switch (normalized) {
            case "theory", "signature", "signature_form" -> {
                form = "THEORY";
                data = buildBb1Placeholders(ctx, candidate);
            }
            case "layout" -> {
                form = "LAYOUT";
                data = buildBb2Placeholders(ctx, candidate);
            }
            case "violation" -> {
                form = "VIOLATION";
                data = buildViolationPlaceholders(ctx, candidate);
            }
            case "result" -> {
                form = ctx.isTheory() ? "THEORY" : "LAYOUT";
                data = "THEORY".equals(form)
                        ? buildBb1Placeholders(ctx, candidate)
                        : buildBb2Placeholders(ctx, candidate);
            }
            default ->
                throw new IOException("Loại văn bản in không được hỗ trợ: " + documentType);
        }

        data.put("_FORM", form);
        if ("THEORY".equals(form)) {
            Map<String, String> answersA = parseAnswerBlockToMap(stringValue(data.get("A")));
            Map<String, String> answersB = parseAnswerBlockToMap(stringValue(data.get("B")));
            List<String> listA = toAnswerList(answersA, BLOCK_A_FROM, BLOCK_A_TO);
            List<String> listB = toAnswerList(answersB, BLOCK_B_FROM, BLOCK_B_TO);
            data.put("answerListA", listA);
            data.put("answerListB", listB);
            data.put("marksA", buildChoiceMarks(listA));
            data.put("marksB", buildChoiceMarks(listB));
        }
        String photoUrl = candidate.getPhotoImageUrl();
        data.put("PHOTO_URL", resolveCandidatePhotoUrl(ctx, candidate.getCandidateNumber(), photoUrl));
        data.put("PIC", "");
        return data;
    }

    // Build a browser-reachable photo URL for print templates (local file via servlet, or remote http).
    private String resolveCandidatePhotoUrl(ExportContextDTO ctx, int sbd, String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return "";
        }
        String trimmed = photoUrl.trim();
        if (CandidatePhotoFiles.isRemoteUrl(trimmed)) {
            return trimmed;
        }
        if (CandidatePhotoFiles.findPhotoFile(trimmed) == null) {
            return "";
        }
        String contextPath = ctx.contextPath() == null ? "" : ctx.contextPath();
        return contextPath + "/examiner/candidate-photo?sbd=" + sbd;
    }

    private Map<String, Object> buildBb1Placeholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("A", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_A_FROM, BLOCK_A_TO));
        data.put("B", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_B_FROM, BLOCK_B_TO));
        data.put("SCORE", format(candidate.getScoreTheory()));
        boolean passed = "Đạt".equalsIgnoreCase(stringValue(candidate.getResultLabel())) || candidate.isPassed();
        data.put("P", passed ? "X" : "");
        data.put("F", passed ? "" : "X");
        return data;
    }

    private Map<String, Object> buildBb2Placeholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("VNO", format(candidate.getVehicleName()));
        data.put("TIME", format(candidate.getExamDate()));
        data.put("RAND1", "");
        data.put("RAND2", "");
        data.put("RAND3", "");
        List<Map<String, Object>> deductionRows = buildLayoutDeductionRows(ctx, candidate);
        data.put("deductionRows", deductionRows);
        int totalTimes = 0;
        double totalDeducted = 0;
        for (Map<String, Object> row : deductionRows) {
            totalTimes += toInt(row.get("occurrenceCount"));
            totalDeducted += toDouble(row.get("totalDeducted"));
        }
        data.put("A", deductionRows);
        data.put("TIMES", totalTimes);
        data.put("TOTAL", formatNumber(totalDeducted));
        data.put("SCORE", format(candidate.getExamScore()));
        boolean passed = isPracticalPassed(candidate);
        data.put("P", passed ? "X" : "");
        data.put("F", passed ? "" : "X");
        ExamResult result = candidate.getEnrollmentId() > 0
                ? examResultDAO.getByExamEnrollmentId(candidate.getEnrollmentId())
                : null;
        data.put("END", result != null && result.getResultDate() != null ? formatTime(result.getResultDate()) : "-");
        return data;
    }

    private Map<String, Object> buildViolationPlaceholders(ExportContextDTO ctx, CandidateRowDTO candidate)
            throws IOException {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        SectionType section = ctx.section() != null ? ctx.section() : SectionType.LAYOUT;
        CandidateViolation violation = candidateViolationDAO.getLatestByExamAndSbd(
                ctx.examId(), candidate.getCandidateNumber(), section.getValue());
        if (violation == null) {
            throw new IOException("Không tìm thấy biên bản vi phạm cho SBD " + candidate.getCandidateNumber());
        }
        data.put("REASON", format(violation.getReason()));
        data.put("TIME", violation.getCreatedAt() != null ? AUDIT_DATE_FMT.format(violation.getCreatedAt()) : "");
        data.put("DETAILS", format(violation.getDetails()));
        data.put("VIOPIC", format(violation.getEvidenceUrl()));
        data.put("VIOPIC_URL", format(violation.getEvidenceUrl()));
        return data;
    }

    private List<Map<String, Object>> buildLayoutDeductionRows(ExportContextDTO ctx, CandidateRowDTO candidate) {
        List<Map<String, Object>> rawRows = deductionRecordViewDAO.getDeductionRowsForCandidate(
                ctx.examId(), candidate.getCandidateNumber(), SectionType.LAYOUT.getValue());
        List<Map<String, Object>> rows = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> raw : rawRows) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("stt", index++);
            row.put("reason", format(raw.get("reason")));
            row.put("occurrenceCount", toInt(raw.get("occurrenceCount")));
            row.put("totalDeducted", formatNumber(toDouble(raw.get("totalDeducted"))));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> baseCandidatePlaceholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = new LinkedHashMap<>();
        TheoryPaper paper = loadTheoryPaper(candidate);
        data.put("DEPT", "TP. HÀ NỘI");
        data.put("FNAME", format(candidate.getFullName()));
        data.put("EXAM", formatExamCode(ctx));
        data.put("PIC", "");
        data.put("DOB", format(candidate.getDob()));
        data.put("DATE", format(candidate.getExamDate()));
        data.put("IDNO", format(candidate.getGovernmentId()));
        data.put("START", formatTime(paper != null ? paper.getStartedAt() : null));
        data.put("CLASS", format(candidate.getLicenceClass()));
        data.put("END", formatTime(paper != null ? paper.getSubmittedAt() : null));
        data.put("CNO", format(candidate.getCandidateNumber()));
        data.put("TAKENO", "1");
        return data;
    }

    private CandidateRowDTO findCandidateRow(ExportContextDTO ctx, int sbd) throws IOException {
        CandidateRowDTO row = viewService.getCandidateViewRow(ctx.examId(), sbd, ctx.section());
        if (row == null) {
            throw new IOException("Không tìm thấy thí sinh SBD " + sbd);
        }
        return row;
    }

    private TheoryPaper loadTheoryPaper(CandidateRowDTO candidate) {
        if (candidate.getEnrollmentId() <= 0) {
            return null;
        }
        return theoryPaperDAO.getByExamEnrollmentId(candidate.getEnrollmentId());
    }

    private String formatExamCode(ExportContextDTO ctx) {
        if (ctx == null || ctx.examId() <= 0) {
            return "-";
        }
        Exam exam = examDAO.get(ctx.examId());
        String examCode = exam != null ? exam.getExamCode() : null;
        return examCode == null || examCode.isBlank() ? "-" : examCode.trim();
    }

    private String buildTheoryAnswerBlock(int enrollmentId, int fromQuestionNo, int toQuestionNo) {
        if (enrollmentId <= 0) {
            return "-";
        }
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            return "-";
        }
        List<CandidateAnswer> answers = candidateAnswerDAO.getAllByTheoryPaperId(paper.getTheoryPaperId());
        if (answers.isEmpty()) {
            return "-";
        }

        Map<Integer, String> answerByNo = new HashMap<>();
        for (int i = 0; i < answers.size(); i++) {
            int paperQuestionNo = i + 1;
            if (paperQuestionNo < fromQuestionNo || paperQuestionNo > toQuestionNo) {
                continue;
            }
            String letter = answers.get(i).getAnswer();
            answerByNo.put(paperQuestionNo, letter == null || letter.isBlank() ? "-" : letter.trim());
        }
        if (answerByNo.isEmpty()) {
            return "-";
        }
        List<Integer> questionNos = new ArrayList<>(answerByNo.keySet());
        Collections.sort(questionNos);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < questionNos.size(); i++) {
            if (i > 0) {
                text.append(' ');
            }
            int no = questionNos.get(i);
            text.append(no).append('.').append(answerByNo.get(no));
        }
        return text.toString();
    }

    private static boolean isPracticalPassed(CandidateRowDTO candidate) {
        if (candidate.getExamScore() != null) {
            return candidate.getExamScore() >= 80;
        }
        return "Đạt".equalsIgnoreCase(stringValue(candidate.getResultLabel())) || candidate.isPassed();
    }

    private static String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "" : text;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString().trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString().trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private static String formatNumber(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static Map<String, String> parseAnswerBlockToMap(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null || text.isBlank() || "-".equals(text.trim())) {
            return map;
        }
        String[] parts = text.trim().split("\\s+");
        for (String part : parts) {
            int dot = part.indexOf('.');
            if (dot <= 0 || dot >= part.length() - 1) {
                continue;
            }
            try {
                Integer.parseInt(part.substring(0, dot));
                map.put(part.substring(0, dot), part.substring(dot + 1).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private static List<String> toAnswerList(Map<String, String> answers, int from, int to) {
        List<String> list = new ArrayList<>();
        for (int q = from; q <= to; q++) {
            String value = answers.get(String.valueOf(q));
            list.add(value == null ? "" : value);
        }
        return list;
    }

    private static List<List<String>> buildChoiceMarks(List<String> answers) {
        List<List<String>> marks = new ArrayList<>();
        for (int choice = 1; choice <= 4; choice++) {
            List<String> row = new ArrayList<>();
            for (String answer : answers) {
                row.add(matchesChoice(answer, choice) ? "X" : "");
            }
            marks.add(row);
        }
        return marks;
    }

    private static boolean matchesChoice(String answer, int choice) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        String normalized = answer.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals(String.valueOf(choice))) {
            return true;
        }
        char letter = (char) ('A' + choice - 1);
        return normalized.equals(String.valueOf(letter));
    }

    // Validates export request for Excel-only session document types.
    private void validateExport(String documentType, FileType format, int sbd) throws IOException {
        String normalized = formatDocumentType(documentType);
        if (format == FileType.DOCX) {
            throw new IOException("Loại tài liệu này chỉ xuất Excel.");
        }
        if (format != FileType.EXCEL) {
            throw new IOException("Định dạng xuất không được hỗ trợ.");
        }
        if (isCandidateResultDocument(normalized, sbd)) {
            throw new IOException("Biên bản kết quả thi chỉ hỗ trợ in.");
        }
    }
}
