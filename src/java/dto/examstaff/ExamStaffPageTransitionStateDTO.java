package dto.examstaff;

public class ExamStaffPageTransitionStateDTO {

    private int examId;
    private boolean clearCandidateCache;
    private boolean clearProcedureState;
    private boolean persistSelection;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public boolean isClearCandidateCache() {
        return clearCandidateCache;
    }

    public void setClearCandidateCache(boolean clearCandidateCache) {
        this.clearCandidateCache = clearCandidateCache;
    }

    public boolean isClearProcedureState() {
        return clearProcedureState;
    }

    public void setClearProcedureState(boolean clearProcedureState) {
        this.clearProcedureState = clearProcedureState;
    }

    public boolean isPersistSelection() {
        return persistSelection;
    }

    public void setPersistSelection(boolean persistSelection) {
        this.persistSelection = persistSelection;
    }
}
