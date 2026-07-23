package shared.service;

import shared.dto.ExamAccessOtpDTO;

public interface ExamAccessOtpService {

    ExamAccessOtpDTO getCurrent(int examId, int examSectionId, int examAreaId);

    boolean verify(int examId, int examSectionId, int examAreaId, String submittedCode);
}
