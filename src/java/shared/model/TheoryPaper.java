package shared.model;

import java.sql.Timestamp;

public class TheoryPaper {

    private int theoryPaperId;
    private int examEnrollmentSectionId;
    private Timestamp startedAt;
    private Timestamp submittedAt;
    private ExamEnrollmentSection examEnrollmentSection;

    public TheoryPaper() {
    }

    public TheoryPaper(int theoryPaperId, int examEnrollmentSectionId, Timestamp startedAt,
            Timestamp submittedAt) {
        this.theoryPaperId = theoryPaperId;
        this.examEnrollmentSectionId = examEnrollmentSectionId;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
    }

    public int getTheoryPaperId() {
        return theoryPaperId;
    }

    public void setTheoryPaperId(int theoryPaperId) {
        this.theoryPaperId = theoryPaperId;
    }

    public int getExamEnrollmentSectionId() {
        return examEnrollmentSectionId;
    }

    public void setExamEnrollmentSectionId(int examEnrollmentSectionId) {
        this.examEnrollmentSectionId = examEnrollmentSectionId;
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

    public ExamEnrollmentSection getExamEnrollmentSection() {
        return examEnrollmentSection;
    }

    public void setExamEnrollmentSection(ExamEnrollmentSection examEnrollmentSection) {
        this.examEnrollmentSection = examEnrollmentSection;
    }
}
