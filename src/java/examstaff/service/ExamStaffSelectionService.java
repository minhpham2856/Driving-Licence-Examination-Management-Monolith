package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;

import java.util.List;

public interface ExamStaffSelectionService {

    int resolveExamId(ExamStaffSelectionResolveInput input);

    int ensureExamId(ExamStaffSelectionResolveInput input);

    int resolveExamFromSessionUrl(int urlExamId, List<ExamSummaryDTO> allSessions);

    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId, List<ExamSummaryDTO> allSessions);

    ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input);

    int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId);
}
