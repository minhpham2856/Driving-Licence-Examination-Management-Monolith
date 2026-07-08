package service;

import dto.CandidateEnrollmentDTO;
import model.Candidate;
import model.ExamEnrollment;
import java.util.List;

public interface CandidateEnrollmentQueryService {
    List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId);
    CandidateEnrollmentDTO toDto(Candidate candidate, ExamEnrollment enrollment);
}
