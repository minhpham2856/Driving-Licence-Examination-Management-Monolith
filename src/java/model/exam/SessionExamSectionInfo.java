package model.exam;

/** Phần thi gắn với một ca thi (Session_ExamSection). */
public class SessionExamSectionInfo {

    private int sectionId;
    private String sectionName;

    public SessionExamSectionInfo() {
    }

    public SessionExamSectionInfo(int sectionId, String sectionName) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}
