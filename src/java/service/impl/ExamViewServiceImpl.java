package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.SessionDAO;
import dao.CandidateAnswerDAO;
import dao.QuestionDAO;
import dao.TheoryPaperDAO;
import dao.impl.CandidateAnswerDAOImpl;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExaminerViewDAOImpl;
import dao.impl.QuestionDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dto.EnrollmentDTO;
import dto.CandidateRowDTO;
import dto.ExamStatsDTO;
import dto.ExamReportDTO;
import dto.InfractionDTO;
import model.Audit;
import model.CandidateAnswer;
import model.Exam;
import model.ExamDevice;
import model.Question;
import model.Session;
import model.TheoryPaper;
import service.ExamViewService;
import service.AuditService;
import util.ExamQueue;
import util.ExamQueue.Lane;
import enums.DeviceStatus;
import enums.DeviceType;
import enums.CandidateStatus;
import enums.SectionType;
import service.RegistrationService;
import enums.ViolationReason;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dao.ExaminerViewDAO;
import dao.DeductionRecordDAO;
import dao.impl.DeductionRecordDAOImpl;
import static util.FormatUtil.text;

// Service implementation to load examminer realted data
public class ExamViewServiceImpl implements ExamViewService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;
    private static final int AUDIT_PAGE_SIZE = 20;
    private final AuditService AuditService = new AuditServiceImpl();

    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final RegistrationService enrollmentistrationService = new RegistrationServiceImpl();
    private final DeductionRecordDAO deductionRecordDAO = new DeductionRecordDAOImpl();

    // Overloaded method to load candidates for a session, defaulting to theory section
    @Override
    public List<CandidateRowDTO> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, true, null, null);
    }

    // Overloaded method to load all candidate rows for a session (no search)
    @Override
    public List<CandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName) {
        return loadCandidateRows(sessionId, isTheory, sectionName, null);
    }

    // Loads candidate rows for a session, optionally filtered by a search keyword.
    // When a keyword is present, only matching enrollments are loaded from the
    // database; otherwise all enrollments for the session are loaded.
    @Override
    public List<CandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName,
            String searchQuery) {
        // Retrieve candidate enrollments for the session, filtered at the DB when searching
        List<EnrollmentDTO> enrollments;
        if (searchQuery != null && !searchQuery.isBlank()) {
            enrollments = enrollmentistrationService.searchCandidatesBySession(sessionId, searchQuery);
        } else {
            enrollments = enrollmentistrationService.getCandidatesBySession(sessionId);
        }

        // Load theory statistics per enrollment (correct/wrong/unanswered counts)
        Map<Integer, int[]> theoryStats = examinerDataDAO.loadTheoryStatsBySession(sessionId);

        // Load section-specific scores if sectionName provided
        Map<Integer, Double> sectionScores = examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName);

        // Load pass/fail flags for each enrollment
        Map<Integer, Boolean> passFlags = examinerDataDAO.loadPassFlagsBySession(sessionId);

        // Format session date for display
        String examDate = formatSessionDate(sessionId);

        // Retrieve licence class of the exam
        String licenceClass = loadLicenceClass(sessionId);

        // Load device names assigned to each enrollment
        Map<Integer, String> deviceNames = examinerDataDAO.loadDeviceNamesBySession(sessionId);
        List<CandidateRowDTO> rows = new ArrayList<>();

        // For each enrollment, build a candidate row DTO and add to list
        for (EnrollmentDTO en : enrollments) {
            rows.add(buildCandidateRow(en, theoryStats, sectionScores, passFlags,
                    examDate, licenceClass, deviceNames));
        }
        return rows;
    }

    // Builds a summary of exam statistics for the session. Counts candidates by
    // status (completed, in-progress, not started) and pass/fail.
    @Override
    public ExamStatsDTO buildCandidateSummary(int sessionId, boolean isTheory, String sectionName) {
        // Load all candidate rows for the session
        List<CandidateRowDTO> rows = loadCandidateRows(sessionId, isTheory, sectionName);
        ExamStatsDTO summary = new ExamStatsDTO();
        int done = 0;        // COMPLETED
        int testing = 0;     // IN_PROGRESS or AWAITING_SIGNATURE
        int pending = 0;     // NOT_STARTED
        int passed = 0;
        int failed = 0;
        // Iterate over rows and tally statuses and results
        for (CandidateRowDTO row : rows) {
            // Count by section status
            if (row.getSectionStatus() != null) {
                switch (row.getSectionStatus()) {
                    case COMPLETED ->
                        done++;
                    case IN_PROGRESS, AWAITING_SIGNATURE ->
                        testing++;
                    case NOT_STARTED ->
                        pending++;
                }
            }
            // Count pass/fail based on passed flag and result label
            if (row.isPassed()) {
                passed++;
            } else if (row.getResultLabel() != null && !"-".equals(row.getResultLabel())) {
                failed++;
            }
        }
        // Retrieve session and exam details for exam code
        Session session = sessionDAO.getById(sessionId);
        Exam exam = session != null ? examDAO.getById(session.getExamId()) : null;
        summary.setTotal(rows.size());
        summary.setDone(done);
        summary.setTesting(testing);
        summary.setPending(pending);
        summary.setPassed(passed);
        summary.setFailed(failed);
        summary.setExamCode(exam != null ? exam.getExamCode() : "-");
        return summary;
    }

    // Builds the end-of-day report for a session: result tallies, per-licence
    // breakdown, candidate list, and top deduction reasons.
    @Override
    public ExamReportDTO buildExamReport(int sessionId) {
        List<CandidateRowDTO> rows = loadCandidateRows(sessionId);
        ExamReportDTO report = new ExamReportDTO();
        int total = 0;
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;
        int a1Count = 0;
        int a1Passed = 0;
        int a1Failed = 0;
        int b2Count = 0;
        int b2Passed = 0;
        int b2Failed = 0;
        for (CandidateRowDTO row : rows) {
            total++;
            if (row.getSectionStatus() != null) {
                switch (row.getSectionStatus()) {
                    case COMPLETED ->
                        done++;
                    case IN_PROGRESS, AWAITING_SIGNATURE ->
                        testing++;
                    case NOT_STARTED ->
                        pending++;
                }
            }
            boolean isA1 = "A1".equalsIgnoreCase(row.getLicenceClass())
                    || "A2".equalsIgnoreCase(row.getLicenceClass());
            boolean isB2 = "B2".equalsIgnoreCase(row.getLicenceClass());
            if (isA1) {
                a1Count++;
            }
            if (isB2) {
                b2Count++;
            }
            if (row.isPassed()) {
                passed++;
                if (isA1) {
                    a1Passed++;
                }
                if (isB2) {
                    b2Passed++;
                }
            } else if (row.getResultLabel() != null && !"-".equals(row.getResultLabel())) {
                failed++;
                if (isA1) {
                    a1Failed++;
                }
                if (isB2) {
                    b2Failed++;
                }
            }
        }
        double passRate = done > 0 ? ((double) passed / done) * 100.0 : 0.0;
        report.setTotalCandidates(total);
        report.setCompletedCount(done);
        report.setTestingCount(testing);
        report.setPendingCount(pending);
        report.setPassedCount(passed);
        report.setFailedCount(failed);
        report.setPassRate(passRate);
        report.setA1Count(a1Count);
        report.setA1Passed(a1Passed);
        report.setA1Failed(a1Failed);
        report.setB2Count(b2Count);
        report.setB2Passed(b2Passed);
        report.setB2Failed(b2Failed);
        report.setCandidateRows(rows);
        report.setTopInfractions(buildInfractionList(3));
        return report;
    }

    // Builds the top deduction reasons list with percentage of total occurrences.
    private List<InfractionDTO> buildInfractionList(int limit) {
        List<Map<String, Object>> raw = deductionRecordDAO.getTopReasons(limit);
        List<InfractionDTO> list = new ArrayList<>();
        int totalOccurrences = 0;
        for (Map<String, Object> row : raw) {
            totalOccurrences += (Integer) row.get("count");
        }
        for (Map<String, Object> row : raw) {
            InfractionDTO item = new InfractionDTO();
            item.setReason((String) row.get("reason"));
            int count = (Integer) row.get("count");
            item.setCount(count);
            double pct = totalOccurrences > 0 ? ((double) count / totalOccurrences) * 100.0 : 0.0;
            item.setPercentage(pct);
            list.add(item);
        }
        return list;
    }

    // Overloaded method to get audit logs data without search query.
    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam) {
        return getAuditLogsData(sessionId, pageParam, null);
    }

    // Fetches paginated audit logs for a session with optional search query.
    // Parses page parameter, calculates total pages, and transforms Audit
    // entities into view-friendly map objects.
    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        int page = 1;
        // Parse page parameter, default to 1 if invalid
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }
        // Get total number of audit logs for session (with optional search)
        int total = AuditService.getLogsCountForSession(sessionId, searchQuery);
        // Calculate total pages
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) AUDIT_PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        // Fetch paginated logs
        List<Audit> logs = AuditService.getLogsForSessionPaginated(sessionId, page, AUDIT_PAGE_SIZE, searchQuery);
        // Load changer names (who performed the action) for each log
        Map<Long, String> changerNames = AuditService.loadChangerNames(logs);
        // Build lookup map from enrollment ID to candidate number (SBD)
        Map<Integer, String> sbdLookup = buildSbdLookup(sessionId);
        List<Map<String, Object>> viewRows = new ArrayList<>();
        // Convert each audit log to view rows
        for (Audit log : logs) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            viewRows.addAll(AuditService.toViewRows(log, changerName, sbdLookup));
        }
        model.put("auditLogs", viewRows);
        model.put("auditPage", page);
        model.put("auditTotalPages", totalPages);
        // Indicate if search is active and include search query
        if (searchQuery != null && !searchQuery.isBlank()) {
            model.put("searchActive", true);
            model.put("searchQuery", searchQuery.trim());
        }
        return model;
    }

    // Gathers detailed paper answers data for a candidate. Compares student
    // answers against correct answers, counts correct/wrong/unanswered. Also
    // includes question metadata and image URLs.
    @Override
    public Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", 0);
        summary.put("correctCount", 0);
        summary.put("wrongCount", 0);
        summary.put("unansweredCount", 0);
        // Find the enrollmentistration for the given candidate number (SBD)
        EnrollmentDTO enrollment = findRegistration(sessionId, sbd);
        if (enrollment == null || enrollment.getEnrollment() == null) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }
        int enrollmentId = enrollment.getEnrollment().getExamEnrollmentId();
        // Retrieve the theory paper for this enrollment
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }
        // Get all candidate answers for this theory paper
        List<CandidateAnswer> answers = candidateAnswerDAO.findByTheoryPaperId(paper.getTheoryPaperId());
        if (answers.isEmpty()) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }
        // Collect all question IDs from answers
        List<Integer> questionIds = new ArrayList<>();
        for (CandidateAnswer answer : answers) {
            questionIds.add(answer.getQuestionId());
        }
        // Load all questions by their IDs into a map for quick lookup
        Map<Integer, Question> questionsById = new HashMap<>();
        for (Question question : questionDAO.findByIds(questionIds)) {
            questionsById.put(question.getQuestionId(), question);
        }
        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;
        // Process each answer
        for (CandidateAnswer answer : answers) {
            Question question = questionsById.get(answer.getQuestionId());
            if (question == null) {
                continue; // skip if question not found
            }
            String studentAnswer = answer.getAnswer();
            boolean unanswered = (studentAnswer == null || studentAnswer.isBlank());
            // Determine if answer is correct (non-empty and matches correct answer)
            boolean correct = !unanswered
                    && question.getCorrectAnswer() != null
                    && question.getCorrectAnswer().equalsIgnoreCase(studentAnswer.trim());
            // Tally counts
            if (unanswered) {
                unansweredCount++;
            } else if (correct) {
                correctCount++;
            } else {
                wrongCount++;
            }
            // Build row for this question
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNo", question.getQuestionNumber());
            row.put("imageUrl", question.getImageUrl());
            row.put("correctAnswer", question.getCorrectAnswer());
            row.put("studentAnswer", unanswered ? "—" : studentAnswer.trim().toUpperCase());
            row.put("unanswered", unanswered);
            row.put("correct", correct);
            row.put("answerStatus", unanswered ? "skipped" : (correct ? "correct" : "wrong"));
            rows.add(row);
        }
        // Sort rows by question number
        rows.sort((a, b) -> Integer.compare(
                ((Number) a.get("questionNo")).intValue(),
                ((Number) b.get("questionNo")).intValue()));
        // Update summary
        summary.put("totalCount", rows.size());
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("unansweredCount", unansweredCount);
        model.put("paperAnswers", rows);
        model.put("paperSummary", summary);
        return model;
    }

    // Returns the pass threshold for theory exam.
    @Override
    public int theoryPassThreshold() {
        return THEORY_PASS_CORRECT;
    }

    // Returns the maximum number of theory questions.
    @Override
    public int theoryMaxQuestions() {
        return THEORY_MAX_QUESTIONS;
    }

    // Finds the enrollmentistration DTO for a given session and candidate number.
    // Returns null if not found.
    @Override
    public EnrollmentDTO findRegistration(int sessionId, int sbd) {
        if (sbd <= 0) {
            return null;
        }
        // Iterate through all candidates in the session and match by candidate number
        for (EnrollmentDTO enrollment : enrollmentistrationService.getCandidatesBySession(sessionId)) {
            if (enrollment.getCandidateNumber() == sbd) {
                return enrollment;
            }
        }
        return null;
    }

    // Fetches a single candidate row DTO for the given session and candidate
    // number. Uses the same data loading logic as loadCandidateRows but returns
    // only the matching row.
    @Override
    public CandidateRowDTO getCandidateViewRow(int sessionId, int sbd, boolean isTheory, String sectionName) {
        // Try to find in the full list of rows
        for (CandidateRowDTO row : loadCandidateRows(sessionId, isTheory, sectionName)) {
            if (row.getCandidateNumber() == sbd) {
                return row;
            }
        }
        // If not found, fallback to building directly from enrollment
        EnrollmentDTO enrollment = findRegistration(sessionId, sbd);
        if (enrollment == null) {
            return null;
        }
        return buildCandidateRow(enrollment,
                examinerDataDAO.loadTheoryStatsBySession(sessionId),
                examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName),
                examinerDataDAO.loadPassFlagsBySession(sessionId),
                formatSessionDate(sessionId),
                loadLicenceClass(sessionId),
                examinerDataDAO.loadDeviceNamesBySession(sessionId));
    }

    // Prepares data for the score entry screen. Builds a queue of eligible
    // candidates (not absent, suspended, completed, or awaiting signature),
    // orders them by the global ExamQueue, loads session vehicles, and fetches
    // score deductions.
    @Override
    public Map<String, Object> getScoreEntryData(int sessionId, Integer sbdParam, String sectionName) {
        Map<String, Object> model = new HashMap<>();
        boolean isTheory = false; // Score entry typically for practical sections
        List<CandidateRowDTO> allRows = loadCandidateRows(sessionId, isTheory, sectionName);
        List<CandidateRowDTO> scoreQueue = new ArrayList<>();
        // Filter candidates eligible for score entry
        for (CandidateRowDTO row : allRows) {
            EnrollmentDTO enrollment = findRegistration(sessionId, row.getCandidateNumber());
            if (isScoreQueueEligible(sessionId, enrollment, isTheory, sectionName)) {
                scoreQueue.add(row);
            }
        }
        // Determine the exam lane (section type) for queue ordering
        Lane lane = ExamQueue.laneFor(examSectionFromName(sectionName));
        List<Integer> eligibleSbds = new ArrayList<>();
        for (CandidateRowDTO row : scoreQueue) {
            eligibleSbds.add(row.getCandidateNumber());
        }
        // Sync the queue with eligible SBDs
        ExamQueue.sync(lane, eligibleSbds);
        // Order rows according to the queue
        scoreQueue = orderRowsByQueue(scoreQueue, lane);
        model.put("scoreQueue", scoreQueue);
        // Load vehicles available in the session area
        model.put("sessionVehicles", loadSessionVehicles(sessionId));
        // Determine active enrollmentistration if sbdParam provided
        EnrollmentDTO activeReg = null;
        if (sbdParam != null && sbdParam > 0) {
            activeReg = findRegistration(sessionId, sbdParam);
        }
        Integer candidateId = activeReg != null ? activeReg.getId() : null;
        // Load deduction rules and occurrences
        model.put("scoreDeductions", loadScoreDeductions(sectionName, candidateId, sessionId));
        // Add current score and disqualification status
        applyScoreSummary(model, candidateId, sessionId, sectionName);
        // If a specific candidate is requested, add it to the model
        if (sbdParam != null && sbdParam > 0) {
            for (CandidateRowDTO row : allRows) {
                if (row.getCandidateNumber() == sbdParam) {
                    model.put("candidate", row);
                    break;
                }
            }
        }
        return model;
    }

    // Fetches detailed data for editing result details of a candidate. Loads
    // the candidate row, score deductions, and current score summary.
    @Override
    public Map<String, Object> getResultDetailsEditData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        EnrollmentDTO enrollment = null;
        // If candidate number provided, locate and add candidate row to model
        if (sbdParam != null && sbdParam > 0) {
            for (CandidateRowDTO row : loadCandidateRows(sessionId, false, null)) {
                if (row.getCandidateNumber() == sbdParam) {
                    model.put("candidate", row);
                    model.put("singleCandidateList", List.of(row));
                    break;
                }
            }
            enrollment = findRegistration(sessionId, sbdParam);
        }
        Integer candidateId = enrollment != null ? enrollment.getId() : null;
        // Load deductions for the candidate
        model.put("scoreDeductions", loadScoreDeductions(null, candidateId, sessionId));
        // Apply score summary
        applyScoreSummary(model, candidateId, sessionId, null);
        return model;
    }

    // Determines if a candidate is eligible to be placed in the score entry
    // queue. Not eligible if absent, suspended, completed, or awaiting
    // signature.
    @Override
    public boolean isScoreQueueEligible(int sessionId, EnrollmentDTO enrollment, boolean isTheory,
            String sectionName) {
        if (enrollment == null || enrollment.isAbsent() || enrollment.isSuspended()) {
            return false;
        }
        String status = enrollment.getSectionStatus();
        // Must not be COMPLETED or AWAITING_SIGNATURE
        return !CandidateStatus.COMPLETED.getValue().equals(status)
                && !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(status);
    }

    // Prepares data for violation handling view. Includes list of candidates
    // and dropdown of violation reasons.
    @Override
    public Map<String, Object> getViolationData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        // Load all candidates (theory section by default)
        List<CandidateRowDTO> candidates = loadCandidateRows(sessionId, true, null);
        model.put("candidates", candidates);
        // Build options for violation reason dropdown
        model.put("violationReasons", buildViolationReasonOptions());
        // If a specific candidate is selected, add to model
        if (sbdParam != null && sbdParam > 0) {
            for (CandidateRowDTO row : candidates) {
                if (row.getCandidateNumber() == sbdParam) {
                    model.put("candidate", row);
                    break;
                }
            }
        }
        return model;
    }

    // Overloaded method to fetch device data without preferred area.
    @Override
    public Map<String, Object> getDevicesData(int sessionId, String searchQuery) {
        return getDevicesData(sessionId, searchQuery, null);
    }

    // Retrieves active and maintenance exam devices in the session's area(s).
    // Filters by search query if provided, and optionally restricts to a
    // specific area. Returns a list of device rows with status, icon, and area
    // name.
    @Override
    public Map<String, Object> getDevicesData(int sessionId, String searchQuery, Integer preferredAreaId) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> devices = new ArrayList<>();
        LinkedHashMap<Integer, String> areaNames = new LinkedHashMap<>();
        List<Integer> areaIds = new ArrayList<>();
        boolean assignedRoomOnly = preferredAreaId != null && preferredAreaId > 0;
        // Determine which area IDs to include
        if (assignedRoomOnly) {
            areaIds.add(preferredAreaId);
        } else {
            // Get all exam area IDs for the session
            for (Integer areaId : sessionDAO.getExamAreaIds(sessionId)) {
                if (areaId != null && areaId > 0 && !areaIds.contains(areaId)) {
                    areaIds.add(areaId);
                }
            }
            // Fallback to primary area if none found
            if (areaIds.isEmpty()) {
                Integer fallback = loadPrimarySessionAreaId(sessionId);
                if (fallback != null && fallback > 0) {
                    areaIds.add(fallback);
                }
            }
        }
        // For each area, fetch devices and build rows
        for (Integer areaId : areaIds) {
            areaNames.putIfAbsent(areaId, loadAreaName(areaId));
            for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
                // If assigned room only, skip non-computer devices
                if (assignedRoomOnly && !isComputerDevice(device.getDeviceType())) {
                    continue;
                }
                // Apply search filter if present
                if (searchQuery != null && !searchQuery.isBlank()) {
                    String q = searchQuery.trim().toLowerCase();
                    String haystack = (device.getDeviceName() + " " + device.getDeviceType()
                            + " " + areaNames.get(areaId)).toLowerCase();
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

    // --------------------------- Private helper methods ---------------------------
    // Transforms an ExamDevice entity into a map for view rendering. Adds
    // status label, CSS class, and icon.
    private Map<String, Object> toDeviceRow(ExamDevice device, String areaName) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", device.getExamDeviceId());
        row.put("name", device.getDeviceName());
        row.put("type", device.getDeviceType());
        row.put("area", areaName);
        // Determine status (active or maintenance)
        if (device.isActive()) {
            row.put("status", DeviceStatus.ACTIVE.getValue());
            row.put("statusLabel", DeviceStatus.ACTIVE.getValue());
            row.put("statusClass", "device-grid-card--available");
        } else {
            row.put("status", DeviceStatus.MAINTENANCE.getValue());
            row.put("statusLabel", DeviceStatus.MAINTENANCE.getValue());
            row.put("statusClass", "device-grid-card--maintenance");
        }
        row.put("icon", deviceIcon(device.getDeviceType()));
        return row;
    }

    // Retrieves area name by area ID, returns empty string if not found.
    private String loadAreaName(int areaId) {
        model.ExamArea area = examAreaDAO.getById(areaId);
        return area != null && area.getAreaName() != null ? area.getAreaName() : "";
    }

    // Builds a lookup map from enrollment ID to candidate number (SBD) for
    // audit logs.
    private Map<Integer, String> buildSbdLookup(int sessionId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (EnrollmentDTO enrollment : enrollmentistrationService.getCandidatesBySession(sessionId)) {
            lookup.put(enrollment.getId(), String.valueOf(enrollment.getCandidateNumber()));
        }
        return lookup;
    }

    // Checks if a candidate is eligible to be called into the exam room.
    // Candidate must not be suspended or already completed.
    @Override
    public boolean isCallEligible(int sessionId, EnrollmentDTO enrollment, boolean isTheory,
            String sectionName) {
        if (enrollment == null || enrollment.isSuspended()) {
            return false;
        }
        return !CandidateStatus.COMPLETED.getValue().equals(enrollment.getSectionStatus());
    }

    // Orders candidate rows based on the global ExamQueue for the given exam
    // section. Uses the lane associated with the section type.
    @Override
    public List<CandidateRowDTO> orderCandidateRowsByQueue(List<CandidateRowDTO> rows,
            SectionType examSection) {
        return orderRowsByQueue(rows, ExamQueue.laneFor(examSection));
    }

    // Converts a section name string to SectionType enum, defaulting to THEORY.
    private static SectionType examSectionFromName(String sectionName) {
        SectionType section = SectionType.fromValue(sectionName);
        return section != null ? section : SectionType.THEORY;
    }

    // Builds a CandidateRowDTO from an EnrollmentDTO
    private CandidateRowDTO buildCandidateRow(EnrollmentDTO enrollment,
            Map<Integer, int[]> theoryStats, Map<Integer, Double> sectionScores,
            Map<Integer, Boolean> passFlags, String examDate, String licenceClass,
            Map<Integer, String> deviceNames) {
        CandidateRowDTO row = new CandidateRowDTO();
        int enrollmentId = enrollment.getEnrollment() != null ? enrollment.getEnrollment().getExamEnrollmentId() : 0;
        // Determine section status from enrollment
        CandidateStatus sectionStatus = sectionStatusOf(enrollment);
        // Set basic candidate details
        row.setCandidateNumber(enrollment.getCandidateNumber());
        row.setEnrollmentId(enrollmentId);
        row.setFullName(enrollment.getFullName());
        row.setDob(formatDate(enrollment.getDob()));
        row.setGovernmentId(enrollment.getGovIdNo());
        row.setAddress(enrollment.getAddress());
        row.setPhoneNo(enrollment.getPhoneNo());
        row.setSex(enums.Sex.fromDbBit(enrollment.isSex()));
        row.setEmail(enrollment.getEmail());
        row.setLicenceClass(licenceClass);
        row.setReasonForTaking(enrollment.getReasonForTaking());
        row.setExamDate(examDate);
        row.setSectionStatus(sectionStatus);
        // Theory stats: [correct, wrong, unanswered]
        int[] stats = theoryStats.getOrDefault(enrollmentId, new int[]{0, 0, 0});
        row.setCorrect(stats[0]);
        row.setWrong(stats[1]);
        row.setUnanswered(stats[2]);
        // Section score if available
        Double examScore = sectionScores.get(enrollmentId);
        row.setExamScore(examScore != null ? examScore.intValue() : null);
        row.setScoreTheory(stats[0] > 0 ? stats[0] : null);
        row.setScorePractical(null);
        // Pass flag and result label
        Boolean passed = passFlags.get(enrollmentId);
        if (passed == null) {
            row.setPassed(false);
            row.setResultLabel("-");
        } else {
            row.setPassed(passed);
            row.setResultLabel(passed ? "Đạt" : "Trượt");
        }
        // Device (vehicle) name
        Integer deviceId = enrollment.getEnrollment() != null ? enrollment.getEnrollment().getExamDeviceId() : null;
        row.setVehicleName(deviceId != null ? deviceNames.getOrDefault(deviceId, "-") : "-");
        return row;
    }

    // Converts section status string to CandidateStatus enum, defaulting to
    // NOT_STARTED.
    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {
        CandidateStatus status = CandidateStatus.fromValue(enrollment.getSectionStatus());
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }

    // Formats a Date to dd/MM/yyyy
    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    // Formats session start time to string, returns "-" if unavailable.
    private String formatSessionDate(int sessionId) {
        Session session = sessionDAO.getById(sessionId);
        if (session == null || session.getStartTime() == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(session.getStartTime());
        }
    }

    // Loads the licence class string for the exam associated with the session.
    private String loadLicenceClass(int sessionId) {
        Session session = sessionDAO.getById(sessionId);
        if (session == null) {
            return "-";
        }
        return examinerDataDAO.findLicenceClassByExamId(session.getExamId());
    }

    // Loads score deduction rules for a section, and if candidateId/sessionId
    // provided, enriches with occurrence counts and recorded timestamps.
    private List<Map<String, Object>> loadScoreDeductions(String sectionName, Integer candidateId, Integer sessionId) {
        // Fetch deduction rules (list of maps with id, name, points, etc.)
        List<Map<String, Object>> list = examinerDataDAO.loadScoreDeductionRules(sectionName,
                sessionId != null ? sessionId : 0);
        // If we have a valid candidate and session, load actual occurrences
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

    // Applies score summary (currentScore and scoreDisqualified) to the model.
    // Loads from examinerDataDAO.
    private void applyScoreSummary(Map<String, Object> model, Integer candidateId, Integer sessionId,
            String sectionName) {
        Map<String, Object> summary = examinerDataDAO.loadScoreSummary(
                candidateId != null ? candidateId : 0,
                sessionId != null ? sessionId : 0,
                sectionName);
        model.put("currentScore", summary.get("currentScore"));
        model.put("scoreDisqualified", summary.get("scoreDisqualified"));
    }

    // Finds the primary exam area ID for the session.
    private Integer loadPrimarySessionAreaId(int sessionId) {
        return examinerDataDAO.findPrimarySessionAreaId(sessionId);
    }

    // Loads vehicles (devices of type car or motorcycle) within the session's
    // primary area. Returns a list of device rows with status and icon.
    private List<Map<String, Object>> loadSessionVehicles(int sessionId) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        Integer areaId = loadPrimarySessionAreaId(sessionId);
        if (areaId == null || areaId <= 0) {
            return vehicles;
        }
        for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
            String type = device.getDeviceType() != null ? device.getDeviceType().toLowerCase() : "";
            // Filter only vehicles (car, motorcycle, etc.)
            if (!type.contains("car") && !type.contains("motorcycle") && !type.contains("xe")
                    && !type.contains("oto")) {
                continue;
            }
            Map<String, Object> row = toDeviceRow(device, loadAreaName(areaId));
            // Ensure status fields are carried over
            row.put("status", row.get("status"));
            row.put("statusLabel", row.get("statusLabel"));
            row.put("statusClass", row.get("statusClass"));
            vehicles.add(row);
        }
        return vehicles;
    }

    // Reorders candidate rows based on the provided Lane queue. Candidates
    // present in the queue appear first in queue order, followed by the rest.
    private static List<CandidateRowDTO> orderRowsByQueue(List<CandidateRowDTO> rows, Lane lane) {
        List<Integer> order = ExamQueue.asList(lane);
        if (order.isEmpty() || rows.isEmpty()) {
            return rows;
        }
        Map<Integer, CandidateRowDTO> bySbd = new LinkedHashMap<>();
        for (CandidateRowDTO row : rows) {
            bySbd.put(row.getCandidateNumber(), row);
        }
        List<CandidateRowDTO> ordered = new ArrayList<>();
        // Add rows that are in the queue first
        for (Integer sbd : order) {
            CandidateRowDTO row = bySbd.remove(sbd);
            if (row != null) {
                ordered.add(row);
            }
        }
        // Add remaining rows not in the queue
        ordered.addAll(bySbd.values());
        return ordered;
    }

    // Builds a list of violation reason options for dropdown (code and label).
    private static List<Map<String, String>> buildViolationReasonOptions() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : ViolationReason.values()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.getValue());
            row.put("label", reason.getValue());
            list.add(row);
        }
        return list;
    }

    // Checks if device type is COMPUTER.
    private static boolean isComputerDevice(String deviceType) {
        return DeviceType.fromValue(deviceType) == DeviceType.COMPUTER;
    }

    // Returns an icon name based on device type for Material Icons.
    private static String deviceIcon(String deviceType) {
        DeviceType type = DeviceType.fromValue(deviceType);
        if (type == DeviceType.COMPUTER) {
            return "computer";
        }
        if (type == DeviceType.MOTORCYCLE) {
            return "two_wheeler";
        }
        if (type == DeviceType.CAR) {
            return "directions_car";
        }
        if (type == DeviceType.TRUCK) {
            return "local_shipping";
        }
        return "devices";
    }
}
