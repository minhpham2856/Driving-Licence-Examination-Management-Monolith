package dto;

public class ExamStartDTO {

    private String examLabel;
    private int examinerCount;

    public ExamStartDTO() {
    }

    public String getExamLabel() {
        return examLabel;
    }

    public void setexamLabel(String examLabel) {
        this.examLabel = examLabel;
    }

    public int getExaminerCount() {
        return examinerCount;
    }

    public void setExaminerCount(int examinerCount) {
        this.examinerCount = examinerCount;
    }
}
