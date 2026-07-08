package dto.examstaff;

public class SessionSelectResultDTO {

    private boolean success;
    private int examId;
    private int sessionId;
    private String errorMessage;
    private boolean clearProcedureOnExamChange;
    private boolean clearCandidateCache;
    private int newExamId;
    private int newSessionId;
    private Integer previousExamId;
    private Integer previousSessionId;

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

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
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

    public int getNewSessionId() {
        return newSessionId;
    }

    public void setNewSessionId(int newSessionId) {
        this.newSessionId = newSessionId;
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
}
