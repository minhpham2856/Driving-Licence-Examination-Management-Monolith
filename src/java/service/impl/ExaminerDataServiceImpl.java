package service.impl;
import dao.AuditDAO;
import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.ExaminerDataDAO;
import dao.SessionDAO;
import dao.TheoryPaperDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.ExaminerDataDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dto.CandidateEnrollmentDTO;
import model.Exam;
import model.ExamDevice;
import model.Session;
import service.ExaminerDataService;
import util.ExamQueue;
import util.ExamQueue.Lane;
import enums.DeviceStatus;
import enums.DeviceType;
import enums.SectionStatus;
import enums.ViolationReason;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class ExaminerDataServiceImpl implements ExaminerDataService {
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final CandidateEnrollmentQueryService enrollmentViewSupport = new service.impl.CandidateEnrollmentQueryServiceImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExaminerDataDAO examinerDataDAO = new ExaminerDataDAOImpl();
    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, Integer sbdParam) {
        return getCandidateCallData(sessionId, sbdParam, null);
    }
    @Override
    public Map<String, Object> getCandidateCallData(int sessionId, Integer sbdParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        boolean isTheory = true;
        String sectionName = null;
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, isTheory, sectionName);
        candidates = filterRows(candidates, searchQuery, model);
        model.put("candidates", candidates);
        model.put("candidateQueue", candidates);
        if (sbdParam != null && sbdParam > 0) {
            CandidateEnrollmentDTO reg = findRegistration(sessionId, sbdParam);
            if (reg != null) {
                model.put("candidate", toViewRow(reg, isTheory,
                        examinerDataDAO.loadTheoryStatsBySession(sessionId),
                        examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName),
                        examinerDataDAO.loadPassFlagsBySession(sessionId),
                        formatSessionDate(sessionId),
                        resolveLicenceClass(sessionId),
                        examinerDataDAO.loadDeviceNamesBySession(sessionId)));
            }
        }
        return model;
    }
    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, true, null);
    }
    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, boolean isTheory, String sectionName) {
        List<CandidateEnrollmentDTO> registrations = enrollmentViewSupport.getCandidatesBySession(sessionId);
        Map<Integer, int[]> theoryStats = examinerDataDAO.loadTheoryStatsBySession(sessionId);
        Map<Integer, Double> sectionScores = examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName);
        Map<Integer, Boolean> passFlags = examinerDataDAO.loadPassFlagsBySession(sessionId);
        String examDate = formatSessionDate(sessionId);
        String licenceClass = resolveLicenceClass(sessionId);
        Map<Integer, String> deviceNames = examinerDataDAO.loadDeviceNamesBySession(sessionId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CandidateEnrollmentDTO reg : registrations) {
            rows.add(toViewRow(reg, isTheory, theoryStats, sectionScores, passFlags, examDate, licenceClass, deviceNames));
        }
        return rows;
    }
    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, boolean isTheory, String sectionName) {
        List<Map<String, Object>> rows = loadCandidateRows(sessionId, isTheory, sectionName);
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
        for (CandidateEnrollmentDTO reg : enrollmentViewSupport.getCandidatesBySession(sessionId)) {
            if (reg.getSbd() == sbd) {
                return reg;
            }
        }
        return null;
    }
    @Override
    public Map<String, Object> getScoreEntryData(int sessionId, Integer sbdParam, String sectionName) {
        Map<String, Object> model = new HashMap<>();
        boolean isTheory = false;
        List<Map<String, Object>> allRows = loadCandidateRows(sessionId, isTheory, sectionName);
        List<Map<String, Object>> scoreQueue = new ArrayList<>();
        for (Map<String, Object> row : allRows) {
            Object sbdObj = row.get("sbd");
            int rowSbd = sbdObj instanceof Number ? ((Number) sbdObj).intValue() : 0;
            CandidateEnrollmentDTO reg = findRegistration(sessionId, rowSbd);
            if (isScoreQueueEligible(sessionId, reg, isTheory, sectionName)) {
                scoreQueue.add(row);
            }
        }
        Lane lane = ExamQueue.resolveLane(isTheory, sectionName);
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
            for (Map<String, Object> row : loadCandidateRows(sessionId, false, null)) {
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
    public boolean isScoreQueueEligible(int sessionId, CandidateEnrollmentDTO reg, boolean isTheory,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        String status = reg.getSectionStatus();
        return SectionStatus.isEligibleForScoreQueue(status);
    }
    @Override
    public Map<String, Object> getViolationData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, true, null);
        model.put("candidates", candidates);
        model.put("violationReasons", ViolationReason.optionList());
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
            row.put("status", DeviceStatus.HOAT_DONG.getDisplayName());
            row.put("statusLabel", DeviceStatus.readyLabel());
            row.put("statusClass", DeviceStatus.cssClassFor(true));
        } else {
            row.put("status", DeviceStatus.BAO_TRI.getDisplayName());
            row.put("statusLabel", DeviceStatus.BAO_TRI.getDisplayName());
            row.put("statusClass", DeviceStatus.cssClassFor(false));
        }
        row.put("icon", DeviceType.iconFor(device.getDeviceType()));
        return row;
    }
    private String resolveAreaName(int areaId) {
        model.ExamArea area = examAreaDAO.getById(areaId);
        return area != null && area.getAreaName() != null ? area.getAreaName() : "";
    }
    @Override
    public boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, boolean isTheory,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        return !SectionStatus.isDone(reg.getSectionStatus());
    }
    @Override
    public List<Map<String, Object>> orderCandidateRowsByQueue(List<Map<String, Object>> rows,
            boolean isTheory, String sectionName) {
        return orderRowsByQueue(rows, ExamQueue.resolveLane(isTheory, sectionName));
    }
    private Map<String, Object> toViewRow(CandidateEnrollmentDTO reg, boolean isTheory,
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
        row.put("callEligible", isCallEligible(0, reg, isTheory, null));
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
                && !SectionStatus.isDone(reg.getSectionStatus()));
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
        return SectionStatus.statusKey(reg.getSectionStatus());
    }
    private static String resolveStatusLabel(CandidateEnrollmentDTO reg, String statusKey) {
        if ("absent".equals(statusKey)) {
            return "Vắng";
        }
        if ("suspended".equals(statusKey)) {
            return "Đình chỉ";
        }
        return SectionStatus.normalizeDisplayName(reg.getSectionStatus());
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
        return examinerDataDAO.findLicenceClassByExamId(session.getExamId());
    }
    private List<Map<String, Object>> loadScoreDeductions(String sectionName, Integer candidateId, Integer sessionId) {
        List<Map<String, Object>> list = examinerDataDAO.loadScoreDeductionRules(sectionName,
                sessionId != null ? sessionId : 0);
        if (candidateId != null && candidateId > 0 && sessionId != null && sessionId > 0 && !list.isEmpty()) {
            Map<Integer, int[]> occurrences = examinerDataDAO.loadDeductionOccurrences(candidateId, sessionId);
            Map<Integer, java.util.Date> recordedAt = examinerDataDAO.loadDeductionRecordedAt(candidateId, sessionId);
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
        Map<String, Object> summary = examinerDataDAO.loadScoreSummary(
                candidateId != null ? candidateId : 0,
                sessionId != null ? sessionId : 0,
                sectionName);
        model.put("currentScore", summary.get("currentScore"));
        model.put("scoreDisqualified", summary.get("scoreDisqualified"));
    }
    private Integer resolveSessionAreaId(int sessionId) {
        return examinerDataDAO.findPrimarySessionAreaId(sessionId);
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
