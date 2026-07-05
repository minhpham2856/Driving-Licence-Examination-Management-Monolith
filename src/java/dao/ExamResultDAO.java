package dao;

import model.ExamResult;

public interface ExamResultDAO {

    int getExamResultIdByExamEnrollmentId(int examEnrollmentId);

    int add(ExamResult result);

    boolean updatePassed(int examResultId, boolean passed);
}
