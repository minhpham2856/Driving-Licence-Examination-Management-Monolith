package examiner.dao;

import shared.model.TheoryPaper;

// DAO contract for TheoryPaper persistence; examiner module SQL boundary.
public interface TheoryPaperDAO {

    // Loads theory paper via enrollment (joins ExamEnrollmentSection).
    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);

    // Loads theory paper by its ExamEnrollmentSectionId.
    TheoryPaper getByExamEnrollmentSectionId(int examEnrollmentSectionId);
}
