package examiner.service.impl;

import examiner.dao.ExamAreaDAO;
import examiner.dao.ExamDAO;
import examiner.dao.ExamDeviceDAO;
import examiner.dao.CandidateAnswerDAO;
import examiner.dao.QuestionDAO;
import examiner.dao.TheoryPaperDAO;
import examiner.dao.impl.CandidateAnswerDAOImpl;
import examiner.dao.impl.ExamAreaDAOImpl;
import examiner.dao.impl.ExamDAOImpl;
import examiner.dao.impl.ExamDeviceDAOImpl;
import examiner.dao.impl.ExaminerViewDAOImpl;
import examiner.dao.impl.QuestionDAOImpl;
import examiner.dao.impl.TheoryPaperDAOImpl;
import examiner.dto.EnrollmentDTO;
import examiner.dto.CandidateRowDTO;
import examiner.dto.ExamStatsDTO;
import shared.model.Audit;
import shared.model.CandidateAnswer;
import shared.model.Exam;
import shared.model.ExamDevice;
import shared.model.Question;
import shared.model.TheoryPaper;
import examiner.service.ExamViewService;
import examiner.service.AuditService;
import shared.queue.ExamRoomQueueRegistry;
import shared.enums.DeviceStatus;
import shared.enums.DeviceType;
import shared.enums.CandidateStatus;
import shared.enums.SectionType;
import shared.enums.Sex;
import shared.enums.ViolationReason;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import examiner.dao.ExaminerViewDAO;
import examiner.dao.LicenceDAO;
import examiner.dao.impl.LicenceDAOImpl;
import shared.model.ExamArea;
import shared.model.Licence;
import examiner.service.ProgressService;
import examiner.service.EnrollmentService;

// Loads and shapes examiner view data: candidate rows, stats, audit logs, score entry, and device lists.
public class ExamViewServiceImpl implements ExamViewService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat RAW_DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int THEORY_PASS_CORRECT = 21;
    private static final int THEORY_MAX_QUESTIONS = 25;
    private static final int AUDIT_PAGE_SIZE = 20;
    private final AuditService AuditService = new AuditServiceImpl();

    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExaminerViewDAO viewDAO = new ExaminerViewDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final ProgressService sectionProgressService = new ProgressServiceImpl();

    // Returns DB section type value, defaulting to layout when null.
    private static String sectionTypeValue(SectionType sectionType) {
        return sectionType != null ? sectionType.getValue() : SectionType.LAYOUT.getValue();
    }

    // Loads candidate rows for display or export.
    @Override
    public List<CandidateRowDTO> getAllFilteredByExam(int examId, SectionType sectionType, String searchQuery) {
        // Retrieve candidate enrollments for the exam, filtered at the DB when searching
        List<EnrollmentDTO> enrollments;
        if (searchQuery != null && !searchQuery.isBlank()) {
            enrollments = enrollmentService.getFilteredByExam(examId, searchQuery, sectionType);
        } else {
            enrollments = enrollmentService.getAllByExam(examId, sectionType);
        }

        // Load theory statistics per enrollment (correct/wrong/unanswered)
        Map<Integer, int[]> theoryStats = viewDAO.getAllTheoryStatsByExam(examId);

        // Load section-specific scores for the active section
        Map<Integer, Double> sectionScores = viewDAO.getAllSectionScoresByExam(examId, sectionTypeValue(sectionType));

        // Load pass/fail flags for each enrollment
        Map<Integer, Boolean> passFlags = viewDAO.getAllPassFlagsByExam(examId);

        // Format exam date for display
        String examDate = formatExamDate(examId);

        // Retrieve licence class of the exam
        String licenceClass = loadLicenceClass(examId);

        // Load device names assigned to each enrollment
        Map<Integer, String> deviceNames = viewDAO.getAllDeviceNamesByExam(examId);
        List<CandidateRowDTO> rows = new ArrayList<>();

        // For each enrollment, build a candidate row DTO and add to list
        for (EnrollmentDTO en : enrollments) {
            rows.add(buildCandidateRow(en, theoryStats, sectionScores, passFlags,
                    examDate, licenceClass, deviceNames, sectionType));
        }
        return rows;
    }

    // Builds aggregate counts (total, done, testing, pending, passed, failed) for the exam section.
    @Override
    public ExamStatsDTO getStatsByExam(int examId, SectionType sectionType) {
        // Load all candidate rows for the session
        List<CandidateRowDTO> rows = getAllFilteredByExam(examId, sectionType, null);
        ExamStatsDTO summary = new ExamStatsDTO();
        int done = 0;        // COMPLETED
        int testing = 0;     // IN_PROGRESS or AWAITING_SIGNATURE
        int pending = 0;     // NOT_STARTED
        int passed = 0;
        int failed = 0;

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

            // Count pass/fail 
            if (row.isPassed()) {
                passed++;
            } else if (row.getResultLabel() != null && !"-".equals(row.getResultLabel())) {
                failed++;
            }
        }

        // Retrieve exam details for exam code
        Exam exam = examDAO.get(examId);
        summary.setTotal(rows.size());
        summary.setDone(done);
        summary.setTesting(testing);
        summary.setPending(pending);
        summary.setPassed(passed);
        summary.setFailed(failed);
        summary.setExamCode(exam != null ? exam.getExamCode() : "");
        summary.setExamDate(formatExamDate(examId));
        summary.setLicenceClass(loadLicenceClass(examId));
        return summary;
    }

    // Loads paginated audit log rows for the exam (page 1 when search is unused).
    @Override
    public Map<String, Object> getAuditViewByExam(int examId, String pageParam) {
        return getAuditViewByExam(examId, pageParam, null);
    }

    // Loads paginated audit log rows with optional keyword search.
    @Override
    public Map<String, Object> getAuditViewByExam(int examId, String pageParam, String searchQuery) {
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
        int total = AuditService.countAllByExam(examId, searchQuery);
        // Calculate total pages
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) AUDIT_PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        // Fetch paginated logs
        List<Audit> logs = AuditService.getAllByExam(examId, page, AUDIT_PAGE_SIZE, searchQuery);
        // Load changer names (who performed the action) for each log
        Map<Long, String> changerNames = AuditService.getAllChangerNamesByAudit(logs);
        // Build lookup map from enrollment ID to candidate number (SBD)
        Map<Integer, String> sbdLookup = buildSbdLookup(examId);
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

    // Loads theory paper answer detail and summary counts for one candidate.
    @Override
    public Map<String, Object> getPaperAnswersData(int examId, int sbd, String contextPath) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", 0);
        summary.put("correctCount", 0);
        summary.put("wrongCount", 0);
        summary.put("unansweredCount", 0);
        // Find the enrollment for the given candidate number (SBD) in theory section.
        EnrollmentDTO enrollment = getIfByExamAndSbd(examId, sbd, SectionType.THEORY);
        if (enrollment == null || enrollment.getExamEnrollmentId() <= 0) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }
        int enrollmentId = enrollment.getExamEnrollmentId();
        // Retrieve the theory paper for this enrollment
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }
        // Get all candidate answers for this theory paper
        List<CandidateAnswer> answers = candidateAnswerDAO.getAllByTheoryPaperId(paper.getTheoryPaperId());
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
        for (Question question : questionDAO.getAllByIds(questionIds)) {
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
            row.put("studentAnswer", unanswered ? Character.toString('\u2014') : studentAnswer.trim().toUpperCase());
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

    // Returns the minimum correct-answer count required to pass the theory exam.
    @Override
    public int theoryPassThreshold() {
        return THEORY_PASS_CORRECT;
    }

    // Returns the total number of questions on the theory exam paper.
    @Override
    public int theoryMaxQuestions() {
        return THEORY_MAX_QUESTIONS;
    }

    // Finds enrollment by exam and SBD using default section context.
    @Override
    public EnrollmentDTO getIfByExamAndSbd(int examId, int sbd) {
        return getIfByExamAndSbd(examId, sbd, null);
    }

    // Finds enrollment by exam, SBD, and active section type.
    @Override
    public EnrollmentDTO getIfByExamAndSbd(int examId, int sbd, SectionType sectionType) {
        if (sbd <= 0) {
            return null;
        }
        for (EnrollmentDTO enrollment : enrollmentService.getAllByExam(examId, sectionType)) {
            if (enrollment.getCandidateNumber() == sbd) {
                return enrollment;
            }
        }
        return null;
    }

    // Loads a single candidate row DTO for detail or action screens.
    @Override
    public CandidateRowDTO getCandidateViewRow(int examId, int sbd, SectionType sectionType) {
        // Try to find in the full list of rows
        for (CandidateRowDTO row : getAllFilteredByExam(examId, sectionType, null)) {
            if (row.getCandidateNumber() == sbd) {
                return row;
            }
        }
        // If not found, fallback to building directly from enrollment for this session section.
        EnrollmentDTO enrollment = getIfByExamAndSbd(examId, sbd, sectionType);
        if (enrollment == null) {
            return null;
        }
        return buildCandidateRow(enrollment,
                viewDAO.getAllTheoryStatsByExam(examId),
                viewDAO.getAllSectionScoresByExam(examId, sectionTypeValue(sectionType)),
                viewDAO.getAllPassFlagsByExam(examId),
                formatExamDate(examId),
                loadLicenceClass(examId),
                viewDAO.getAllDeviceNamesByExam(examId),
                sectionType);
    }

    // Builds the score-entry view model: candidates, vehicles, deductions, and active SBD.
    @Override
    public Map<String, Object> getScoreEntryViewByExam(int examId, Integer sbdParam, SectionType sectionType) {
        Map<String, Object> model = new HashMap<>();
        SectionType activeSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        List<CandidateRowDTO> allRows = getAllFilteredByExam(examId, activeSection, null);
        // Keep suspended candidates visible but mark selection for the open SBD.
        for (CandidateRowDTO row : allRows) {
            int rowSbd = row.getCandidateNumber();
            boolean selected = sbdParam != null && sbdParam == rowSbd;
            row.setActive(selected);
            row.setInvoked(selected);
        }
        model.put("scoreQueue", allRows);
        model.put("candidates", allRows);
        model.put("examVehicles", loadExamVehicles(examId));
        String licenceClass = loadLicenceClass(examId);
        model.put("licenceClass", licenceClass);
        model.put("defaultTimerMinutes", defaultTimerMinutesForLicence(licenceClass));
        EnrollmentDTO activeReg = null;
        if (sbdParam != null && sbdParam > 0) {
            activeReg = getIfByExamAndSbd(examId, sbdParam, activeSection);
        }
        Integer candidateId = activeReg != null ? activeReg.getCandidateId() : null;
        if (activeReg != null && activeReg.getExamEnrollmentId() > 0) {
            model.put("candidateVehicleId", activeReg.getExamDeviceId());
        }
        model.put("scoreDeductions", loadScoreDeductions(activeSection, candidateId, examId));
        applyScoreSummary(model, candidateId, examId, activeSection);
        if (sbdParam != null && sbdParam > 0) {
            for (CandidateRowDTO row : allRows) {
                if (row.getCandidateNumber() == sbdParam) {
                    model.put("candidate", row);
                    model.put("singleCandidateList", List.of(row));
                    break;
                }
            }
        }
        return model;
    }

    // Builds result-details edit data for layout section (default section overload).
    @Override
    public Map<String, Object> getResultDetailsViewByExam(int examId, Integer sbdParam) {
        return getResultDetailsViewByExam(examId, sbdParam, SectionType.LAYOUT);
    }

    // Builds result-details edit data for the given section type.
    @Override
    public Map<String, Object> getResultDetailsViewByExam(int examId, Integer sbdParam, SectionType sectionType) {
        SectionType activeSection = sectionType != null ? sectionType : SectionType.LAYOUT;
        Map<String, Object> model = new HashMap<>();
        EnrollmentDTO enrollment = null;
        if (sbdParam != null && sbdParam > 0) {
            for (CandidateRowDTO row : getAllFilteredByExam(examId, activeSection, null)) {
                if (row.getCandidateNumber() == sbdParam) {
                    model.put("candidate", row);
                    model.put("singleCandidateList", List.of(row));
                    break;
                }
            }
            enrollment = getIfByExamAndSbd(examId, sbdParam, activeSection);
        }
        Integer candidateId = enrollment != null ? enrollment.getCandidateId() : null;
        model.put("scoreDeductions", loadScoreDeductions(activeSection, candidateId, examId));
        applyScoreSummary(model, candidateId, examId, activeSection);
        return model;
    }

    // Returns whether a candidate may appear in the score-entry queue.
    @Override
    public boolean isScoreQueueEligible(int examId, EnrollmentDTO enrollment, SectionType sectionType) {
        if (enrollment == null || enrollment.isSuspended()) {
            return false;
        }
        CandidateStatus status = enrollment.getSectionStatus();
        // Must not be COMPLETED or AWAITING_SIGNATURE
        return status != CandidateStatus.COMPLETED
                && status != CandidateStatus.AWAITING_SIGNATURE;
    }

    // Builds violation-handling view data with default theory section.
    @Override
    public Map<String, Object> getViolationViewByExam(int examId, Integer sbdParam) {
        return getViolationViewByExam(examId, sbdParam, SectionType.THEORY);
    }

    // Builds violation-handling view data for the given section type.
    @Override
    public Map<String, Object> getViolationViewByExam(int examId, Integer sbdParam, SectionType sectionType) {
        Map<String, Object> model = new HashMap<>();
        SectionType activeSection = sectionType != null ? sectionType : SectionType.THEORY;
        List<CandidateRowDTO> candidates = getAllFilteredByExam(examId, activeSection, null);
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

    // Lists exam devices with optional search (theory section default).
    @Override
    public Map<String, Object> getDeviceViewByExam(int examId, String searchQuery) {
        return getDeviceViewByExam(examId, searchQuery, null, SectionType.THEORY);
    }

    // Lists exam devices filtered by preferred theory room area.
    @Override
    public Map<String, Object> getDeviceViewByExam(int examId, String searchQuery, Integer preferredAreaId) {
        return getDeviceViewByExam(examId, searchQuery, preferredAreaId, SectionType.THEORY);
    }

    // Lists devices or vehicles depending on section type (computers vs practical vehicles).
    @Override
    public Map<String, Object> getDeviceViewByExam(int examId, String searchQuery, Integer preferredAreaId,
            SectionType sectionType) {
        Map<String, Object> model = new HashMap<>();
        boolean isTheory = sectionType == SectionType.THEORY;
        if (!isTheory) {
            List<Map<String, Object>> vehicles = loadExamVehicles(examId);
            if (searchQuery != null && !searchQuery.isBlank()) {
                String q = searchQuery.trim().toLowerCase();
                List<Map<String, Object>> filtered = new ArrayList<>();
                for (Map<String, Object> row : vehicles) {
                    String haystack = String.valueOf(row.get("name")) + " "
                            + String.valueOf(row.get("type")) + " "
                            + String.valueOf(row.get("area"));
                    if (haystack.toLowerCase().contains(q)) {
                        filtered.add(row);
                    }
                }
                model.put("devices", filtered);
            } else {
                model.put("devices", vehicles);
            }
            return model;
        }

        List<Map<String, Object>> devices = new ArrayList<>();
        LinkedHashMap<Integer, String> areaNames = new LinkedHashMap<>();
        List<Integer> areaIds = new ArrayList<>();
        boolean assignedRoomOnly = preferredAreaId != null && preferredAreaId > 0;
        if (assignedRoomOnly) {
            areaIds.add(preferredAreaId);
        } else {
            ExamArea primaryArea = viewDAO.getIfPrimaryByExam(examId);
            Integer primaryAreaId = primaryArea != null ? primaryArea.getExamAreaId() : null;
            if (primaryAreaId != null && primaryAreaId > 0 && !areaIds.contains(primaryAreaId)) {
                areaIds.add(primaryAreaId);
            }
            if (areaIds.isEmpty()) {
                Integer fallback = loadPrimaryExamAreaId(examId);
                if (fallback != null && fallback > 0) {
                    areaIds.add(fallback);
                }
            }
        }
        for (Integer areaId : areaIds) {
            areaNames.putIfAbsent(areaId, loadAreaName(areaId));
            for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
                if (assignedRoomOnly && !isComputerDevice(device.getDeviceType())) {
                    continue;
                }
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
        ExamArea area = examAreaDAO.get(areaId);
        return area != null && area.getAreaName() != null ? area.getAreaName() : "";
    }

    // Builds a lookup map from enrollment ID to candidate number (SBD) for
    // audit logs.
    private Map<Integer, String> buildSbdLookup(int examId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (EnrollmentDTO enrollment : enrollmentService.getAllByExam(examId)) {
            lookup.put(enrollment.getCandidateId(), String.valueOf(enrollment.getCandidateNumber()));
        }
        return lookup;
    }

    // Returns whether a candidate is eligible for call-board invoke actions.
    @Override
    public boolean isActionEligible(int examId, EnrollmentDTO enrollment, SectionType sectionType) {
        if (enrollment == null || enrollment.isSuspended()) {
            return false;
        }
        return enrollment.getSectionStatus() != CandidateStatus.COMPLETED;
    }

    // Reorders candidate rows to match room/yard queue display order.
    @Override
    public List<CandidateRowDTO> orderCandidateRowsByQueue(List<CandidateRowDTO> rows, int examId,
            int examAreaId, SectionType sectionType) {
        if (rows == null || rows.isEmpty() || examId <= 0 || examAreaId <= 0 || sectionType == null) {
            return rows;
        }
        return orderRowsByQueue(rows, examId, examAreaId, sectionType);
    }

    // Builds a CandidateRowDTO from an EnrollmentDTO
    private CandidateRowDTO buildCandidateRow(EnrollmentDTO enrollment,
            Map<Integer, int[]> theoryStats, Map<Integer, Double> sectionScores,
            Map<Integer, Boolean> passFlags, String examDate, String licenceClass,
            Map<Integer, String> deviceNames, SectionType sectionType) {
        CandidateRowDTO row = new CandidateRowDTO();
        int enrollmentId = enrollment.getExamEnrollmentId();
        // Determine section status from enrollment
        CandidateStatus sectionStatus = sectionStatusOf(enrollment);
        // Set basic candidate details
        row.setCandidateNumber(enrollment.getCandidateNumber());
        row.setEnrollmentId(enrollmentId);
        row.setFullName(enrollment.getFullName());
        row.setDob(formatDate(enrollment.getDateOfBirth()));
        row.setDobRaw(formatDateRaw(enrollment.getDateOfBirth()));
        row.setGovernmentId(enrollment.getGovernmentIdNumber());
        row.setAddress(enrollment.getAddress());
        row.setPhoneNo(enrollment.getPhoneNumber());
        Sex sex = enrollment.isSex() ? Sex.FEMALE : Sex.MALE;
        row.setSex(sex);
        row.setEmail(enrollment.getEmail());
        row.setPhotoImageUrl(enrollment.getPhotoImageUrl());
        row.setLicenceClass(licenceClass);
        row.setReasonForTaking(enrollment.getReasonForTaking());
        row.setExamDate(examDate);
        row.setSectionStatus(sectionStatus);
        row.setSuspended(enrollment.isSuspended());
        row.setStatus(resolveStatusKey(sectionStatus, enrollment.isSuspended()));
        row.setStatusLabel(resolveStatusLabel(sectionStatus, enrollment.isSuspended()));
        row.setSexValue(sex == Sex.FEMALE ? "1" : "0");
        row.setSexLabel(resolveSexLabel(sex));
        row.setAwaitingSignature(sectionStatus == CandidateStatus.AWAITING_SIGNATURE);
        row.setAbsent(enrollment.isAbsent());
        row.setResultPrinted(enrollment.isResultPrinted());
        boolean isTheory = sectionType == SectionType.THEORY;
        boolean practicalSection = sectionType == null || sectionType == SectionType.LAYOUT;
        boolean practicalEntryAllowed = true;
        if (practicalSection && enrollmentId > 0 && enrollment.getCandidateId() > 0) {
            practicalEntryAllowed = sectionProgressService.isPracticalEntryAllowed(
                    enrollmentId,
                    enrollment.isTakeTheory(),
                    enrollment.isTakeLayout());
        }
        row.setPracticalEntryAllowed(practicalEntryAllowed);
        // Action eligibility for examiner table buttons.
        boolean notDone = sectionStatus != CandidateStatus.COMPLETED;
        row.setActionEligible(!enrollment.isSuspended() && notDone);
        row.setViolationEligible(!enrollment.isSuspended());
        row.setMarkPresentEligible(!enrollment.isSuspended()
                && sectionStatus == CandidateStatus.NOT_STARTED
                && practicalEntryAllowed);
        row.setUndoPresentEligible(!enrollment.isSuspended()
                && sectionStatus == CandidateStatus.IN_PROGRESS);
        row.setWrongInfoEligible(!enrollment.isSuspended() && notDone);
        row.setCompleteEligible(sectionStatus == CandidateStatus.AWAITING_SIGNATURE
                && enrollment.isResultPrinted());
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
        // Pass/fail: practical uses score >= 80; theory uses ExamResult.IsPassed / correct count.
        if (practicalSection && examScore != null) {
            boolean passed = examScore >= 80;
            row.setPassed(passed);
            row.setResultLabel(passed ? "Đạt" : "Trượt");
        } else {
            Boolean passed = passFlags.get(enrollmentId);
            if (passed == null && Boolean.TRUE.equals(isTheory) && stats[0] > 0) {
                passed = stats[0] >= THEORY_PASS_CORRECT;
            }
            if (passed == null) {
                row.setPassed(false);
                row.setResultLabel("-");
            } else {
                row.setPassed(passed);
                row.setResultLabel(passed ? "Đạt" : "Trượt");
            }
        }
        // Device (vehicle) name
        Integer deviceId = enrollment.getExamDeviceId();
        row.setVehicleName(deviceId != null ? deviceNames.getOrDefault(deviceId, "-") : "-");
        return row;
    }


    private static String resolveStatusKey(CandidateStatus sectionStatus, boolean suspended) {
        if (suspended) {
            return "suspended";
        }
        if (sectionStatus == null) {
            return "pending";
        }
        if (sectionStatus == CandidateStatus.COMPLETED) {
            return "done";
        }
        if (sectionStatus == CandidateStatus.AWAITING_SIGNATURE) {
            return "awaiting";
        }
        if (sectionStatus == CandidateStatus.IN_PROGRESS) {
            return "testing";
        }
        return "pending";
    }

    private static String resolveStatusLabel(CandidateStatus sectionStatus, boolean suspended) {
        if (suspended) {
            return "Đình chỉ";
        }
        if (sectionStatus == null) {
            return CandidateStatus.NOT_STARTED.getValue();
        }
        return sectionStatus.getValue();
    }

    private static String resolveSexLabel(Sex sex) {
        if (sex == null) {
            return "-";
        }
        return sex.getValue();
    }

    // Converts section status string to CandidateStatus enum, defaulting to
    // NOT_STARTED.
    private static CandidateStatus sectionStatusOf(EnrollmentDTO enrollment) {
        CandidateStatus status = enrollment.getSectionStatus();
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }


    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return formatDate(new Date(timestamp.getTime()));
    }

    private String formatDateRaw(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return formatDateRaw(new Date(timestamp.getTime()));
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

    // Formats a Date to yyyy-MM-dd for HTML date fields.
    private String formatDateRaw(Date date) {
        if (date == null) {
            return "";
        }
        synchronized (RAW_DATE_FMT) {
            return RAW_DATE_FMT.format(date);
        }
    }

    // Formats exam date (dd/MM/yyyy) for display; falls back to start time date.
    private String formatExamDate(int examId) {
        Exam exam = examDAO.get(examId);
        if (exam == null) {
            return "-";
        }
        java.util.Date date = exam.getExamDate();
        if (date == null) {
            date = exam.getStartTime();
        }
        if (date == null) {
            return "-";
        }
        synchronized (DATE_FMT) {
            return DATE_FMT.format(date);
        }
    }

    // Loads the licence class string for the exam.
    private String loadLicenceClass(int examId) {
        Exam exam = examDAO.get(examId);
        if (exam == null || exam.getLicenceId() <= 0) {
            return "";
        }
        Licence licence = licenceDAO.get(exam.getLicenceId());
        if (licence != null && licence.getLicenceClass() != null && !licence.getLicenceClass().isBlank()) {
            return licence.getLicenceClass().trim();
        }
        return "";
    }

    // Practical exam duration presets (minutes) by licence class.
    private static int defaultTimerMinutesForLicence(String licenceClass) {
        if (licenceClass == null || licenceClass.isBlank()) {
            return 20;
        }
        String cls = licenceClass.trim().toUpperCase();
        if ("A1".equals(cls) || "A".equals(cls)) {
            return 10;
        }
        if ("B1".equals(cls) || "B".equals(cls)) {
            return 18;
        }
        if ("D1".equals(cls) || "D2".equals(cls)) {
            return 15;
        }
        if ("C1".equals(cls) || "C".equals(cls) || "D".equals(cls)) {
            return 20;
        }
        return 20;
    }

    // Loads score deduction rules for a section, and if candidateId/examId
    // provided, enriches with occurrence counts and recorded timestamps.
    private List<Map<String, Object>> loadScoreDeductions(SectionType sectionType, Integer candidateId, Integer examId) {
        // Fetch deduction rules (list of maps with id, name, points, etc.)
        List<Map<String, Object>> list = viewDAO.getAllScoreDeductionRulesByExam(sectionTypeValue(sectionType),
                examId != null ? examId : 0);
        // If we have a valid candidate and session, load actual occurrences
        if (candidateId != null && candidateId > 0 && examId != null && examId > 0 && !list.isEmpty()) {
            Map<Integer, int[]> occurrences = viewDAO.getAllDeductionOccurrencesByExam(candidateId, examId);
            Map<Integer, java.util.Date> recordedAt = viewDAO.getAllDeductionRecordedAtByExam(candidateId, examId);
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
    // Loads from viewDAO.
    private void applyScoreSummary(Map<String, Object> model, Integer candidateId, Integer examId,
            SectionType sectionType) {
        Map<String, Object> summary = viewDAO.getIfScoreSummaryByCandidateAndExam(
                candidateId != null ? candidateId : 0,
                examId != null ? examId : 0,
                sectionTypeValue(sectionType));
        model.put("currentScore", summary.get("currentScore"));
        model.put("scoreDisqualified", summary.get("scoreDisqualified"));
    }

    // Finds the primary exam area ID for the session.
    private Integer loadPrimaryExamAreaId(int examId) {
        ExamArea area = viewDAO.getIfPrimaryByExam(examId);
        return area != null ? area.getExamAreaId() : null;
    }

    // Loads vehicles (mô tô / mô tô ba bánh) across all areas linked to the exam.
    private List<Map<String, Object>> loadExamVehicles(int examId) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        List<ExamArea> areas = examAreaDAO.getAreasByExamId(examId);
        if (areas == null || areas.isEmpty()) {
            return vehicles;
        }
        List<Integer> areaIds = new ArrayList<>();
        for (ExamArea area : areas) {
            if (area != null && area.getExamAreaId() > 0) {
                areaIds.add(area.getExamAreaId());
            }
        }
        if (areaIds.isEmpty()) {
            return vehicles;
        }
        for (ExamDevice device : deviceDAO.getAllByAreaIds(areaIds)) {
            if (!isVehicleDevice(device.getDeviceType())) {
                continue;
            }
            vehicles.add(toDeviceRow(device, loadAreaName(device.getExamAreaId())));
        }
        return vehicles;
    }

    // True for motorcycle / tricycle device types used in practical scoring.
    private static boolean isVehicleDevice(String deviceType) {
        DeviceType type = DeviceType.fromValue(deviceType);
        return type == DeviceType.MOTORCYCLE || type == DeviceType.TRICYCLE;
    }

    // Reorders candidate rows based on the room queue (testing first, then waiting).
    private static List<CandidateRowDTO> orderRowsByQueue(List<CandidateRowDTO> rows, int examId,
            int examAreaId, SectionType sectionType) {
        List<Integer> order = ExamRoomQueueRegistry.displayOrder(examId, examAreaId, sectionType);
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
        if (type == DeviceType.TRICYCLE) {
            return "local_shipping";
        }
        return "devices";
    }
}
