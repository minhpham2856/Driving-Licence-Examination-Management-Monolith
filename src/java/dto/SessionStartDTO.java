package dto;

public class SessionStartDTO {

    private String caLabel;
    private int examinerCount;

    public SessionStartDTO() {
    }

    public SessionStartDTO(String caLabel, int examinerCount) {
        this.caLabel = caLabel;
        this.examinerCount = examinerCount;
    }

    public String getCaLabel() {
        return caLabel;
    }

    public void setCaLabel(String caLabel) {
        this.caLabel = caLabel;
    }

    public int getExaminerCount() {
        return examinerCount;
    }

    public void setExaminerCount(int examinerCount) {
        this.examinerCount = examinerCount;
    }
}
