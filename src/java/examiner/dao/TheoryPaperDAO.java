package examiner.dao;

import examiner.model.TheoryPaper;

public interface TheoryPaperDAO {

    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);
}
