package dao;

import model.exam.ExamEnrollment;
import java.util.List;

public interface ExamEnrollmentDAO {
    ExamEnrollment findBySessionAndCandidate(int sessionId, int candidateId);
    boolean assignExamDevice(int candidateId, int sessionId, int targetDevice);
    List<ExamEnrollment> getEnrollmentsBySession(int sessionId);
    List<ExamEnrollment> findBySessionId(int sessionId);
    boolean update(ExamEnrollment e);
}
