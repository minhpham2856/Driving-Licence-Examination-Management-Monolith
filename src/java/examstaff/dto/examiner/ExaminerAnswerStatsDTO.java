package examstaff.dto.examiner;


 // DTO carrying the answer statistics for a candidate's theory paper.
public class ExaminerAnswerStatsDTO {

    // Number of questions answered correctly.
    private int correct;

    // Number of questions answered incorrectly.
    private int wrong;

    // Number of questions left unanswered (blank or null).
    private int unanswered;

    // Getter: returns the number of correct answers
    public int getCorrect() { return correct; }
    // Setter: assigns the correct answer count from the DB query result
    public void setCorrect(int correct) { this.correct = correct; }
    // Getter: returns the number of wrong answers
    public int getWrong() { return wrong; }
    // Setter: assigns the wrong answer count from the DB query result
    public void setWrong(int wrong) { this.wrong = wrong; }
    // Getter: returns the number of unanswered questions
    public int getUnanswered() { return unanswered; }
    // Setter: assigns the unanswered count from the DB query result
    public void setUnanswered(int unanswered) { this.unanswered = unanswered; }
}
