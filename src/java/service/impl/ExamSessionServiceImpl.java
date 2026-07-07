package service.impl;

import dao.ExamSectionDAO;
import dao.SessionDAO;
import dao.impl.ExamSectionDAOImpl;
import dao.impl.SessionDAOImpl;
import enums.ExamSection;
import model.ExaminerSchedule;
import model.Session;
import service.ExamSessionService;

public class ExamSessionServiceImpl implements ExamSessionService {

    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final ExamSectionDAO sectionDAO = new ExamSectionDAOImpl();

    @Override
    public Session getById(int sessionId) {
        return sessionDAO.getById(sessionId);
    }

    @Override
    public ExamSection resolveExamSection(ExaminerSchedule schedule, Session session) {
        Integer sectionId = resolveSectionId(schedule, session);
        if (sectionId == null) {
            return ExamSection.THEORY;
        }
        model.ExamSection section = sectionDAO.getById(sectionId);
        if (section == null || section.getSectionName() == null) {
            return ExamSection.THEORY;
        }
        ExamSection resolved = ExamSection.fromValue(section.getSectionName());
        return resolved != null ? resolved : ExamSection.THEORY;
    }

    @Override
    public model.ExamSection getExamSectionModel(ExaminerSchedule schedule, Session session) {
        Integer sectionId = resolveSectionId(schedule, session);
        if (sectionId == null) {
            return null;
        }
        return sectionDAO.getById(sectionId);
    }

    private Integer resolveSectionId(ExaminerSchedule schedule, Session session) {
        if (schedule != null && schedule.getExamSectionId() != null) {
            return schedule.getExamSectionId();
        }
        if (session != null) {
            return sessionDAO.getExamSectionId(session.getSessionId());
        }
        return null;
    }
}
