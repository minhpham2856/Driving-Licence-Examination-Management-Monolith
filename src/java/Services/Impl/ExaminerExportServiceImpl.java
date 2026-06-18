package Services.Impl;

import Utils.ExamConstants.SectionType;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAOs.AuditLogDAO;
import DAOs.ExaminerSessionDataDAO;
import DAOs.Impl.AuditLogDAOImpl;
import DAOs.Impl.ExaminerSessionDataDAOImpl;
import DTOs.AuditDTO;
import DTOs.CandidateDTO;
import DAOs.CandidateDAO;
import DAOs.Impl.CandidateDAOImpl;
import Services.ExaminerExportContext;
import Services.ExaminerExportPayload;
import Services.ExaminerExportService;
import Services.XmlExportTable;
import Utils.AuditLogViewHelper;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExaminerExportServiceImpl implements ExaminerExportService {

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

    private final Services.ExaminerViewDataService viewDataService = new ExaminerViewDataServiceImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();

    @Override
    public ExaminerExportPayload buildCandidatesExport(ExaminerExportContext ctx) {
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
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

    @Override
    public ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx) {
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;

        if (ctx.sectionType() == SectionType.SCORE_BASED) {
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

    @Override
    public ExaminerExportPayload buildMinutesExport(ExaminerExportContext ctx) {
        Map<String, Object> meta = sessionDataDAO.findSessionExportMeta(ctx.sessionId());
        Map<String, Object> summary = viewDataService.buildCandidateSummary(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.sectionType(), ctx.sectionName());

        Map<String, Object> metadata = buildMinutesMetadata(meta, summary, ctx.slot(), ctx.sectionType(),
                ctx.sectionName());
        List<List<Object>> preamble = buildMinutesPreamble(meta, summary, ctx.slot(), ctx.sectionType(),
                ctx.sectionName());

        List<String> fields;
        List<String> headers;
        if (ctx.sectionType() == SectionType.SCORE_BASED) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Điểm", "Kết quả", "Tình trạng", "Vắng thi");
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Họ và tên", "Đúng", "Sai", "Không TL", "Kết quả", "Tình trạng",
                    "Vắng thi");
        }

        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.sectionType()));
        return new ExaminerExportPayload(
                "Biên bản thi", "bienBanThi", metadata, List.of(table), preamble);
    }

    @Override
    public ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx) {
        Map<String, Object> meta = sessionDataDAO.findSessionExportMeta(ctx.sessionId());
        List<AuditDTO> auditViolations = auditLogDAO.getViolationLogsForSession(ctx.sessionId(), AUDIT_LIMIT);
        List<Map<String, Object>> scoreViolations = sessionDataDAO.findScoreViolationRows(ctx.sessionId());

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIÊN BẢN VI PHẠM");
        metadata.put("caThi", nullToDash(meta.get("sessionName")));
        metadata.put("maDotThi", nullToDash(meta.get("examCode")));

        List<List<Object>> auditRows = new ArrayList<>();
        int index = 1;
        for (AuditDTO log : auditViolations) {
            String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "-";
            auditRows.add(Arrays.asList(
                    index++,
                    nullToDash(log.getChangerName()),
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

        List<List<Object>> excelRows = buildViolationsExcelRows(meta, auditViolations, scoreViolations);
        return new ExaminerExportPayload(
                "Biên bản vi phạm", "bienBanViPham", metadata, List.of(auditTable, scoreTable), excelRows);
    }

    @Override
    public ExaminerExportPayload buildAuditExport(ExaminerExportContext ctx, String searchQuery) {
        List<AuditDTO> logs = auditLogDAO.getLogsForSessionPaginated(ctx.sessionId(), 1, AUDIT_LIMIT, searchQuery);
        Map<Integer, String> sbdByRecordId = buildSbdLookup(ctx.sessionId());
        List<List<Object>> rows = new ArrayList<>();
        for (AuditDTO log : logs) {
            for (Map<String, Object> viewRow : AuditLogViewHelper.toViewRows(log, sbdByRecordId)) {
                String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "";
                Object reason = viewRow.get("reason");
                rows.add(Arrays.asList(
                        viewRow.get("username"),
                        log.getAction(),
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
            ExaminerSlot slot, SectionType sectionType, String sectionName) {
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
                sectionType == SectionType.SCORE_BASED ? nullToDash(sectionName) : "Lý thuyết");

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
            ExaminerSlot slot, SectionType sectionType, String sectionName) {
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
                sectionType == SectionType.SCORE_BASED ? nullToDash(sectionName) : "Lý thuyết"));
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
            SectionType sectionType) {
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> c : candidates) {
            if (sectionType == SectionType.SCORE_BASED) {
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

    private List<List<Object>> buildViolationsExcelRows(Map<String, Object> meta, List<AuditDTO> auditViolations,
            List<Map<String, Object>> scoreViolations) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("BIÊN BẢN VI PHẠM"));
        rows.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        rows.add(Arrays.asList("Mã đợt thi", nullToDash(meta.get("examCode"))));
        rows.add(Arrays.asList());

        rows.add(Arrays.asList("I. Vi phạm quy chế thi (nhật ký)"));
        rows.add(Arrays.asList("STT", "Người ghi", "Nội dung", "Lý do", "Thời gian"));
        int index = 1;
        for (AuditDTO log : auditViolations) {
            String time = log.getChangedAt() != null ? AUDIT_DATE_FMT.format(log.getChangedAt()) : "-";
            rows.add(Arrays.asList(
                    index++,
                    nullToDash(log.getChangerName()),
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
        if (value instanceof Date date) {
            synchronized (DATE_FMT) {
                return DATE_FMT.format(date);
            }
        }
        return nullToDash(value);
    }

    private static String formatTime(Object value) {
        if (value instanceof Time time) {
            synchronized (TIME_FMT) {
                return TIME_FMT.format(time);
            }
        }
        if (value instanceof Date date) {
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
        for (CandidateDTO reg : candidateDAO.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), reg.getSbd());
        }
        return lookup;
    }
}
