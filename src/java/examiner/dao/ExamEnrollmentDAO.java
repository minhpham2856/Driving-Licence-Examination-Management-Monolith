package examiner.dao;

import shared.model.ExamEnrollment;
import java.util.List;

// DAO contract for ExamEnrollment persistence; examiner module SQL boundary.
public interface ExamEnrollmentDAO {

    // Updates device and allocated area on an enrollment row.
    boolean update(ExamEnrollment enrollment);

    // Inserts a new exam-day enrollment and returns generated id.
    int add(ExamEnrollment enrollment);

    // Lists all enrollments for one exam.
    List<ExamEnrollment> getByExamId(int examId);

    // Searches enrollments in one exam by candidate number, name, or gov id.
    List<ExamEnrollment> getFilteredByExam(int examId, String keyword);

    // Lists enrollments with candidate data for lightweight dashboard/list pages.
    List<ExamEnrollment> getWithCandidateByExam(int examId, String keyword);

    // Loads the enrollment row for one exam and candidate pair.
    ExamEnrollment getByExamAndCandidate(int examId, int candidateId);

    // Loads the most recent enrollment row for one candidate.
    ExamEnrollment getLatestByCandidateId(int candidateId);

    // Loads enrollment for gov id within one exam (null if none).
    ExamEnrollment getIfByGovIdAndExam(String governmentIdNumber, int examId);

    // Sets IsAbsent=true on the candidate linked to this enrollment.
    boolean markAbsent(int candidateId);

    // Clears IsAbsent on the candidate linked to this enrollment.
    boolean clearAbsentMarking(int candidateId);

    // Assigns an exam device to a candidate enrollment for one exam.
    boolean assignExamDevice(int candidateId, int examId, int deviceId);
}
