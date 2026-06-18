package Services.Impl;

import Utils.ExamConstants;
import Utils.ExamConstants.SectionType;
import Controllers.Examiner.ExaminerScoreEntryQueue;
import Controllers.Staff.ExamStaff.ExaminerSlot;
import DAOs.AuditLogDAO;
import DAOs.ExamCandidateVehicleDAO;
import DAOs.CandidateDAO;
import DAOs.ExaminerSessionDataDAO;
import DAOs.TheoryPaperDAO;
import DAOs.Impl.AuditLogDAOImpl;
import DAOs.Impl.ExamCandidateVehicleDAOImpl;
import DAOs.Impl.CandidateDAOImpl;
import DAOs.Impl.ExaminerSessionDataDAOImpl;
import DAOs.Impl.TheoryPaperDAOImpl;
import DTOs.AuditDTO;
import DTOs.CandidateDTO;
import DTOs.ExaminerAnswerStats;
import DTOs.ExaminerPaperState;
import DTOs.TheoryPaperAnswer;
import Services.ExaminerSessionContextService;
import Services.ExaminerViewDataService;
import Utils.AuditLogViewHelper;
import Utils.ExaminerCandidateSort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    private final ExamCandidateVehicleDAO vehicleDAO = new ExamCandidateVehicleDAOImpl();

    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
    }

    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery) {
        candidateDAO.syncSectionStatusesForSession(sessionId);
        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
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

        SectionType sectionType = resolveSectionType(request);
        String sectionName = resolveSectionName(request);

        List<Map<String, Object>> candidates = new ArrayList<>();
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;

        for (CandidateDTO reg : registrations) {
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
            } else if (!"absent".equals(status) && !"suspended".equals(status) && !"awaiting".equals(status)) {
                pending++;
            }
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if ("done".equals(status) && !"-".equals(row.get("resultLabel"))) {
                failed++;
            }
        }

        ExaminerCandidateSort.sort(candidates, sortSpec);
        request.setAttribute("candidates", candidates);
        request.setAttribute("examSummary", buildSummary(examCode, candidates.size(), done, testing, pending, passed, failed));

        Map<String, Object> selected = resolveCandidate(candidates, registrations, sessionId, sbdParam,
                paperStates, answerStats, sectionType, sectionName);
        if (selected == null && sbdParam != null && !sbdParam.isBlank()) {
            CandidateDTO reg = findRegistration(sessionId, sbdParam);
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
        return loadCandidateRows(sessionId, SectionType.THEORY, null);
    }

    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType,
            String sectionName) {
        candidateDAO.syncSectionStatusesForSession(sessionId);
        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperState> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStats> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (CandidateDTO reg : registrations) {
            candidates.add(toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                    sectionType, sectionName));
        }
        return candidates;
    }

    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType,
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
            } else if (!"absent".equals(status) && !"suspended".equals(status) && !"awaiting".equals(status)) {
                pending++;
            }
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if ("done".equals(status) && !"-".equals(row.get("resultLabel"))) {
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

        List<AuditDTO> logs = auditLogDAO.getLogsForSessionPaginated(sessionId, page, AUDIT_PAGE_SIZE, search);
        int total = auditLogDAO.getLogsCountForSession(sessionId, search);
        Map<Integer, String> sbdByRecordId = buildSbdLookup(sessionId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuditDTO log : logs) {
            for (Map<String, Object> row : AuditLogViewHelper.toViewRows(log, sbdByRecordId)) {
                Timestamp changedAt = log.getChangedAt();
                if (changedAt != null) {
                    synchronized (TIME_FMT) {
                        row.put("time", TIME_FMT.format(changedAt));
                    }
                    synchronized (DATE_FMT) {
                        row.put("date", DATE_FMT.format(changedAt));
                    }
                } else {
                    row.put("time", "-");
                    row.put("date", "-");
                }
                rows.add(row);
            }
        }
        request.setAttribute("auditLogs", rows);
        request.setAttribute("auditPage", page);
        int totalPages = Math.max(1, (total + AUDIT_PAGE_SIZE - 1) / AUDIT_PAGE_SIZE);
        request.setAttribute("auditTotalPages", totalPages);
        request.setAttribute("auditTotal", total);
    }

    @Override
    public void attachScoreEntry(HttpServletRequest request, int sessionId, String sbdParam) {
        HttpSession httpSession = request.getSession(false);
        SectionType sectionType = resolveSectionType(request);
        String sectionName = resolveSectionName(request);

        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperState> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStats> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);

        List<String> eligibleSbds = new ArrayList<>();
        Map<String, Map<String, Object>> rowBySbd = new LinkedHashMap<>();

        for (CandidateDTO reg : registrations) {
            if (!isScoreQueueEligibleInternal(sessionId, reg, sectionType, sectionName)) {
                continue;
            }
            Map<String, Object> row = toScoreQueueRow(reg);
            eligibleSbds.add(reg.getSbd());
            rowBySbd.put(reg.getSbd(), row);
        }

        if (httpSession != null) {
            ExaminerScoreEntryQueue.syncQueue(httpSession, sessionId, eligibleSbds);
        }

        String activeSbd = httpSession != null ? ExaminerScoreEntryQueue.getActiveSbd(httpSession, sessionId) : null;
        String calledSbd = httpSession != null ? ExaminerScoreEntryQueue.getCalledSbd(httpSession, sessionId) : null;

        if (sbdParam != null && !sbdParam.isBlank()) {
            activeSbd = sbdParam.trim();
            if (httpSession != null) {
                ExaminerScoreEntryQueue.setActiveSbd(httpSession, sessionId, activeSbd);
            }
        }

        List<Map<String, Object>> scoreQueue = new ArrayList<>();
        List<String> queueOrder = httpSession != null
                ? ExaminerScoreEntryQueue.getQueue(httpSession, sessionId)
                : eligibleSbds;
        for (String sbd : queueOrder) {
            Map<String, Object> row = rowBySbd.get(sbd);
            if (row == null) {
                continue;
            }
            row.put("active", sbd.equals(activeSbd));
            row.put("called", sbd.equals(calledSbd));
            scoreQueue.add(row);
        }

        request.setAttribute("scoreQueue", scoreQueue);
        request.setAttribute("scoreQueueTotal", scoreQueue.size());

        String licenceClass = resolveLicenceClass(registrations);
        request.setAttribute("licenceClass", licenceClass);

        List<Map<String, Object>> sessionDevices = buildSessionDevices(sessionId, licenceClass, true);
        List<Integer> availableDeviceIds = new ArrayList<>();
        for (Map<String, Object> device : sessionDevices) {
            String status = device.get("status") != null ? String.valueOf(device.get("status")) : "";
            if ("Available".equalsIgnoreCase(status) || "Operational".equalsIgnoreCase(status)) {
                Object idObj = device.get("id");
                if (idObj instanceof Number) {
                    availableDeviceIds.add(((Number) idObj).intValue());
                }
            }
        }

        Map<String, Integer> sbdToCandidateId = new LinkedHashMap<>();
        for (CandidateDTO reg : registrations) {
            sbdToCandidateId.put(reg.getSbd(), reg.getId());
        }
        List<Integer> candidateIdsInOrder = new ArrayList<>();
        for (String sbd : queueOrder) {
            Integer candidateId = sbdToCandidateId.get(sbd);
            if (candidateId != null) {
                candidateIdsInOrder.add(candidateId);
            }
        }
        if (!availableDeviceIds.isEmpty()) {
            vehicleDAO.syncRoundRobinAssignments(sessionId, candidateIdsInOrder, availableDeviceIds);
        }

        Map<Integer, Map<String, Object>> assignments = vehicleDAO.findAssignmentDetailsBySession(sessionId);
        for (Map<String, Object> row : scoreQueue) {
            attachVehicleToQueueRow(row, sbdToCandidateId, assignments);
        }
        request.setAttribute("sessionVehicles", sessionDevices);

        Integer examSectionId = sessionDataDAO.findExamSectionIdForSession(sessionId);
        List<Map<String, Object>> scoreDeductions = examSectionId != null
                ? sessionDataDAO.findScoreDeductionsBySectionId(examSectionId)
                : sessionDataDAO.findScoreDeductions();
        request.setAttribute("examSectionId", examSectionId);

        CandidateDTO activeReg = null;
        if (activeSbd != null) {
            for (CandidateDTO reg : registrations) {
                if (activeSbd.equals(reg.getSbd())) {
                    activeReg = reg;
                    break;
                }
            }
        }

        Map<Integer, Map<String, Object>> appliedById = new LinkedHashMap<>();
        boolean scoreDisqualified = false;
        double displayScore = 100;
        if (activeReg != null && examSectionId != null) {
            for (Map<String, Object> applied : candidateDAO.findAppliedScoreDeductions(
                    activeReg.getId(), sessionId)) {
                Object idObj = applied.get("id");
                if (idObj instanceof Integer) {
                    Integer id = (Integer) idObj;
                    appliedById.put(id, applied);
                    if (Boolean.TRUE.equals(applied.get("critical"))) {
                        scoreDisqualified = true;
                    }
                }
            }
            Integer sectionScore = resolveScoreForSection(activeReg, sectionName);
            if (sectionScore != null) {
                displayScore = sectionScore;
                scoreDisqualified = sectionScore <= 0;
            } else if (scoreDisqualified) {
                displayScore = 0;
            } else {
                for (Map<String, Object> applied : appliedById.values()) {
                    if (Boolean.TRUE.equals(applied.get("critical"))) {
                        continue;
                    }
                    int count = 0;
                    Object countObj = applied.get("occurrenceCount");
                    if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    double points = 0;
                    Object pointsObj = applied.get("points");
                    if (pointsObj instanceof Number) {
                        points = ((Number) pointsObj).doubleValue();
                    }
                    displayScore -= points * count;
                }
                displayScore = Math.max(0, displayScore);
            }
        }

        for (Map<String, Object> deduction : scoreDeductions) {
            Object idObj = deduction.get("id");
            if (idObj instanceof Integer) {
                Integer id = (Integer) idObj;
                if (appliedById.containsKey(id)) {
                    Map<String, Object> applied = appliedById.get(id);
                    deduction.put("occurrenceCount", applied.get("occurrenceCount"));
                    deduction.put("recordedAt", applied.get("recordedAt"));
                } else {
                    deduction.put("occurrenceCount", 0);
                    deduction.put("recordedAt", null);
                }
            } else {
                deduction.put("occurrenceCount", 0);
                deduction.put("recordedAt", null);
            }
        }
        request.setAttribute("scoreDeductions", scoreDeductions);
        request.setAttribute("scoreDisqualified", scoreDisqualified);
        request.setAttribute("currentScore", displayScore);

        if (activeSbd != null && rowBySbd.containsKey(activeSbd)) {
            Map<String, Object> selected = new LinkedHashMap<>(rowBySbd.get(activeSbd));
            selected.put("status", "testing");
            selected.put("statusLabel", "Đang thi");
            attachVehicleToQueueRow(selected, sbdToCandidateId, assignments);
            request.setAttribute("candidate", selected);
            Object vehicleId = selected.get("vehicleId");
            request.setAttribute("candidateVehicleId", vehicleId);
        }
    }

    @Override
    public boolean isScoreQueueEligible(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName) {
        return isScoreQueueEligibleInternal(sessionId, reg, sectionType, sectionName);
    }

    private boolean isScoreQueueEligibleInternal(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName) {
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        if (sectionType != SectionType.SCORE_BASED) {
            return false;
        }
        Integer sectionScore = resolveScoreForSection(reg, sectionName);
        String passStatus = resolvePassStatusForSection(reg, sectionName);
        if (sectionScore != null
                || "passed".equalsIgnoreCase(passStatus)
                || "failed".equalsIgnoreCase(passStatus)) {
            return false;
        }
        String normalized = sectionName != null ? sectionName.trim().toLowerCase() : "";
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return "passed".equalsIgnoreCase(reg.getPracticalPassed());
        }
        return "passed".equalsIgnoreCase(reg.getTheoryPassed());
    }

    private static Map<String, Object> toScoreQueueRow(CandidateDTO reg) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sbd", reg.getSbd());
        row.put("fullName", nullToDash(reg.getFullName()));
        row.put("governmentId", nullToDash(reg.getGovIdNo()));
        row.put("active", false);
        row.put("called", false);
        return row;
    }

    @Override
    public void attachViolation(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
        request.setAttribute("scoreDeductions", sessionDataDAO.findScoreDeductions());
        request.setAttribute("violationReasons", ExamConstants.violationOptionList());
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

        SectionType sectionType = resolveSectionType(request);
        String licenceClass = resolveLicenceClass(candidateDAO.getCandidatesBySession(sessionId));
        request.setAttribute("licenceClass", licenceClass);
        boolean scoreBased = sectionType == SectionType.SCORE_BASED;
        request.setAttribute("devicesAreVehicles", scoreBased);
        request.setAttribute("devicesTitle", scoreBased ? "Xe thi" : "Máy thi");
        request.setAttribute("devicesUnit", scoreBased ? "xe" : "máy");

        List<Map<String, Object>> sessionDevices = buildSessionDevices(sessionId, licenceClass, scoreBased);

        if (search == null) {
            request.setAttribute("devices", sessionDevices);
            return;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        String needle = search.toLowerCase();
        for (Map<String, Object> device : sessionDevices) {
            if (matchesDeviceSearch(device, needle)) {
                filtered.add(device);
            }
        }
        request.setAttribute("devices", filtered);
    }

    private List<Map<String, Object>> buildSessionDevices(int sessionId, String licenceClass, boolean vehiclesOnly) {
        List<Map<String, Object>> all = sessionDataDAO.findDevicesBySessionId(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> device : all) {
            String type = device.get("type") != null ? String.valueOf(device.get("type")) : "";
            boolean include = vehiclesOnly
                    ? ExamConstants.isVehicle(type)
                            && ExamConstants.matchesLicence(licenceClass, type)
                    : ExamConstants.isComputer(type);
            if (include) {
                ExamConstants.enrichDeviceRow(device, licenceClass);
                result.add(device);
            }
        }
        return result;
    }

    private static String resolveLicenceClass(List<CandidateDTO> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return "B";
        }
        String licence = registrations.get(0).getLicenseCode();
        return licence != null && !licence.isBlank() ? licence.trim() : "B";
    }

    private static void attachVehicleToQueueRow(Map<String, Object> row,
            Map<String, Integer> sbdToCandidateId,
            Map<Integer, Map<String, Object>> assignments) {
        if (row == null || sbdToCandidateId == null || assignments == null) {
            return;
        }
        Object sbdObj = row.get("sbd");
        if (sbdObj == null) {
            return;
        }
        Integer candidateId = sbdToCandidateId.get(String.valueOf(sbdObj));
        if (candidateId == null) {
            return;
        }
        Map<String, Object> vehicle = assignments.get(candidateId);
        if (vehicle == null) {
            row.put("vehicleName", "-");
            row.put("vehicleIcon", ExamConstants.iconFor(null));
            row.put("vehicleId", null);
            return;
        }
        String type = vehicle.get("type") != null ? String.valueOf(vehicle.get("type")) : null;
        row.put("vehicleName", vehicle.get("name") != null ? vehicle.get("name") : "-");
        row.put("vehicleIcon", ExamConstants.iconFor(type));
        row.put("vehicleId", vehicle.get("deviceId"));
    }

    @Override
    public boolean isCallEligible(int sessionId, CandidateDTO reg, SectionType sectionType,
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

    private static SectionType resolveSectionType(HttpServletRequest request) {
        Object value = request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
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
        int unansweredCount = 0;
        String filter = request.getParameter("filter");
        if (filter == null || filter.isEmpty()) {
            filter = "all";
        }
        for (TheoryPaperAnswer answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNo", answer.getQuestionNo());
            row.put("imageUrl", resolveImageUrl(contextPath, answer.getImageUrl()));
            row.put("correctAnswer", answer.getCorrectAnswer());
            row.put("studentAnswer", answer.getStudentAnswer());
            boolean unanswered = isUnanswered(answer.getStudentAnswer());
            row.put("unanswered", unanswered);
            row.put("correct", !unanswered && answer.isCorrect());
            String answerStatus;
            if (unanswered) {
                answerStatus = "unanswered";
                unansweredCount++;
            } else if (answer.isCorrect()) {
                answerStatus = "correct";
                correctCount++;
            } else {
                answerStatus = "wrong";
                wrongCount++;
            }
            row.put("answerStatus", answerStatus);
            if ("all".equals(filter) || filter.equals(answerStatus)) {
                paperAnswers.add(row);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("unansweredCount", unansweredCount);
        summary.put("totalCount", answers.size());
        String sort = request.getParameter("sort");
        String dir = request.getParameter("dir");
        if (sort != null && !sort.isEmpty()) {
            boolean asc = !"desc".equalsIgnoreCase(dir);
            paperAnswers.sort((m1, m2) -> {
                Object v1 = m1.get(sort);
                Object v2 = m2.get(sort);
                if (v1 == null && v2 == null) return 0;
                if (v1 == null) return asc ? 1 : -1;
                if (v2 == null) return asc ? -1 : 1;
                int cmp;
                if (v1 instanceof Comparable && v1.getClass().equals(v2.getClass())) {
                    cmp = ((Comparable) v1).compareTo(v2);
                } else {
                    cmp = String.valueOf(v1).compareTo(String.valueOf(v2));
                }
                return asc ? cmp : -cmp;
            });
            request.setAttribute("sortBy", sort);
            request.setAttribute("sortDir", asc ? "asc" : "desc");
        }
        
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
    public CandidateDTO findRegistration(int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        CandidateDTO reg = candidateDAO.getBySessionAndSbd(sessionId, sbd.trim());
        if (reg != null) {
            return reg;
        }
        List<CandidateDTO> all = candidateDAO.getCandidatesBySession(sessionId);
        String normalized = sbd.trim();
        for (CandidateDTO candidate : all) {
            if (normalized.equalsIgnoreCase(candidate.getSbd())) {
                return candidate;
            }
        }
        if (!normalized.contains("-") && !all.isEmpty()) {
            String license = all.get(0).getLicenseCode();
            if (license != null) {
                return candidateDAO.getBySessionAndSbd(sessionId, license + "-" + normalized);
            }
        }
        return null;
    }

    private Map<Integer, String> buildSbdLookup(int sessionId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (CandidateDTO reg : candidateDAO.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), reg.getSbd());
        }
        return lookup;
    }

    private static boolean isUnanswered(String studentAnswer) {
        return studentAnswer == null || studentAnswer.isBlank() || "-".equals(studentAnswer.trim());
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
        summary.put("examCode", examCode != null ? examCode : "-");
        summary.put("total", total);
        summary.put("done", done);
        summary.put("testing", testing);
        summary.put("pending", pending);
        summary.put("passed", passed);
        summary.put("failed", failed);
        return summary;
    }

    private Map<String, Object> toViewRow(CandidateDTO reg, ExaminerPaperState paper, ExaminerAnswerStats stats,
            SectionType sectionType, String sectionName) {
        Map<String, Object> row = new LinkedHashMap<>();

        boolean absent = reg.isAbsent();
        boolean suspended = reg.isSuspended();
        String status;
        String statusLabel;
        boolean passed = false;
        String resultLabel = "-";

        if (sectionType == SectionType.SCORE_BASED) {
            Integer sectionScore = resolveScoreForSection(reg, sectionName);
            String passStatus = resolvePassStatusForSection(reg, sectionName);
            row.put("examScore", sectionScore != null ? String.valueOf(sectionScore) : "-");

            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            } else {
                applySectionStatus(reg, paper);
                status = mapViewStatus(reg.getSectionStatus());
                statusLabel = ExamConstants.candidateStatusLabel(reg.getSectionStatus());
            }

            if ("passed".equalsIgnoreCase(passStatus)) {
                passed = true;
                resultLabel = "ĐẠT";
            } else if ("failed".equalsIgnoreCase(passStatus)) {
                passed = false;
                resultLabel = "TRƯỢT";
            } else if (sectionScore != null && ExamConstants.isCandidateDone(reg.getSectionStatus())) {
                passed = sectionScore >= 80;
                resultLabel = passed ? "ĐẠT" : "TRƯỢT";
            }
        } else {
            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            } else {
                applySectionStatus(reg, paper);
                status = mapViewStatus(reg.getSectionStatus());
                statusLabel = ExamConstants.candidateStatusLabel(reg.getSectionStatus());
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

            String correctDisplay = theoryCorrectScore != null ? String.valueOf(theoryCorrectScore) : "-";
            String wrongDisplay = stats != null ? String.valueOf(stats.getWrong()) : "-";
            String unansweredDisplay = stats != null ? String.valueOf(stats.getUnanswered()) : "-";
            if (reg.getTheoryScore() != null && theoryCorrectScore != null) {
                int wrong = Math.max(0, THEORY_MAX_QUESTIONS - theoryCorrectScore);
                wrongDisplay = String.valueOf(wrong);
                unansweredDisplay = "0";
            }
            row.put("correct", correctDisplay);
            row.put("wrong", wrongDisplay);
            row.put("unanswered", unansweredDisplay);
            row.put("theoryCorrectScore", theoryCorrectScore);
            row.put("examScore", "-");
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
        row.put("scoreRoadLayout", "-");
        row.put("scoreOnRoad", formatScore(reg.getRoadTestScore()));
        row.put("email", reg.getEmail() != null ? reg.getEmail() : "");
        row.put("phoneNo", reg.getPhoneNo() != null ? reg.getPhoneNo() : "");
        row.put("dobRaw", formatDateRaw(reg.getDateOfBirth()));
        row.put("genderValue", reg.isGender() ? "1" : "0");
        row.put("absent", absent);
        row.put("suspended", suspended);
        row.put("sectionStatus", reg.getSectionStatus());
        row.put("awaitingSignature", ExamConstants.isCandidateAwaitingSignature(reg.getSectionStatus()));
        row.put("signaturePrinted", reg.isSignaturePrinted());
        row.put("completeEligible", ExamConstants.isCandidateAwaitingSignature(reg.getSectionStatus())
                && !suspended && !absent);
        row.put("callEligible", "pending".equals(status) && !suspended);
        return row;
    }

    private static void applySectionStatus(CandidateDTO reg, ExaminerPaperState paper) {
        String sectionStatus = reg.getSectionStatus();
        if (sectionStatus == null || sectionStatus.isBlank()) {
            reg.setSectionStatus(ExamConstants.CANDIDATE_PENDING);
            sectionStatus = ExamConstants.CANDIDATE_PENDING;
        }
        if (ExamConstants.CANDIDATE_PENDING.equals(sectionStatus) && paper != null) {
            if (paper.isSubmitted()) {
                reg.setSectionStatus(ExamConstants.CANDIDATE_AWAITING_SIGNATURE);
            } else if (paper.isStarted()) {
                reg.setSectionStatus(ExamConstants.CANDIDATE_TESTING);
            }
        }
    }

    private static String mapViewStatus(String sectionStatus) {
        if (sectionStatus == null || sectionStatus.isBlank()) {
            return "pending";
        }
        return switch (sectionStatus) {
            case ExamConstants.CANDIDATE_TESTING -> "testing";
            case ExamConstants.CANDIDATE_AWAITING_SIGNATURE -> "awaiting";
            case ExamConstants.CANDIDATE_DONE -> "done";
            default -> "pending";
        };
    }

    private static Integer resolveScoreForSection(CandidateDTO reg, String sectionName) {
        if (sectionName == null) {
            return reg.getPracticalScore();
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return reg.getRoadTestScore();
        }
        return reg.getPracticalScore();
    }

    private static String resolvePassStatusForSection(CandidateDTO reg, String sectionName) {
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
            List<CandidateDTO> registrations, int sessionId, String sbdParam,
            Map<Integer, ExaminerPaperState> paperStates, Map<Integer, ExaminerAnswerStats> answerStats,
            SectionType sectionType, String sectionName) {
        if (sbdParam == null || sbdParam.isBlank()) {
            return null;
        }
        String normalized = sbdParam.trim();
        for (Map<String, Object> row : candidates) {
            if (normalized.equalsIgnoreCase(String.valueOf(row.get("sbd")))) {
                return row;
            }
        }
        CandidateDTO reg = candidateDAO.getBySessionAndSbd(sessionId, normalized);
        if (reg == null && !normalized.contains("-") && !registrations.isEmpty()) {
            String license = registrations.get(0).getLicenseCode();
            if (license != null) {
                reg = candidateDAO.getBySessionAndSbd(sessionId, license + "-" + normalized);
            }
        }
        if (reg == null) {
            return null;
        }
        return toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()), sectionType, sectionName);
    }

    private static String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    private static String formatScore(Integer score) {
        if (score == null) {
            return "-";
        }
        return score + "/100";
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String normalizeSearch(String searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean matchesCandidateSearch(CandidateDTO reg, String search) {
        String q = search.toLowerCase();
        return containsIgnoreCase(reg.getFullName(), q)
                || containsIgnoreCase(reg.getSbd(), q)
                || containsIgnoreCase(reg.getGovIdNo(), q)
                || containsIgnoreCase(String.valueOf(reg.getCandidateNo()), q);
    }

    private static Integer resolveTheoryCorrectScore(CandidateDTO reg, ExaminerAnswerStats stats) {
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
