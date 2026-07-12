package examiner.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import examiner.dao.CandidateAnswerDAO;
import examiner.dao.DeductionRecordViewDAO;
import examiner.dao.ExamDAO;
import examiner.dao.QuestionDAO;
import examiner.dao.TheoryPaperDAO;
import examiner.dao.impl.CandidateAnswerDAOImpl;
import examiner.dao.impl.DeductionRecordViewDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.dao.impl.QuestionDAOImpl;
import examiner.dao.impl.TheoryPaperDAOImpl;
import examiner.dto.CandidateRowDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExamStatsDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.ExportPayloadDTO;
import examiner.dto.XmlExportDocument;
import examiner.dto.XmlExportTable;
import examiner.enums.DocumentFormat;
import shared.model.Audit;
import shared.model.CandidateAnswer;
import shared.model.Exam;
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
import examiner.service.DocumentService;
import examiner.service.ExamViewService;
import examiner.service.RegistrationService;
import examiner.service.impl.AuditServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.service.impl.RegistrationServiceImpl;
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

public class DocxServiceImpl implements DocumentService {

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
    private static final int BLOCK_A_FROM = 1;
    private static final int BLOCK_A_TO = 20;
    private static final int BLOCK_B_FROM = 21;
    private static final int BLOCK_B_TO = 35;
    private static final String TEMPLATE_DIR = "/docx-template/examiner/";

    private final AuditService auditService = new AuditServiceImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();
    private final ExamViewService viewDataService = new ExamViewServiceImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();

    private static Configure poiTlConfig() {
        return Configure.builder().buildGramer("<<", ">>").build();
    }

    // === build* (duplicated from DocumentServiceImpl) ===

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

    // === DocumentService ===

    @Override
    public void export(ExportContextDTO ctx, String documentType, DocumentFormat format,
            String searchQuery, OutputStream out) throws IOException {
        if (format != DocumentFormat.DOCX) {
            throw new IOException("DocxService chá»‰ há»— trá»£ xuáº¥t DOCX.");
        }
        ExportPayloadDTO payload = buildPayload(ctx, documentType, searchQuery);
        renderTableExport(payload, out);
    }

    @Override
    public void print(ExportContextDTO ctx, String documentType, int sbd, OutputStream out) throws IOException {
        String normalized = documentType == null ? "" : documentType.trim().toUpperCase();
        switch (normalized) {
            case "BB1", "SIGNATURE", "SIGNATURE_FORM" ->
                renderBb1Theory(ctx, sbd, out);
            case "BB2", "LAYOUT", "SCORE_SHEET" ->
                renderBb2Layout(ctx, sbd, out);
            default ->
                throw new IOException("Loáº¡i vÄƒn báº£n in khÃ´ng Ä‘Æ°á»£c há»— trá»£: " + documentType);
        }
    }

    // === DOCX rendering (kept from old DocxServiceImpl) ===

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

    public void renderBb1Theory(ExportContextDTO ctx, int sbd, OutputStream out) throws IOException {
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB1", candidate.getLicenceClass());
        if (template == null) {
            throw new IOException("KhÃ´ng tÃ¬m tháº¥y máº«u BB1.");
        }
        render(template, buildBb1Placeholders(ctx, candidate), out);
    }

    public void renderBb2Layout(ExportContextDTO ctx, int sbd, OutputStream out) throws IOException {
        CandidateRowDTO candidate = findCandidateRow(ctx, sbd);
        String template = pickTemplate("BB2", candidate.getLicenceClass());
        if (template == null) {
            throw new IOException("KhÃ´ng tÃ¬m tháº¥y máº«u BB2.");
        }
        render(template, buildBb2Placeholders(ctx, candidate), out);
    }

    public void renderTableExport(ExportPayloadDTO payload, OutputStream out) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            titleRun.setText(payload.excelSheetName());

            if (payload.metadata() != null) {
                for (Map.Entry<String, Object> entry : payload.metadata().entrySet()) {
                    appendLine(document, entry.getKey() + ": " + format(entry.getValue()));
                }
            }
            if (payload.excelPreambleRows() != null) {
                for (List<Object> row : payload.excelPreambleRows()) {
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

    private Map<String, Object> buildBb1Placeholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = baseCandidatePlaceholders(ctx, candidate);
        data.put("A", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_A_FROM, BLOCK_A_TO));
        data.put("B", buildTheoryAnswerBlock(candidate.getEnrollmentId(), BLOCK_B_FROM, BLOCK_B_TO));
        data.put("SCORE", format(candidate.getScoreTheory()));
        boolean passed = "Äáº¡t".equalsIgnoreCase(stringValue(candidate.getResultLabel()))
                || candidate.isPassed();
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
        data.put("A", format(candidate.getExamScore()));
        return data;
    }

    private Map<String, Object> baseCandidatePlaceholders(ExportContextDTO ctx, CandidateRowDTO candidate) {
        Map<String, Object> data = new LinkedHashMap<>();
        TheoryPaper paper = loadTheoryPaper(candidate);
        String shiftLabel = "-";

        data.put("DEPT", "TP. HÃ€ Ná»˜I");
        data.put("FNAME", format(candidate.getFullName()));
        data.put("EXAM", shiftLabel);
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
        CandidateRowDTO row = viewDataService.getCandidateViewRow(
                ctx.examId(), sbd, ctx.isTheory(), ctx.sectionName());
        if (row == null) {
            throw new IOException("KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh SBD " + sbd);
        }
        return row;
    }

    private String pickTemplate(String documentCode, String licenceClass) {
        String cls = normalizeLicenceClass(licenceClass);
        if ("BB1".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls) || "B1".equals(cls)) {
                return TEMPLATE_DIR + "BB1(A1-A-B1).docx";
            }
            return TEMPLATE_DIR + "BB1(B-C1-C-D1-D2-D).docx";
        }
        if ("BB2".equals(documentCode)) {
            if ("A1".equals(cls) || "A".equals(cls)) {
                return TEMPLATE_DIR + "BB2(A1-A).docx";
            }
            if ("B1".equals(cls)) {
                return TEMPLATE_DIR + "BB2(B1).docx";
            }
            return TEMPLATE_DIR + "BB2(B-C1-C-D1-D2-D).docx";
        }
        if ("BB3".equals(documentCode)) {
            return TEMPLATE_DIR + "BB3(B-C1-C-D1-D2-D).docx";
        }
        return null;
    }

    private static String normalizeLicenceClass(String licenceClass) {
        if (licenceClass == null || licenceClass.isBlank()) {
            return "B2";
        }
        String normalized = licenceClass.trim().toUpperCase(Locale.ROOT);
        if ("B".equals(normalized)) {
            return "B2";
        }
        return normalized;
    }

    private TheoryPaper loadTheoryPaper(CandidateRowDTO candidate) {
        if (candidate.getEnrollmentId() <= 0) {
            return null;
        }
        return theoryPaperDAO.getByExamEnrollmentId(candidate.getEnrollmentId());
    }

    private String buildTheoryAnswerBlock(int enrollmentId, int fromQuestionNo, int toQuestionNo) {
        if (enrollmentId <= 0) {
            return "-";
        }
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            return "-";
        }
        List<CandidateAnswer> answers = candidateAnswerDAO.findByTheoryPaperId(paper.getTheoryPaperId());
        if (answers.isEmpty()) {
            return "-";
        }

        List<Integer> questionIds = new ArrayList<>();
        for (CandidateAnswer answer : answers) {
            questionIds.add(answer.getQuestionId());
        }
        Map<Integer, Integer> questionNoById = new HashMap<>();
        for (Question question : questionDAO.findByIds(questionIds)) {
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

    private static void appendLine(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text == null ? "" : text);
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "" : text;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}

