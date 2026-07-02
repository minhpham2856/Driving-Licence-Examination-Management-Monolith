package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidateQueueQueryService {

    List<ExamRegistrationDTO> listByExamId(int examId);

    ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd);

    void normalizePhotoPaths(String webRootPath, List<ExamRegistrationDTO> queue);
}
