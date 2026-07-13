package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.util.ExamStaffExamRules;

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

        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams != null && !allExams.isEmpty()) {
            return pageService.resolveDefaultExamId(allExams);
        }
        return 0;
    }

    @Override
    public int ensureExamId(ExamStaffSelectionResolveInput input) {
        int examId = resolveExamId(input);
        if (examId > 0) {
            return examId;
        }
        List<ExamSummaryDTO> allExams = input.getAllExams();
        if (allExams == null || allExams.isEmpty()) {
            allExams = pageService.listAllExams();
            input.setAllExams(allExams);
        }
        return pageService.resolveDefaultExamId(allExams);
    }

    @Override
    public int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams) {
        if (urlExamId <= 0) {
            return 0;
        }
        ExamSummaryDTO picked = pageService.findExamById(urlExamId, allExams);
        if (picked == null || picked.getExamId() <= 0) {
            return 0;
        }
        return picked.getExamId();
    }

    @Override
    public ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId,
            List<ExamSummaryDTO> allExams) {
        ExamStaffSelectionStateDTO state = new ExamStaffSelectionStateDTO();
        if (examId <= 0) {
            return state;
        }

        int resolved = currentExamId != null ? currentExamId : 0;
        if (resolved <= 0) {
            resolved = ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
        } else if (allExams != null) {
            ExamSummaryDTO picked = ExamStaffExamRules.findExamById(allExams, resolved);
            if (picked == null || picked.getExamId() != examId) {
                resolved = ExamStaffExamRules.resolvePrimaryExamId(allExams, examId);
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

        List<ExamSummaryDTO> allExams = input.getAllExams();
        ExamSummaryDTO urlExam = pageService.findExamById(input.getUrlExamId(), allExams);
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
