package examstaff.service.impl;

import dto.ExamSummaryDTO;
import dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPickerViewDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.util.ExamStaffSessionRules;

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
    public List<ExamSummaryDTO> listAllSessions() {
        return sessionQuery.listAllSessions();
    }

    @Override
    public ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allSessions) {
        if (examId <= 0) {
            return null;
        }
        ExamSummaryDTO found = ExamStaffSessionRules.findExamById(allSessions, examId);
        if (found != null) {
            return found;
        }
        try {
            return sessionQuery.findByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ExamSummaryDTO representativeSessionForExam(List<ExamSummaryDTO> allSessions, int examId) {
        if (examId <= 0) {
            return null;
        }
        List<ExamSummaryDTO> daySessions = sessionsForExam(allSessions, examId);
        if (!daySessions.isEmpty()) {
            return daySessions.get(0);
        }
        int primaryId = resolvePrimaryExamId(allSessions, examId);
        if (primaryId > 0) {
            return sessionQuery.findByExamId(primaryId);
        }
        return null;
    }

    @Override
    public List<ExamSummaryDTO> sessionsForExam(List<ExamSummaryDTO> allSessions, int examId) {
        if (allSessions == null || examId <= 0) {
            return List.of();
        }
        List<ExamSummaryDTO> result = new ArrayList<>();
        for (ExamSummaryDTO s : allSessions) {
            if (s.getExamId() == examId || s.getId() == examId) {
                result.add(s);
            }
        }
        result.sort(Comparator
                .comparing(ExamSummaryDTO::getShiftStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ExamSummaryDTO::getId));
        return result;
    }

    @Override
    public int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId) {
        return ExamStaffSessionRules.resolvePrimaryExamId(allSessions, examId);
    }

    @Override
    public int resolveDefaultExamId(List<ExamSummaryDTO> allSessions) {
        ExamSummaryDTO first = firstPickerOption(allSessions);
        if (first == null) {
            return 0;
        }
        return first.getId() > 0 ? first.getId() : first.getExamId();
    }

    @Override
    public ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allSessions, int examId, int urlExamId) {
        ExamStaffPickerViewDTO view = new ExamStaffPickerViewDTO();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = listAllSessions();
        }
        List<ExamSummaryDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        view.setExamOptions(options);
        view.setAllSessions(allSessions);

        ExamSummaryDTO current = null;
        if (urlExamId > 0) {
            current = findExamById(urlExamId, allSessions);
            if (current != null) {
                examId = current.getId() > 0 ? current.getId() : current.getExamId();
                view.setSelectedExamId(urlExamId);
            }
        } else if (examId > 0) {
            current = findExamById(examId, allSessions);
            view.setSelectedExamId(examId);
        }
        if (current == null && examId > 0) {
            current = representativeSessionForExam(allSessions, examId);
        }
        if (current == null && !options.isEmpty()) {
            current = options.get(0);
            examId = current.getId() > 0 ? current.getId() : current.getExamId();
        }
        view.setCurrentSession(current);
        view.setExamId(examId);

        int committedExamId = 0;
        if (urlExamId > 0 && current != null) {
            committedExamId = urlExamId;
        } else {
            committedExamId = resolvePickerOptionExamId(options, examId);
        }
        if (committedExamId > 0) {
            view.setPickerCommittedExamId(committedExamId);
        } else if (examId > 0) {
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

        List<ExamSummaryDTO> allSessions = input.getAllSessions();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = listAllSessions();
        }
        ctx.setAllSessions(allSessions);

        int examId = resolveExamId(input, allSessions);

        ExamStaffPickerViewDTO picker = buildPickerView(allSessions, examId, input.getUrlExamId());
        examId = picker.getExamId();
        if (input.getUrlExamId() > 0) {
            examId = input.getUrlExamId();
        } else if (picker.getSelectedExamId() != null && picker.getSelectedExamId() > 0) {
            examId = picker.getSelectedExamId();
        }
        if (examId <= 0) {
            examId = resolvePrimaryExamId(allSessions, picker.getExamId());
        }

        if (examId <= 0 && picker.getExamOptions() != null && !picker.getExamOptions().isEmpty()) {
            ExamSummaryDTO first = picker.getExamOptions().get(0);
            examId = first.getId() > 0 ? first.getId() : first.getExamId();
        }

        ctx.setExamId(examId);
        ctx.setPickerView(picker);

        List<ExamRegistrationDTO> candidates = resolveCandidates(input, examId, allSessions);
        ctx.setCandidates(candidates);
        return ctx;
    }

    private List<ExamRegistrationDTO> resolveCandidates(ExamStaffPagePrepareInput input,
            int examId, List<ExamSummaryDTO> allSessions) {
        if (!input.isLoadCandidates()) {
            if (input.getCachedQueue() != null
                    && input.getLoadedExamId() != null && input.getLoadedExamId() == examId) {
                return input.getCachedQueue();
            }
            return new ArrayList<>();
        }

        ExamStaffQueueRefreshInput refresh = new ExamStaffQueueRefreshInput();
        refresh.setExamId(examId);
        refresh.setWebRoot(input.getWebRoot());
        refresh.setAllSessions(allSessions);
        refresh.setSelectedExamId(input.getSelectedExamId());
        refresh.setCallQueueOrder(input.getCallQueueOrder());
        refresh.setCallQueueOrderExamId(input.getCallQueueOrderExamId());
        CandidateQueueSnapshotDTO snapshot = queueService.refreshQueue(refresh);
        return snapshot.getFullQueue();
    }

    private int resolveExamId(ExamStaffPagePrepareInput input, List<ExamSummaryDTO> allSessions) {
        int examId = input.getUrlExamId();
        if (examId > 0) {
            ExamSummaryDTO picked = findExamById(examId, allSessions);
            if (picked != null) {
                return picked.getId() > 0 ? picked.getId() : picked.getExamId();
            }
            return examId;
        }
        if (input.getSelectedExamId() != null && input.getSelectedExamId() > 0) {
            return input.getSelectedExamId();
        }
        int parsed = parseExamIdParam(input.getExamIdParam());
        if (parsed > 0) {
            return parsed;
        }
        return resolveDefaultExamId(allSessions);
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

    private List<ExamSummaryDTO> buildExamOptions(List<ExamSummaryDTO> allSessions) {
        LinkedHashMap<Integer, ExamSummaryDTO> examOptionMap = new LinkedHashMap<>();
        if (allSessions != null) {
            for (ExamSummaryDTO s : allSessions) {
                int key = s.getId() > 0 ? s.getId() : s.getExamId();
                if (key <= 0 || examOptionMap.containsKey(key)) {
                    continue;
                }
                examOptionMap.put(key, s);
            }
        }
        return new ArrayList<>(examOptionMap.values());
    }

    private static int resolvePickerOptionExamId(List<ExamSummaryDTO> options, int examId) {
        if (options == null || examId <= 0) {
            return 0;
        }
        for (ExamSummaryDTO opt : options) {
            if (opt.getExamId() == examId || opt.getId() == examId) {
                return opt.getId() > 0 ? opt.getId() : opt.getExamId();
            }
        }
        return 0;
    }

    private static List<ExamSummaryDTO> sortExamDaysForSidebar(List<ExamSummaryDTO> options) {
        return ExamStaffSessionRules.sortExamDaysForSidebar(options);
    }

    private ExamSummaryDTO firstPickerOption(List<ExamSummaryDTO> allSessions) {
        List<ExamSummaryDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        return options.isEmpty() ? null : options.get(0);
    }
}
