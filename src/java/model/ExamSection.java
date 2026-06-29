package model;

public class ExamSection {

    private int examSectionId;
    private String sectionName;

    public ExamSection() {
    }

    public ExamSection(int examSectionId, String sectionName) {
        this.examSectionId = examSectionId;
        this.sectionName = sectionName;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
