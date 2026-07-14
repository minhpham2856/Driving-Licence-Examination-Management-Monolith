package shared.model;

public class LicenceExamSection {

    private int licenceExamSectionId;
    private int licenceId;
    private int examSectionId;
    private Integer durationMinutes;
    private Licence licence;
    private ExamSection examSection;

    public LicenceExamSection() {
    }

    public LicenceExamSection(int licenceExamSectionId, int licenceId, int examSectionId, Integer durationMinutes) {
        this.licenceExamSectionId = licenceExamSectionId;
        this.licenceId = licenceId;
        this.examSectionId = examSectionId;
        this.durationMinutes = durationMinutes;
    }

    public int getLicenceExamSectionId() {
        return licenceExamSectionId;
    }

    public void setLicenceExamSectionId(int licenceExamSectionId) {
        this.licenceExamSectionId = licenceExamSectionId;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
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

