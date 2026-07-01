package examiner.model;

public class ScoreDeduction {

    private int scoreDeductionId;
    private int licenceId;
    private String reason;
    private double points;
    private boolean isCritical;
    private int examSectionId;
    private Licence licence;
    private ExamSection examSection;

    public ScoreDeduction() {
    }

    public ScoreDeduction(int scoreDeductionId, int licenceId, String reason, double points, boolean isCritical,
            int examSectionId) {
        this.scoreDeductionId = scoreDeductionId;
        this.licenceId = licenceId;
        this.reason = reason;
        this.points = points;
        this.isCritical = isCritical;
        this.examSectionId = examSectionId;
    }

    public int getScoreDeductionId() {
        return scoreDeductionId;
    }

    public void setScoreDeductionId(int scoreDeductionId) {
        this.scoreDeductionId = scoreDeductionId;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean critical) {
        isCritical = critical;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public Licence getLicence() {
        return licence;
    }

    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    public ExamSection getExamSection() {
        return examSection;
    }

    public void setExamSection(ExamSection examSection) {
        this.examSection = examSection;
    }
}
