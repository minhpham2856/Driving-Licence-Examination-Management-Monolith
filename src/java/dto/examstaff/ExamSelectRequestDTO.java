package dto.examstaff;

public class ExamSelectRequestDTO {

    private int urlExamId;
    private Integer previousExamId;
    private String webRoot;

    public int getUrlExamId() {
        return urlExamId;
    }

    public void setUrlExamId(int urlExamId) {
        this.urlExamId = urlExamId;
    }

    public Integer getPreviousExamId() {
        return previousExamId;
    }

    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }
}
