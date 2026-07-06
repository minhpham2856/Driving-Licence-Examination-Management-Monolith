package service.impl;

import dto.*;
import model.*;
import dto.ExaminerSlotDTO;
import dao.SessionDAO;
import dao.impl.SessionDAOImpl;
import dao.ExamDAO;
import dao.impl.ExamDAOImpl;
import dao.DeductionRecordViewDAO;
import dao.impl.DeductionRecordViewDAOImpl;
import model.Audit;
import dto.CandidateEnrollmentDTO;
import service.ExamRegistrationService;
import service.ExaminerDocumentService;
import service.XmlService;
import dto.XmlExportTable;
import service.AuditLogService;
import enums.DocumentFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import service.ExaminerDataService;

public class ExaminerDocumentServiceImpl implements ExaminerDocumentService {

    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final XmlService xmlService = new XmlServiceImpl();
    private final DocxServiceImpl docxService = new DocxServiceImpl();
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat AUDIT_DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final int AUDIT_LIMIT = 5000;
    private static final List<String> CANDIDATE_FIELDS = List.of(
            "stt", "sbd", "hoVaTen", "ngaySinh", "gioiTinh", "cccd", "email", "soDienThoai",
            "diaChi", "hangGplx", "lyDoThi", "ngayThi", "vangThi", "tinhTrangThi",
            "dung", "sai", "khongTraLoi", "diemLyThuyet", "ketQuaLt",
            "diemThucHanh", "diemDuongTruong");
    private static final List<String> CANDIDATE_HEADERS = List.of(
            "STT", "SBD", "Họ và tên", "Ngày sinh", "Giới tính", "Số căn cước", "Email", "Số điện thoại",
            "Địa chỉ", "Hạng GPLX", "Lý do thi", "Ngày thi", "Vắng thi", "Tình trạng thi",
            "Đúng", "Sai", "Không TL", "Điểm lý thuyết", "Kết quả LT",
            "Điểm thực hành", "Điểm đường trường");
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordViewDAO deductionRecordViewDAO = new DeductionRecordViewDAOImpl();

    private Map<String, Object> getSessionExportMeta(int sessionId) {
        Map<String, Object> meta = new LinkedHashMap<>();
        Session s = sessionDAO.getById(sessionId);
        if (s != null) {
            meta.put("sessionName", s.getSessionName());
            /* meta.put("examDate", s.getExamDate().toString()); */
            meta.put("startTime", s.getStartTime() != null ? s.getStartTime().toString() : "");
            meta.put("endTime", s.getEndTime() != null ? s.getEndTime().toString() : "");
            Exam e = examDAO.getById(s.getExamId());
            meta.put("examCode", e != null ? e.getExamCode() : null);
        }
        return meta;
    }
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    public ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx) {
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.isTheory(), ctx.sectionName());
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> c : candidates) {
            rows.add(Arrays.asList(
                    index++,
                    c.get("sbd"),
                    c.get("fullName"),
                    c.get("dob"),
                    c.get("sex"),
                    c.get("governmentId"),
                    c.get("email"),
                    c.get("phoneNo"),
                    c.get("address"),
                    c.get("licenceClass"),
                    c.get("reasonForTaking"),
                    c.get("examDate"),
                    Boolean.TRUE.equals(c.get("absent")) ? "Có" : "Không",
                    c.get("statusLabel"),
                    c.get("correct"),
                    c.get("wrong"),
                    c.get("unanswered"),
                    c.get("scoreTheory"),
                    c.get("resultLabel"),
                    c.get("scorePractical"),
                    c.get("scoreOnRoad")));
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", CANDIDATE_FIELDS, CANDIDATE_HEADERS, rows);
        return new ExaminerExportPayload(
                "Danh sách thí sinh", "danhSachThiSinh", Map.of(), List.of(table), null);
    }

    public ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx) {
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.isTheory(), ctx.sectionName());
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        if (ctx.isTheory() == false) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Điểm", "Kết quả", "Tình trạng", "Vắng thi");
            for (Map<String, Object> c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("examScore"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Có" : "Không"));
            }
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Đúng", "Sai", "Không TL", "Kết quả", "Tình trạng",
                    "Vắng thi");
            for (Map<String, Object> c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("correct"),
                        c.get("wrong"),
                        c.get("unanswered"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Có" : "Không"));
            }
        }
        XmlExportTable table = new XmlExportTable("ketQuaThi", "ketQua", fields, headers, rows);
        return new ExaminerExportPayload("Kết quả thi", "ketQuaThi", Map.of(), List.of(table), null);
    }

    public ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx) {
        Map<String, Object> meta = getSessionExportMeta(ctx.sessionId());
        Map<String, Object> summary = viewDataService.buildCandidateSummary(
                ctx.sessionId(), ctx.isTheory(), ctx.sectionName());
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.isTheory(), ctx.sectionName());
        Map<String, Object> metadata = buildMinutesMetadata(meta, summary, ctx.slot(), ctx.isTheory(),
                ctx.sectionName());
        List<List<Object>> preamble = buildMinutesPreamble(meta, summary, ctx.slot(), ctx.isTheory(),
                ctx.sectionName());
        List<String> fields;
        List<String> headers;
        if (ctx.isTheory() == false) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Điểm", "Kết quả", "Tình trạng", "Vắng thi");
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Đúng", "Sai", "Không TL", "Kết quả", "Tình trạng",
                    "Vắng thi");
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.isTheory()));
        return new ExaminerExportPayload(
                "Biên bản thi", "bienBanThi", metadata, List.of(table), preamble);
    }

    public ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx) {
        return buildViolationsExport(ctx, null);
    }

    public ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx, String sbdFilterRaw) {
        Map<String, Object> meta = getSessionExportMeta(ctx.sessionId());
        List<Audit> auditViolations = auditLogService.getViolationLogsForSession(ctx.sessionId(), AUDIT_LIMIT);
        Map<Long, String> changerNames = auditLogService.loadChangerNames(auditViolations);
        List<Map<String, Object>> scoreViolations = deductionRecordViewDAO.getViolationRowsForSession(ctx.sessionId());
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
            Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.sessionId());
            List<Audit> filteredAudits = new ArrayList<>();
            for (Audit log : auditViolations) {
                if (sbdText.equals(auditLogService.resolveSbd(log, sbdByRecordId))) {
                    filteredAudits.add(log);
                }
            }
            auditViolations = filteredAudits;
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÊN BẢN VI PHẠM");
        metadata.put("caThi", nullToDash(meta.get("sessionName")));
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
                    Boolean.TRUE.equals(row.get("critical")) ? "Có" : "Không",
                    row.get("currentScore")));
        }
        XmlExportTable auditTable = new XmlExportTable(
                "viPhamQuyChe",
                "viPham",
                List.of("stt", "nguoiGhi", "noiDung", "lyDo", "thoiGian"),
                List.of("STT", "Người ghi", "Nội dung", "Lý do", "Thời gian"),
                auditRows);
        XmlExportTable scoreTable = new XmlExportTable(
                "truDiemThi",
                "banTruDiem",
                List.of("stt", "sbd", "hoVaTen", "phanThi", "lyDoTruDiem", "diemTru", "loiNghiemTrong", "diemHienTai"),
                List.of("STT", "SBD", "Họ và tên", "Phần thi", "Lý do trừ điểm", "Điểm trừ",
                        "Lỗi nghiêm trọng", "Điểm hiện tại"),
                scoreRows);
        List<List<Object>> excelRows = buildViolationsExcelRows(meta, auditViolations, changerNames, scoreViolations);
        return new ExaminerExportPayload(
                "Biên bản vi phạm", "bienBanViPham", metadata, List.of(auditTable, scoreTable), excelRows);
    }

    public ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery) {
        List<Audit> logs = auditLogService.getLogsForSessionPaginated(ctx.sessionId(), 1, AUDIT_LIMIT, searchQuery);
        Map<Long, String> changerNames = auditLogService.loadChangerNames(logs);
        Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.sessionId());
        List<List<Object>> rows = new ArrayList<>();
        for (Audit log : logs) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            for (Map<String, Object> viewRow : auditLogService.toViewRows(log, changerName, sbdByRecordId)) {
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
        return new ExaminerExportPayload("Nhật ký", "nhatKyHeThong", metadata, List.of(table), null);
    }

    private Map<String, Object> buildMinutesMetadata(Map<String, Object> meta, Map<String, Object> summary,
            ExaminerSlotDTO slot, boolean isTheory, String sectionName) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÊN BẢN TỔ CHỨC THI");
        metadata.put("caThi", nullToDash(meta.get("sessionName")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));
        metadata.put("ngayThi", formatDate(meta.get("examDate")));
        metadata.put("gioBatDau", formatTime(meta.get("startTime")));
        metadata.put("gioKetThuc", formatTime(meta.get("endTime")));
        if (slot != null) {
            metadata.put("khuVucPhong", nullToDash(slot.getAreaName()));
            metadata.put("giamThi", nullToDash(slot.getExaminerName()));
        }
        metadata.put("phanThi",
                !isTheory ? nullToDash(sectionName) : "Lý thuyết");
        Map<String, Object> thongKe = new LinkedHashMap<>();
        thongKe.put("tongThiSinh", summary.get("total"));
        thongKe.put("daThi", summary.get("done"));
        thongKe.put("dangThi", summary.get("testing"));
        thongKe.put("chuaThi", summary.get("pending"));
        thongKe.put("dat", summary.get("passed"));
        thongKe.put("truot", summary.get("failed"));
        metadata.put("thongKe", thongKe);
        return metadata;
    }

    private List<List<Object>> buildMinutesPreamble(Map<String, Object> meta, Map<String, Object> summary,
            ExaminerSlotDTO slot, boolean isTheory, String sectionName) {
        List<List<Object>> preamble = new ArrayList<>();
        preamble.add(Arrays.asList("BIÊN BẢN TỔ CHỨC THI"));
        preamble.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        preamble.add(Arrays.asList("Mã đợt thi", nullToDash(meta.get("examCode"))));
        preamble.add(Arrays.asList("Ngày thi", formatDate(meta.get("examDate"))));
        preamble.add(Arrays.asList("Giờ bắt đầu", formatTime(meta.get("startTime"))));
        preamble.add(Arrays.asList("Giờ kết thúc", formatTime(meta.get("endTime"))));
        if (slot != null) {
            preamble.add(Arrays.asList("Khu vực / Phòng", nullToDash(slot.getAreaName())));
            preamble.add(Arrays.asList("Giám thị", nullToDash(slot.getExaminerName())));
        }
        preamble.add(Arrays.asList("Phần thi",
                !isTheory ? nullToDash(sectionName) : "Lý thuyết"));
        preamble.add(Arrays.asList());
        preamble.add(Arrays.asList("Tổng thí sinh", summary.get("total")));
        preamble.add(Arrays.asList("Đã thi", summary.get("done")));
        preamble.add(Arrays.asList("Đang thi", summary.get("testing")));
        preamble.add(Arrays.asList("Chưa thi", summary.get("pending")));
        preamble.add(Arrays.asList("Đạt", summary.get("passed")));
        preamble.add(Arrays.asList("Trượt", summary.get("failed")));
        preamble.add(Arrays.asList());
        return preamble;
    }

    private static List<List<Object>> buildMinutesRows(List<Map<String, Object>> candidates,
            boolean isTheory) {
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> c : candidates) {
            if (!isTheory) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("examScore"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Có" : "Không"));
            } else {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("correct"),
                        c.get("wrong"),
                        c.get("unanswered"),
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Có" : "Không"));
            }
        }
        return rows;
    }

    private List<List<Object>> buildViolationsExcelRows(Map<String, Object> meta, List<Audit> auditViolations,
            Map<Long, String> changerNames, List<Map<String, Object>> scoreViolations) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("BIÊN BẢN VI PHẠM"));
        rows.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        rows.add(Arrays.asList("Mã đợt thi", nullToDash(meta.get("examCode"))));
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("I. Vi phạm quy chế thi (nhật ký)"));
        rows.add(Arrays.asList("STT", "Người ghi", "Nội dung", "Lý do", "Thời gian"));
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
            rows.add(Arrays.asList("-", "-", "Không có vi phạm", "-", "-"));
        }
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("II. Trừ điểm thi"));
        rows.add(Arrays.asList("STT", "SBD", "Họ và tên", "Phần thi", "Lý do trừ điểm", "Điểm trừ",
                "Lỗi nghiêm trọng", "Điểm hiện tại"));
        index = 1;
        for (Map<String, Object> row : scoreViolations) {
            rows.add(Arrays.asList(
                    index++,
                    row.get("sbd"),
                    row.get("fullName"),
                    row.get("sectionName"),
                    row.get("violationReason"),
                    row.get("deductionPoints"),
                    Boolean.TRUE.equals(row.get("critical")) ? "Có" : "Không",
                    row.get("currentScore")));
        }
        if (scoreViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "Không có trừ điểm", "-", "-", "-", "-", "-"));
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

    private Map<Integer, String> buildSbdLookup(int sessionId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (CandidateEnrollmentDTO reg : registrationService.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), String.valueOf(reg.getSbd()));
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

    @Override
    public void export(ExaminerExportContext ctx, String documentType, DocumentFormat format,
            String searchQuery, OutputStream out) throws IOException {
        ExaminerExportPayload payload = buildPayload(ctx, documentType, searchQuery);
        switch (format) {
            case EXCEL -> {
                if (payload.excelPreambleRows() != null) {
                    xmlService.exportToExcel(payload.excelSheetName(), payload.excelPreambleRows(),
                            payload.primaryHeaders(), payload.primaryRows(), out);
                } else {
                    xmlService.exportToExcel(payload.excelSheetName(), payload.primaryHeaders(),
                            payload.primaryRows(), out);
                }
            }
            case XML ->
                xmlService.exportToXml(payload.toXmlDocument(), out);
            case DOCX ->
                docxService.renderTableExport(payload, out);
            default ->
                throw new IOException("Định dạng xuất không được hỗ trợ.");
        }
    }

    @Override
    public void print(ExaminerExportContext ctx, String documentType, int sbd, OutputStream out) throws IOException {
        String normalized = documentType == null ? "" : documentType.trim().toUpperCase();
        switch (normalized) {
            case "BB1", "SIGNATURE", "SIGNATURE_FORM" ->
                docxService.renderBb1Theory(ctx, sbd, out);
            case "BB2", "LAYOUT", "SCORE_SHEET" ->
                docxService.renderBb2Layout(ctx, sbd, out);
            case "BB3", "ROAD" ->
                docxService.renderBb3Road(ctx, sbd, out);
            default ->
                throw new IOException("Loại văn bản in không được hỗ trợ: " + documentType);
        }
    }

    private ExaminerExportPayload buildPayload(ExaminerExportContext ctx, String documentType, String searchQuery) {
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
                throw new IllegalArgumentException("Loại tài liệu xuất không được hỗ trợ: " + documentType);
        };
    }
}
