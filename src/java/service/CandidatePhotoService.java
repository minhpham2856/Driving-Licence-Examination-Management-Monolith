package service;


import dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidatePhotoService {
    void normalizeQueue(String appRoot, List<ExamRegistrationDTO> qList);
}
