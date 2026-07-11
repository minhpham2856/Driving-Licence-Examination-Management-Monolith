package service;

import dto.ExamSummaryDTO;
import dto.UserDTO;
import dto.examstaff.ExaminerAllocationActionResultDTO;
import dto.examstaff.ExaminerAllocationViewDTO;

import java.util.List;
import java.util.Map;

public interface ExaminerAllocationDeskService {

    ExaminerAllocationViewDTO buildAllocationView(int examId, int sessionId, List<ExamSummaryDTO> allSessions);

    Map<Integer, UserDTO> buildExaminerMap();

    ExaminerAllocationActionResultDTO assignExaminer(int targetSessionId, int areaId,
            int examinerUserId, int staffId);

    ExaminerAllocationActionResultDTO removeExaminer(String slotKey);
}
