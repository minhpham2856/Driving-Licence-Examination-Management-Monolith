package candidate.dto;

/** Câu hỏi hiển thị trong bài thi (tự chứa, không phụ thuộc shared.model.Question). */
public class QuestionView {
    private int questionId;
    private int questionNumber;
    private String imageUrl;
    private String correctAnswer;
    private boolean critical;

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int v) { this.questionId = v; }
    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int v) { this.questionNumber = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { this.imageUrl = v; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String v) { this.correctAnswer = v; }
    public boolean isCritical() { return critical; }
    public void setCritical(boolean v) { this.critical = v; }
}
