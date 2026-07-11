package service;


import dto.AutoAllocateResultDTO;
import dto.ExaminerSlotDTO;

import dto.SessionDTO;

import dto.UserDTO;

import model.ExamArea;
import model.ExamDevice;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExaminerAllocationService {
    List<SessionDTO> getAllSessions();
    SessionDTO getSessionById(int sessionId);
    List<SessionDTO> getSessionsByExamDate(Date date);
    
    List<ExamArea> getAreasBySessionId(int sessionId);
    ExamArea getAreaById(int id);
    List<ExamDevice> getDevicesByAreaId(int areaId);
    
    List<UserDTO> getActiveExaminers();
    
    boolean isAreaInSession(int sessionId, int areaId);

    List<ExamArea> getAvailableAreasForSession(int sessionId);
    
    List<ExaminerSlotDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> sessionDates);
    List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId);
    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> sessionDates);
    boolean assignExaminer(ExaminerSlotDTO slot);
    boolean removeAssignment(String slotKey);
    
    AutoAllocateResultDTO autoAllocateSession(int sessionId);
    AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId);

    /** Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có giám khảo). */
    AutoAllocateResultDTO autoAllocatePracticalSession(int sessionId);

    AutoAllocateResultDTO autoAllocatePracticalCandidate(int sessionId, int registrationId);
}
