package service;

import dto.candidate.CandidateEnrollmentDTO;

import java.util.List;

public interface CandidatePhotoService {
    void normalizeQueue(String appRoot, List<CandidateEnrollmentDTO> qList);
}
