package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPickerViewDTO;

import java.util.List;

public interface ExamStaffPageService {

    List<ExamSummaryDTO> listAllExams();

    ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allExams);

    ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId);

    int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId);

    int resolveDefaultExamId(List<ExamSummaryDTO> allExams);

    ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allExams, int examId, int urlExamId);

    ExamStaffPageContextDTO preparePageContext(ExamStaffPagePrepareInput input);
}
