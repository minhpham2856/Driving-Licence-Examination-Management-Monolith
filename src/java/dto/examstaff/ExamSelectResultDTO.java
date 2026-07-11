package dto.examstaff;

public class ExamSelectResultDTO {

    private boolean success;
    private int examId;
    private String errorMessage;
    private boolean clearProcedureOnExamChange;
    private boolean clearCandidateCache;
    private int newExamId;
    private Integer previousExamId;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isClearProcedureOnExamChange() {
        return clearProcedureOnExamChange;
    }

    public void setClearProcedureOnExamChange(boolean clearProcedureOnExamChange) {
        this.clearProcedureOnExamChange = clearProcedureOnExamChange;
    }

    public boolean isClearCandidateCache() {
        return clearCandidateCache;
    }

    public void setClearCandidateCache(boolean clearCandidateCache) {
        this.clearCandidateCache = clearCandidateCache;
    }

    public int getNewExamId() {
        return newExamId;
    }

    public void setNewExamId(int newExamId) {
        this.newExamId = newExamId;
    }

    public Integer getPreviousExamId() {
        return previousExamId;
    }

    public void setPreviousExamId(Integer previousExamId) {
        this.previousExamId = previousExamId;
    }
}
