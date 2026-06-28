package dao;

import model.exam.ExamCandidate;

public interface ExamCandidateDAO {
    ExamCandidate findBySessionAndCandidate(int sessionId, int candidateId);
}
