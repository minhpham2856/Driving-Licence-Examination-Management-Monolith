package examstaff.service;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.ExaminerSlotDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import shared.model.ExamArea;

import java.util.List;

public interface ExaminerAllocationService {
    ExamSummaryDTO getExamById(int examId);

    ExamArea getAreaById(int id);

    List<UserDTO> getActiveExaminers();

    List<ExamArea> getAvailableAreasForExam(int examId);

    List<ExaminerSlotDTO> getAssignmentsByExamId(int examId);

    boolean assignExaminer(ExaminerSlotDTO slot);

    boolean removeAssignment(String slotKey);

    AutoAllocateResultDTO autoAllocateExam(int examId);

    AutoAllocateResultDTO autoAllocateCandidate(int examId, int registrationId);

    /** Phân sân thực hành cho thí sinh đã đỗ lý thuyết (cân bằng tải trên sân có giám khảo). */
    AutoAllocateResultDTO autoAllocatePracticalExam(int examId);
}
