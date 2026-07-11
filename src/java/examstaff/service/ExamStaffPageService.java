package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPickerViewDTO;

import java.util.List;

public interface ExamStaffPageService {

    List<ExamSummaryDTO> listAllSessions();

    ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allSessions);

    ExamSummaryDTO representativeSessionForExam(List<ExamSummaryDTO> allSessions, int examId);

    List<ExamSummaryDTO> sessionsForExam(List<ExamSummaryDTO> allSessions, int examId);

    int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId);

    int resolveDefaultExamId(List<ExamSummaryDTO> allSessions);

    ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allSessions, int examId, int urlExamId);

    ExamStaffPageContextDTO preparePageContext(ExamStaffPagePrepareInput input);
}
