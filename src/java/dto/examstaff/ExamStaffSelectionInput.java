package dto.examstaff;

public class ExamStaffSelectionInput {

    private int urlSessionId;
    private Integer selectedExamId;
    private Integer selectedSessionId;
    private Integer cachedExamId;
    private Integer cachedSessionId;

    public int getUrlSessionId() {
        return urlSessionId;
    }

    public void setUrlSessionId(int urlSessionId) {
        this.urlSessionId = urlSessionId;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public Integer getSelectedSessionId() {
        return selectedSessionId;
    }

    public void setSelectedSessionId(Integer selectedSessionId) {
        this.selectedSessionId = selectedSessionId;
    }

    public Integer getCachedExamId() {
        return cachedExamId;
    }

    public void setCachedExamId(Integer cachedExamId) {
        this.cachedExamId = cachedExamId;
    }

    public Integer getCachedSessionId() {
        return cachedSessionId;
    }

    public void setCachedSessionId(Integer cachedSessionId) {
        this.cachedSessionId = cachedSessionId;
    }
}
