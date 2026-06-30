package service.impl;

import dao.AuditDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.SessionDAO;
import dao.TheoryPaperDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dbconnection.DBContext;
import dto.CandidateEnrollmentDTO;
import enums.CandidateStatus;
import enums.SectionType;
import model.Audit;
import model.Exam;
import model.ExamDevice;
import model.Session;
import service.EnumMappingService;
import service.ExaminerDataService;
import util.ExamQueue;
import util.ExamQueue.Lane;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExaminerDataServiceImpl extends DBContext implements ExaminerDataService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;

    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, Integer sbdParam) {
        return getCandidateCallData(sessionId, sbdParam, null);
    }

    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, Integer sbdParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        SectionType sectionType = SectionType.THEORY;
        String sectionName = null;
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, sectionType, sectionName);
        candidates = filterRows(candidates, searchQuery, model);
        model.put("candidates", candidates);
        model.put("candidateQueue", candidates);
        if (sbdParam != null && sbdParam > 0) {
            CandidateEnrollmentDTO reg = findRegistration(sessionId, sbdParam);
            if (reg != null) {
                model.put("candidate", toViewRow(reg, sectionType,
                        loadTheoryStats(sessionId),
                        loadSectionScores(sessionId, sectionName),
                        loadPassFlags(sessionId),
                        formatSessionDate(sessionId),
                        resolveLicenceClass(sessionId),
                        loadDeviceNames(sessionId)));
            }
        }
        return model;
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, SectionType.THEORY, null);
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType, String sectionName) {
        List<CandidateEnrollmentDTO> registrations = enrollmentDAO.getCandidatesBySession(sessionId);
        Map<Integer, int[]> theoryStats = loadTheoryStats(sessionId);
        Map<Integer, Double> sectionScores = loadSectionScores(sessionId, sectionName);
        Map<Integer, Boolean> passFlags = loadPassFlags(sessionId);
        String examDate = formatSessionDate(sessionId);
        String licenceClass = resolveLicenceClass(sessionId);
        Map<Integer, String> deviceNames = loadDeviceNames(sessionId);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (CandidateEnrollmentDTO reg : registrations) {
            rows.add(toViewRow(reg, sectionType, theoryStats, sectionScores, passFlags, examDate, licenceClass, deviceNames));
        }
        return rows;
    }

    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType, String sectionName) {
        List<Map<String, Object>> rows = loadCandidateRows(sessionId, sectionType, sectionName);
        Map<String, Object> summary = new LinkedHashMap<>();
        int total = rows.size();
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;
        for (Map<String, Object> row : rows) {
            String status = String.valueOf(row.get("status"));
            if ("done".equals(status)) {
                done++;
            } else if ("testing".equals(status) || "awaiting".equals(status)) {
                testing++;
            } else if ("pending".equals(status)) {
                pending++;
            }
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if (row.get("resultLabel") != null && !"-".equals(String.valueOf(row.get("resultLabel")))) {
                failed++;
            }
        }
        Session session = sessionDAO.getById(sessionId);
        Exam exam = session != null ? examDAO.getById(session.getExamId()) : null;
        summary.put("total", total);
        summary.put("done", done);
        summary.put("testing", testing);
        summary.put("pending", pending);
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("examCode", exam != null ? exam.getExamCode() : "-");
        return summary;
    }

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam) {
        return getAuditLogsData(sessionId, pageParam, null);
    }

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        model.put("auditLogs", auditDAO.findAll());
        return model;
    }

    @Override
    public Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath) {
        return new HashMap<>();
    }

    @Override
    public int theoryPassThreshold() {
        return THEORY_PASS_CORRECT;
    }

    @Override
    public int theoryMaxQuestions() {
        return THEORY_MAX_QUESTIONS;
    }

    @Override
    public CandidateEnrollmentDTO findRegistration(int sessionId, int sbd) {
        if (sbd <= 0) {
            return null;
        }
        for (CandidateEnrollmentDTO reg : enrollmentDAO.getCandidatesBySession(sessionId)) {
            if (reg.getSbd() == sbd) {
                return reg;
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getScoreEntryData(int sessionId, Integer sbdParam, String sectionName) {
        Map<String, Object> model = new HashMap<>();
        SectionType sectionType = SectionType.SCORE_BASED;
        List<Map<String, Object>> allRows = loadCandidateRows(sessionId, sectionType, sectionName);
        List<Map<String, Object>> scoreQueue = new ArrayList<>();
        for (Map<String, Object> row : allRows) {
            Object sbdObj = row.get("sbd");
            int rowSbd = sbdObj instanceof Number ? ((Number) sbdObj).intValue() : 0;
            CandidateEnrollmentDTO reg = findRegistration(sessionId, rowSbd);
            if (isScoreQueueEligible(sessionId, reg, sectionType, sectionName)) {
                scoreQueue.add(row);
            }
        }
        Lane lane = ExamQueue.resolveLane(sectionType, sectionName);
        List<Integer> eligibleSbds = new ArrayList<>();
        for (Map<String, Object> row : scoreQueue) {
            Object sbdObj = row.get("sbd");
            if (sbdObj instanceof Number) {
                eligibleSbds.add(((Number) sbdObj).intValue());
            }
        }
        ExamQueue.sync(lane, eligibleSbds);
        scoreQueue = orderRowsByQueue(scoreQueue, lane);
        model.put("scoreQueue", scoreQueue);
        model.put("sessionVehicles", loadSessionVehicles(sessionId));

        CandidateEnrollmentDTO activeReg = null;
        if (sbdParam != null && sbdParam > 0) {
            activeReg = findRegistration(sessionId, sbdParam);
        }
        Integer candidateId = activeReg != null ? activeReg.getId() : null;
        model.put("scoreDeductions", loadScoreDeductions(sectionName, candidateId, sessionId));
        applyScoreSummary(model, candidateId, sessionId, sectionName);

        if (sbdParam != null && sbdParam > 0) {
            for (Map<String, Object> row : allRows) {
                Object sbdObj = row.get("sbd");
                if (sbdObj instanceof Number && ((Number) sbdObj).intValue() == sbdParam) {
                    model.put("candidate", row);
                    break;
                }
            }
        }
        return model;
    }

    @Override
    public Map<String, Object> getResultDetailsEditData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        CandidateEnrollmentDTO reg = null;
        if (sbdParam != null && sbdParam > 0) {
            for (Map<String, Object> row : loadCandidateRows(sessionId, SectionType.SCORE_BASED, null)) {
                Object sbdObj = row.get("sbd");
                if (sbdObj instanceof Number && ((Number) sbdObj).intValue() == sbdParam) {
                    model.put("candidate", row);
                    model.put("singleCandidateList", List.of(row));
                    break;
                }
            }
            reg = findRegistration(sessionId, sbdParam);
        }
        Integer candidateId = reg != null ? reg.getId() : null;
        model.put("scoreDeductions", loadScoreDeductions(null, candidateId, sessionId));
        applyScoreSummary(model, candidateId, sessionId, null);
        return model;
    }

    @Override
    public boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg, SectionType sectionType,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        String status = reg.getSectionStatus();
        return "Testing".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status);
    }

    @Override
    public Map<String, Object> getViolationData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, SectionType.THEORY, null);
        model.put("candidates", candidates);
        model.put("violationReasons", enumMappingService.violationOptionList());
        if (sbdParam != null && sbdParam > 0) {
            for (Map<String, Object> row : candidates) {
                Object sbdObj = row.get("sbd");
                if (sbdObj instanceof Number && ((Number) sbdObj).intValue() == sbdParam) {
                    model.put("candidate", row);
                    break;
                }
            }
        }
        return model;
    }

    @Override
    public Map<String, Object> getDevicesData(int sessionId, String searchQuery) {
        return getDevicesData(sessionId, searchQuery, null);
    }

    @Override
    public Map<String, Object> getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> devices = new ArrayList<>();
        LinkedHashMap<Integer, String> areaNames = new LinkedHashMap<>();

        List<Integer> areaIds = new ArrayList<>();
        if (preferredAreaId != null && preferredAreaId > 0) {
            areaIds.add(preferredAreaId);
        }
        for (Integer areaId : sessionDAO.getExamAreaIds(sessionId)) {
            if (areaId != null && areaId > 0 && !areaIds.contains(areaId)) {
                areaIds.add(areaId);
            }
        }
        if (areaIds.isEmpty()) {
            Integer fallback = resolveSessionAreaId(sessionId);
            if (fallback != null && fallback > 0) {
                areaIds.add(fallback);
            }
        }

        for (Integer areaId : areaIds) {
            areaNames.putIfAbsent(areaId, resolveAreaName(areaId));
            for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
                if (searchQuery != null && !searchQuery.isBlank()) {
                    String q = searchQuery.trim().toLowerCase(Locale.ROOT);
                    String haystack = (device.getDeviceName() + " " + device.getDeviceType()
                            + " " + areaNames.get(areaId)).toLowerCase(Locale.ROOT);
                    if (!haystack.contains(q)) {
                        continue;
                    }
                }
                devices.add(toDeviceRow(device, areaNames.get(areaId)));
            }
        }
        model.put("devices", devices);
        return model;
    }

    private Map<String, Object> toDeviceRow(ExamDevice device, String areaName) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", device.getExamDeviceId());
        row.put("name", device.getDeviceName());
        row.put("type", device.getDeviceType());
        row.put("area", areaName);
        if (device.isActive()) {
            row.put("status", "Available");
            row.put("statusLabel", "Sẵn sàng");
            row.put("statusClass", "device-grid-card--available");
        } else {
            row.put("status", "Maintenance");
            row.put("statusLabel", "Bảo trì");
            row.put("statusClass", "device-grid-card--maintenance");
        }
        row.put("icon", resolveDeviceIcon(device.getDeviceType()));
        return row;
    }

    private static String resolveDeviceIcon(String deviceType) {
        if (deviceType == null) {
            return "devices";
        }
        String normalized = deviceType.toLowerCase(Locale.ROOT);
        if (normalized.contains("computer") || normalized.contains("pc")) {
            return "computer";
        }
        if (normalized.contains("car") || normalized.contains("oto") || normalized.contains("xe")) {
            return "directions_car";
        }
        if (normalized.contains("motor")) {
            return "two_wheeler";
        }
        return "devices";
    }

    private String resolveAreaName(int areaId) {
        String sql = "SELECT AreaName FROM ExamArea WHERE ExamAreaId = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("AreaName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, SectionType sectionType,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        return !"Done".equalsIgnoreCase(reg.getSectionStatus());
    }

    @Override
    public List<Map<String, Object>> orderCandidateRowsByQueue(List<Map<String, Object>> rows,
            SectionType sectionType, String sectionName) {
        return orderRowsByQueue(rows, ExamQueue.resolveLane(sectionType, sectionName));
    }

    private Map<String, Object> toViewRow(CandidateEnrollmentDTO reg, SectionType sectionType,
            Map<Integer, int[]> theoryStats, Map<Integer, Double> sectionScores,
            Map<Integer, Boolean> passFlags, String examDate, String licenceClass,
            Map<Integer, String> deviceNames) {
        Map<String, Object> row = new LinkedHashMap<>();
        int enrollmentId = reg.getEnrollment() != null ? reg.getEnrollment().getExamEnrollmentId() : 0;
        String statusKey = resolveStatusKey(reg);
        row.put("sbd", reg.getSbd());
        row.put("fullName", reg.getFullName());
        row.put("dob", formatDate(reg.getDob()));
        if (reg.getDob() != null) {
            row.put("dobRaw", new java.text.SimpleDateFormat("yyyy-MM-dd").format(reg.getDob()));
        } else if (reg.getDateOfBirth() != null) {
            row.put("dobRaw", new java.text.SimpleDateFormat("yyyy-MM-dd").format(reg.getDateOfBirth()));
        } else {
            row.put("dobRaw", "");
        }
        row.put("governmentId", reg.getGovIdNo());
        row.put("address", reg.getAddress());
        row.put("phoneNo", reg.getPhoneNo());
        row.put("sex", reg.isSex() ? "Nữ" : "Nam");
        row.put("sexValue", reg.isSex() ? "1" : "0");
        row.put("email", reg.getEmail());
        row.put("licenceClass", licenceClass);
        row.put("reasonForTaking", reg.getReasonForTaking());
        row.put("examDate", examDate);
        row.put("status", statusKey);
        row.put("statusLabel", resolveStatusLabel(reg, statusKey));
        row.put("absent", reg.isAbsent());
        row.put("suspended", reg.isSuspended());
        row.put("callEligible", isCallEligible(0, reg, sectionType, null));

        int[] stats = theoryStats.getOrDefault(enrollmentId, new int[]{0, 0, 0});
        row.put("correct", stats[0]);
        row.put("wrong", stats[1]);
        row.put("unanswered", stats[2]);

        Double examScore = sectionScores.get(enrollmentId);
        row.put("examScore", examScore != null ? examScore.intValue() : "-");
        row.put("scoreTheory", stats[0] > 0 ? stats[0] : "-");
        row.put("scorePractical", "-");
        row.put("scoreOnRoad", "-");

        Boolean passed = passFlags.get(enrollmentId);
        if (passed == null) {
            row.put("passed", false);
            row.put("resultLabel", "-");
        } else {
            row.put("passed", passed);
            row.put("resultLabel", passed ? "Đạt" : "Trượt");
        }

        Integer deviceId = reg.getEnrollment() != null ? reg.getEnrollment().getExamDeviceId() : null;
        row.put("vehicleName", deviceId != null ? deviceNames.getOrDefault(deviceId, "-") : "-");
        row.put("awaitingSignature", "awaiting".equals(statusKey));
        row.put("markAbsentEligible", !reg.isAbsent() && !reg.isSuspended()
                && !"Done".equalsIgnoreCase(reg.getSectionStatus()));
        row.put("completeEligible", "awaiting".equals(statusKey) && reg.isSignaturePrinted());
        return row;
    }

    private static String resolveStatusKey(CandidateEnrollmentDTO reg) {
        if (reg.isAbsent()) {
            return "absent";
        }
        if (reg.isSuspended()) {
            return "suspended";
        }
        String ss = reg.getSectionStatus();
        if (ss == null || ss.isBlank()) {
            return "pending";
        }
        if ("Done".equalsIgnoreCase(ss)) {
            return "done";
        }
        if ("AwaitingSignature".equalsIgnoreCase(ss)) {
            return "awaiting";
        }
        if ("Testing".equalsIgnoreCase(ss)) {
            return "testing";
        }
        return "pending";
    }

    private String resolveStatusLabel(CandidateEnrollmentDTO reg, String statusKey) {
        if ("absent".equals(statusKey)) {
            return "Vắng";
        }
        if ("suspended".equals(statusKey)) {
            return "Đình chỉ";
        }
        String ss = reg.getSectionStatus();
        if (ss != null) {
            for (CandidateStatus cs : CandidateStatus.values()) {
                if (cs.getStatus().equalsIgnoreCase(ss)) {
                    return cs.getLabelVi();
                }
            }
            if ("AwaitingSignature".equalsIgnoreCase(ss)) {
                return "Chờ ký";
            }
        }
        return enumMappingService.candidateStatusLabel(ss);
    }

    private List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, String searchQuery,
            Map<String, Object> model) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return rows;
        }
        String q = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (matchesSearch(row, q)) {
                filtered.add(row);
            }
        }
        model.put("searchActive", true);
        model.put("searchQuery", searchQuery.trim());
        return filtered;
    }

    private static boolean matchesSearch(Map<String, Object> row, String q) {
        return contains(row, "sbd", q) || contains(row, "fullName", q) || contains(row, "governmentId", q);
    }

    private static boolean contains(Map<String, Object> row, String key, String q) {
        Object val = row.get(key);
        return val != null && String.valueOf(val).toLowerCase(Locale.ROOT).contains(q);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    private String formatSessionDate(int sessionId) {
        Session session = sessionDAO.getById(sessionId);
        if (session == null || session.getStartTime() == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(session.getStartTime());
        }
    }

    private String resolveLicenceClass(int sessionId) {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            return "-";
        }
        String sql = "SELECT l.LicenceClass FROM Exam e JOIN Licence l ON l.LicenceId = e.LicenceId WHERE e.ExamId = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, session.getExamId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("LicenceClass");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "-";
    }

    private Map<Integer, int[]> loadTheoryStats(int sessionId) {
        Map<Integer, int[]> stats = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer = q.CorrectAnswer THEN 1 ELSE 0 END) AS correctCount,
                       SUM(CASE WHEN ca.Answer IS NOT NULL AND ca.Answer <> q.CorrectAnswer THEN 1 ELSE 0 END) AS wrongCount,
                       SUM(CASE WHEN ca.Answer IS NULL OR ca.Answer = '' THEN 1 ELSE 0 END) AS unansweredCount
                FROM ExamEnrollment ec
                LEFT JOIN TheoryPaper tp ON tp.ExamEnrollmentId = ec.ExamEnrollmentId
                LEFT JOIN CandidateAnswer ca ON ca.TheoryPaperId = tp.TheoryPaperId
                LEFT JOIN Question q ON q.QuestionId = ca.QuestionId
                WHERE ec.SessionId = ?
                GROUP BY ec.ExamEnrollmentId
                """;
        queryStats(sql, sessionId, stats);
        return stats;
    }

    private Map<Integer, Double> loadSectionScores(int sessionId, String sectionName) {
        Map<Integer, Double> scores = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, es.Score
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ec.SessionId = ?
                """;
        if (sectionName != null && !sectionName.isBlank()) {
            sql += " AND sec.SectionName = ?";
        }
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            if (sectionName != null && !sectionName.isBlank()) {
                ps.setString(2, sectionName);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    scores.put(rs.getInt("ExamEnrollmentId"), rs.getDouble("Score"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    private Map<Integer, Boolean> loadPassFlags(int sessionId) {
        Map<Integer, Boolean> flags = new HashMap<>();
        String sql = """
                SELECT ec.ExamEnrollmentId, er.IsPassed
                FROM ExamEnrollment ec
                JOIN ExamResult er ON er.ExamEnrollmentId = ec.ExamEnrollmentId
                WHERE ec.SessionId = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flags.put(rs.getInt("ExamEnrollmentId"), rs.getBoolean("IsPassed"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flags;
    }

    private void queryStats(String sql, int sessionId, Map<Integer, int[]> target) {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    target.put(rs.getInt("ExamEnrollmentId"), new int[]{
                        rs.getInt("correctCount"),
                        rs.getInt("wrongCount"),
                        rs.getInt("unansweredCount")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, String> loadDeviceNames(int sessionId) {
        Map<Integer, String> names = new HashMap<>();
        String sql = """
                SELECT ed.ExamDeviceId, ed.DeviceName
                FROM ExamDevice ed
                JOIN Session_ExamArea sea ON sea.ExamAreaId = ed.ExamAreaId
                WHERE sea.SessionId = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.put(rs.getInt("ExamDeviceId"), rs.getString("DeviceName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    private List<Map<String, Object>> loadSessionVehicles(int sessionId) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        Integer areaId = resolveSessionAreaId(sessionId);
        if (areaId == null || areaId <= 0) {
            return vehicles;
        }
        for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
            String type = device.getDeviceType() != null ? device.getDeviceType().toLowerCase(Locale.ROOT) : "";
            if (!type.contains("car") && !type.contains("motorcycle") && !type.contains("xe")
                    && !type.contains("oto")) {
                continue;
            }
            Map<String, Object> row = toDeviceRow(device, resolveAreaName(areaId));
            row.put("status", row.get("status"));
            row.put("statusLabel", row.get("statusLabel"));
            row.put("statusClass", row.get("statusClass"));
            vehicles.add(row);
        }
        return vehicles;
    }

    private List<Map<String, Object>> loadScoreDeductions(String sectionName) {
        return loadScoreDeductions(sectionName, null, null);
    }

    private List<Map<String, Object>> loadScoreDeductions(String sectionName, Integer candidateId, Integer sessionId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                SELECT ScoreDeductionId, Reason, Points, IsCritical, SortOrder
                FROM ScoreDeduction
                WHERE ExamSectionId = (
                    SELECT TOP 1 ExamSectionId FROM ExamSection
                    WHERE SectionName = ISNULL(?, N'Sa hình')
                )
                   OR ExamSectionId IS NULL
                ORDER BY SortOrder, ScoreDeductionId
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sectionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ScoreDeductionId"));
                    row.put("reason", rs.getString("Reason"));
                    row.put("points", rs.getDouble("Points"));
                    row.put("critical", rs.getBoolean("IsCritical"));
                    row.put("occurrenceCount", 0);
                    row.put("count", 0);
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (candidateId != null && candidateId > 0 && sessionId != null && sessionId > 0 && !list.isEmpty()) {
            String occSql = """
                    SELECT dr.ScoreDeductionId, dr.OccurrenceCount, dr.RecordedAt
                    FROM ExamEnrollment ee
                    JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                    JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                    JOIN DeductionRecord dr ON dr.ExamScoreId = es.ExamScoreId
                    WHERE ee.CandidateId = ? AND ee.SessionId = ?
                    """;
            Map<Integer, int[]> occurrences = new HashMap<>();
            Map<Integer, java.util.Date> recordedAt = new HashMap<>();
            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(occSql)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("ScoreDeductionId");
                        occurrences.put(id, new int[]{rs.getInt("OccurrenceCount")});
                        Timestamp ts = rs.getTimestamp("RecordedAt");
                        if (ts != null) {
                            recordedAt.put(id, new java.util.Date(ts.getTime()));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            for (Map<String, Object> row : list) {
                int id = (Integer) row.get("id");
                int[] occ = occurrences.get(id);
                if (occ != null) {
                    row.put("occurrenceCount", occ[0]);
                    row.put("count", occ[0]);
                    row.put("recordedAt", recordedAt.get(id));
                }
            }
        }
        return list;
    }

    private void applyScoreSummary(Map<String, Object> model, Integer candidateId, Integer sessionId,
            String sectionName) {
        model.put("currentScore", 100);
        model.put("scoreDisqualified", false);
        if (candidateId == null || candidateId <= 0 || sessionId == null || sessionId <= 0) {
            return;
        }
        String sql = """
                SELECT TOP 1 es.Score,
                       CASE WHEN EXISTS (
                           SELECT 1
                           FROM DeductionRecord dr
                           JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId
                           WHERE dr.ExamScoreId = es.ExamScoreId
                             AND sd.IsCritical = 1
                             AND dr.OccurrenceCount > 0
                       ) THEN 1 ELSE 0 END AS hasCritical
                FROM ExamEnrollment ee
                JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamScore es ON es.ExamResultId = er.ExamResultId
                JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.SessionId = ?
                  AND sec.SectionName = ISNULL(?, N'Sa hình')
                ORDER BY es.ExamScoreId
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            ps.setString(3, sectionName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double score = rs.getDouble("Score");
                    boolean critical = rs.getBoolean("hasCritical");
                    model.put("currentScore", (int) Math.round(score));
                    model.put("scoreDisqualified", critical);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer resolveSessionAreaId(int sessionId) {
        String sql = "SELECT TOP 1 ExamAreaId FROM Session_ExamArea WHERE SessionId = ? ORDER BY ExamAreaId";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamAreaId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Map<String, Object>> orderRowsByQueue(List<Map<String, Object>> rows, Lane lane) {
        List<Integer> order = ExamQueue.asList(lane);
        if (order.isEmpty() || rows.isEmpty()) {
            return rows;
        }
        Map<Integer, Map<String, Object>> bySbd = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object sbdObj = row.get("sbd");
            if (sbdObj instanceof Number) {
                bySbd.put(((Number) sbdObj).intValue(), row);
            }
        }
        List<Map<String, Object>> ordered = new ArrayList<>();
        for (Integer sbd : order) {
            Map<String, Object> row = bySbd.remove(sbd);
            if (row != null) {
                ordered.add(row);
            }
        }
        ordered.addAll(bySbd.values());
        return ordered;
    }
}
