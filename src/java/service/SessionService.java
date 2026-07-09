package service;

import dto.AssignmentDTO;
import dto.ServiceResult;
import dto.SessionViewDTO;
import dto.SessionStartDTO;
import enums.SectionType;
import model.ExaminerSchedule;
import model.Session;
import java.util.List;
import model.ExamSection;

public interface SessionService {

    ServiceResult<SessionStartDTO> startSession(int sessionId, int staffUserId);

    ServiceResult<SessionStartDTO> endSession(int sessionId);

    List<AssignmentDTO> getLoginEligibleAssignments(int examinerUserId);

    SessionViewDTO getSessionById(int id);

    List<SessionViewDTO> getAllSessions();

    List<SessionViewDTO> getActiveSessions();

    Session getById(int sessionId);

    SectionType getExamSection(ExaminerSchedule schedule, Session session);

    ExamSection getExamSectionModel(ExaminerSchedule schedule, Session session);

    int getActiveSessionId();

    void setActiveSessionId(int sessionId);

    void clearActiveSessionId(int sessionId);
}
