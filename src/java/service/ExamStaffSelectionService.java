package service;

import dto.ExamSummaryDTO;
import dto.examstaff.ExamStaffPageTransitionInput;
import dto.examstaff.ExamStaffPageTransitionStateDTO;
import dto.examstaff.ExamStaffSelectionResolveInput;
import dto.examstaff.ExamStaffSelectionStateDTO;

import java.util.List;

public interface ExamStaffSelectionService {

    int resolveExamId(ExamStaffSelectionResolveInput input);

    int ensureExamId(ExamStaffSelectionResolveInput input);

    int resolveExamFromSessionUrl(int urlExamId, List<ExamSummaryDTO> allSessions);

    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentExamId, List<ExamSummaryDTO> allSessions);

    ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input);

    int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId);
}
