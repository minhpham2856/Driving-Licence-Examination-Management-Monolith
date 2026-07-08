package service;

import dto.AllocateResultDTO;
import dto.AssignmentDTO;
import dto.ServiceResult;
import dto.SessionViewDTO;
import dto.UserRowDTO;
import model.ExamArea;
import model.ExamDevice;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AllocationService {

    List<SessionViewDTO> getAllSessions();

    SessionViewDTO getSessionById(int sessionId);

    List<SessionViewDTO> getSessionsByExamDate(Date date);

    List<ExamArea> getAreasBySessionId(int sessionId);

    ExamArea getAreaById(int id);

    List<ExamDevice> getDevicesByAreaId(int areaId);

    List<UserRowDTO> getActiveExaminers();

    boolean isAreaInSession(int sessionId, int areaId);

    List<AssignmentDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates);

    List<AssignmentDTO> getAssignmentsBySessionId(int sessionId);

    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates);

    boolean assignExaminer(AssignmentDTO slot);

    boolean removeAssignment(String slotKey);

    ServiceResult<AllocateResultDTO> autoAllocateSession(int sessionId);

    ServiceResult<AllocateResultDTO> autoAllocateCandidate(int sessionId, int registrationId);
}
