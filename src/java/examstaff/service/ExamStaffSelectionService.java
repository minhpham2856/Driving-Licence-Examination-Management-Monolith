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

    int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams);

    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId, List<ExamSummaryDTO> allExams);

    ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input);

    int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId);
}
