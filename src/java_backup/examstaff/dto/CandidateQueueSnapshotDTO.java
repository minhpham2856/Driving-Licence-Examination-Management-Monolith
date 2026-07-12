package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

public class CandidateQueueSnapshotDTO {

    private List<ExamRegistrationDTO> fullQueue;
    private List<ExamRegistrationDTO> activeQueue;
    private List<ExamRegistrationDTO> procedureDone;
    private int resolvedExamId;

    public List<ExamRegistrationDTO> getFullQueue() {
        return fullQueue;
    }

    public void setFullQueue(List<ExamRegistrationDTO> fullQueue) {
        this.fullQueue = fullQueue;
    }

    public List<ExamRegistrationDTO> getActiveQueue() {
        return activeQueue;
    }

    public void setActiveQueue(List<ExamRegistrationDTO> activeQueue) {
        this.activeQueue = activeQueue;
    }

    public List<ExamRegistrationDTO> getProcedureDone() {
        return procedureDone;
    }

    public void setProcedureDone(List<ExamRegistrationDTO> procedureDone) {
        this.procedureDone = procedureDone;
    }

    public int getResolvedExamId() {
        return resolvedExamId;
    }

    public void setResolvedExamId(int resolvedExamId) {
        this.resolvedExamId = resolvedExamId;
    }
}
