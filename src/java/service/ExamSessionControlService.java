package service;

import dto.ExaminerSlotDTO;
import dto.ServiceResult;
import dto.SessionDTO;
import dto.payload.SessionControlData;

import java.util.List;

public interface ExamSessionControlService {

    ServiceResult<SessionControlData> startSession(int sessionId, int staffUserId);

    ServiceResult<SessionControlData> endSession(int sessionId);

    List<ExaminerSlotDTO> getLoginEligibleAssignments(int examinerUserId);

    SessionDTO getSessionById(int id);

    List<SessionDTO> getAllSessions();

    List<SessionDTO> getActiveSessions();
}
