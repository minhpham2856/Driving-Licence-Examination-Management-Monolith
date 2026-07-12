package examstaff.service;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

public interface ExamStaffExamQueryService {

    List<ExamSummaryDTO> listAllExams();

    ExamSummaryDTO findByExamId(int examId);

    List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId);
}
