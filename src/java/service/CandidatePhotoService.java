package service;

import dto.CandidateEnrollmentDTO;

import java.util.List;

public interface CandidatePhotoService {
    void normalizeQueue(String appRoot, List<CandidateEnrollmentDTO> qList);
}
