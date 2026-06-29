package service;

import dto.AllocateResultDTO;
import dto.AssignmentDTO;
import dto.ServiceResult;
import dto.ExamViewDTO;
import dto.UserRowDTO;
import model.ExamArea;
import model.ExamDevice;
import model.ExamEnrollment;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AllocationService {

    List<ExamViewDTO> getAllExams();

    ExamViewDTO getExamById(int examId);

    List<ExamViewDTO> getExamsByExamDate(Date date);

    List<ExamArea> getAreasByExamId(int examId);

    // Candidate-room allocation (ported from the examstaff branch).
    // Main models candidates per session as ExamEnrollment; there is no
    // candidate-to-theory-room column, so autoAllocate computes a capacity
    // balanced plan and reports it rather than persisting a room link.
    List<ExamArea> getActiveTheoryRooms();

    List<ExamEnrollment> getCandidatesByExam(int examId);

    ServiceResult<Boolean> checkInCandidate(int candidateId);

    ExamArea getAreaById(int id);

    List<ExamDevice> getDevicesByAreaId(int areaId);

    List<UserRowDTO> getActiveExaminers();

    boolean isAreaInExam(int examId, int areaId);

    List<AssignmentDTO> getAssignmentsByExamDate(Date date, Map<Integer, Date> examDates);

    List<AssignmentDTO> getAssignmentsByExamId(int examId);

    Set<Integer> getBusyExaminerIds(Date examDate, Map<Integer, Date> examDates);

    boolean assignExaminer(AssignmentDTO slot);

    boolean removeAssignment(String slotKey);

    ServiceResult<AllocateResultDTO> autoAllocateExam(int examId);

    ServiceResult<AllocateResultDTO> autoAllocateCandidate(int examId, int registrationId);
}
