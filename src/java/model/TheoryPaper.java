package model;

import java.sql.Timestamp;

public class TheoryPaper {

    private int theoryPaperId;
    private int examEnrollmentId;
    private int examDeviceId;
    private Timestamp startedAt;
    private Timestamp submittedAt;
    private ExamEnrollment examEnrollment;
    private ExamDevice examDevice;

    public TheoryPaper() {
    }

    public TheoryPaper(int theoryPaperId, int examEnrollmentId, int examDeviceId, Timestamp startedAt,
            Timestamp submittedAt) {
        this.theoryPaperId = theoryPaperId;
        this.examEnrollmentId = examEnrollmentId;
        this.examDeviceId = examDeviceId;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
    }

    public int getTheoryPaperId() {
        return theoryPaperId;
    }

    public void setTheoryPaperId(int theoryPaperId) {
        this.theoryPaperId = theoryPaperId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(int examDeviceId) {
        this.examDeviceId = examDeviceId;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ExamEnrollment getExamEnrollment() {
        return examEnrollment;
    }

    public void setExamEnrollment(ExamEnrollment examEnrollment) {
        this.examEnrollment = examEnrollment;
    }

    public ExamDevice getExamDevice() {
        return examDevice;
    }

    public void setExamDevice(ExamDevice examDevice) {
        this.examDevice = examDevice;
    }
}
