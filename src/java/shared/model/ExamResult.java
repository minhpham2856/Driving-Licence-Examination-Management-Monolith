package shared.model;

import java.sql.Timestamp;

public class ExamResult {

    private int examResultId;
    private int examEnrollmentId;
    private boolean isPassed;
    private Timestamp resultDate;
    private ExamEnrollment examEnrollment;

    public ExamResult() {
    }

    public ExamResult(int examResultId, int examEnrollmentId, boolean isPassed, Timestamp resultDate) {
        this.examResultId = examResultId;
        this.examEnrollmentId = examEnrollmentId;
        this.isPassed = isPassed;
        this.resultDate = resultDate;
    }

    public int getExamResultId() {
        return examResultId;
    }

    public void setExamResultId(int examResultId) {
        this.examResultId = examResultId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public void setPassed(boolean passed) {
        isPassed = passed;
    }

    public Timestamp getResultDate() {
        return resultDate;
    }

    public void setResultDate(Timestamp resultDate) {
        this.resultDate = resultDate;
    }

    public ExamEnrollment getExamEnrollment() {
        return examEnrollment;
    }

    public void setExamEnrollment(ExamEnrollment examEnrollment) {
        this.examEnrollment = examEnrollment;
    }
}

