package service;

import dto.ExamSummaryDTO;

import java.util.List;

public interface ExamStaffSessionQueryService {

    List<ExamSummaryDTO> listAllSessions();

    ExamSummaryDTO findByExamId(int examId);

    List<ExamSummaryDTO> listSessionsForExam(List<ExamSummaryDTO> allSessions, int examId);

    int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId);
}
