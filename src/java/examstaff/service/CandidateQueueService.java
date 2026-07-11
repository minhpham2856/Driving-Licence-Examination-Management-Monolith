package examstaff.service;

import dto.exam.ExamRegistrationDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffQueueRefreshInput;

import java.util.List;

public interface CandidateQueueService {

    CandidateQueueSnapshotDTO refreshQueue(ExamStaffQueueRefreshInput input);

    CandidateQueueSnapshotDTO buildSnapshot(List<ExamRegistrationDTO> queue, int examId, int sessionId);

    List<ExamRegistrationDTO> filterPendingForCall(List<ExamRegistrationDTO> queue);

    boolean isCallablePending(ExamRegistrationDTO candidate);

    ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd);

    String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd);

    String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd);

    boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd);

    boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd);

    List<ExamRegistrationDTO> listSuspendedInSession(List<ExamRegistrationDTO> queue);

    List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue);

    ExamRegistrationDTO findByExamOrSession(int examId, int sessionId, String sbd);
}
