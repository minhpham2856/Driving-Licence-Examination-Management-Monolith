package examiner.service.impl;

import examiner.dto.CandidateRowDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExamStatsDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.ExportPayloadDTO;
import examiner.dto.PrintPreviewDTO;
import examiner.dto.XmlExportDocument;
import examiner.dto.XmlExportTable;
import shared.enums.FileType;
import shared.enums.SectionType;
import shared.model.Audit;
import shared.model.Exam;
import shared.model.ExaminerSchedule;
import examiner.dao.DeductionRecordViewDAO;
import examiner.dao.ExamDAO;
import examiner.dao.impl.DeductionRecordViewDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.service.AuditService;
import examiner.service.ExamViewService;
import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.formatPrintTitle;
import static examiner.util.FormatUtil.formatSbdFilter;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.isSessionDocumentType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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

// Builds Excel exports for examiner reports using Apache POI.
public class ExcelServiceImpl implements FileService {

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();
    private final ExamViewService viewService = new ExamViewServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final int AUDIT_LIMIT = 5000;
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

    // Private helper: export to xml.
    private void exportToXml(XmlExportDocument document, OutputStream out) throws IOException {
        if (document == null || document.rootElement() == null || document.rootElement().isBlank()) {
            throw new IllegalArgumentException("XML root element is required.");
        }
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        appendOpenTag(sb, document.rootElement());
        sb.append('\n');
        if (document.metadata() != null && !document.metadata().isEmpty()) {
            appendMetadata(sb, document.metadata(), "  ");
        }
        if (document.tables() != null) {
            for (XmlExportTable table : document.tables()) {
                appendTable(sb, table, "  ");
            }
        }
        appendCloseTag(sb, document.rootElement());
        sb.append('\n');
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // Writes nested metadata map entries into XML export document body.
    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata, String indent) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            appendValueElement(sb, entry.getKey(), entry.getValue(), indent);
        }
    }

    // Private helper: append value element.
    private void appendValueElement(StringBuilder sb, String elementName, Object value, String indent) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            appendOpenTag(sb, indent, elementName);
            sb.append('\n');
            appendMetadata(sb, nestedMap, indent + "  ");
            appendCloseTag(sb, indent, elementName);
            sb.append('\n');
            return;
        }
        sb.append(indent).append('<').append(elementName).append('>');
        sb.append(escapeXml(formatXmlValue(value)));
        appendCloseTag(sb, elementName);
        sb.append('\n');
    }

    // Private helper: append table.
    private void appendTable(StringBuilder sb, XmlExportTable table, String indent) {
        if (table == null || table.listElement() == null || table.listElement().isBlank()) {
            return;
        }
        appendOpenTag(sb, indent, table.listElement());
        sb.append('\n');
        String itemIndent = indent + "  ";
        String fieldIndent = itemIndent + "  ";
        List<String> fields = table.fieldElements();
        List<List<Object>> rows = table.rows();
        if (rows != null) {
            for (List<Object> row : rows) {
                appendOpenTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
                if (fields != null && row != null) {
                    int count = Math.min(fields.size(), row.size());
                    for (int i = 0; i < count; i++) {
                        appendValueElement(sb, fields.get(i), row.get(i), fieldIndent);
                    }
                }
                appendCloseTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
            }
        }
        appendCloseTag(sb, indent, table.listElement());
        sb.append('\n');
    }

    // Private helper: format xml value.
    private static String formatXmlValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date) {
            Date date = (Date) value;
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        return value.toString();
    }

    // Private helper: escape xml.
    private static String escapeXml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // Appends an XML open tag without indent.
    private static void appendOpenTag(StringBuilder sb, String tag) {
        sb.append('<').append(tag).append('>');
    }

    // Appends an XML open tag with leading indent.
    private static void appendOpenTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append('<').append(tag).append('>');
    }

    // Appends an XML close tag without indent.
    private static void appendCloseTag(StringBuilder sb, String tag) {
        sb.append("</").append(tag).append('>');
    }

    // Appends an XML close tag with leading indent.
    private static void appendCloseTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append("</").append(tag).append('>');
    }

    // === FileService ===

    // Exports examiner session reports to Excel; rejects DOCX and per-candidate result forms.
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

    // Builds session-wide table print preview for browser printing.
    @Override
    public PrintPreviewDTO print(ExportContextDTO ctx, String documentType,
            int sbd, String searchQuery) throws IOException {
        String normalized = formatDocumentType(documentType);
        if (!isSessionDocumentType(normalized) || isCandidateResultDocument(normalized, sbd)) {
            throw new IOException("ExcelService không hỗ trợ in loại tài liệu này.");
        }
        ExportPayloadDTO payload = buildPayload(ctx, normalized, searchQuery);
        return new PrintPreviewDTO(
                "/views/examiner/print/table.jsp",
                payload,
                null,
                formatPrintTitle(normalized, sbd));
    }

    // Validates export request for Excel-only session document types.
    private void validateExport(String documentType, FileType format, int sbd) throws IOException {
        String normalized = formatDocumentType(documentType);
        if (format == FileType.DOCX) {
            throw new IOException("Loại tài liệu này chỉ xuất Excel, không xuất DOCX.");
        }
        if (format != FileType.EXCEL) {
            throw new IOException("Định dạng xuất không được hỗ trợ.");
        }
        if (isCandidateResultDocument(normalized, sbd)) {
            throw new IOException("Biên bản kết quả thi chỉ xuất DOCX.");
        }
    }
}

