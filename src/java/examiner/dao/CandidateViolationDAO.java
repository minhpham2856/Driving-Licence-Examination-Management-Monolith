package examiner.dao;

import shared.model.CandidateViolation;

public interface CandidateViolationDAO {
    boolean addAndSuspend(int candidateId, CandidateViolation violation);

    CandidateViolation getLatestByExamAndSbd(int examId, int sbd, String sectionType);
}
