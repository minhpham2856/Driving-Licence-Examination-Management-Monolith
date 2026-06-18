package Models;

public class LicenceQuestion {
    private int licenceQuestionId;
    private int licenceId;
    private int questionId;

    public LicenceQuestion() {
    }

    public LicenceQuestion(int licenceQuestionId, int licenceId, int questionId) {
        this.licenceQuestionId = licenceQuestionId;
        this.licenceId = licenceId;
        this.questionId = questionId;
    }

    public int getLicenceQuestionId() {
        return licenceQuestionId;
    }

    public void setLicenceQuestionId(int licenceQuestionId) {
        this.licenceQuestionId = licenceQuestionId;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
}
