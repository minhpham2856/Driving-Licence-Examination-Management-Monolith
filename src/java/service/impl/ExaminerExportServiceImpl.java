package service.impl;
import dto.*;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import dto.ExaminerSlotDTO;
import dao.AuditDAO;
import dao.SessionDAO;
import dao.impl.SessionDAOImpl;
import dao.ExamDAO;
import dao.impl.ExamDAOImpl;
import dao.DeductionRecordDAO;
import dao.impl.DeductionRecordDAOImpl;
import model.Audit;
import dto.exam.ExamRegistrationDTO;
import dao.CandidateDAO;
import dao.impl.CandidateDAOImpl;
import service.ExamRegistrationService;
import service.ExaminerExportService;
import dto.XmlExportTable;
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
public class ExaminerExportServiceImpl implements ExaminerExportService {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
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
            "STT", "SBD", "Ho va ten", "Ngay sinh", "Gioi tinh", "So can cuoc", "Email", "So dien thoai",
            "Dia chi", "Hang GPLX", "Ly do thi", "Ngay thi", "Vang thi", "Tinh trang thi",
            "Dung", "Sai", "Khong TL", "Diem ly thuyet", "Ket qua LT",
            "Diem thuc hanh", "Diem duong truong");
    private final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final DeductionRecordDAO deductionRecordDAO = new DeductionRecordDAOImpl();
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
    @Override
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
                    Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong", 
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
                "Danh sach thi sinh", "danhSachThiSinh", Map.of(), List.of(table), null);
    }
    @Override
    public ExaminerExportPayload buildResultsExport(ExaminerExportContext ctx) {
        List<Map<String, Object>> candidates = viewDataService.loadCandidateRows(
                ctx.sessionId(), ctx.isTheory(), ctx.sectionName());
        List<String> fields;
        List<String> headers;
        List<List<Object>> rows = new ArrayList<>();
        int index = 1;
        if (ctx.isTheory() == false) {
            fields = List.of("stt", "sbd", "hoVaTen", "diem", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Diem", "Ket qua", "Tinh trang", "Vang thi");
            for (Map<String, Object> c : candidates) {
                rows.add(Arrays.asList(
                        index++,
                        c.get("sbd"),
                        c.get("fullName"),
                        c.get("examScore"), 
                        c.get("resultLabel"),
                        c.get("statusLabel"),
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Dung", "Sai", "Khong TL", "Ket qua", "Tinh trang",
                    "Vang thi");
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
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        }
        XmlExportTable table = new XmlExportTable("ketQuaThi", "ketQua", fields, headers, rows);
        return new ExaminerExportPayload("Ket qua thi", "ketQuaThi", Map.of(), List.of(table), null);
    }
    @Override
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
            headers = List.of("STT", "SBD", "Ho va ten", "Diem", "Ket qua", "Tinh trang", "Vang thi");
        } else {
            fields = List.of("stt", "sbd", "hoVaTen", "dung", "sai", "khongTraLoi", "ketQua", "tinhTrang", "vangThi");
            headers = List.of("STT", "SBD", "Ho va ten", "Dung", "Sai", "Khong TL", "Ket qua", "Tinh trang",
                    "Vang thi");
        }
        XmlExportTable table = new XmlExportTable(
                "danhSachThiSinh", "thiSinh", fields, headers, buildMinutesRows(candidates, ctx.isTheory()));
        return new ExaminerExportPayload(
                "Bien ban thi", "bienBanThi", metadata, List.of(table), preamble);
    }
    @Override
    public ExaminerExportPayload buildViolationsExport(ExaminerExportContext ctx) {
        Map<String, Object> meta = getSessionExportMeta(ctx.sessionId());
        List<Audit> auditViolations = auditLogService.getViolationLogsForSession(ctx.sessionId(), AUDIT_LIMIT);
        Map<Long, String> changerNames = auditLogService.loadChangerNames(auditViolations);
        List<Map<String, Object>> scoreViolations = deductionRecordDAO.getViolationRowsForSession(ctx.sessionId());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIEN BAN VI PHAM");
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
                    Boolean.TRUE.equals(row.get("critical")) ? "Co" : "Khong", 
                    row.get("currentScore")));                                          
        }
        XmlExportTable auditTable = new XmlExportTable(
                "viPhamQuyChe",
                "viPham",
                List.of("stt", "nguoiGhi", "noiDung", "lyDo", "thoiGian"),
                List.of("STT", "Nguoi ghi", "Noi dung", "Ly do", "Thoi gian"),
                auditRows);
        XmlExportTable scoreTable = new XmlExportTable(
                "truDiemThi",
                "banTruDiem",
                List.of("stt", "sbd", "hoVaTen", "phanThi", "lyDoTruDiem", "diemTru", "loiNghiemTrong", "diemHienTai"),
                List.of("STT", "SBD", "Ho va ten", "Phan thi", "Ly do tru diem", "Diem tru",
                        "Loi nghiem trong", "Diem hien tai"),
                scoreRows);
        List<List<Object>> excelRows = buildViolationsExcelRows(meta, auditViolations, changerNames, scoreViolations);
        return new ExaminerExportPayload(
                "Bien ban vi pham", "bienBanViPham", metadata, List.of(auditTable, scoreTable), excelRows);
    }
    @Override
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
                List.of("Nguoi dung", "Thao tac", "Doi tuong", "SBD", "Thong tin", "Cu", "Moi", "Ly do",
                        "Thoi gian"),
                rows);
        Map<String, Object> metadata = Map.of();
        if (searchQuery != null && !searchQuery.isBlank()) {
            metadata = Map.of("tuKhoa", searchQuery.trim());
        }
        return new ExaminerExportPayload("Nhat ky", "nhatKyHeThong", metadata, List.of(table), null);
    }
    private Map<String, Object> buildMinutesMetadata(Map<String, Object> meta, Map<String, Object> summary,
            ExaminerSlotDTO slot, boolean isTheory, String sectionName) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("tieuDe", "BIEN BAN TO CHUC THI");
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
                !isTheory ? nullToDash(sectionName) : "Ly thuyet");
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
        preamble.add(Arrays.asList("BIEN BAN TO CHUC THI"));
        preamble.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        preamble.add(Arrays.asList("Ma dot thi", nullToDash(meta.get("examCode"))));
        preamble.add(Arrays.asList("Ngay thi", formatDate(meta.get("examDate"))));
        preamble.add(Arrays.asList("Gio bat dau", formatTime(meta.get("startTime"))));
        preamble.add(Arrays.asList("Gio ket thuc", formatTime(meta.get("endTime"))));
        if (slot != null) {
            preamble.add(Arrays.asList("Khu vuc / Phong", nullToDash(slot.getAreaName())));
            preamble.add(Arrays.asList("Giam thi", nullToDash(slot.getExaminerName())));
        }
        preamble.add(Arrays.asList("Phan thi",
                !isTheory ? nullToDash(sectionName) : "Ly thuyet"));
        preamble.add(Arrays.asList());
        preamble.add(Arrays.asList("Tong thi sinh", summary.get("total")));
        preamble.add(Arrays.asList("Da thi", summary.get("done")));
        preamble.add(Arrays.asList("Dang thi", summary.get("testing")));
        preamble.add(Arrays.asList("Chua thi", summary.get("pending")));
        preamble.add(Arrays.asList("Dat", summary.get("passed")));
        preamble.add(Arrays.asList("Truot", summary.get("failed")));
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
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
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
                        Boolean.TRUE.equals(c.get("absent")) ? "Co" : "Khong"));
            }
        }
        return rows;
    }
    private List<List<Object>> buildViolationsExcelRows(Map<String, Object> meta, List<Audit> auditViolations,
            Map<Long, String> changerNames, List<Map<String, Object>> scoreViolations) {
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("BIEN BAN VI PHAM"));
        rows.add(Arrays.asList("Ca thi", nullToDash(meta.get("sessionName"))));
        rows.add(Arrays.asList("Ma dot thi", nullToDash(meta.get("examCode"))));
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("I. Vi pham quy che thi (nhat ky)"));
        rows.add(Arrays.asList("STT", "Nguoi ghi", "Noi dung", "Ly do", "Thoi gian"));
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
            rows.add(Arrays.asList("-", "-", "Khong co vi pham", "-", "-"));
        }
        rows.add(Arrays.asList());
        rows.add(Arrays.asList("II. Tru diem thi"));
        rows.add(Arrays.asList("STT", "SBD", "Ho va ten", "Phan thi", "Ly do tru diem", "Diem tru",
                "Loi nghiem trong", "Diem hien tai"));
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
        if (scoreViolations.isEmpty()) {
            rows.add(Arrays.asList("-", "-", "Khong co tru diem", "-", "-", "-", "-", "-"));
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
        for (ExamRegistrationDTO reg : registrationService.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), String.valueOf(reg.getSbd()));
        }
        return lookup;
    }
}
