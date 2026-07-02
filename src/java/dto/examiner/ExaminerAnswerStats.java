<<<<<<<< Updated upstream:src/java/dto/examiner/ExaminerAnswerStats.java
package dto.examiner;
========
package model.exam;
>>>>>>>> Stashed changes:src/java/model/exam/ExaminerAnswerStats.java

public class ExaminerAnswerStats {
    private int correct;
    private int wrong;
    private int unanswered;

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getWrong() {
        return wrong;
    }

    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

    public int getUnanswered() {
        return unanswered;
    }

    public void setUnanswered(int unanswered) {
        this.unanswered = unanswered;
    }
}
