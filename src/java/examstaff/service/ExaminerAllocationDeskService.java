package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;

import java.util.List;
import java.util.Map;

public interface ExaminerAllocationDeskService {

    ExaminerAllocationViewDTO buildAllocationView(int examId, int fallbackExamId, List<ExamSummaryDTO> allExams);

    Map<Integer, UserDTO> buildExaminerMap();

    ExaminerAllocationActionResultDTO assignExaminer(int targetExamId, int areaId,
            int examinerUserId, int staffId);

    ExaminerAllocationActionResultDTO removeExaminer(String slotKey);
}
