package examstaff.model;

public class CandidateAnswer {

    private int candidateAnswerId;
    private int theoryPaperId;
    private int questionId;
    private String answer;
    private TheoryPaper theoryPaper;
    private Question question;

    public CandidateAnswer() {
    }

    public CandidateAnswer(int candidateAnswerId, int theoryPaperId, int questionId, String answer) {
        this.candidateAnswerId = candidateAnswerId;
        this.theoryPaperId = theoryPaperId;
        this.questionId = questionId;
        this.answer = answer;
    }

    public int getCandidateAnswerId() {
        return candidateAnswerId;
    }

    public void setCandidateAnswerId(int candidateAnswerId) {
        this.candidateAnswerId = candidateAnswerId;
    }

    public int getTheoryPaperId() {
        return theoryPaperId;
    }

    public void setTheoryPaperId(int theoryPaperId) {
        this.theoryPaperId = theoryPaperId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public TheoryPaper getTheoryPaper() {
        return theoryPaper;
    }

    public void setTheoryPaper(TheoryPaper theoryPaper) {
        this.theoryPaper = theoryPaper;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
