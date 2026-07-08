package service;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidateQueueQueryService {

    List<ExamRegistrationDTO> listBySessionId(int sessionId);

    List<ExamRegistrationDTO> listByExamId(int examId);

    ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd);

    void normalizePhotoPaths(String webRootPath, List<ExamRegistrationDTO> queue);
}
