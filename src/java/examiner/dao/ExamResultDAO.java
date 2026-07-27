package examiner.dao;

import shared.model.ExamResult;

// DAO contract for ExamResult persistence; examiner module SQL boundary.
public interface ExamResultDAO {

    // Returns ExamResultId for one enrollment (0 if no result row exists).
    int getExamResultIdByExamEnrollmentId(int examEnrollmentId);

    // Loads the exam result row for one enrollment.
    ExamResult getByExamEnrollmentId(int examEnrollmentId);

    // Inserts a new exam result row and returns generated id.
    int add(ExamResult result);

    // Updates pass flag only (does not change ResultDate).
    boolean updateIsPassed(int examResultId, boolean passed);

    // Updates pass flag and stamps ResultDate = now (finalize / theory upsert).
    boolean updatePassed(int examResultId, boolean passed);
}
