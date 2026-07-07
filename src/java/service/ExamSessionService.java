package service;

import enums.ExamSection;
import model.ExaminerSchedule;
import model.Session;

public interface ExamSessionService {

    Session getById(int sessionId);

    ExamSection resolveExamSection(ExaminerSchedule schedule, Session session);

    model.ExamSection getExamSectionModel(ExaminerSchedule schedule, Session session);
}
