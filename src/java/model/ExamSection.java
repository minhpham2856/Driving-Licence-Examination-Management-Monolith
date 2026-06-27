package model;

public class ExamSection {
    private int examSectionId;
    private String sectionType;
    private int licenceId;
    private Integer durationMinutes;
    private int examId;
    private Licence licence;
    private Exam exam;

    public ExamSection() {
    }

    public ExamSection(int examSectionId, String sectionType, int licenceId, Integer durationMinutes, int examId) {
        this.examSectionId = examSectionId;
        this.sectionType = sectionType;
        this.licenceId = licenceId;
        this.durationMinutes = durationMinutes;
        this.examId = examId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public String getSectionType() {
        return sectionType;
    }

    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public Licence getLicence() {
        return licence;
    }

    public void setLicence(Licence licence) {
        this.licence = licence;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}
