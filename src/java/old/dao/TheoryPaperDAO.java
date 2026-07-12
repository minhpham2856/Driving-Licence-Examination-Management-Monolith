package dao;

import model.TheoryPaper;

public interface TheoryPaperDAO {

    TheoryPaper getByExamEnrollmentId(int examEnrollmentId);
}
