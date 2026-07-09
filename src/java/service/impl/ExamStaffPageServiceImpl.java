package service.impl;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffPageContextDTO;
import dto.examstaff.ExamStaffPagePrepareInput;
import dto.examstaff.ExamStaffPickerViewDTO;
import dto.examstaff.ExamStaffQueueRefreshInput;
import service.CandidateQueueService;
import service.ExamStaffPageService;
import service.ExamStaffSessionQueryService;
import util.examstaff.ExamStaffSessionRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ExamStaffPageServiceImpl implements ExamStaffPageService {

    private final ExamStaffSessionQueryService sessionQuery;
    private final CandidateQueueService queueService;

    public ExamStaffPageServiceImpl() {
        this(new ExamStaffSessionQueryServiceImpl(), new CandidateQueueServiceImpl());
    }

    public ExamStaffPageServiceImpl(ExamStaffSessionQueryService sessionQuery,
            CandidateQueueService queueService) {
        this.sessionQuery = sessionQuery;
        this.queueService = queueService;
    }

    @Override
    public List<SessionDTO> listAllSessions() {
        return sessionQuery.listAllSessions();
    }

    @Override
    public SessionDTO findSessionById(int sessionId, List<SessionDTO> allSessions) {
        if (sessionId <= 0) {
            return null;
        }
        SessionDTO found = ExamStaffSessionRules.findSessionById(allSessions, sessionId);
        if (found != null) {
            return found;
        }
        try {
            return sessionQuery.findBySessionId(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SessionDTO representativeSessionForExam(List<SessionDTO> allSessions, int examId) {
        if (examId <= 0) {
            return null;
        }
        List<SessionDTO> daySessions = sessionsForExam(allSessions, examId);
        if (!daySessions.isEmpty()) {
            return daySessions.get(0);
        }
        int primaryId = resolvePrimarySessionId(allSessions, examId);
        if (primaryId > 0) {
            return sessionQuery.findBySessionId(primaryId);
        }
        return null;
    }

    @Override
    public List<SessionDTO> sessionsForExam(List<SessionDTO> allSessions, int examId) {
        if (allSessions == null || examId <= 0) {
            return List.of();
        }
        List<SessionDTO> result = new ArrayList<>();
        for (SessionDTO s : allSessions) {
            if (s.getExamId() == examId) {
                result.add(s);
            }
        }
        result.sort(Comparator
                .comparing(SessionDTO::getShiftStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SessionDTO::getId));
        return result;
    }

    @Override
    public int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        if (examId <= 0) {
            return 0;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = listAllSessions();
        }
        List<SessionDTO> daySessions = sessionsForExam(allSessions, examId);
        if (daySessions.isEmpty()) {
            return 0;
        }
        SessionDTO best = daySessions.get(0);
        for (SessionDTO s : daySessions) {
            if (s.getRegisteredCount() > best.getRegisteredCount()) {
                best = s;
            }
        }
        return best.getId();
    }

    @Override
    public int resolveDefaultExamId(List<SessionDTO> allSessions) {
        SessionDTO first = firstPickerOption(allSessions);
        return first != null && first.getExamId() > 0 ? first.getExamId() : 0;
    }

    @Override
    public int resolveDefaultSessionId(List<SessionDTO> allSessions) {
        SessionDTO first = firstPickerOption(allSessions);
        return first != null ? first.getId() : 0;
    }

    @Override
    public ExamStaffPickerViewDTO buildPickerView(List<SessionDTO> allSessions, int examId, int urlSessionId) {
        ExamStaffPickerViewDTO view = new ExamStaffPickerViewDTO();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = listAllSessions();
        }
        List<SessionDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        view.setExamOptions(options);
        view.setAllSessions(allSessions);

        SessionDTO current = null;
        if (urlSessionId > 0) {
            current = findSessionById(urlSessionId, allSessions);
            if (current != null && current.getExamId() > 0) {
                examId = current.getExamId();
                view.setSelectedSessionId(urlSessionId);
            }
        } else if (examId > 0) {
            int sessionId = resolvePrimarySessionId(allSessions, examId);
            current = findSessionById(sessionId, allSessions);
            view.setSelectedSessionId(sessionId > 0 ? sessionId : null);
        }
        if (current == null && examId > 0) {
            current = representativeSessionForExam(allSessions, examId);
        }
        if (current == null && !options.isEmpty()) {
            current = options.get(0);
            examId = current.getExamId();
        }
        view.setCurrentSession(current);
        view.setExamId(examId);

        int committedSessionId = 0;
        if (urlSessionId > 0 && current != null && current.getExamId() == examId) {
            committedSessionId = urlSessionId;
        } else {
            committedSessionId = resolvePickerOptionSessionId(options, examId);
        }
        if (committedSessionId > 0) {
            view.setPickerCommittedSessionId(committedSessionId);
        }
        if (examId > 0) {
            view.setPickerCommittedExamId(examId);
        }
        return view;
    }

    @Override
    public ExamStaffPageContextDTO preparePageContext(ExamStaffPagePrepareInput input) {
        ExamStaffPageContextDTO ctx = new ExamStaffPageContextDTO();
        if (input == null) {
            return ctx;
        }

        List<SessionDTO> allSessions = input.getAllSessions();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = listAllSessions();
        }
        ctx.setAllSessions(allSessions);

        int examId = resolveExamId(input, allSessions);
        int sessionId = resolveSessionId(input, allSessions, examId);

        ExamStaffPickerViewDTO picker = buildPickerView(allSessions, examId, input.getUrlSessionId());
        examId = picker.getExamId();
        if (input.getUrlSessionId() > 0) {
            sessionId = input.getUrlSessionId();
        } else if (picker.getSelectedSessionId() != null && picker.getSelectedSessionId() > 0) {
            sessionId = picker.getSelectedSessionId();
        }
        if (sessionId <= 0 && examId > 0) {
            sessionId = resolvePrimarySessionId(allSessions, examId);
        }

        if (examId <= 0 && !picker.getExamOptions().isEmpty()) {
            SessionDTO first = picker.getExamOptions().get(0);
            examId = first.getExamId();
            sessionId = first.getId();
        }

        ctx.setExamId(examId);
        ctx.setSessionId(sessionId);
        ctx.setPickerView(picker);

        List<ExamRegistrationDTO> candidates = resolveCandidates(input, examId, sessionId, allSessions);
        ctx.setCandidates(candidates);
        return ctx;
    }

    private List<ExamRegistrationDTO> resolveCandidates(ExamStaffPagePrepareInput input,
            int examId, int sessionId, List<SessionDTO> allSessions) {
        if (!input.isLoadCandidates()) {
            if (input.getCachedQueue() != null
                    && input.getLoadedExamId() != null && input.getLoadedExamId() == examId
                    && (sessionId <= 0 || input.getLoadedSessionId() == null
                    || input.getLoadedSessionId() == sessionId)) {
                return input.getCachedQueue();
            }
            return new ArrayList<>();
        }

        ExamStaffQueueRefreshInput refresh = new ExamStaffQueueRefreshInput();
        refresh.setExamId(examId);
        refresh.setSessionId(sessionId);
        refresh.setWebRoot(input.getWebRoot());
        refresh.setAllSessions(allSessions);
        refresh.setSelectedSessionId(input.getSelectedSessionId());
        refresh.setCallQueueOrder(input.getCallQueueOrder());
        refresh.setCallQueueOrderSessionId(input.getCallQueueOrderSessionId());
        CandidateQueueSnapshotDTO snapshot = queueService.refreshQueue(refresh);
        return snapshot.getFullQueue();
    }

    private int resolveExamId(ExamStaffPagePrepareInput input, List<SessionDTO> allSessions) {
        int sessionId = input.getUrlSessionId();
        int examId = 0;
        if (sessionId > 0) {
            SessionDTO picked = findSessionById(sessionId, allSessions);
            if (picked != null) {
                examId = picked.getExamId();
            }
        }
        if (examId <= 0 && input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            examId = input.getSelectedExamId();
        }
        if (examId <= 0) {
            examId = parseExamIdParam(input.getExamIdParam());
        }
        if (examId <= 0) {
            examId = resolveDefaultExamId(allSessions);
        }
        return examId;
    }

    private int resolveSessionId(ExamStaffPagePrepareInput input, List<SessionDTO> allSessions, int examId) {
        int sessionId = input.getUrlSessionId();
        if (sessionId <= 0 && input.getSelectedSessionId() != null && input.getSelectedSessionId() > 0) {
            sessionId = input.getSelectedSessionId();
        }
        if (sessionId <= 0 && examId > 0) {
            sessionId = resolvePrimarySessionId(allSessions, examId);
        }
        return sessionId;
    }

    private static int parseExamIdParam(String examIdParam) {
        if (examIdParam == null || examIdParam.isBlank()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(examIdParam.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private List<SessionDTO> buildExamOptions(List<SessionDTO> allSessions) {
        LinkedHashMap<Integer, SessionDTO> examOptionMap = new LinkedHashMap<>();
        if (allSessions != null) {
            LinkedHashMap<Integer, SessionDTO> byId = new LinkedHashMap<>();
            for (SessionDTO s : allSessions) {
                if (s.getId() > 0) {
                    byId.put(s.getId(), s);
                }
            }
            for (SessionDTO s : allSessions) {
                if (s.getExamId() <= 0 || examOptionMap.containsKey(s.getExamId())) {
                    continue;
                }
                int primaryId = resolvePrimarySessionId(allSessions, s.getExamId());
                SessionDTO primary = primaryId > 0 ? byId.get(primaryId) : null;
                if (primary != null) {
                    examOptionMap.put(s.getExamId(), primary);
                }
            }
        }
        return new ArrayList<>(examOptionMap.values());
    }

    private static int resolvePickerOptionSessionId(List<SessionDTO> options, int examId) {
        if (options == null || examId <= 0) {
            return 0;
        }
        for (SessionDTO opt : options) {
            if (opt.getExamId() == examId) {
                return opt.getId();
            }
        }
        return 0;
    }

    private static List<SessionDTO> sortExamDaysForSidebar(List<SessionDTO> options) {
        return util.examstaff.ExamStaffSessionRules.sortExamDaysForSidebar(options);
    }

    private SessionDTO firstPickerOption(List<SessionDTO> allSessions) {
        List<SessionDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        return options.isEmpty() ? null : options.get(0);
    }
}
