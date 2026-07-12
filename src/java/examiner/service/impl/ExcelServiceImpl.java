package examiner.service.impl;

import examiner.dto.CandidateRowDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExamStatsDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.ExportPayloadDTO;
import examiner.dto.XmlExportDocument;
import examiner.dto.XmlExportTable;
import examiner.enums.DocumentFormat;
import shared.model.Audit;
import shared.model.Exam;
import shared.model.ExaminerSchedule;
import examiner.dao.DeductionRecordViewDAO;
import examiner.dao.ExamDAO;
import examiner.dao.impl.DeductionRecordViewDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.service.AuditService;
import examiner.service.DocumentService;
import examiner.service.ExamViewService;
import examiner.service.RegistrationService;
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

public class ExcelServiceImpl implements DocumentService {

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();
    private final ExamViewService viewDataService = new ExamViewServiceImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final int AUDIT_LIMIT = 5000;
    private static final List<String> CANDIDATE_FIELDS = List.of(
            "stt", "sbd", "hoVaTen", "ngaySinh", "gioiTinh", "cccd", "email", "soDienThoai",
            "diaChi", "hangGplx", "lyDoThi", "ngayThi", "vangThi", "tinhTrangThi",
            "dung", "sai", "khongTraLoi", "diemLyThuyet", "ketQuaLt",
            "diemThucHanh");
    private static final List<String> CANDIDATE_HEADERS = List.of(
            "STT", "SBD", "Há» vÃ  tÃªn", "NgÃ y sinh", "Giá»›i tÃ­nh", "Sá»‘ cÄƒn cÆ°á»›c", "Email", "Sá»‘ Ä‘iá»‡n thoáº¡i",
            "Äá»‹a chá»‰", "Háº¡ng GPLX", "LÃ½ do thi", "NgÃ y thi", "Váº¯ng thi", "TÃ¬nh tráº¡ng thi",
            "ÄÃºng", "Sai", "KhÃ´ng TL", "Äiá»ƒm lÃ½ thuyáº¿t", "Káº¿t quáº£ LT",
            "Äiá»ƒm thá»±c hÃ nh");

    private Map<String, Object> getExamExportMeta(int examId) {
        Map<String, Object> meta = new LinkedHashMap<>();
        Exam e = examDAO.getById(examId);
        if (e != null) {
            meta.put("shiftLabel", false);
            meta.put("startTime", e.getStartTime() != null ? e.getStartTime().toString() : "");
            meta.put("endTime", e.getEndTime() != null ? e.getEndTime().toString() : "");
            meta.put("examCode", e.getExamCode());
        }
        return meta;
    }

    public ExportPayloadDTO buildCandidatesExport(ExportContextDTO ctx) {
        List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(
                ctx.examId(), ctx.isTheory(), ctx.sectionName());
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        for (CandidateRowDTO c : candidates) {
            rows.add(Arrays.asList(
                    index++,
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
                    "KhÃ´ng",
                    c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                    c.getCorrect(),
                    c.getWrong(),
                    c.getUnanswered(),
                    c.getScoreTheory(),
                    c.getResultLabel(),
                    c.getScorePractical()));
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", CANDIDATE_FIELDS, CANDIDATE_HEADERS, rows);
        return new ExportPayloadDTO(
                "Danh sÃ¡ch thÃ­ sinh", "danhSachThiSinh", Map.of(), List.of(table), null);
    }

    public ExportPayloadDTO buildResultsExport(ExportContextDTO ctx) {
        List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(
                ctx.examId(), ctx.isTheory(), ctx.sectionName());
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        if (ctx.isTheory() == false) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Há» vÃ  tÃªn", "Äiá»ƒm", "Káº¿t quáº£", "TÃ¬nh tráº¡ng", "Váº¯ng thi");
            for (CandidateRowDTO c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getExamScore(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        "KhÃ´ng"));
            }
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Há» vÃ  tÃªn", "ÄÃºng", "Sai", "KhÃ´ng TL", "Káº¿t quáº£", "TÃ¬nh tráº¡ng",
                    "Váº¯ng thi");
            for (CandidateRowDTO c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getCorrect(),
                        c.getWrong(),
                        c.getUnanswered(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        "KhÃ´ng"));
            }
        }
        XmlExportTable table = new XmlExportTable("ketQuaThi", "ketQua", fields, headers, rows);
        return new ExportPayloadDTO("Káº¿t quáº£ thi", "ketQuaThi", Map.of(), List.of(table), null);
    }

    public ExportPayloadDTO buildMinutesExport(ExportContextDTO ctx) {
        Map<String, Object> meta = getExamExportMeta(ctx.examId());
        ExamStatsDTO summary = viewDataService.buildCandidateSummary(
                ctx.examId(), ctx.isTheory(), ctx.sectionName());
        List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(
                ctx.examId(), ctx.isTheory(), ctx.sectionName());
        Map<String, Object> metadata = buildMinutesMetadata(meta, summary, ctx.schedule(), ctx.isTheory(),
                ctx.sectionName());
        List<List<Object>> preamble = buildMinutesPreamble(meta, summary, ctx.schedule(), ctx.isTheory(),
                ctx.sectionName());
        List<String> fields;
        List<String> headers;
        if (ctx.isTheory() == false) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Há» vÃ  tÃªn", "Äiá»ƒm", "Káº¿t quáº£", "TÃ¬nh tráº¡ng", "Váº¯ng thi");
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Há» vÃ  tÃªn", "ÄÃºng", "Sai", "KhÃ´ng TL", "Káº¿t quáº£", "TÃ¬nh tráº¡ng",
                    "Váº¯ng thi");
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.isTheory()));
        return new ExportPayloadDTO(
                "BiÃªn báº£n thi", "bienBanThi", metadata, List.of(table), preamble);
    }

    public ExportPayloadDTO buildViolationsExport(ExportContextDTO ctx) {
        return buildViolationsExport(ctx, null);
    }

    public ExportPayloadDTO buildViolationsExport(ExportContextDTO ctx, String sbdFilterRaw) {
        Map<String, Object> meta = getExamExportMeta(ctx.examId());
        List<Audit> auditViolations = auditService.getViolationLogsForExam(ctx.examId(), AUDIT_LIMIT);
        Map<Long, String> changerNames = auditService.loadChangerNames(auditViolations);
        List<Map<String, Object>> scoreViolations = deductionRecordViewDAO.getViolationRowsForExam(ctx.examId());
        Integer sbdFilter = parseSbdFilter(sbdFilterRaw);
        if (sbdFilter != null) {
            final String sbdText = String.valueOf(sbdFilter);
            List<Map<String, Object>> filteredScore = new ArrayList<>();
            for (Map<String, Object> row : scoreViolations) {
                if (sbdFilter.equals(toInteger(row.get("sbd")))) {
                    filteredScore.add(row);
                }
            }
            scoreViolations = filteredScore;
            Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.examId());
            List<Audit> filteredAudits = new ArrayList<>();
            for (Audit log : auditViolations) {
                if (sbdText.equals(auditService.extractSbdForDisplay(log, sbdByRecordId))) {
                    filteredAudits.add(log);
                }
            }
            auditViolations = filteredAudits;
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÃŠN Báº¢N VI PHáº M");
        metadata.put("caThi", nullToDash(meta.get("shiftLabel")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));
        List<List<Object>> auditRows = new ArrayList<>();
        int index = 1;
        for (Audit log : auditViolations) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            String time = log.getCreatedAt() != null ? AUDIT_DATE_FMT.format(log.getCreatedAt()) : "-";
            auditRows.add(Arrays.asList(
                    index++,
                    nullToDash(changerName),
                    nullToDash(log.getNewValue()),
                    nullToDash(log.getReason()),
                    time));
        }
        List<List<Object>> scoreRows = new ArrayList<>();
        index = 1;
        for (Map<String, Object> row : scoreViolations) {
            scoreRows.add(Arrays.asList(
                    index++,
                    row.get("sbd"),
                    row.get("fullName"),
                    row.get("sectionName"),
                    row.get("violationReason"),
                    row.get("deductionPoints"),
                    Boolean.TRUE.equals(row.get("critical")) ? "CÃ³" : "KhÃ´ng",
                    row.get("currentScore")));
        }
        XmlExportTable auditTable = new XmlExportTable(
                "viPhamQuyChe",
                "viPham",
                List.of("stt", "nguoiGhi", "noiDung", "lyDo", "thoiGian"),
                List.of("STT", "NgÆ°á»i ghi", "Ná»™i dung", "LÃ½ do", "Thá»i gian"),
                auditRows);
        XmlExportTable scoreTable = new XmlExportTable(
                "truDiemThi",
                "banTruDiem",
                List.of("stt", "sbd", "hoVaTen", "phanThi", "lyDoTruDiem", "diemTru", "loiNghiemTrong", "diemHienTai"),
                List.of("STT", "SBD", "Há» vÃ  tÃªn", "Pháº§n thi", "LÃ½ do trá»« Ä‘iá»ƒm", "Äiá»ƒm trá»«",
                        "Lá»—i nghiÃªm trá»ng", "Äiá»ƒm hiá»‡n táº¡i"),
                scoreRows);
        List<List<Object>> excelRows = buildViolationsExcelRows(meta, auditViolations, changerNames, scoreViolations);
        return new ExportPayloadDTO(
                "BiÃªn báº£n vi pháº¡m", "bienBanViPham", metadata, List.of(auditTable, scoreTable), excelRows);
    }

    public ExportPayloadDTO buildAuditExport(ExportContextDTO ctx, String searchQuery) {
        List<Audit> logs = auditService.getLogsForExamPaginated(ctx.examId(), 1, AUDIT_LIMIT, searchQuery);
        Map<Long, String> changerNames = auditService.loadChangerNames(logs);
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
                List.of("NgÆ°á»i dÃ¹ng", "Thao tÃ¡c", "Äá»‘i tÆ°á»£ng", "SBD", "ThÃ´ng tin", "CÅ©", "Má»›i", "LÃ½ do",
                        "Thá»i gian"),
                rows);
        Map<String, Object> metadata = Map.of();
        if (searchQuery != null && !searchQuery.isBlank()) {
            metadata = Map.of("tuKhoa", searchQuery.trim());
        }
        return new ExportPayloadDTO("Nháº­t kÃ½", "nhatKyHeThong", metadata, List.of(table), null);
    }

    private Map<String, Object> buildMinutesMetadata(Map<String, Object> meta, ExamStatsDTO summary,
            ExaminerSchedule schedule, boolean isTheory, String sectionName) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÃŠN Báº¢N Tá»” CHá»¨C THI");
        metadata.put("caThi", nullToDash(meta.get("shiftLabel")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));
        metadata.put("ngayThi", formatDate(meta.get("examDate")));
        metadata.put("gioBatDau", formatTime(meta.get("startTime")));
        metadata.put("gioKetThuc", formatTime(meta.get("endTime")));
        if (schedule != null && schedule.getExamArea() != null) {
            metadata.put("khuVucPhong", nullToDash(schedule.getExamArea().getAreaName()));
        }
        metadata.put("phanThi",
                !isTheory ? nullToDash(sectionName) : "LÃ½ thuyáº¿t");
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

    private List<List<Object>> buildMinutesPreamble(Map<String, Object> meta, ExamStatsDTO summary,
            ExaminerSchedule schedule, boolean isTheory, String sectionName) {
        List<List<Object>> preamble = new ArrayList<>();
        preamble.add(Arrays.asList("BIÃŠN Báº¢N Tá»” CHá»¨C THI"));
        preamble.add(Arrays.asList("Ca thi", nullToDash(meta.get("shiftLabel"))));
        preamble.add(Arrays.asList("MÃ£ Ä‘á»£t thi", nullToDash(meta.get("examCode"))));
        preamble.add(Arrays.asList("NgÃ y thi", formatDate(meta.get("examDate"))));
        preamble.add(Arrays.asList("Giá» báº¯t Ä‘áº§u", formatTime(meta.get("startTime"))));
        preamble.add(Arrays.asList("Giá» káº¿t thÃºc", formatTime(meta.get("endTime"))));
        if (schedule != null && schedule.getExamArea() != null) {
            preamble.add(Arrays.asList("Khu vá»±c / PhÃ²ng", nullToDash(schedule.getExamArea().getAreaName())));
        }
        preamble.add(Arrays.asList("Pháº§n thi",
                !isTheory ? nullToDash(sectionName) : "LÃ½ thuyáº¿t"));
        preamble.add(Arrays.asList());
        preamble.add(Arrays.asList("Tá»•ng thÃ­ sinh", summary.getTotal()));
        preamble.add(Arrays.asList("ÄÃ£ thi", summary.getDone()));
        preamble.add(Arrays.asList("Äang thi", summary.getTesting()));
        preamble.add(Arrays.asList("ChÆ°a thi", summary.getPending()));
        preamble.add(Arrays.asList("Äáº¡t", summary.getPassed()));
        preamble.add(Arrays.asList("TrÆ°á»£t", summary.getFailed()));
        preamble.add(Arrays.asList());
        return preamble;
    }

    private static List<List<Object>> buildMinutesRows(List<CandidateRowDTO> candidates,
            boolean isTheory) {
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        for (CandidateRowDTO c : candidates) {
            if (!isTheory) {
                rows.add(Arrays.asList(
                        index++,
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getExamScore(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        "KhÃ´ng"));
            } else {
                rows.add(Arrays.asList(
                        index++,
                        c.getCandidateNumber(),
                        c.getFullName(),
                        c.getCorrect(),
                        c.getWrong(),
                        c.getUnanswered(),
                        c.getResultLabel(),
                        c.getSectionStatus() != null ? c.getSectionStatus().getValue() : "",
                        "KhÃ´ng"));
            }
        }
        return rows;
    }

    private List<List<Object>> buildViolationsExcelRows(Map<String, Object> meta, List<Audit> auditViolations,
            Map<Long, String> changerNames, List<Map<String, Object>> scoreViolations) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("BIÃŠN Báº¢N VI PHáº M"));
        rows.add(Arrays.asList("Ca thi", nullToDash(meta.get("shiftLabel"))));
        rows.add(Arrays.asList("MÃ£ Ä‘á»£t thi", nullToDash(meta.get("examCode"))));
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("I. Vi pháº¡m quy cháº¿ thi (nháº­t kÃ½)"));
        rows.add(Arrays.asList("STT", "NgÆ°á»i ghi", "Ná»™i dung", "LÃ½ do", "Thá»i gian"));
        int index = 1;
        for (Audit log : auditViolations) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            String time = log.getCreatedAt() != null ? AUDIT_DATE_FMT.format(log.getCreatedAt()) : "-";
            rows.add(Arrays.asList(
                    index++,
                    nullToDash(changerName),
                    nullToDash(log.getNewValue()),
                    nullToDash(log.getReason()),
                    time));
        }
        if (auditViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "KhÃ´ng cÃ³ vi pháº¡m", "-", "-"));
        }
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("II. Trá»« Ä‘iá»ƒm thi"));
        rows.add(Arrays.asList("STT", "SBD", "Há» vÃ  tÃªn", "Pháº§n thi", "LÃ½ do trá»« Ä‘iá»ƒm", "Äiá»ƒm trá»«",
                "Lá»—i nghiÃªm trá»ng", "Äiá»ƒm hiá»‡n táº¡i"));
        index = 1;
        for (Map<String, Object> row : scoreViolations) {
            rows.add(Arrays.asList(
                    index++,
                    row.get("sbd"),
                    row.get("fullName"),
                    row.get("sectionName"),
                    row.get("violationReason"),
                    row.get("deductionPoints"),
                    Boolean.TRUE.equals(row.get("critical")) ? "CÃ³" : "KhÃ´ng",
                    row.get("currentScore")));
        }
        if (scoreViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "KhÃ´ng cÃ³ trá»« Ä‘iá»ƒm", "-", "-", "-", "-", "-"));
        }
        return rows;
    }

    private static String formatDate(Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            synchronized (DATE_FMT) {
                return DATE_FMT.format(date);
            }
        }
        return nullToDash(value);
    }

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

    private static String nullToDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "-" : text;
    }

    private Map<Integer, String> buildSbdLookup(int examId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (EnrollmentDTO reg : registrationService.getCandidatesByExam(examId)) {
            lookup.put(reg.getId(), String.valueOf(reg.getCandidateNumber()));
        }
        return lookup;
    }

    private static Integer parseSbdFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

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

    private ExportPayloadDTO buildPayload(ExportContextDTO ctx, String documentType, String searchQuery) {
        String normalized = documentType == null ? "" : documentType.trim().toLowerCase();
        return switch (normalized) {
            case "candidates" ->
                buildCandidatesExport(ctx);
            case "results" ->
                buildResultsExport(ctx);
            case "minutes" ->
                buildMinutesExport(ctx);
            case "violations" ->
                buildViolationsExport(ctx, searchQuery);
            case "audit" ->
                buildAuditExport(ctx, searchQuery);
            default ->
                throw new IllegalArgumentException("Loáº¡i tÃ i liá»‡u xuáº¥t khÃ´ng Ä‘Æ°á»£c há»— trá»£: " + documentType);
        };
    }

    // === Rendering (from XmlServiceImpl) ===
    private void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException {
        exportToExcel(sheetName, null, headers, rows, out);
    }

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

    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata, String indent) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            appendValueElement(sb, entry.getKey(), entry.getValue(), indent);
        }
    }

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

    private static void appendOpenTag(StringBuilder sb, String tag) {
        sb.append('<').append(tag).append('>');
    }

    private static void appendOpenTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append('<').append(tag).append('>');
    }

    private static void appendCloseTag(StringBuilder sb, String tag) {
        sb.append("</").append(tag).append('>');
    }

    private static void appendCloseTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append("</").append(tag).append('>');
    }

    // === DocumentService ===
    @Override
    public void export(ExportContextDTO ctx, String documentType, DocumentFormat format,
            String searchQuery, OutputStream out) throws IOException {
        ExportPayloadDTO payload = buildPayload(ctx, documentType, searchQuery);
        switch (format) {
            case EXCEL -> {
                if (payload.excelPreambleRows() != null) {
                    exportToExcel(payload.excelSheetName(), payload.excelPreambleRows(),
                            payload.primaryHeaders(), payload.primaryRows(), out);
                } else {
                    exportToExcel(payload.excelSheetName(), payload.primaryHeaders(),
                            payload.primaryRows(), out);
                }
            }
            case XML ->
                exportToXml(payload.toXmlDocument(), out);
            case DOCX ->
                throw new IOException("ExcelService khÃ´ng há»— trá»£ xuáº¥t DOCX.");
            default ->
                throw new IOException("Äá»‹nh dáº¡ng xuáº¥t khÃ´ng Ä‘Æ°á»£c há»— trá»£.");
        }
    }

    @Override
    public void print(ExportContextDTO ctx, String documentType, int sbd, OutputStream out) throws IOException {
        throw new UnsupportedOperationException("ExcelService khÃ´ng há»— trá»£ in tÃ i liá»‡u.");
    }
}

