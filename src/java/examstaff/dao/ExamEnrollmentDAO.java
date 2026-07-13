package examstaff.dao;

import shared.model.ExamEnrollment;
import java.sql.Date;
import java.util.List;

public interface ExamEnrollmentDAO {

    // --- CleanMyBranch methods (Æ°u tiÃªn) ---
    ExamEnrollment getById(int examEnrollmentId);

    int insert(ExamEnrollment enrollment);

    boolean update(ExamEnrollment enrollment);

    boolean delete(int examEnrollmentId);

    int countAll();

    List<ExamEnrollment> getByExamId(int examId);

    ExamEnrollment getByExamAndCandidate(int examId, int candidateId);

    boolean updateExaminerProfile(int candidateId, String fullName, Date dob, String govIdNo, String email,
            String phoneNo, String address, String sexDb, String reasonForTaking);

    boolean markAbsent(int candidateId);

    boolean clearAbsentMarking(int candidateId);

    boolean assignExamDevice(int candidateId, int examId, int deviceId);

    // --- mainTest-only methods ---
    List<ExamEnrollment> searchByExam(int examId, String keyword);

    ExamEnrollment getLatestByCandidateId(int candidateId);

    Integer findCandidateIdByGovIdAndExam(String governmentIdNumber, int examId);
}

