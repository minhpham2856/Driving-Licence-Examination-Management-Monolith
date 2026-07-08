package dto.examstaff;

public class SessionSelectRequestDTO {

    private int urlSessionId;
    private Integer previousExamId;
    private Integer previousSessionId;
    private String webRoot;

    public int getUrlSessionId() {
        return urlSessionId;
    }

    public void setUrlSessionId(int urlSessionId) {
        this.urlSessionId = urlSessionId;
    }

    public Integer getPreviousExamId() {
        return previousExamId;
    }

    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }

    public Integer getPreviousSessionId() {
        return previousSessionId;
    }

    public void setPreviousSessionId(Integer previousSessionId) {
        this.previousSessionId = previousSessionId;
    }

    public String getWebRoot() {
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }
}
