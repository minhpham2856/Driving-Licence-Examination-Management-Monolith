package examstaff.service.impl;

import examstaff.dao.view.ExamViewDAO;
import examstaff.dao.view.impl.ExamViewDAOImpl;
import examstaff.dto.ExamSummaryDTO;
import examstaff.service.ExamStaffExamQueryService;
import examstaff.util.ExamSummaryMapper;
import examstaff.util.ExamStaffExamRules;

import java.util.List;

/** Implementation: truy vấn danh sách kỳ thi qua {@link ExamViewDAO}. */
public class ExamStaffExamQueryServiceImpl implements ExamStaffExamQueryService {

    private final ExamViewDAO examViewDAO = new ExamViewDAOImpl();

    /** {@inheritDoc} */
    @Override
    public List<ExamSummaryDTO> listAllExams() {
        return ExamSummaryMapper.toDtoList(examViewDAO.findAllOrdered());
    }

    /** {@inheritDoc} */
    @Override
    public ExamSummaryDTO findByExamId(int examId) {
        return ExamSummaryMapper.toDto(examViewDAO.findByExamId(examId));
    }

    /** {@inheritDoc} */
    @Override
    public List<ExamSummaryDTO> listExamsForDay(List<ExamSummaryDTO> allExams, int examId) {
        return ExamStaffExamRules.examsForExam(allExams, examId);
    }
}
