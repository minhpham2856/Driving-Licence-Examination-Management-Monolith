package Models;

public class Question {
    private int questionId;
    private int questionNumber;
    private String imageUrl;
    private String correctAnswer;
    private boolean isCritical;
    private int questionCategoryId;

    public Question() {
    }

    public Question(int questionId, int questionNumber, String imageUrl, String correctAnswer, boolean isCritical, int questionCategoryId) {
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.imageUrl = imageUrl;
        this.correctAnswer = correctAnswer;
        this.isCritical = isCritical;
        this.questionCategoryId = questionCategoryId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isIsCritical() {
        return isCritical;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setIsCritical(boolean isCritical) {
        this.isCritical = isCritical;
    }

    public int getQuestionCategoryId() {
        return questionCategoryId;
    }

    public void setQuestionCategoryId(int questionCategoryId) {
        this.questionCategoryId = questionCategoryId;
    }
}
