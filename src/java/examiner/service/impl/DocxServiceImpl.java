package examiner.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.Pictures;
import examiner.dao.CandidateAnswerDAO;
import examiner.dao.DeductionRecordViewDAO;
import examiner.dao.ExamDAO;
import examiner.dao.ExamResultDAO;
import examiner.dao.QuestionDAO;
import examiner.dao.TheoryPaperDAO;
import examiner.dao.impl.CandidateAnswerDAOImpl;
import examiner.dao.impl.DeductionRecordViewDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.dao.impl.ExamResultDAOImpl;
import examiner.dao.impl.QuestionDAOImpl;
import examiner.dao.impl.TheoryPaperDAOImpl;
import examiner.dto.CandidateRowDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExamStatsDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.ExportPayloadDTO;
import examiner.dto.PrintPreviewDTO;
import examiner.dto.XmlExportTable;
import static examiner.util.FormatUtil.formatBbPrintTitle;
import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.formatSbdFilter;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.isSessionDocumentType;
import shared.enums.FileType;
import shared.enums.SectionType;
import shared.model.Audit;
import shared.model.CandidateAnswer;
import shared.model.Exam;
import shared.model.ExamResult;
import shared.model.ExaminerSchedule;
import shared.model.Question;
import shared.model.TheoryPaper;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import examiner.service.AuditService;
import examiner.service.ExamViewService;
import java.io.IOException;
import java.io.InputStream;
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
import examiner.service.FileService;
import examiner.service.EnrollmentService;

// Builds and renders examiner export/print documents (DOCX templates, BB1/BB2 forms, table exports).
public class DocxServiceImpl implements FileService {

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
    // BB1(A1-A-B1) template answer grids: questions 1-12 and 13-25.
    private static final int BLOCK_A_FROM = 1;
    private static final int BLOCK_A_TO = 12;
    private static final int BLOCK_B_FROM = 13;
    private static final int BLOCK_B_TO = 25;
    private static final String TEMPLATE_DIR = "/docx-template/examiner/";

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();
    private final ExamViewService viewService = new ExamViewServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final ExamResultDAO examResultDAO = new ExamResultDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();

    // Builds poi-tl template config using << >> placeholder delimiters.
    private static Configure poiTlConfig() {
        return Configure.builder().buildGramer("<<", ">>").build();
    }

    // === build* (duplicated from DocumentServiceImpl) ===

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
    private ExportPayloadDTO buildMinutesExport(ExportContextDTO ctx) {
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

    // Adds export timestamp row used as table preamble in payloads.
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
                throw new IOException("Biên bản vi phạm chỉ xuất Excel, không hỗ trợ DOCX.");
            case "audit" ->
                buildAuditExport(ctx, searchQuery);
            default ->
                throw new IOException("Loại tài liệu xuất không được hỗ trợ: " + documentType);
        };
    }

    // === FileService ===

    // Exports session DOCX tables or per-candidate result forms to the output stream.
    @Override
    public void export(ExportContextDTO ctx, String documentType, FileType format,
            String searchQuery, int sbd, OutputStream out) throws IOException {
        validateExport(documentType, format, sbd);
        if (isPerCandidateResultExport(documentType, sbd)) {
            renderResultDocument(ctx, sbd, out);
            return;
        }
        ExportPayloadDTO payload = buildPayload(ctx, documentType, searchQuery);
        renderTableExport(payload, out);
    }

    // Builds per-candidate BB1/BB2 print preview for browser printing.
    @Override
    public PrintPreviewDTO print(ExportContextDTO ctx, String documentType,
            int sbd, String searchQuery) throws IOException {
        if (sbd <= 0) {
            throw new IOException("Thiếu số báo danh.");
        }
        Map<String, Object> model = buildPrintModel(ctx, documentType, sbd);
        String form = model.get("_FORM") == null ? "BB1" : model.get("_FORM").toString();
        String jspPath = "BB2".equalsIgnoreCase(form)
                ? "/views/examiner/print/bb2.jsp"
                : "/views/examiner/print/bb1.jsp";
        return new PrintPreviewDTO(
                jspPath,
                null,
                model,
                formatBbPrintTitle(documentType, sbd));
    }

    // Validates export request for DOCX session and per-candidate result forms.
    private void validateExport(String documentType, FileType format, int sbd) throws IOException {
        if (format != FileType.DOCX) {
            throw new IOException("DocxService chỉ hỗ trợ xuất DOCX.");
        }
        String normalized = formatDocumentType(documentType);
        if (isPerCandidateResultExport(documentType, sbd)) {
            return;
        }
        if (requiresSbd(normalized, sbd)) {
            throw new IOException("Thiếu số báo danh.");
        }
    }

    // Return true when exporting a per-candidate result DOCX for a specific SBD.
    private static boolean isPerCandidateResultExport(String documentType, int sbd) {
        return isCandidateResultDocument(documentType, sbd);
    }

    // Return true when DOCX export requires SBD but none was provided.
    private static boolean requiresSbd(String type, int sbd) {
        if (sbd > 0) {
            return false;
        }
        if (isCandidateResultDocument(type, sbd)) {
            return true;
        }
        return !isSessionDocumentType(type);
    }

    // Builds placeholder map and answer grids for JSP print pages (no PDF conversion).
    private Map<String, Object> buildPrintModel(ExportContextDTO ctx, String documentType, int sbd)
            throws IOException {
        String normalized = documentType == null ? "" : documentType.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("BB1")) {
            normalized = "BB1";
        } else if (normalized.startsWith("BB2")) {
            normalized = "BB2";
        }
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String form;
        Map<String, Object> data;
        switch (normalized) {
            case "BB1", "SIGNATURE", "SIGNATURE_FORM" -> {
                form = "BB1";
                data = buildBb1Placeholders(ctx, candidate);
            }
            case "BB2", "LAYOUT", "SCORE_SHEET" -> {
                form = "BB2";
                data = buildBb2Placeholders(ctx, candidate);
            }
            case "MINUTES", "RESULT" -> {
                form = resolveResultDocumentType(ctx, candidate);
                if ("BB1".equals(form)) {
                    data = buildBb1Placeholders(ctx, candidate);
                } else {
                    data = buildBb2Placeholders(ctx, candidate);
                }
            }
            default ->
                throw new IOException("Loại văn bản in không được hỗ trợ: " + documentType);
        }
        data.put("_FORM", form);
        if ("BB1".equals(form)) {
            Map<String, String> answersA = parseAnswerBlockToMap(stringValue(data.get("A")));
            Map<String, String> answersB = parseAnswerBlockToMap(stringValue(data.get("B")));
            List<String> listA = toAnswerList(answersA, BLOCK_A_FROM, BLOCK_A_TO);
            List<String> listB = toAnswerList(answersB, BLOCK_B_FROM, BLOCK_B_TO);
            data.put("answerListA", listA);
            data.put("answerListB", listB);
            data.put("marksA", buildChoiceMarks(listA));
            data.put("marksB", buildChoiceMarks(listB));
        }
        // PIC is a Pictures object for DOCX — expose URL separately for JSP.
        String photoUrl = candidate.getPhotoImageUrl();
        data.put("PHOTO_URL", photoUrl == null ? "" : photoUrl.trim());
        data.put("PIC", "");
        return data;
    }

    // Auto-picks BB1 (theory) or BB2 (practical) and renders the matching template.
    private void renderResultDocument(ExportContextDTO ctx, int sbd, OutputStream out) throws IOException {
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String docCode = resolveResultDocumentType(ctx, candidate);
        if ("BB1".equals(docCode)) {
            renderBb1Theory(ctx, sbd, out);
            return;
        }
        renderBb2Layout(ctx, sbd, out);
    }

    // Resolves BB1 vs BB2 document code from session section context.
    private String resolveResultDocumentType(ExportContextDTO ctx, CandidateRowDTO candidate) throws IOException {
        if (ctx.isTheory()) {
            return "BB1";
        }
        // JSP print page (bb2.jsp) is licence-agnostic; always use BB2 for practical result forms.
        return "BB2";
    }

    // === DOCX rendering (kept from old DocxServiceImpl) ===

    // Renders a DOCX template from classpath with poi-tl placeholders.
    public void render(String templateClasspath, Map<String, Object> placeholders, OutputStream out)
            throws IOException {
        try (InputStream in = DocxServiceImpl.class.getResourceAsStream(templateClasspath)) {
            if (in == null) {
                throw new IOException("Cannot find: " + templateClasspath);
            }
            Map<String, Object> safe = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                safe.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
            XWPFTemplate.compile(in, poiTlConfig()).render(safe).write(out);
        }
    }

    // Renders BB1 theory result form for one candidate.
    public void renderBb1Theory(ExportContextDTO ctx, int sbd, OutputStream out) throws IOException {
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB1", candidate.getLicenceClass());
        render(template, buildBb1Placeholders(ctx, candidate), out);
    }

    // Renders BB2 practical score sheet for one candidate.
    public void renderBb2Layout(ExportContextDTO ctx, int sbd, OutputStream out) throws IOException {
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB2", candidate.getLicenceClass());
        render(template, buildBb2Placeholders(ctx, candidate), out);
    }

    // Renders export payload tables into a standalone DOCX workbook-like document.
    public void renderTableExport(ExportPayloadDTO payload, OutputStream out) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            titleRun.setText(payload.excelSheetName());

            XWPFParagraph printedAt = document.createParagraph();
            printedAt.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun printedAtRun = printedAt.createRun();
            printedAtRun.setFontSize(10);
            printedAtRun.setText("Thời gian in: "
                    + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

            if (payload.metadata() != null) {
                for (Map.Entry<String, Object> entry : payload.metadata().entrySet()) {
                    appendLine(document, entry.getKey() + ": " + format(entry.getValue()));
                }
            }
            if (payload.excelPreambleRows() != null) {
                for (List<Object> row : payload.excelPreambleRows()) {
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    Object first = row.get(0);
                    if (first != null && "Thời gian xuất".equals(first.toString().trim())) {
                        continue;
                    }
                    StringBuilder line = new StringBuilder();
                    for (int i = 0; i < row.size(); i++) {
                        if (i > 0) {
                            line.append(" | ");
                        }
                        line.append(format(row.get(i)));
                    }
                    appendLine(document, line.toString());
                }
            }
            for (XmlExportTable table : payload.tables()) {
                appendLine(document, "");
                XWPFTable xwpfTable = document.createTable();
                XWPFTableRow headerRow = xwpfTable.getRow(0);
                for (int i = 0; i < table.headers().size(); i++) {
                    if (i > 0) {
                        headerRow.addNewTableCell();
                    }
                    headerRow.getCell(i).setText(table.headers().get(i));
                }
                for (List<Object> row : table.rows()) {
                    XWPFTableRow dataRow = xwpfTable.createRow();
                    for (int i = 0; i < row.size(); i++) {
                        dataRow.getCell(i).setText(format(row.get(i)));
                    }
                }
            }
            document.write(out);
        }
    }

    // Private helper: build bb1 placeholders.
    private Map<String, Object> buildBb1Placeholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("A", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_A_FROM, BLOCK_A_TO));
        data.put("B", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_B_FROM, BLOCK_B_TO));
        data.put("SCORE", format(candidate.getScoreTheory()));
        boolean passed = "Đạt".equalsIgnoreCase(stringValue(candidate.getResultLabel()))
                || candidate.isPassed();
        data.put("P", passed ? "X" : "");
        data.put("F", passed ? "" : "X");
        return data;
    }

    // Private helper: build bb2 placeholders.
    private Map<String, Object> buildBb2Placeholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("VNO", format(candidate.getVehicleName()));
        data.put("TIME", format(candidate.getExamDate()));
        data.put("RAND1", "");
        data.put("RAND2", "");
        data.put("RAND3", "");
        data.put("A", format(candidate.getExamScore()));
        data.put("SCORE", format(candidate.getExamScore()));
        boolean passed = isPracticalPassed(candidate);
        data.put("P", passed ? "X" : "");
        data.put("F", passed ? "" : "X");
        // End time = when SHV finalized score (ExamResult.ResultDate).
        ExamResult result = candidate.getEnrollmentId() > 0
                ? examResultDAO.getByExamEnrollmentId(candidate.getEnrollmentId())
                : null;
        if (result != null && result.getResultDate() != null) {
            data.put("END", formatTime(result.getResultDate()));
        } else {
            data.put("END", "-");
        }
        return data;
    }

    // Determines practical pass from score threshold or result label flags.
    private static boolean isPracticalPassed(CandidateRowDTO candidate) {
        if (candidate.getExamScore() != null) {
            return candidate.getExamScore() >= 80;
        }
        return "Đạt".equalsIgnoreCase(stringValue(candidate.getResultLabel()))
                || candidate.isPassed();
    }

    // Private helper: base candidate placeholders.
    private Map<String, Object> baseCandidatePlaceholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = new LinkedHashMap<>();
        TheoryPaper paper = loadTheoryPaper(candidate);
        String shiftLabel = "-";

        data.put("DEPT", "TP. HÀ NỘI");
        data.put("FNAME", format(candidate.getFullName()));
        data.put("EXAM", shiftLabel);
        data.put("PIC", buildPicPlaceholder(candidate));
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

    // Private helper: build pic placeholder.
    private Object buildPicPlaceholder(CandidateRowDTO candidate) {
        String url = candidate.getPhotoImageUrl();
        if (url == null || url.isBlank()) {
            return "";
        }
        try {
            return Pictures.ofUrl(url.trim()).size(120, 150).create();
        } catch (Exception e) {
            return "";
        }
    }

    // Private helper: find candidate row.
    private CandidateRowDTO findCandidateRow(ExportContextDTO ctx, int sbd) throws IOException {
        CandidateRowDTO row = viewService.getCandidateViewRow(
                ctx.examId(), sbd, ctx.section());
        if (row == null) {
            throw new IOException("Không tìm thấy thí sinh SBD " + sbd);
        }
        return row;
    }

    // Picks BB1/BB2 template path for A1/A/B1 only; null for unsupported licence.
    private String pickTemplate(String documentCode, String licenceClass) throws IOException {
        String cls = normalizeLicenceClass(licenceClass);
        if (cls.isEmpty()) {
            throw new IOException("Không xác định được hạng GPLX của kỳ thi. Vui lòng kiểm tra cấu hình kỳ thi.");
        }
        if ("BB1".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls) || "B1".equals(cls)) {
                return TEMPLATE_DIR + "BB1(A1-A-B1).docx";
            }
            throw new IOException("Không có mẫu BB1 cho hạng " + cls + " (chỉ hỗ trợ A1/A/B1).");
        }
        if ("BB2".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls)) {
                return TEMPLATE_DIR + "BB2(A1-A).docx";
            }
            if ("B1".equals(cls)) {
                return TEMPLATE_DIR + "BB2(B1).docx";
            }
            throw new IOException("Không có mẫu BB2 cho hạng " + cls + " (chỉ hỗ trợ A1/A/B1).");
        }
        throw new IOException("Loại biên bản không hợp lệ: " + documentCode);
    }

    // Private helper: normalize licence class.
    private static String normalizeLicenceClass(String licenceClass) {
        if (licenceClass == null || licenceClass.isBlank()) {
            return "";
        }
        String normalized = licenceClass.trim().toUpperCase(Locale.ROOT);
        if ("-".equals(normalized)) {
            return "";
        }
        return normalized;
    }

    // Loads theory paper row for enrollment when building BB1 placeholders.
    private TheoryPaper loadTheoryPaper(CandidateRowDTO candidate) {
        if (candidate.getEnrollmentId() <= 0) {
            return null;
        }
        return theoryPaperDAO.getByExamEnrollmentId(candidate.getEnrollmentId());
    }

    // Private helper: build theory answer block.
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

        List<Integer> questionIds = new ArrayList<>();
        for (CandidateAnswer answer : answers) {
            questionIds.add(answer.getQuestionId());
        }
        Map<Integer, Integer> questionNoById = new HashMap<>();
        for (Question question : questionDAO.getAllByIds(questionIds)) {
            questionNoById.put(question.getQuestionId(), question.getQuestionNumber());
        }

        Map<Integer, String> answerByNo = new HashMap<>();
        for (CandidateAnswer answer : answers) {
            Integer questionNo = questionNoById.get(answer.getQuestionId());
            if (questionNo == null || questionNo < fromQuestionNo || questionNo > toQuestionNo) {
                continue;
            }
            String letter = answer.getAnswer();
            if (letter == null || letter.isBlank()) {
                letter = "-";
            }
            answerByNo.put(questionNo, letter.trim());
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

    // Appends one text line paragraph to an in-memory DOCX document.
    private static void appendLine(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text == null ? "" : text);
    }

    // Formats placeholder values as trimmed strings for DOCX output.
    private String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "" : text;
    }

    // Converts nullable object to trimmed string for template logic.
    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    // Private helper: parse answer block to map.
    private static Map<String, String> parseAnswerBlockToMap(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null || text.isBlank() || "-".equals(text.trim())) {
            return map;
        }
        String[] parts = text.trim().split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
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

    // Private helper: to answer list.
    private static List<String> toAnswerList(Map<String, String> answers, int from, int to) {
        List<String> list = new ArrayList<>();
        for (int q = from; q <= to; q++) {
            String value = answers.get(String.valueOf(q));
            list.add(value == null ? "" : value);
        }
        return list;
    }

    // 4 rows (choices 1-4) × N columns; cell is "X" if that choice was selected.
    private static List<List<String>> buildChoiceMarks(List<String> answers) {
        List<List<String>> marks = new ArrayList<>();
        for (int choice = 1; choice <= 4; choice++) {
            List<String> row = new ArrayList<>();
            for (int i = 0; i < answers.size(); i++) {
                row.add(matchesChoice(answers.get(i), choice) ? "X" : "");
            }
            marks.add(row);
        }
        return marks;
    }

    // Private helper: matches choice.
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
}

