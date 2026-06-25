package model.exam;

import java.sql.Timestamp;

public class TheoryPaper {

    private int theoryPaperId;
    private int examCandidateId;
    private int examDeviceId;
    private Timestamp startedAt;
    private Timestamp submittedAt;

    public TheoryPaper() {
    }

    public TheoryPaper(int theoryPaperId, int examCandidateId, int examDeviceId, Timestamp startedAt, Timestamp submittedAt) {
        this.theoryPaperId = theoryPaperId;
        this.examCandidateId = examCandidateId;
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

    public int getExamCandidateId() {
        return examCandidateId;
    }

    public void setExamCandidateId(int examCandidateId) {
        this.examCandidateId = examCandidateId;
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
}
