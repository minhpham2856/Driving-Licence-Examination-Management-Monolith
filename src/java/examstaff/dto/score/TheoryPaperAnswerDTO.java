package examstaff.dto.score;


public class TheoryPaperAnswerDTO {
    private int questionNo;
    private String imageUrl;
    private String correctAnswer;
    private String studentAnswer;
    private boolean correct;

    public int getQuestionNo() { return questionNo; }
    public void setQuestionNo(int v) { this.questionNo = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { this.imageUrl = v; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String v) { this.correctAnswer = v; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String v) { this.studentAnswer = v; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean v) { this.correct = v; }
}
