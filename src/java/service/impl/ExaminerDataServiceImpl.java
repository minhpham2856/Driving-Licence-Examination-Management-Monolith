package service.impl;

import dao.ExamAreaDAO;
import dao.ExamDAO;
import dao.ExamDeviceDAO;
import dao.ExamEnrollmentDAO;
import dao.SessionDAO;
import dao.CandidateAnswerDAO;
import dao.QuestionDAO;
import dao.TheoryPaperDAO;
import dao.impl.CandidateAnswerDAOImpl;
import dao.impl.ExamAreaDAOImpl;
import dao.impl.ExamDAOImpl;
import dao.impl.ExamDeviceDAOImpl;
import dao.impl.ExamEnrollmentDAOImpl;
import dao.impl.ExaminerViewDAOImpl;
import dao.impl.QuestionDAOImpl;
import dao.impl.SessionDAOImpl;
import dao.impl.TheoryPaperDAOImpl;
import dto.CandidateEnrollmentDTO;
import dto.ExaminerCandidateRowDTO;
import dto.payload.CandidateCallDataDTO;
import dto.payload.CandidateSummaryDTO;
import model.Audit;
import model.CandidateAnswer;
import model.Exam;
import model.ExamDevice;
import model.Question;
import model.Session;
import model.TheoryPaper;
import service.ExaminerDataService;
import service.AuditLogService;
import util.ExamQueue;
import util.ExamQueue.Lane;
import enums.DeviceStatus;
import enums.DeviceType;
import enums.CandidateStatus;
import enums.ExamSection;
import service.ExamRegistrationService;
import enums.ViolationReason;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import dao.ExaminerViewDAO;

public class ExaminerDataServiceImpl implements ExaminerDataService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");
    private static final int THEORY_PASS_CORRECT = 32;
    private static final int THEORY_MAX_QUESTIONS = 35;
    private static final int AUDIT_PAGE_SIZE = 20;
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final ExamEnrollmentDAO enrollmentDAO = new ExamEnrollmentDAOImpl();
    private final TheoryPaperDAO theoryPaperDAO = new TheoryPaperDAOImpl();
    private final CandidateAnswerDAO candidateAnswerDAO = new CandidateAnswerDAOImpl();
    private final QuestionDAO questionDAO = new QuestionDAOImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamDAO examDAO = new ExamDAOImpl();
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    private final ExamAreaDAO examAreaDAO = new ExamAreaDAOImpl();
    private final ExaminerViewDAO examinerDataDAO = new ExaminerViewDAOImpl();
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    @Override
    public CandidateCallDataDTO getCandidateCallData(int sessionId, Integer sbdParam) {
        return getCandidateCallData(sessionId, sbdParam, null);
    }

    @Override
    public CandidateCallDataDTO getCandidateCallData(int sessionId, Integer sbdParam, String searchQuery) {
        CandidateCallDataDTO data = new CandidateCallDataDTO();
        boolean isTheory = true;
        String sectionName = null;
        List<ExaminerCandidateRowDTO> candidates = filterRows(
                loadCandidateRows(sessionId, isTheory, sectionName), searchQuery, data);
        data.setCandidates(candidates);
        data.setCandidateQueue(candidates);
        if (sbdParam != null && sbdParam > 0) {
            CandidateEnrollmentDTO reg = findRegistration(sessionId, sbdParam);
            if (reg != null) {
                data.setCandidate(buildCandidateRow(reg, isTheory,
                        examinerDataDAO.loadTheoryStatsBySession(sessionId),
                        examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName),
                        examinerDataDAO.loadPassFlagsBySession(sessionId),
                        formatSessionDate(sessionId),
                        loadLicenceClass(sessionId),
                        examinerDataDAO.loadDeviceNamesBySession(sessionId)));
            }
        }
        return data;
    }

    @Override
    public List<ExaminerCandidateRowDTO> loadCandidateRows(int sessionId) {
        return loadCandidateRows(sessionId, true, null);
    }

    @Override
    public List<ExaminerCandidateRowDTO> loadCandidateRows(int sessionId, boolean isTheory, String sectionName) {
        List<CandidateEnrollmentDTO> registrations = registrationService.getCandidatesBySession(sessionId);
        Map<Integer, int[]> theoryStats = examinerDataDAO.loadTheoryStatsBySession(sessionId);
        Map<Integer, Double> sectionScores = examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName);
        Map<Integer, Boolean> passFlags = examinerDataDAO.loadPassFlagsBySession(sessionId);
        String examDate = formatSessionDate(sessionId);
        String licenceClass = loadLicenceClass(sessionId);
        Map<Integer, String> deviceNames = examinerDataDAO.loadDeviceNamesBySession(sessionId);
        List<ExaminerCandidateRowDTO> rows = new ArrayList<>();
        for (CandidateEnrollmentDTO reg : registrations) {
            rows.add(buildCandidateRow(reg, isTheory, theoryStats, sectionScores, passFlags,
                    examDate, licenceClass, deviceNames));
        }
        return rows;
    }

    @Override
    public CandidateSummaryDTO buildCandidateSummary(int sessionId, boolean isTheory, String sectionName) {
        List<ExaminerCandidateRowDTO> rows = loadCandidateRows(sessionId, isTheory, sectionName);
        CandidateSummaryDTO summary = new CandidateSummaryDTO();
        int done = 0;
        int testing = 0;
        int pending = 0;
        int passed = 0;
        int failed = 0;
        for (ExaminerCandidateRowDTO row : rows) {
            String status = row.getStatus();
            if ("done".equals(status)) {
                done++;
            } else if ("testing".equals(status) || "awaiting".equals(status)) {
                testing++;
            } else if ("pending".equals(status)) {
                pending++;
            }
            if (row.isPassed()) {
                passed++;
            } else if (row.getResultLabel() != null && !"-".equals(row.getResultLabel())) {
                failed++;
            }
        }
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

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam) {
        return getAuditLogsData(sessionId, pageParam, null);
    }

    @Override
    public Map<String, Object> getAuditLogsData(int sessionId, String pageParam, String searchQuery) {
        Map<String, Object> model = new HashMap<>();
        int page = 1;
        if (pageParam != null && !pageParam.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {
                page = 1;
            }
        }
        int total = auditLogService.getLogsCountForSession(sessionId, searchQuery);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) AUDIT_PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }
        List<Audit> logs = auditLogService.getLogsForSessionPaginated(sessionId, page, AUDIT_PAGE_SIZE, searchQuery);
        Map<Long, String> changerNames = auditLogService.loadChangerNames(logs);
        Map<Integer, String> sbdLookup = buildSbdLookup(sessionId);
        List<Map<String, Object>> viewRows = new ArrayList<>();
        for (Audit log : logs) {
            String changerName = changerNames.getOrDefault(log.getAuditId(), "-");
            viewRows.addAll(auditLogService.toViewRows(log, changerName, sbdLookup));
        }
        model.put("auditLogs", viewRows);
        model.put("auditPage", page);
        model.put("auditTotalPages", totalPages);
        if (searchQuery != null && !searchQuery.isBlank()) {
            model.put("searchActive", true);
            model.put("searchQuery", searchQuery.trim());
        }
        return model;
    }

    @Override
    public Map<String, Object> getPaperAnswersData(int sessionId, int sbd, String contextPath) {
        Map<String, Object> model = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", 0);
        summary.put("correctCount", 0);
        summary.put("wrongCount", 0);
        summary.put("unansweredCount", 0);

        CandidateEnrollmentDTO reg = findRegistration(sessionId, sbd);
        if (reg == null || reg.getEnrollment() == null) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }

        int enrollmentId = reg.getEnrollment().getExamEnrollmentId();
        TheoryPaper paper = theoryPaperDAO.getByExamEnrollmentId(enrollmentId);
        if (paper == null) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }

        List<CandidateAnswer> answers = candidateAnswerDAO.findByTheoryPaperId(paper.getTheoryPaperId());
        if (answers.isEmpty()) {
            model.put("paperAnswers", rows);
            model.put("paperSummary", summary);
            return model;
        }

        List<Integer> questionIds = new ArrayList<>();
        for (CandidateAnswer answer : answers) {
            questionIds.add(answer.getQuestionId());
        }
        Map<Integer, Question> questionsById = new HashMap<>();
        for (Question question : questionDAO.findByIds(questionIds)) {
            questionsById.put(question.getQuestionId(), question);
        }

        int correctCount = 0;
        int wrongCount = 0;
        int unansweredCount = 0;
        for (CandidateAnswer answer : answers) {
            Question question = questionsById.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }
            String studentAnswer = answer.getAnswer();
            boolean unanswered = studentAnswer == null || studentAnswer.isBlank();
            boolean correct = !unanswered
                    && question.getCorrectAnswer() != null
                    && question.getCorrectAnswer().equalsIgnoreCase(studentAnswer.trim());
            if (unanswered) {
                unansweredCount++;
            } else if (correct) {
                correctCount++;
            } else {
                wrongCount++;
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionNo", question.getQuestionNumber());
            row.put("imageUrl", question.getImageUrl());
            row.put("correctAnswer", question.getCorrectAnswer());
            row.put("studentAnswer", unanswered ? "—" : studentAnswer.trim().toUpperCase(Locale.ROOT));
            row.put("unanswered", unanswered);
            row.put("correct", correct);
            row.put("answerStatus", unanswered ? "skipped" : (correct ? "correct" : "wrong"));
            rows.add(row);
        }

        rows.sort((a, b) -> Integer.compare(
                ((Number) a.get("questionNo")).intValue(),
                ((Number) b.get("questionNo")).intValue()));

        summary.put("totalCount", rows.size());
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("unansweredCount", unansweredCount);
        model.put("paperAnswers", rows);
        model.put("paperSummary", summary);
        return model;
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
        for (CandidateEnrollmentDTO reg : registrationService.getCandidatesBySession(sessionId)) {
            if (reg.getSbd() == sbd) {
                return reg;
            }
        }
        return null;
    }

    @Override
    public ExaminerCandidateRowDTO getCandidateViewRow(int sessionId, int sbd, boolean isTheory, String sectionName) {
        for (ExaminerCandidateRowDTO row : loadCandidateRows(sessionId, isTheory, sectionName)) {
            if (row.getSbd() == sbd) {
                return row;
            }
        }
        CandidateEnrollmentDTO reg = findRegistration(sessionId, sbd);
        if (reg == null) {
            return null;
        }
        return buildCandidateRow(reg, isTheory,
                examinerDataDAO.loadTheoryStatsBySession(sessionId),
                examinerDataDAO.loadSectionScoresBySession(sessionId, sectionName),
                examinerDataDAO.loadPassFlagsBySession(sessionId),
                formatSessionDate(sessionId),
                loadLicenceClass(sessionId),
                examinerDataDAO.loadDeviceNamesBySession(sessionId));
    }

    @Override
    public Map<String, Object> getScoreEntryData(int sessionId, Integer sbdParam, String sectionName) {
        Map<String, Object> model = new HashMap<>();
        boolean isTheory = false;
        List<ExaminerCandidateRowDTO> allRows = loadCandidateRows(sessionId, isTheory, sectionName);
        List<ExaminerCandidateRowDTO> scoreQueue = new ArrayList<>();
        for (ExaminerCandidateRowDTO row : allRows) {
            CandidateEnrollmentDTO reg = findRegistration(sessionId, row.getSbd());
            if (isScoreQueueEligible(sessionId, reg, isTheory, sectionName)) {
                scoreQueue.add(row);
            }
        }
        Lane lane = ExamQueue.laneFor(examSectionFromName(sectionName));
        List<Integer> eligibleSbds = new ArrayList<>();
        for (ExaminerCandidateRowDTO row : scoreQueue) {
            eligibleSbds.add(row.getSbd());
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
            for (ExaminerCandidateRowDTO row : allRows) {
                if (row.getSbd() == sbdParam) {
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
            for (ExaminerCandidateRowDTO row : loadCandidateRows(sessionId, false, null)) {
                if (row.getSbd() == sbdParam) {
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
        return !CandidateStatus.COMPLETED.getValue().equals(status)
                && !CandidateStatus.AWAITING_SIGNATURE.getValue().equals(status);
    }

    @Override
    public Map<String, Object> getViolationData(int sessionId, Integer sbdParam) {
        Map<String, Object> model = new HashMap<>();
        List<ExaminerCandidateRowDTO> candidates = loadCandidateRows(sessionId, true, null);
        model.put("candidates", candidates);
        model.put("violationReasons", buildViolationReasonOptions());
        if (sbdParam != null && sbdParam > 0) {
            for (ExaminerCandidateRowDTO row : candidates) {
                if (row.getSbd() == sbdParam) {
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
        boolean assignedRoomOnly = preferredAreaId != null && preferredAreaId > 0;
        if (assignedRoomOnly) {
            areaIds.add(preferredAreaId);
        } else {
            for (Integer areaId : sessionDAO.getExamAreaIds(sessionId)) {
                if (areaId != null && areaId > 0 && !areaIds.contains(areaId)) {
                    areaIds.add(areaId);
                }
            }
            if (areaIds.isEmpty()) {
                Integer fallback = loadPrimarySessionAreaId(sessionId);
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

    private String loadAreaName(int areaId) {
        model.ExamArea area = examAreaDAO.getById(areaId);
        return area != null && area.getAreaName() != null ? area.getAreaName() : "";
    }

    private Map<Integer, String> buildSbdLookup(int sessionId) {
        Map<Integer, String> lookup = new LinkedHashMap<>();
        for (CandidateEnrollmentDTO reg : registrationService.getCandidatesBySession(sessionId)) {
            lookup.put(reg.getId(), String.valueOf(reg.getSbd()));
        }
        return lookup;
    }

    @Override
    public boolean isCallEligible(int sessionId, CandidateEnrollmentDTO reg, boolean isTheory,
            String sectionName) {
        if (reg == null || reg.isSuspended()) {
            return false;
        }
        return !CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus());
    }

    @Override
    public List<ExaminerCandidateRowDTO> orderCandidateRowsByQueue(List<ExaminerCandidateRowDTO> rows,
            ExamSection examSection) {
        return orderRowsByQueue(rows, ExamQueue.laneFor(examSection));
    }

    private static ExamSection examSectionFromName(String sectionName) {
        ExamSection section = ExamSection.fromValue(sectionName);
        return section != null ? section : ExamSection.THEORY;
    }

    private ExaminerCandidateRowDTO buildCandidateRow(CandidateEnrollmentDTO reg, boolean isTheory,
            Map<Integer, int[]> theoryStats, Map<Integer, Double> sectionScores,
            Map<Integer, Boolean> passFlags, String examDate, String licenceClass,
            Map<Integer, String> deviceNames) {
        ExaminerCandidateRowDTO row = new ExaminerCandidateRowDTO();
        int enrollmentId = reg.getEnrollment() != null ? reg.getEnrollment().getExamEnrollmentId() : 0;
        CandidateStatus sectionStatus = sectionStatusOf(reg);
        String statusKey = statusCssKey(sectionStatus);
        row.setSbd(reg.getSbd());
        row.setEnrollmentId(enrollmentId);
        row.setFullName(reg.getFullName());
        row.setDob(formatDate(reg.getDob()));
        if (reg.getDob() != null) {
            row.setDobRaw(new java.text.SimpleDateFormat("yyyy-MM-dd").format(reg.getDob()));
        } else if (reg.getDateOfBirth() != null) {
            row.setDobRaw(new java.text.SimpleDateFormat("yyyy-MM-dd").format(reg.getDateOfBirth()));
        } else {
            row.setDobRaw("");
        }
        row.setGovernmentId(reg.getGovIdNo());
        row.setAddress(reg.getAddress());
        row.setPhoneNo(reg.getPhoneNo());
        row.setSex(reg.isSex() ? "Nữ" : "Nam");
        row.setSexValue(reg.isSex() ? "1" : "0");
        row.setEmail(reg.getEmail());
        row.setLicenceClass(licenceClass);
        row.setReasonForTaking(reg.getReasonForTaking());
        row.setExamDate(examDate);
        row.setSectionStatus(sectionStatus);
        row.setStatus(statusKey);
        row.setStatusLabel(sectionStatus.getValue());
        row.setAbsent(reg.isAbsent());
        row.setSuspended(reg.isSuspended());
        row.setCallEligible(isCallEligible(0, reg, isTheory, null));
        int[] stats = theoryStats.getOrDefault(enrollmentId, new int[]{0, 0, 0});
        row.setCorrect(stats[0]);
        row.setWrong(stats[1]);
        row.setUnanswered(stats[2]);
        Double examScore = sectionScores.get(enrollmentId);
        row.setExamScore(examScore != null ? examScore.intValue() : "-");
        row.setScoreTheory(stats[0] > 0 ? stats[0] : "-");
        row.setScorePractical("-");
        row.setScoreOnRoad("-");
        Boolean passed = passFlags.get(enrollmentId);
        if (passed == null) {
            row.setPassed(false);
            row.setResultLabel("-");
        } else {
            row.setPassed(passed);
            row.setResultLabel(passed ? "Đạt" : "Trượt");
        }
        Integer deviceId = reg.getEnrollment() != null ? reg.getEnrollment().getExamDeviceId() : null;
        row.setVehicleName(deviceId != null ? deviceNames.getOrDefault(deviceId, "-") : "-");
        row.setAwaitingSignature("awaiting".equals(statusKey));
        row.setViolationEligible(!reg.isSuspended()
                && !CandidateStatus.COMPLETED.getValue().equals(reg.getSectionStatus()));
        row.setCompleteEligible("awaiting".equals(statusKey) && reg.isSignaturePrinted());
        return row;
    }

    private static CandidateStatus sectionStatusOf(CandidateEnrollmentDTO reg) {
        CandidateStatus status = CandidateStatus.fromValue(reg.getSectionStatus());
        return status != null ? status : CandidateStatus.NOT_STARTED;
    }

    private static String statusCssKey(CandidateStatus status) {
        if (status == CandidateStatus.COMPLETED) {
            return "done";
        }
        if (status == CandidateStatus.AWAITING_SIGNATURE) {
            return "awaiting";
        }
        if (status == CandidateStatus.IN_PROGRESS) {
            return "testing";
        }
        return "pending";
    }

    private List<ExaminerCandidateRowDTO> filterRows(List<ExaminerCandidateRowDTO> rows, String searchQuery,
            CandidateCallDataDTO data) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return rows;
        }
        String q = searchQuery.trim().toLowerCase(Locale.ROOT);
        List<ExaminerCandidateRowDTO> filtered = new ArrayList<>();
        for (ExaminerCandidateRowDTO row : rows) {
            if (matchesSearch(row, q)) {
                filtered.add(row);
            }
        }
        data.setSearchActive(true);
        data.setSearchQuery(searchQuery.trim());
        return filtered;
    }

    private static boolean matchesSearch(ExaminerCandidateRowDTO row, String q) {
        return String.valueOf(row.getSbd()).toLowerCase(Locale.ROOT).contains(q)
                || contains(row.getFullName(), q)
                || contains(row.getGovernmentId(), q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
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

    private String loadLicenceClass(int sessionId) {
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

    private Integer loadPrimarySessionAreaId(int sessionId) {
        return examinerDataDAO.findPrimarySessionAreaId(sessionId);
    }

    private List<Map<String, Object>> loadSessionVehicles(int sessionId) {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        Integer areaId = loadPrimarySessionAreaId(sessionId);
        if (areaId == null || areaId <= 0) {
            return vehicles;
        }
        for (ExamDevice device : deviceDAO.getDevicesByAreaId(areaId)) {
            String type = device.getDeviceType() != null ? device.getDeviceType().toLowerCase(Locale.ROOT) : "";
            if (!type.contains("car") && !type.contains("motorcycle") && !type.contains("xe")
                    && !type.contains("oto")) {
                continue;
            }
            Map<String, Object> row = toDeviceRow(device, loadAreaName(areaId));
            row.put("status", row.get("status"));
            row.put("statusLabel", row.get("statusLabel"));
            row.put("statusClass", row.get("statusClass"));
            vehicles.add(row);
        }
        return vehicles;
    }

    private static List<ExaminerCandidateRowDTO> orderRowsByQueue(List<ExaminerCandidateRowDTO> rows, Lane lane) {
        List<Integer> order = ExamQueue.asList(lane);
        if (order.isEmpty() || rows.isEmpty()) {
            return rows;
        }
        Map<Integer, ExaminerCandidateRowDTO> bySbd = new LinkedHashMap<>();
        for (ExaminerCandidateRowDTO row : rows) {
            bySbd.put(row.getSbd(), row);
        }
        List<ExaminerCandidateRowDTO> ordered = new ArrayList<>();
        for (Integer sbd : order) {
            ExaminerCandidateRowDTO row = bySbd.remove(sbd);
            if (row != null) {
                ordered.add(row);
            }
        }
        ordered.addAll(bySbd.values());
        return ordered;
    }

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

    private static boolean isComputerDevice(String deviceType) {
        return DeviceType.fromValue(deviceType) == DeviceType.COMPUTER;
    }

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
