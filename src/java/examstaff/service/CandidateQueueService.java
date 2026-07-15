package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;

import java.util.List;

public interface CandidateQueueService {

    CandidateQueueSnapshotDTO refreshQueue(ExamStaffQueueRefreshInput input);

    CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId);

    List<ExamRegistrationDTO> filterPendingForCall(List<ExamRegistrationDTO> queue);

    ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd);

    String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd);

    boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd);

    boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd);

    List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue);

    ExamRegistrationDTO findByExam(int examId, int fallbackExamId, String sbd);
}
