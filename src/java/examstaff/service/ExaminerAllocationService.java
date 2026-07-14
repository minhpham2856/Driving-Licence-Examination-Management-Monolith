package examstaff.service;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import shared.model.ExamArea;

import java.util.List;

public interface ExaminerAllocationService {
    ExamSummaryDTO getSessionById(int sessionId);

    ExamArea getAreaById(int id);

    List<UserDTO> getActiveExaminers();

    List<ExamArea> getAvailableAreasForSession(int sessionId);

    List<ExaminerSlotDTO> getAssignmentsBySessionId(int sessionId);

    boolean assignExaminer(ExaminerSlotDTO slot);

    boolean removeAssignment(String slotKey);

    AutoAllocateResultDTO autoAllocateSession(int sessionId);

    AutoAllocateResultDTO autoAllocateCandidate(int sessionId, int registrationId);

    /** Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có giám khảo). */
    AutoAllocateResultDTO autoAllocatePracticalSession(int sessionId);
}

