package examstaff.service.impl;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamSelectRequestDTO;
import examstaff.dto.ExamSelectResultDTO;
import shared.enums.ExamStaffMessage;
import examstaff.service.ExamSelectService;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.service.ExamStaffSessionQueryService;

import java.util.List;

public class ExamSelectServiceImpl implements ExamSelectService {

    private final ExamStaffSessionQueryService sessionQuery = new ExamStaffSessionQueryServiceImpl();
    private final ExamStaffSelectionService selectionService = new ExamStaffSelectionServiceImpl();
    private final ExamStaffPageService pageService = new ExamStaffPageServiceImpl();

    @Override
    public ExamSelectResultDTO processSelection(ExamSelectRequestDTO request) {
        ExamSelectResultDTO result = new ExamSelectResultDTO();
        result.setPreviousExamId(request.getPreviousExamId());

        List<ExamSummaryDTO> allSessions = sessionQuery.listAllSessions();
        int urlExamId = request.getUrlExamId();
        int examId = selectionService.resolveExamFromSessionUrl(urlExamId, allSessions);

        if (examId <= 0) {
            result.setSuccess(false);
            String param = urlExamId > 0 ? String.valueOf(urlExamId) : null;
            result.setErrorMessage(ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.formatExamNotFound(param));
            return result;
        }

        int resolvedExamId = urlExamId > 0
                ? urlExamId
                : pageService.resolvePrimaryExamId(allSessions, examId);

        result.setSuccess(true);
        result.setExamId(resolvedExamId > 0 ? resolvedExamId : examId);
        result.setNewExamId(result.getExamId());

        Integer previousExamId = request.getPreviousExamId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(result.getExamId())) {
            result.setClearProcedureOnExamChange(true);
            result.setClearCandidateCache(true);
        }
        return result;
    }
}

