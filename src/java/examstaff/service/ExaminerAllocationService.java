package examstaff.service;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.user.UserDTO;
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

    /** PhÃ¢n sÃ¢n thá»±c hÃ nh cho thÃ­ sinh Ä‘Ã£ Ä‘á»— lÃ½ thuyáº¿t (cÃ¢n báº±ng táº£i trÃªn sÃ¢n cÃ³ giÃ¡m kháº£o). */
    AutoAllocateResultDTO autoAllocatePracticalSession(int sessionId);
}

