package dao;

import model.exam.ExamEnrollment;
import java.util.List;
import dto.candidate.CandidateEnrollmentDTO;

public interface ExamEnrollmentDAO {

    ExamEnrollment findById(int examEnrollmentId);

    int insert(ExamEnrollment enrollment);

    boolean update(ExamEnrollment enrollment);

    boolean delete(int examEnrollmentId);

    int countAll();

    List<ExamEnrollment> getBySessionId(int sessionId);

    List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId);

    boolean updateExaminerProfile(int candidateId, String fullName, String dob, String govIdNo, String email, String phoneNo, String address, String sexDb, String reasonForTaking);
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    boolean assignExamDevice(int regId, int sessionId, int deviceId);
    model.exam.ExamEnrollment findBySessionAndCandidate(int sessionId, int candidateId);
}


