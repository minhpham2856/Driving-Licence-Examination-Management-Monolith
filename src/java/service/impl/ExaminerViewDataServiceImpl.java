package service.impl;




import enums.SectionType;

import controller.examiner.ExaminerScoreEntryQueue;

import dto.examiner.ExaminerSlotDTO;

import dao.AuditLogDAO;
import dao.ExamCandidateVehicleDAO;
import dao.CandidateDAO;
import dao.ExaminerSessionDataDAO;
import dao.TheoryPaperDAO;
import dao.impl.AuditLogDAOImpl;
import dao.impl.ExamCandidateVehicleDAOImpl;
import dao.impl.CandidateDAOImpl;
import dao.impl.ExaminerSessionDataDAOImpl;
import dao.impl.TheoryPaperDAOImpl;

import dto.user.AuditDTO;

import dto.candidate.CandidateDTO;

import dto.examiner.ExaminerAnswerStatsDTO;

import dto.examiner.ExaminerPaperStateDTO;

import dto.score.TheoryPaperAnswerDTO;

import service.ExaminerSessionContextService;
import service.ExaminerViewDataService;
import util.AuditLogViewHelper;

import util.ExaminerCandidateSort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Implementation of ExaminerViewDataService providing comprehensive dashboard data preparation
public class ExaminerViewDataServiceImpl implements ExaminerViewDataService {

    // Date formatters for view presentation
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    
    // Pagination settings for the audit log view
    private static final int AUDIT_PAGE_SIZE = 20;
    
    // Theory exam constants
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;

    // DAO dependencies for fetching raw data
    private final CandidateDAO candidateDAO = new CandidateDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final ExaminerSessionDataDAO sessionDataDAO = new ExaminerSessionDataDAOImpl();
    private final ExamCandidateVehicleDAO vehicleDAO = new ExamCandidateVehicleDAOImpl();

    // Attaches all dashboard data to the request (no search filter)
    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
    }

    // Attaches all dashboard data to the request, with search filter and sorting.
    @Override
    public void attachToRequest(HttpServletRequest request, int sessionId, String sbdParam, String searchQuery) {
        // Synchronise current section statuses from external systems
        candidateDAO.syncSectionStatusesForSession(sessionId);
        
        // Fetch raw registrations and mapped state data
        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperStateDTO> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStatsDTO> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        String examCode = sessionDataDAO.findExamCodeBySessionId(sessionId);
        
        // Normalize the search keyword (null if blank)
        String search = normalizeSearch(searchQuery);
        
        // Parse sorting specification from request parameters
        ExaminerCandidateSort.Spec sortSpec = ExaminerCandidateSort.parse(
                request.getParameter("sort"), request.getParameter("dir"));

        // Set search and sort attributes for the UI to persist state
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("searchActive", search != null);
        request.setAttribute("sortBy", sortSpec.getColumn());
        request.setAttribute("sortDir", sortSpec.isAscending() ? "asc" : "desc");

        // Determine current section scope from the context
        SectionType sectionType = resolveSectionType(request);
        String sectionName = resolveSectionName(request);

        // List to hold the mapped candidate view rows
        List<Map<String, Object>> candidates = new ArrayList<>();
        
        // Summary counters
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;

        // Iterate through each candidate to build their view row and aggregate stats
        for (CandidateDTO reg : registrations) {
            // Apply search filter if active
            if (search != null && !matchesCandidateSearch(reg, search)) {
                continue;
            }
            
            // Build the view row map for the candidate
            ExaminerPaperStateDTO paper = paperStates.get(reg.getId());
            ExaminerAnswerStatsDTO stats = answerStats.get(reg.getId());
            Map<String, Object> row = toViewRow(reg, paper, stats, sectionType, sectionName);
            candidates.add(row);

            // Accumulate statistics based on the computed status and result
            String status = (String) row.get("status");
            if ("done".equals(status)) {
                done++;
            } else if ("testing".equals(status)) {
                testing++;
            } else if (!"absent".equals(status) && !"suspended".equals(status) && !"awaiting".equals(status)) {
                pending++;
            }
            
            // Increment passed/failed counters
            if (Boolean.TRUE.equals(row.get("passed"))) {
                passed++;
            } else if ("done".equals(status) && !"-".equals(row.get("resultLabel"))) {
                failed++;
            }
        }

        // Apply sorting based on the parsed spec
        ExaminerCandidateSort.sort(candidates, sortSpec);
        
        // Attach processed collections to the request
        request.setAttribute("candidates", candidates);
        request.setAttribute("examSummary", buildSummary(examCode, candidates.size(), done, testing, pending, passed, failed));

        // Resolve the actively selected candidate for the details panel
        Map<String, Object> selected = resolveCandidate(candidates, registrations, sessionId, sbdParam,
                paperStates, answerStats, sectionType, sectionName);
                
        // Fallback: If search excluded the selected candidate, force load them
        if (selected == null && sbdParam != null && !sbdParam.isBlank()) {
            CandidateDTO reg = findRegistration(sessionId, sbdParam);
            if (reg != null) {
                selected = toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                        sectionType, sectionName);
            }
        }
        
        // Expose the active candidate to the UI
        if (selected != null) {
            request.setAttribute("candidate", selected);
        }
    }

    // Loads candidate view-rows for the default theory section type.
    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, SectionType.THEORY, null);
    }

    // Loads candidate view-rows for a specific section type and name.
    @Override
    public List<Map<String, Object>> loadCandidateRows(int sessionId, SectionType sectionType,
            String sectionName) {
        // Sync the latest status from upstream systems
        candidateDAO.syncSectionStatusesForSession(sessionId);
        
        // Fetch registrations and their states
        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperStateDTO> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStatsDTO> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        
        // Map DTOs to UI-friendly maps
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (CandidateDTO reg : registrations) {
            candidates.add(toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                    sectionType, sectionName));
        }
        return candidates;
    }

    // Builds a summary map for the given session and section type.
    @Override
    public Map<String, Object> buildCandidateSummary(int sessionId, SectionType sectionType,
            String sectionName) {
        // Load the fully mapped rows
        List<Map<String, Object>> candidates = loadCandidateRows(sessionId, sectionType, sectionName);
        String examCode = sessionDataDAO.findExamCodeBySessionId(sessionId);
        
        // Counters
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;
        
        // Calculate aggregations
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

    // Attaches audit logs to the request, paginated.
    @Override
    public void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam) {
        attachAuditLogs(request, sessionId, pageParam, null);
    }

    // Attaches audit logs to the request with optional search filtering and pagination.
    @Override
    public void attachAuditLogs(HttpServletRequest request, int sessionId, String pageParam, String searchQuery) {
        // Parse requested page number safely, defaulting to 1
        int page = 1;
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        
        // Normalise search keyword
        String search = normalizeSearch(searchQuery);
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("searchActive", search != null);

        // Fetch paginated logs
        List<AuditDTO> logs = auditLogDAO.getLogsForSessionPaginated(sessionId, page, AUDIT_PAGE_SIZE, search);
        int total = auditLogDAO.getLogsCountForSession(sessionId, search);
        
        // Lookup map to translate Registration IDs to SBD numbers in the UI
        Map<Integer, String> sbdByRecordId = buildSbdLookup(sessionId);
        List<Map<String, Object>> rows = new ArrayList<>();
        
        // Process raw logs into UI models
        for (AuditDTO log : logs) {
            for (Map<String, Object> row : AuditLogViewHelper.toViewRows(log, sbdByRecordId)) {
                Timestamp changedAt = log.getChangedAt();
                // Format timestamps into separate time and date strings
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
        
        // Expose pagination metrics
        request.setAttribute("auditLogs", rows);
        request.setAttribute("auditPage", page);
        int totalPages = Math.max(1, (total + AUDIT_PAGE_SIZE - 1) / AUDIT_PAGE_SIZE);
        request.setAttribute("auditTotalPages", totalPages);
        request.setAttribute("auditTotal", total);
    }

    // Attaches score-entry data (queue, devices, score deductions, active candidate) to the request.
    @Override
    public void attachScoreEntry(HttpServletRequest request, int sessionId, String sbdParam) {
        HttpSession httpSession = request.getSession(false);
        SectionType sectionType = resolveSectionType(request);
        String sectionName = resolveSectionName(request);

        // Fetch raw state data
        List<CandidateDTO> registrations = candidateDAO.getCandidatesBySession(sessionId);
        Map<Integer, ExaminerPaperStateDTO> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStatsDTO> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);

        List<String> eligibleSbds = new ArrayList<>();
        Map<String, Map<String, Object>> rowBySbd = new LinkedHashMap<>();
        Map<String, Map<String, Object>> absentRowBySbd = new LinkedHashMap<>();

        // Segregate candidates into eligible queue and absent subset
        for (CandidateDTO reg : registrations) {
            // Exclude suspended candidates entirely
            if (reg.isSuspended()) {
                continue;
            }
            if (reg.isAbsent()) {
                // Determine if absent candidate should be visible based on prerequisite rules
                boolean sectionMatch = isSectionMatchForDisplay(sessionId, reg, sectionType, sectionName);
                if (sectionMatch) {
                    absentRowBySbd.put(reg.getSbd(), toScoreQueueRow(reg, sectionName));
                }
                continue;
            }
            // Exclude if already done or failed prerequisite
            if (!isScoreQueueEligibleInternal(sessionId, reg, sectionType, sectionName)) {
                continue;
            }
            // Candidate is eligible for the queue
            Map<String, Object> row = toScoreQueueRow(reg, sectionName);
            eligibleSbds.add(reg.getSbd());
            rowBySbd.put(reg.getSbd(), row);
        }

        // Synchronise the current set of eligible SBDs with the session queue manager
        if (httpSession != null) {
            ExaminerScoreEntryQueue.syncQueue(httpSession, sessionId, eligibleSbds);
        }

        // Fetch queue states
        String activeSbd = httpSession != null ? ExaminerScoreEntryQueue.getActiveSbd(httpSession, sessionId) : null;
        String calledSbd = httpSession != null ? ExaminerScoreEntryQueue.getCalledSbd(httpSession, sessionId) : null;

        // Force an override if an SBD was specifically requested via URL param
        if (sbdParam != null && !sbdParam.isBlank()) {
            activeSbd = sbdParam.trim();
            if (httpSession != null) {
                ExaminerScoreEntryQueue.setActiveSbd(httpSession, sessionId, activeSbd);
            }
        }

        // Reconstruct the ordered queue from the session
        List<Map<String, Object>> scoreQueue = new ArrayList<>();
        List<String> queueOrder = httpSession != null
                ? ExaminerScoreEntryQueue.getQueue(httpSession, sessionId)
                : eligibleSbds;
                
        // Add eligible candidates
        for (String sbd : queueOrder) {
            Map<String, Object> row = rowBySbd.get(sbd);
            if (row == null) {
                continue;
            }
            row.put("active", sbd.equals(activeSbd));
            row.put("called", sbd.equals(calledSbd));
            scoreQueue.add(row);
        }
        
        // Append absent candidates at the bottom
        for (Map<String, Object> absentRow : absentRowBySbd.values()) {
            absentRow.put("active", false);
            absentRow.put("called", false);
            scoreQueue.add(absentRow);
        }

        request.setAttribute("scoreQueue", scoreQueue);
        request.setAttribute("scoreQueueTotal", scoreQueue.size());

        // Process licence class and available vehicles
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

        // Distribute vehicles using round-robin assignment based on current queue order
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
        
        Integer activeCandidateId = null;
        if (activeSbd != null) {
            for (CandidateDTO reg : registrations) {
                if (activeSbd.equals(reg.getSbd())) {
                    activeCandidateId = reg.getId();
                    break;
                }
            }
        }
        
        // Determine if redistribution is necessary due to vehicle availability changes
        boolean forceRedistribute = false;
        if (httpSession != null && !availableDeviceIds.isEmpty()) {
            String sessionAttrKey = "availableDeviceIds_" + sessionId;
            @SuppressWarnings("unchecked")
            List<Integer> prevAvailableDeviceIds = (List<Integer>) httpSession.getAttribute(sessionAttrKey);
            if (prevAvailableDeviceIds == null || !prevAvailableDeviceIds.equals(availableDeviceIds)) {
                forceRedistribute = true;
                httpSession.setAttribute(sessionAttrKey, new ArrayList<>(availableDeviceIds));
            }
        }
        if (!availableDeviceIds.isEmpty()) {
            vehicleDAO.syncRoundRobinAssignments(sessionId, candidateIdsInOrder, availableDeviceIds, activeCandidateId, forceRedistribute);
        }

        // Attach vehicle details to each queue row
        Map<Integer, Map<String, Object>> assignments = vehicleDAO.findAssignmentDetailsBySession(sessionId);
        for (Map<String, Object> row : scoreQueue) {
            attachVehicleToQueueRow(row, sbdToCandidateId, assignments);
        }
        request.setAttribute("sessionVehicles", sessionDevices);

        // Load section-specific score deduction rules
        Integer examSectionId = sessionDataDAO.findExamSectionIdForSession(sessionId);
        List<Map<String, Object>> scoreDeductions = examSectionId != null
                ? sessionDataDAO.findScoreDeductionsBySectionId(examSectionId)
                : sessionDataDAO.findScoreDeductions();
        request.setAttribute("examSectionId", examSectionId);

        // Calculate score and load applied deductions for the active candidate
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
            // Find violations already applied
            for (Map<String, Object> applied : candidateDAO.findAppliedScoreDeductions(
                    activeReg.getId(), sessionId)) {
                Object idObj = applied.get("id");
                if (idObj instanceof Integer) {
                    Integer id = (Integer) idObj;
                    appliedById.put(id, applied);
                    if (Boolean.TRUE.equals(applied.get("critical"))) {
                        scoreDisqualified = true; // Critical violations force disqualification
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
                // Calculate display score actively based on deductions
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

        // Merge applied violation counts into the master deductions list
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
        
        // Export score panel data to request
        request.setAttribute("scoreDeductions", scoreDeductions);
        request.setAttribute("scoreDisqualified", scoreDisqualified);
        request.setAttribute("currentScore", displayScore);

        // Attach the fully populated active candidate row to the request
        if (activeSbd != null) {
            if (rowBySbd.containsKey(activeSbd)) {
                Map<String, Object> selected = new LinkedHashMap<>(rowBySbd.get(activeSbd));
                if (!"awaiting".equals(selected.get("status"))) {
                    selected.put("status", "testing");
                    selected.put("statusLabel", "Đang thi");
                }
                attachVehicleToQueueRow(selected, sbdToCandidateId, assignments);
                request.setAttribute("candidate", selected);
                request.setAttribute("candidateVehicleId", selected.get("vehicleId"));
            } else if (absentRowBySbd.containsKey(activeSbd)) {
                Map<String, Object> selected = new LinkedHashMap<>(absentRowBySbd.get(activeSbd));
                attachVehicleToQueueRow(selected, sbdToCandidateId, assignments);
                request.setAttribute("candidate", selected);
                request.setAttribute("candidateVehicleId", selected.get("vehicleId"));
            } else {
                for (CandidateDTO reg : registrations) {
                    if (activeSbd.equals(reg.getSbd())) {
                        Map<String, Object> selected = toScoreQueueRow(reg, sectionName);
                        attachVehicleToQueueRow(selected, sbdToCandidateId, assignments);
                        request.setAttribute("candidate", selected);
                        request.setAttribute("candidateVehicleId", selected.get("vehicleId"));
                        break;
                    }
                }
            }
        }
    }

    // Attaches result-details-edit data (score deductions, current score) to the request.
    @Override
    public void attachResultDetailsEdit(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);

        CandidateDTO reg = findRegistration(sessionId, sbdParam);
        if (reg == null) {
            return;
        }

        String sectionName = resolveSectionName(request);
        Integer examSectionId = sessionDataDAO.findExamSectionIdForSession(sessionId);
        List<Map<String, Object>> scoreDeductions = examSectionId != null
                ? sessionDataDAO.findScoreDeductionsBySectionId(examSectionId)
                : sessionDataDAO.findScoreDeductions();

        Map<Integer, Map<String, Object>> appliedById = new LinkedHashMap<>();
        boolean scoreDisqualified = false;
        double displayScore = 100;

        if (examSectionId != null) {
            // Determine active score or compute from deductions
            for (Map<String, Object> applied : candidateDAO.findAppliedScoreDeductions(reg.getId(), sessionId)) {
                Object idObj = applied.get("id");
                if (idObj instanceof Integer) {
                    Integer id = (Integer) idObj;
                    appliedById.put(id, applied);
                    if (Boolean.TRUE.equals(applied.get("critical"))) {
                        scoreDisqualified = true;
                    }
                }
            }
            Integer sectionScore = resolveScoreForSection(reg, sectionName);
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

        // Merge occurrences back into the list
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
    }

    // Checks whether a candidate is eligible to appear in the score-entry queue.
    @Override
    public boolean isScoreQueueEligible(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName) {
        return isScoreQueueEligibleInternal(sessionId, reg, sectionType, sectionName);
    }

    // Internal eligibility check: not absent/suspended, SCORE_BASED section, not done, prerequisite met.
    private boolean isScoreQueueEligibleInternal(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName) {
        // Exclude suspended/absent
        if (reg == null || reg.isSuspended() || reg.isAbsent()) {
            return false;
        }
        // Exclude non-score sections
        if (sectionType != SectionType.SCORE_BASED) {
            return false;
        }
        // Exclude done candidates
        if (enums.CandidateStatus.isCandidateDone(reg.getSectionStatus())) {
            return false;
        }
        // Validate sequential progression prerequisites
        String normalized = sectionName != null ? sectionName.trim().toLowerCase() : "";
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return "passed".equalsIgnoreCase(reg.getPracticalPassed());
        }
        return "passed".equalsIgnoreCase(reg.getTheoryPassed());
    }

    // Checks if an absent candidate should be displayed in the score-entry queue (i.e., met prerequisites).
    private static boolean isSectionMatchForDisplay(int sessionId, CandidateDTO reg,
            SectionType sectionType, String sectionName) {
        if (reg == null || sectionType != SectionType.SCORE_BASED) {
            return false;
        }
        String normalized = sectionName != null ? sectionName.trim().toLowerCase() : "";
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return "passed".equalsIgnoreCase(reg.getPracticalPassed());
        }
        return "passed".equalsIgnoreCase(reg.getTheoryPassed());
    }

    // Builds a lightweight score-entry queue row for a candidate.
    private static Map<String, Object> toScoreQueueRow(CandidateDTO reg, String sectionName) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sbd", reg.getSbd());
        row.put("fullName", nullToDash(reg.getFullName()));
        row.put("governmentId", nullToDash(reg.getGovIdNo()));
        row.put("active", false);
        row.put("called", false);

        Integer score = resolveScoreForSection(reg, sectionName);
        row.put("examScore", score != null ? score : "-");

        // Determine descriptive status for the score queue list
        String sectionStatus = reg.getSectionStatus();
        String status;
        String statusLabel;
        if (reg.isAbsent()) {
            status = "absent";
            statusLabel = "Vắng";
        } else if (reg.isSuspended()) {
            status = "suspended";
            statusLabel = "Đình chỉ";
        } else if (enums.CandidateStatus.isCandidateDone(sectionStatus)) {
            status = "done";
            statusLabel = "Đã thi";
        } else if (enums.CandidateStatus.isCandidateAwaitingSignature(sectionStatus)) {
            status = "awaiting";
            statusLabel = "Chờ ký";
        } else if (score != null) {
            status = "awaiting";
            statusLabel = "Chờ ký";
        } else {
            status = "pending";
            statusLabel = "Chờ thi";
        }
        row.put("status", status);
        row.put("statusLabel", statusLabel);

        row.put("awaitingSignature", enums.CandidateStatus.isCandidateAwaitingSignature(sectionStatus));
        row.put("signaturePrinted", reg.isSignaturePrinted());
        row.put("completeEligible", enums.CandidateStatus.isCandidateAwaitingSignature(sectionStatus)
                && reg.isSignaturePrinted()
                && !reg.isAbsent() && !reg.isSuspended());

        return row;
    }

    // Attaches violation data (score deductions and violation reason options) to the request.
    @Override
    public void attachViolation(HttpServletRequest request, int sessionId, String sbdParam) {
        attachToRequest(request, sessionId, sbdParam, null);
        request.setAttribute("scoreDeductions", sessionDataDAO.findScoreDeductions());
        request.setAttribute("violationReasons", enums.ViolationReason.violationOptionList());
        
        if (sbdParam == null || sbdParam.isBlank()) {
            request.setAttribute("candidateViolations", List.of());
            return;
        }
        // Filter violations for the specific candidate
        List<Map<String, Object>> applied = new ArrayList<>();
        for (Map<String, Object> row : sessionDataDAO.findScoreViolationRows(sessionId)) {
            if (sbdParam.equals(String.valueOf(row.get("sbd")))) {
                applied.add(row);
            }
        }
        request.setAttribute("candidateViolations", applied);
    }

    // Attaches device/vehicle data to the request, filtered by search query and licence class.
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

        // Serve raw list if no search filter
        if (search == null) {
            request.setAttribute("devices", sessionDevices);
            return;
        }

        // Apply textual search filter
        List<Map<String, Object>> filtered = new ArrayList<>();
        String needle = search.toLowerCase();
        for (Map<String, Object> device : sessionDevices) {
            if (matchesDeviceSearch(device, needle)) {
                filtered.add(device);
            }
        }
        request.setAttribute("devices", filtered);
    }

    // Builds a filtered list of session devices (vehicles or computers) based on the section type.
    private List<Map<String, Object>> buildSessionDevices(int sessionId, String licenceClass, boolean vehiclesOnly) {
        List<Map<String, Object>> all = sessionDataDAO.findDevicesBySessionId(sessionId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> device : all) {
            String type = device.get("type") != null ? String.valueOf(device.get("type")) : "";
            // Filter by device type (car vs computer) and licence classification
            boolean include = vehiclesOnly
                    ? enums.DeviceType.isVehicle(type)
                    && enums.DeviceType.matchesLicence(licenceClass, type)
                    : enums.DeviceType.isComputer(type);
            if (include) {
                util.FormatUtil.enrichDeviceRow(device, licenceClass);
                result.add(device);
            }
        }
        return result;
    }

    // Resolves the licence class from the first registration, defaulting to "B".
    private static String resolveLicenceClass(List<CandidateDTO> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return "B";
        }
        String licence = registrations.get(0).getLicenseCode();
        return licence != null && !licence.isBlank() ? licence.trim() : "B";
    }

    // Attaches vehicle assignment data to a queue row.
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
        // Locate candidate ID from SBD
        Integer candidateId = sbdToCandidateId.get(String.valueOf(sbdObj));
        if (candidateId == null) {
            return;
        }
        // Merge vehicle mapping into row
        Map<String, Object> vehicle = assignments.get(candidateId);
        if (vehicle == null) {
            row.put("vehicleName", "-");
            row.put("vehicleIcon", enums.DeviceType.iconFor(null));
            row.put("vehicleId", null);
            return;
        }
        String type = vehicle.get("type") != null ? String.valueOf(vehicle.get("type")) : null;
        row.put("vehicleName", vehicle.get("name") != null ? vehicle.get("name") : "-");
        row.put("vehicleIcon", enums.DeviceType.iconFor(type));
        row.put("vehicleId", vehicle.get("deviceId"));
    }

    // Checks whether a candidate is eligible to be called for their exam section. A candidate must not be absent or suspended, and their status must be "pending".
    @Override
    public boolean isCallEligible(int sessionId, CandidateDTO reg, SectionType sectionType,
            String sectionName) {
        if (reg == null || reg.isAbsent() || reg.isSuspended()) {
            return false;
        }
        Map<Integer, ExaminerPaperStateDTO> paperStates = sessionDataDAO.findPaperStatesBySessionId(sessionId);
        Map<Integer, ExaminerAnswerStatsDTO> answerStats = sessionDataDAO.findAnswerStatsBySessionId(sessionId);
        Map<String, Object> row = toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()),
                sectionType, sectionName);
        return Boolean.TRUE.equals(row.get("callEligible"));
    }

    // Resolves the section type from the request's cached context.
    private static SectionType resolveSectionType(HttpServletRequest request) {
        Object value = request.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        if (value instanceof SectionType) {
            return (SectionType) value;
        }
        return SectionType.THEORY;
    }

    // Resolves the section display name from the request's cached context.
    private static String resolveSectionName(HttpServletRequest request) {
        Object slotObj = request.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = request.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    // Checks if a device row matches a search string.
    private static boolean matchesDeviceSearch(Map<String, Object> device, String needle) {
        for (String key : new String[]{"name", "type", "status", "area"}) {
            Object val = device.get(key);
            if (val != null && String.valueOf(val).toLowerCase().contains(needle)) {
                return true;
            }
        }
        return false;
    }

    // Attaches theory paper answers for a candidate to the request, with filtering and sorting.
    @Override
    public void attachPaperAnswers(HttpServletRequest request, int sessionId, String sbd, String contextPath) {
        List<TheoryPaperAnswerDTO> answers = theoryPaperDAO.getAnswersBySessionAndSbd(sessionId, sbd);
        List<Map<String, Object>> paperAnswers = new ArrayList<>();
        
        // Counters for answer types
        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;
        
        // Filter attribute resolution
        String filter = request.getParameter("filter");
        if (filter == null || filter.isEmpty()) {
            filter = "all";
        }
        
        for (TheoryPaperAnswerDTO answer : answers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNo", answer.getQuestionNo());
            row.put("imageUrl", resolveImageUrl(contextPath, answer.getImageUrl()));
            row.put("correctAnswer", answer.getCorrectAnswer());
            row.put("studentAnswer", answer.getStudentAnswer());
            
            // Check status of individual answer
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
            
            // Only add if matching filter
            if ("all".equals(filter) || filter.equals(answerStatus)) {
                paperAnswers.add(row);
            }
        }
        
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("unansweredCount", unansweredCount);
        summary.put("totalCount", answers.size());
        
        // Apply sorting to the answer table
        String sort = request.getParameter("sort");
        String dir = request.getParameter("dir");
        if (sort != null && !sort.isEmpty()) {
            boolean asc = !"desc".equalsIgnoreCase(dir);
            paperAnswers.sort((m1, m2) -> {
                Object v1 = m1.get(sort);
                Object v2 = m2.get(sort);
                if (v1 == null && v2 == null) {
                    return 0;
                }
                if (v1 == null) {
                    return asc ? 1 : -1;
                }
                if (v2 == null) {
                    return asc ? -1 : 1;
                }
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

    // Returns the minimum number of correct answers required to pass the theory exam.
    @Override
    public int theoryPassThreshold() {
        return THEORY_PASS_CORRECT;
    }

    // Returns the total number of questions in the theory exam.
    @Override
    public int theoryMaxQuestions() {
        return THEORY_MAX_QUESTIONS;
    }

    // Finds a registration (candidate DTO) by session ID and SBD.
    @Override
    public CandidateDTO findRegistration(int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        // Direct database lookup
        CandidateDTO reg = candidateDAO.getBySessionAndSbd(sessionId, sbd.trim());
        if (reg != null) {
            return reg;
        }
        // Fallback: search locally if case mismatch or missing prefix
        List<CandidateDTO> all = candidateDAO.getCandidatesBySession(sessionId);
        String normalized = sbd.trim();
        for (CandidateDTO candidate : all) {
            if (normalized.equalsIgnoreCase(candidate.getSbd())) {
                return candidate;
            }
        }
        // Fallback: missing license class prefix
        if (!normalized.contains("-") && !all.isEmpty()) {
            String license = all.get(0).getLicenseCode();
            if (license != null) {
                return candidateDAO.getBySessionAndSbd(sessionId, license + "-" + normalized);
            }
        }
        return null;
    }

    // Builds a lookup map of candidate record ID to SBD string.
    private Map<Integer, String> buildSbdLookup(int sessionId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (CandidateDTO reg : candidateDAO.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), reg.getSbd());
        }
        return lookup;
    }

    // Checks whether a student answer is effectively unanswered (null, blank, or "-").
    private static boolean isUnanswered(String studentAnswer) {
        return studentAnswer == null || studentAnswer.isBlank() || "-".equals(studentAnswer.trim());
    }

    // Resolves the full image URL from a relative or absolute path.
    private static String resolveImageUrl(String contextPath, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return (contextPath != null ? contextPath : "") + "/assets/imgs/LOGO.png";
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
            return imageUrl.startsWith("/") && contextPath != null ? contextPath + imageUrl : imageUrl;
        }
        return (contextPath != null ? contextPath : "") + "/" + imageUrl.replaceFirst("^/+", "");
    }

    // Builds a summary statistics map.
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

    // Converts a candidate DTO and its paper state/answer stats into a display-ready map for the examiner dashboard.
    private Map<String, Object> toViewRow(CandidateDTO reg, ExaminerPaperStateDTO paper, ExaminerAnswerStatsDTO stats,
            SectionType sectionType, String sectionName) {
        Map<String, Object> row = new LinkedHashMap<>();

        boolean absent = reg.isAbsent();
        boolean suspended = reg.isSuspended();
        String status;
        String statusLabel;
        boolean passed = false;
        String resultLabel = "-";

        if (sectionType == SectionType.SCORE_BASED) {
            // Process Practical / Road Test views
            Integer sectionScore = resolveScoreForSection(reg, sectionName);
            String passStatus = resolvePassStatusForSection(reg, sectionName);
            row.put("examScore", sectionScore != null ? String.valueOf(sectionScore) : "-");

            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            } else {
                applySectionStatus(reg, paper);
                status = mapViewStatus(reg.getSectionStatus());
                statusLabel = enums.CandidateStatus.candidateStatusLabel(reg.getSectionStatus());
            }

            // Determine pass/fail labelling
            if ("passed".equalsIgnoreCase(passStatus)) {
                passed = true;
                resultLabel = "ĐẠT";
            } else if ("failed".equalsIgnoreCase(passStatus)) {
                passed = false;
                resultLabel = "TRƯỢT";
            } else if (sectionScore != null && enums.CandidateStatus.isCandidateDone(reg.getSectionStatus())) {
                passed = sectionScore >= 80;
                resultLabel = passed ? "ĐẠT" : "TRƯỢT";
            }
        } else {
            // Process Theory views
            if (absent) {
                status = "absent";
                statusLabel = "Vắng thi";
            } else {
                applySectionStatus(reg, paper);
                status = mapViewStatus(reg.getSectionStatus());
                statusLabel = enums.CandidateStatus.candidateStatusLabel(reg.getSectionStatus());
            }

            Integer theoryCorrectScore = resolveTheoryCorrectScore(reg, stats);
            // Deduce pass status based on correct answers and pass threshold
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

            // Map stats for theory columns
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

        // Apply suspension overrides
        if (suspended) {
            status = "suspended";
            statusLabel = "Đình chỉ";
        }

        // Map core fields
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
        
        // Map score snapshots
        row.put("scoreTheory", formatScore(reg.getTheoryScore()));
        row.put("scorePractical", formatScore(reg.getPracticalScore()));
        row.put("scoreRoadLayout", "-"); // Deprecated placeholder
        row.put("scoreOnRoad", formatScore(reg.getRoadTestScore()));
        
        row.put("email", reg.getEmail() != null ? reg.getEmail() : "");
        row.put("phoneNo", reg.getPhoneNo() != null ? reg.getPhoneNo() : "");
        row.put("dobRaw", formatDateRaw(reg.getDateOfBirth()));
        row.put("genderValue", reg.isGender() ? "1" : "0");
        row.put("absent", absent);
        row.put("suspended", suspended);
        row.put("sectionStatus", reg.getSectionStatus());
        row.put("awaitingSignature", enums.CandidateStatus.isCandidateAwaitingSignature(reg.getSectionStatus()));
        row.put("signaturePrinted", reg.isSignaturePrinted());
        
        // Compute complex UI action flags
        row.put("completeEligible", enums.CandidateStatus.isCandidateAwaitingSignature(reg.getSectionStatus())
                && !suspended && !absent);
        row.put("callEligible", "pending".equals(status) && !suspended);
        return row;
    }

    // Updates the candidate DTO's sectionStatus based on paper state (started/submitted).
    private static void applySectionStatus(CandidateDTO reg, ExaminerPaperStateDTO paper) {
        String sectionStatus = reg.getSectionStatus();
        if (sectionStatus == null || sectionStatus.isBlank()) {
            reg.setSectionStatus(enums.CandidateStatus.PENDING.getStatus());
            sectionStatus = enums.CandidateStatus.PENDING.getStatus();
        }
        if (enums.CandidateStatus.PENDING.getStatus().equals(sectionStatus) && paper != null) {
            if (paper.isSubmitted()) {
                reg.setSectionStatus("AwaitingSignature");
            } else if (paper.isStarted()) {
                reg.setSectionStatus("Testing");
            }
        }
    }

    // Maps a DB section status constant to a short view-status string.
    private static String mapViewStatus(String sectionStatus) {
        if (sectionStatus == null || sectionStatus.isBlank()) {
            return "pending";
        }
        return switch (sectionStatus) {
            case "Testing" -> "testing";
            case "AwaitingSignature" -> "awaiting";
            case "Done" -> "done";
            default -> "pending";
        };
    }

    // Resolves the numeric score for the section matching the given section name.
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

    // Resolves the pass/fail status string for the section matching the given section name.
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

    // Formats a SQL Date as dd/MM/yyyy, or "-" if null.
    private static String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    // Formats a SQL Date as yyyy-MM-dd (ISO format), or empty string if null.
    private static String formatDateRaw(Date date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }

    // Formats a score as "N/100", or "-" if null.
    private static String formatScore(Integer score) {
        if (score == null) {
            return "-";
        }
        return score + "/100";
    }

    // Returns the value or "-" if null or blank.
    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    // Normalises a search query (trims -> null if blank).
    private static String normalizeSearch(String searchQuery) {
        if (searchQuery == null) {
            return null;
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Checks if a candidate matches a search query by name, SBD, government ID, or number.
    private static boolean matchesCandidateSearch(CandidateDTO reg, String search) {
        String q = search.toLowerCase();
        return containsIgnoreCase(reg.getFullName(), q)
                || containsIgnoreCase(reg.getSbd(), q)
                || containsIgnoreCase(reg.getGovIdNo(), q)
                || containsIgnoreCase(String.valueOf(reg.getCandidateNo()), q);
    }

    // Returns the theory correct count from the DTO or stats object.
    private static Integer resolveTheoryCorrectScore(CandidateDTO reg, ExaminerAnswerStatsDTO stats) {
        if (reg.getTheoryScore() != null && reg.getTheoryScore() <= THEORY_MAX_QUESTIONS) {
            return reg.getTheoryScore();
        }
        if (stats != null && stats.getCorrect() + stats.getWrong() + stats.getUnanswered() > 0) {
            return stats.getCorrect();
        }
        return null;
    }

    // Case-insensitive substring check.
    private static boolean containsIgnoreCase(String value, String queryLower) {
        return value != null && value.toLowerCase().contains(queryLower);
    }

    // Resolves the selected candidate from the candidate list or by DAO lookup.
    private Map<String, Object> resolveCandidate(List<Map<String, Object>> candidates,
            List<CandidateDTO> registrations, int sessionId, String sbdParam,
            Map<Integer, ExaminerPaperStateDTO> paperStates, Map<Integer, ExaminerAnswerStatsDTO> answerStats,
            SectionType sectionType, String sectionName) {
        if (sbdParam == null || sbdParam.isBlank()) {
            return null;
        }
        String normalized = sbdParam.trim();
        // First try to locate in the pre-built view rows
        for (Map<String, Object> row : candidates) {
            if (normalized.equalsIgnoreCase(String.valueOf(row.get("sbd")))) {
                return row;
            }
        }
        // Fallback to direct DB fetch if not matched (e.g. filtered out by search)
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
        // Build map for the fallback candidate
        return toViewRow(reg, paperStates.get(reg.getId()), answerStats.get(reg.getId()), sectionType, sectionName);
    }
}





