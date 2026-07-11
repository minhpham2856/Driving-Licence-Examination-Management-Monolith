package service.impl;

import dao.view.ExamSessionViewDAO;
import dao.view.impl.ExamSessionViewDAOImpl;
import dto.SessionDTO;
import service.ExamStaffSessionQueryService;
import util.examstaff.ExamSessionSummaryMapper;
import util.examstaff.ExamStaffSessionRules;

import java.sql.Date;
import java.util.List;

public class ExamStaffSessionQueryServiceImpl implements ExamStaffSessionQueryService {

    private final ExamSessionViewDAO sessionViewDAO = new ExamSessionViewDAOImpl();

    @Override
    public List<SessionDTO> listAllSessions() {
        return ExamSessionSummaryMapper.toDtoList(sessionViewDAO.findAllOrdered());
    }

    @Override
    public SessionDTO findBySessionId(int sessionId) {
        return ExamSessionSummaryMapper.toDto(sessionViewDAO.findBySessionId(sessionId));
    }

    @Override
    public List<SessionDTO> listSessionsByExamDate(Date examDate) {
        return ExamSessionSummaryMapper.toDtoList(sessionViewDAO.findByExamDate(examDate));
    }

    @Override
    public List<SessionDTO> listSessionsForExam(List<SessionDTO> allSessions, int examId) {
        return ExamStaffSessionRules.sessionsForExam(allSessions, examId);
    }

    @Override
    public int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        return ExamStaffSessionRules.resolvePrimarySessionId(allSessions, examId);
    }
}
