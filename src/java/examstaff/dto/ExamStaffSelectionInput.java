package examstaff.dto;

public class ExamStaffSelectionInput {

    private int urlExamId;
    private Integer selectedExamId;
    private Integer cachedExamId;

    public int getUrlExamId() {
        return urlExamId;
    }

    public void setUrlExamId(int urlExamId) {
        this.urlExamId = urlExamId;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public Integer getCachedExamId() {
        return cachedExamId;
    }

    public void setCachedExamId(Integer cachedExamId) {
        this.cachedExamId = cachedExamId;
    }
}
