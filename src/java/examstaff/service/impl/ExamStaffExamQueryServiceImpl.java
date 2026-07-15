package examstaff.service.impl;

import examstaff.dao.view.ExamViewDAO;
import examstaff.dao.view.impl.ExamViewDAOImpl;
import examstaff.dto.ExamSummaryDTO;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.util.ExamSummaryMapper;
import examstaff.util.ExamStaffExamRules;

import java.util.List;

public class ExamStaffExamQueryServiceImpl implements ExamStaffExamQueryService {

    private final ExamViewDAO examViewDAO = new ExamViewDAOImpl();

    @Override
    public List<ExamSummaryDTO> listAllExams() {
        return ExamSummaryMapper.toDtoList(examViewDAO.findAllOrdered());
    }

    @Override
    public ExamSummaryDTO findByExamId(int examId) {
        return ExamSummaryMapper.toDto(examViewDAO.findByExamId(examId));
    }

    @Override
    public List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId) {
        return ExamStaffExamRules.examsForExam(allExams, examId);
    }
}
