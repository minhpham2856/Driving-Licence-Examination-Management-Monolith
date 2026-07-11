package dao;
import model.ExamEnrollment;
import java.sql.Date;
import java.util.List;
public interface ExamEnrollmentDAO {
    ExamEnrollment getById(int examEnrollmentId);
    int insert(ExamEnrollment enrollment);
    boolean update(ExamEnrollment enrollment);
    boolean delete(int examEnrollmentId);
    int countAll();
    List<ExamEnrollment> getByExamId(int examId);
    boolean updateExaminerProfile(int candidateId, String fullName, Date dob, String govIdNo, String email,
            String phoneNo, String address, String sexDb, String reasonForTaking);
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    boolean assignExamDevice(int regId, int examId, int deviceId);
    ExamEnrollment getByExamAndCandidate(int examId, int candidateId);
}
