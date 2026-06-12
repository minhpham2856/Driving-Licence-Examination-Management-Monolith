package Services.Impl;

import Constants.ExamSectionType;
import Constants.ViolationReasonCodes;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAO.AuditLogDAO;
import DAO.ExamRegistrationDAO;
import DAO.ExaminerSessionDataDAO;
import DAO.TheoryPaperDAO;
import DAO.Impl.AuditLogDAOImpl;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExaminerSessionDataDAOImpl;
import DAO.Impl.TheoryPaperDAOImpl;
import Models.AuditLog;
import Models.ExamRegistration;
import Models.ExaminerAnswerStats;
import Models.ExaminerPaperState;
import Models.TheoryPaperAnswer;
import Services.ExaminerSessionContextService;
import Services.ExaminerViewDataService;
import Utils.ExaminerCandidateSort;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExaminerViewDataServiceImpl implements ExaminerViewDataService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final int AUDIT_PAGE_SIZE = 20;
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;

    private final ExamRegistrationDAO registrationDAO = new ExamRegistrationDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();

    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
    }

    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery) {
        List<ExamRegistration> registrations = registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperState> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStats> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        String examCode = sessionDataDAO.findExamCodeBySessionId(sessionId);
        String search = normalizeSearch(searchQuery);
        ExaminerCandidateSort.Spec sortSpec = ExaminerCandidateSort.parse(
                request.getParameter("sort"), request.getParameter("dir"));

        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("searchActive", search != null);
        request.setAttribute("sortBy", sortSpec.getColumn());
        request.setAttribute("sortDir", sortSpec.isAscending() ? "asc" : "desc");

        ExamSectionType sectionType = resolveSectionType(request);
        String sectionName = resolveSectionName(request);

        List<Map<String, Object>> candidates = new ArrayList<>();
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;

        for (ExamRegistration reg : registrations) {
            if (search != null && !matchesCandidateSearch(reg, search)) {
                continue;
            }
            ExaminerPaperState paper = paperStates.get(reg.getId());
            ExaminerAnswerStats stats = answerStats.get(reg.getId());
            Map<String, Object> row = toViewRow(reg, paper, stats, sectionType, sectionName);
            candidates.add(row);

            String status = (String) row.get("status");
            if ("done".equals(status)) {
                done++;
            } else if ("testing".equals(status)) {
                testing++;
            } else {
                pending++;
            }
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if ("done".equals(status) && !"—".equals(row.get("resultLabel"))) {
                failed++;
            }
        }

        ExaminerCandidateSort.sort(candidates, sortSpec);
        request.setAttribute("candidates", candidates);
        request.setAttribute("examSummary", buildSummary(examCode, candidates.size(), done, testing, pending, passed, failed));

        Map<String, Object> selected = resolveCandidate(candidates, registrations, sessionId, sbdParam,
                paperStates, answerStats, sectionType, sectionName);
        if (selected == null && sbdParam != null && !sbdParam.isBlank()) {
            ExamRegistration reg = findRegistration(sessionId, sbdParam);
            if (reg != null) {
                selected = toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                        sectionType, sectionName);
            }
        }
        if (selected != null) {
            request.setAttribute("candidate", selected);
        }
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, ExamSectionType.THEORY, null);
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, ExamSectionType sectionType,
            String sectionName) {
        List<ExamRegistration> registrations = registrationDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperState> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStats> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (ExamRegistration reg : registrations) {
            candidates.add(toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                    sectionType, sectionName));
        }
        return candidates;
    }

    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, ExamSectionType sectionType,
            String sectionName) {
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, sectionType, sectionName);
        String examCode = sessionDataDAO.findExamCodeBySessionId(sessionId);
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;
        for (Map<String, Object> row : candidates) {
            String status = (String) row.get("status");
            if ("done".equals(status)) {
                done++;
            } else if ("testing".equals(status)) {
                testing++;
            } else if (!"absent".equals(status)) {
                pending++;
            }
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if ("done".equals(status) && !"—".equals(row.get("resultLabel"))) {
                failed++;
            }
        }
        return buildSummary(examCode, candidates.size(), done, testing, pending, passed, failed);
    }

    @Override
    public void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam) {
        attachAuditLogs(request, sessionId, pageParam, null);
    }

    @Override
    public void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam, String searchQuery) {
        int page = 1;
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        String search = normalizeSearch(searchQuery);
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("searchActive", search != null);

        List<AuditLog> logs = auditLogDAO.getLogsForSessionPaginated(sessionId, page, AUDIT_PAGE_SIZE, search);
        int total = auditLogDAO.getLogsCountForSession(sessionId, search);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuditLog log : logs) {
            rows.add(toAuditViewRow(log));
        }
        request.setAttribute("auditLogs", rows);
        request.setAttribute("auditPage", page);
        int totalPages = Math.max(1, (total + AUDIT_PAGE_SIZE - 1) / AUDIT_PAGE_SIZE);
        request.setAttribute("auditTotalPages", totalPages);
        request.setAttribute("auditTotal", total);
    }

    @Override
    public void attachScoreEntry(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
        if (request.getAttribute("candidate") == null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) request.getAttribute("candidates");
            if (candidates != null) {
                for (Map<String, Object> row : candidates) {
                    if ("testing".equals(row.get("status"))) {
                        request.setAttribute("candidate", row);
                        break;
                    }
                }
                if (request.getAttribute("candidate") == null && !candidates.isEmpty()) {
                    request.setAttribute("candidate", candidates.get(0));
                }
            }
        }
        request.setAttribute("scoreDeductions", sessionDataDAO.findScoreDeductions());
    }

    @Override
    public void attachViolation(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
        request.setAttribute("scoreDeductions", sessionDataDAO.findScoreDeductions());
        request.setAttribute("violationReasons", ViolationReasonCodes.asOptionList());
        if (sbdParam == null || sbdParam.isBlank()) {
            request.setAttribute("candidateViolations", List.of());
            return;
        }
        List<Map<String, Object>> applied = new ArrayList<>();
        for (Map<String, Object> row : sessionDataDAO.findScoreViolationRows(sessionId)) {
            if (sbdParam.equals(String.valueOf(row.get("sbd")))) {
                applied.add(row);
            }
        }
        request.setAttribute("candidateViolations", applied);
    }

    @Override
    public void attachDevices(HttpServletRequest request, int sessionId, String searchQuery) {
        String search = normalizeSearch(searchQuery);
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("searchActive", search != null);

        List<Map<String, Object>> all = sessionDataDAO.findDevicesBySessionId(sessionId);
        List<Map<String, Object>> computers = new ArrayList<>();
        for (Map<String, Object> device : all) {
            if (isComputerDevice(device)) {
                computers.add(device);
            }
        }

        if (search == null) {
            request.setAttribute("devices", computers);
            return;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        String needle = search.toLowerCase();
        for (Map<String, Object> device : computers) {
            if (matchesDeviceSearch(device, needle)) {
                filtered.add(device);
            }
        }
        request.setAttribute("devices", filtered);
    }

    private static boolean isComputerDevice(Map<String, Object> device) {
        Object type = device.get("type");
        if (type == null) {
            return false;
        }
        return "computer".equalsIgnoreCase(String.valueOf(type).trim());
    }

    @Override
    public boolean isCallEligible(int sessionId, ExamRegistration reg, ExamSectionType sectionType,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        Map<Integer, ExaminerPaperState> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStats> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        Map<String, Object> row = toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                sectionType, sectionName);
        return Boolean.TRUE.equals(row.get("callEligible"));
    }

    private static ExamSectionType resolveSectionType(HttpServletRequest request) {
        Object value = request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof ExamSectionType) {
            return (ExamSectionType) value;
        }
        return ExamSectionType.THEORY;
    }

    private static String resolveSectionName(HttpServletRequest request) {
        Object slotObj = request.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlot) {
            return ((ExaminerSlot) slotObj).getExamTypeName();
        }
        Object name = request.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    private static boolean matchesDeviceSearch(Map<String, Object> device, String needle) {
        for (String key : new String[] { "name", "type", "status", "area" }) {
            Object val = device.get(key);
            if (val != null && String.valueOf(val).toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void attachPaperAnswers(HttpServletRequest request, int sessionId, String sbd, String contextPath) {
        List<TheoryPaperAnswer> answers = theoryPaperDAO.getAnswersBySessionAndSbd(sessionId, sbd);
        List<Map<String, Object>> paperAnswers = new ArrayList<>();
        int correctCount = 0;
        int wrongCount = 0;
        for (TheoryPaperAnswer answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNo", answer.getQuestionNo());
            row.put("imageUrl", resolveImageUrl(contextPath, answer.getImageUrl()));
            row.put("correctAnswer", answer.getCorrectAnswer());
            row.put("studentAnswer", answer.getStudentAnswer());
            row.put("correct", answer.isCorrect());
            paperAnswers.add(row);
            if (answer.isCorrect()) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("totalQuestions", answers.size());
        request.setAttribute("paperAnswers", paperAnswers);
        request.setAttribute("paperSummary", summary);
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
    public ExamRegistration findRegistration(int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        ExamRegistration reg = registrationDAO.getBySessionAndSbd(sessionId, sbd.trim());
        if (reg != null) {
            return reg;
        }
        List<ExamRegistration> all = registrationDAO.getCandidatesBySession(sessionId);
        String normalized = sbd.trim();
        for (ExamRegistration candidate : all) {
            if (normalized.equalsIgnoreCase(candidate.getSbd())) {
                return candidate;
            }
        }
        if (!normalized.contains("-") && !all.isEmpty()) {
            String license = all.get(0).getLicenseCode();
            if (license != null) {
                return registrationDAO.getBySessionAndSbd(sessionId, license + "-" + normalized);
            }
        }
        return null;
    }

    private Map<String, Object> toAuditViewRow(AuditLog log) {
        Map<String, Object> row = new LinkedHashMap<>();
        String action = log.getAction() != null ? log.getAction() : "UPDATE";
        row.put("username", nullToDash(log.getChangerName()));
        row.put("actionLabel", mapActionLabel(action));
        row.put("actionBadge", mapActionBadge(action));
        row.put("entityName", nullToDash(log.getTableName()));
        row.put("entityId", log.getRecordId() != null ? String.valueOf(log.getRecordId()) : "—");
        row.put("info", nullToDash(log.getNewValue()));
        row.put("oldValue", log.getOldValue());
        row.put("newValue", nullToDash(log.getNewValue()));
        row.put("newValueClass", mapNewValueClass(action));
        row.put("reason", nullToDash(log.getReason()));
        Timestamp changedAt = log.getChangedAt();
        if (changedAt != null) {
            synchronized (TIME_FMT) {
                row.put("time", TIME_FMT.format(changedAt));
            }
            synchronized (DATE_FMT) {
                row.put("date", DATE_FMT.format(changedAt));
            }
        } else {
            row.put("time", "—");
            row.put("date", "—");
        }
        return row;
    }

    private static String mapActionLabel(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "Thêm";
            case "DELETE" -> "Xóa";
            case "EXPORT" -> "Xuất";
            case "ASSIGN" -> "Phân công";
            case "IMPORT" -> "Nhập";
            default -> "Cập nhật";
        };
    }

    private static String mapActionBadge(String action) {
        return switch (action.toUpperCase()) {
            case "INSERT" -> "audit-badge--insert";
            case "DELETE" -> "audit-badge--delete";
            case "EXPORT" -> "audit-badge--export";
            default -> "audit-badge--update";
        };
    }

    private static String mapNewValueClass(String action) {
        if ("DELETE".equalsIgnoreCase(action)) {
            return "audit-td--old";
        }
        return "audit-td--new";
    }

    private static String resolveImageUrl(String contextPath, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return (contextPath != null ? contextPath : "") + "/assets/imgs/LOGO.png";
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
            return imageUrl.startsWith("/") && contextPath != null ? contextPath + imageUrl : imageUrl;
        }
        return (contextPath != null ? contextPath : "") + "/" + imageUrl.replaceFirst("^/+", "");
    }

    private Map<String, Object> buildSummary(String examCode, int total, int done, int testing,
            int pending, int passed, int failed) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("examCode", examCode != null ? examCode : "—");
        summary.put("total", total);
        summary.put("done", done);
        summary.put("testing", testing);
        summary.put("pending", pending);
        summary.put("passed", passed);
        summary.put("failed", failed);
        return summary;
    }

    private Map<String, Object> toViewRow(ExamRegistration reg, ExaminerPaperState paper, ExaminerAnswerStats stats,
            ExamSectionType sectionType, String sectionName) {
        Map<String, Object> row = new LinkedHashMap<>();

        boolean absent = reg.isAbsent();
        boolean suspended = reg.isSuspended();
        String status;
        String statusLabel;
        boolean passed = false;
        String resultLabel = "—";

        if (sectionType == ExamSectionType.SCORE_BASED) {
            Integer sectionScore = resolveScoreForSection(reg, sectionName);
            String passStatus = resolvePassStatusForSection(reg, sectionName);
            row.put("examScore", sectionScore != null ? String.valueOf(sectionScore) : "—");

            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            } else if (sectionScore != null
                    || "passed".equalsIgnoreCase(passStatus)
                    || "failed".equalsIgnoreCase(passStatus)) {
                status = "done";
                statusLabel = "Đã thi";
            } else {
                status = "pending";
                statusLabel = "Chưa thi";
            }

            if ("passed".equalsIgnoreCase(passStatus)) {
                passed = true;
                resultLabel = "ĐẠT";
            } else if ("failed".equalsIgnoreCase(passStatus)) {
                passed = false;
                resultLabel = "TRƯỢT";
            } else if (sectionScore != null && status.equals("done")) {
                passed = sectionScore >= 80;
                resultLabel = passed ? "ĐẠT" : "TRƯỢT";
            }
        } else {
            if (paper != null && paper.isSubmitted()) {
                status = "done";
                statusLabel = "Đã thi";
            } else if (paper != null && paper.isStarted()) {
                status = "testing";
                statusLabel = "Đang thi";
            } else {
                status = "pending";
                statusLabel = "Chưa thi";
            }

            Integer theoryCorrectScore = resolveTheoryCorrectScore(reg, stats);
            if (reg.getTheoryScore() != null) {
                passed = "passed".equalsIgnoreCase(reg.getTheoryPassed());
                if ("passed".equalsIgnoreCase(reg.getTheoryPassed())) {
                    resultLabel = "ĐẠT";
                } else if ("failed".equalsIgnoreCase(reg.getTheoryPassed())) {
                    resultLabel = "TRƯỢT";
                }
            } else if (stats != null && stats.getCorrect() + stats.getWrong() + stats.getUnanswered() > 0) {
                passed = stats.getCorrect() >= THEORY_PASS_CORRECT;
                if (paper != null && paper.isSubmitted()) {
                    resultLabel = passed ? "ĐẠT" : "TRƯỢT";
                } else {
                    passed = false;
                }
            }

            String correctDisplay = theoryCorrectScore != null ? String.valueOf(theoryCorrectScore) : "—";
            String wrongDisplay = stats != null ? String.valueOf(stats.getWrong()) : "—";
            String unansweredDisplay = stats != null ? String.valueOf(stats.getUnanswered()) : "—";
            if (reg.getTheoryScore() != null && theoryCorrectScore != null) {
                int wrong = Math.max(0, THEORY_MAX_QUESTIONS - theoryCorrectScore);
                wrongDisplay = String.valueOf(wrong);
                unansweredDisplay = "0";
            }
            row.put("correct", correctDisplay);
            row.put("wrong", wrongDisplay);
            row.put("unanswered", unansweredDisplay);
            row.put("theoryCorrectScore", theoryCorrectScore);
            row.put("examScore", "—");

            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            }
        }

        if (suspended) {
            status = "suspended";
            statusLabel = "Đình chỉ";
        }

        row.put("fullName", nullToDash(reg.getFullName()));
        row.put("sbd", reg.getSbd());
        row.put("dob", formatDate(reg.getDateOfBirth()));
        row.put("address", nullToDash(reg.getAddress()));
        row.put("status", status);
        row.put("statusLabel", statusLabel);
        row.put("governmentId", nullToDash(reg.getGovIdNo()));
        row.put("passed", passed);
        row.put("resultLabel", resultLabel);
        row.put("examDate", formatDate(reg.getExamDate()));
        row.put("examDateRaw", formatDateRaw(reg.getExamDate()));
        row.put("licenceClass", nullToDash(reg.getLicenseCode()));
        row.put("sex", reg.isGender() ? "Nữ" : "Nam");
        row.put("reasonForTaking", nullToDash(reg.getReasonForTaking()));
        row.put("scoreTheory", formatScore(reg.getTheoryScore()));
        row.put("scorePractical", formatScore(reg.getPracticalScore()));
        row.put("scoreRoadLayout", "—");
        row.put("scoreOnRoad", formatScore(reg.getRoadTestScore()));
        row.put("email", reg.getEmail() != null ? reg.getEmail() : "");
        row.put("phoneNo", reg.getPhoneNo() != null ? reg.getPhoneNo() : "");
        row.put("dobRaw", formatDateRaw(reg.getDateOfBirth()));
        row.put("genderValue", reg.isGender() ? "1" : "0");
        row.put("absent", absent);
        row.put("suspended", suspended);
        row.put("callEligible", "pending".equals(status) && !suspended);
        return row;
    }

    private static Integer resolveScoreForSection(ExamRegistration reg, String sectionName) {
        if (sectionName == null) {
            return reg.getPracticalScore();
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return reg.getRoadTestScore();
        }
        return reg.getPracticalScore();
    }

    private static String resolvePassStatusForSection(ExamRegistration reg, String sectionName) {
        if (sectionName == null) {
            return reg.getPracticalPassed();
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return reg.getRoadTestPassed();
        }
        return reg.getPracticalPassed();
    }

    private static String formatDateRaw(Date date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }

    private Map<String, Object> resolveCandidate(List<Map<String, Object>> candidates,
            List<ExamRegistration> registrations, int sessionId, String sbdParam,
            Map<Integer, ExaminerPaperState> paperStates, Map<Integer, ExaminerAnswerStats> answerStats,
            ExamSectionType sectionType, String sectionName) {
        if (sbdParam == null || sbdParam.isBlank()) {
            return null;
        }
        String normalized = sbdParam.trim();
        for (Map<String, Object> row : candidates) {
            if (normalized.equalsIgnoreCase(String.valueOf(row.get("sbd")))) {
                return row;
            }
        }
        ExamRegistration reg = registrationDAO.getBySessionAndSbd(sessionId, normalized);
        if (reg == null && !normalized.contains("-") && !registrations.isEmpty()) {
            String license = registrations.get(0).getLicenseCode();
            if (license != null) {
                reg = registrationDAO.getBySessionAndSbd(sessionId, license + "-" + normalized);
            }
        }
        if (reg == null) {
            return null;
        }
        return toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()), sectionType, sectionName);
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "—";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    private static String formatScore(Integer score) {
        if (score == null) {
            return "—";
        }
        return score + "/100";
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String normalizeSearch(String searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean matchesCandidateSearch(ExamRegistration reg, String search) {
        String q = search.toLowerCase();
        return containsIgnoreCase(reg.getFullName(), q)
                || containsIgnoreCase(reg.getSbd(), q)
                || containsIgnoreCase(reg.getGovIdNo(), q)
                || containsIgnoreCase(String.valueOf(reg.getCandidateNo()), q);
    }

    private static Integer resolveTheoryCorrectScore(ExamRegistration reg, ExaminerAnswerStats stats) {
        if (reg.getTheoryScore() != null && reg.getTheoryScore() <= THEORY_MAX_QUESTIONS) {
            return reg.getTheoryScore();
        }
        if (stats != null && stats.getCorrect() + stats.getWrong() + stats.getUnanswered() > 0) {
            return stats.getCorrect();
        }
        return null;
    }

    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase().contains(queryLower);
    }
}
