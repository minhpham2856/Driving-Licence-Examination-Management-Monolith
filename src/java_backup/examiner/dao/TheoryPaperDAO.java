package examiner.dao;

import shared.model.TheoryPaper;

public interface TheoryPaperDAO {

    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);
}

