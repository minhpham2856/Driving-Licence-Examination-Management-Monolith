package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.util.ExamStaffSessionRules;

import java.util.List;

public class ExamStaffSelectionServiceImpl implements ExamStaffSelectionService {

    private final ExamStaffPageService pageService;

    public ExamStaffSelectionServiceImpl() {
        this(new ExamStaffPageServiceImpl());
    }

    public ExamStaffSelectionServiceImpl(ExamStaffPageService pageService) {
        this.pageService = pageService;
    }

    @Override
    public int resolveExamId(ExamStaffSelectionResolveInput input) {
        if (input == null) {
            return 0;
        }
        if (input.getUrlExamId() > 0) {
            return input.getUrlExamId();
        }
        Integer selectedExam = input.getSelectedExamId();
        if (selectedExam != null && selectedExam > 0) {
            return selectedExam;
        }
        if (input.getDefaultExamId() > 0) {
            return input.getDefaultExamId();
        }

        String examIdParam = input.getExamIdParam();
        if (examIdParam != null && !examIdParam.isBlank()) {
            try {
                int parsed = Integer.parseInt(examIdParam.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        List<ExamSummaryDTO> allSessions = input.getAllSessions();
        if (allSessions != null && !allSessions.isEmpty()) {
            return pageService.resolveDefaultExamId(allSessions);
        }
        return 0;
    }

    @Override
    public int ensureExamId(ExamStaffSelectionResolveInput input) {
        int examId = resolveExamId(input);
        if (examId > 0) {
            return examId;
        }
        List<ExamSummaryDTO> allSessions = input.getAllSessions();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = pageService.listAllSessions();
            input.setAllSessions(allSessions);
        }
        return pageService.resolveDefaultExamId(allSessions);
    }

    @Override
    public int resolveExamFromSessionUrl(int urlExamId, List<ExamSummaryDTO> allSessions) {
        if (urlExamId <= 0) {
            return 0;
        }
        ExamSummaryDTO picked = pageService.findExamById(urlExamId, allSessions);
        if (picked == null || picked.getExamId() <= 0) {
            return 0;
        }
        return picked.getExamId();
    }

    @Override
    public ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId,
            List<ExamSummaryDTO> allSessions) {
        ExamStaffSelectionStateDTO state = new ExamStaffSelectionStateDTO();
        if (examId <= 0) {
            return state;
        }

        int resolved = currentExamId != null ? currentExamId : 0;
        if (resolved <= 0) {
            resolved = ExamStaffSessionRules.resolvePrimaryExamId(allSessions, examId);
        } else if (allSessions != null) {
            ExamSummaryDTO picked = ExamStaffSessionRules.findExamById(allSessions, resolved);
            if (picked == null || picked.getExamId() != examId) {
                resolved = ExamStaffSessionRules.resolvePrimaryExamId(allSessions, examId);
            }
        }
        state.setExamId(resolved > 0 ? resolved : examId);
        return state;
    }

    @Override
    public ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input) {
        ExamStaffPageTransitionStateDTO state = new ExamStaffPageTransitionStateDTO();
        if (input == null || input.getUrlExamId() <= 0) {
            return state;
        }

        List<ExamSummaryDTO> allSessions = input.getAllSessions();
        ExamSummaryDTO urlExam = pageService.findExamById(input.getUrlExamId(), allSessions);
        if (urlExam == null || urlExam.getExamId() <= 0) {
            return state;
        }

        state.setExamId(input.getUrlExamId());
        state.setPersistSelection(true);

        Integer loadedExamId = input.getLoadedExamId();
        if (loadedExamId == null || loadedExamId != input.getUrlExamId()) {
            state.setClearCandidateCache(true);
        }

        Integer previousExamId = input.getPreviousExamId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(urlExam.getExamId())
                && !previousExamId.equals(input.getUrlExamId())) {
            state.setClearProcedureState(true);
        }

        return state;
    }

    @Override
    public int resolveActiveExamId(int urlExamId, Integer selectedExamId,
            Integer runtimeActiveExamId) {
        if (urlExamId > 0) {
            return urlExamId;
        }
        if (selectedExamId != null && selectedExamId > 0) {
            return selectedExamId;
        }
        if (runtimeActiveExamId != null && runtimeActiveExamId > 0) {
            return runtimeActiveExamId;
        }
        return 0;
    }
}
