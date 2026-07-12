package model;

public class LicenceQuestion {

    private int licenceQuestionId;
    private int licenceId;
    private int questionId;
    private Licence licence;
    private Question question;

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

    public Licence getLicence() {
        return licence;
    }

    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
