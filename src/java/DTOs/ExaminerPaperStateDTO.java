package DTOs;

 // DTO capturing the lifecycle state of a candidate's theory paper.
public class ExaminerPaperStateDTO {

    // Whether the candidate has opened / started the theory paper.
    private boolean started;

    // Whether the candidate has formally submitted the theory paper.
    private boolean submitted;

    // Getter: checks if the candidate has begun the theory paper
    public boolean isStarted() { return started; }
    // Setter: marks whether the theory paper has been started
    public void setStarted(boolean started) { this.started = started; }
    // Getter: checks if the candidate has submitted the theory paper
    public boolean isSubmitted() { return submitted; }
    // Setter: marks whether the theory paper has been submitted
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
}
