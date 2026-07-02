<<<<<<<< Updated upstream:src/java/dto/examiner/ExaminerPaperState.java
package dto.examiner;
========
package model.exam;
>>>>>>>> Stashed changes:src/java/model/exam/ExaminerPaperState.java

public class ExaminerPaperState {
    private boolean started;
    private boolean submitted;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}
