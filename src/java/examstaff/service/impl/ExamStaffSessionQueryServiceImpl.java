package examstaff.service.impl;

import examstaff.dao.view.ExamSessionViewDAO;
import examstaff.dao.view.impl.ExamSessionViewDAOImpl;
import dto.ExamSummaryDTO;
import examstaff.service.ExamStaffSessionQueryService;
import examstaff.util.ExamSessionSummaryMapper;
import examstaff.util.ExamStaffSessionRules;

import java.util.List;

public class ExamStaffSessionQueryServiceImpl implements ExamStaffSessionQueryService {

    private final ExamSessionViewDAO sessionViewDAO = new ExamSessionViewDAOImpl();

    @Override
    public List<ExamSummaryDTO> listAllSessions() {
        return ExamSessionSummaryMapper.toDtoList(sessionViewDAO.findAllOrdered());
    }

    @Override
    public ExamSummaryDTO findByExamId(int examId) {
        return ExamSessionSummaryMapper.toDto(sessionViewDAO.findByExamId(examId));
    }

    @Override
    public List<ExamSummaryDTO> listSessionsForExam(List<ExamSummaryDTO> allSessions, int examId) {
        return ExamStaffSessionRules.sessionsForExam(allSessions, examId);
    }

    @Override
    public int resolvePrimaryExamId(List<ExamSummaryDTO> allSessions, int examId) {
        return ExamStaffSessionRules.resolvePrimaryExamId(allSessions, examId);
    }
}
