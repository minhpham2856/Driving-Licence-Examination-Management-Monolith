package examstaff.dto;

import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ProcedureResetOutcomeDTO {

    private boolean success;
    private String sbd;
    private int candidateId;
    private List<ExamRegistrationDTO> queue;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public List<ExamRegistrationDTO> getQueue() {
        return queue;
    }

    public void setQueue(List<ExamRegistrationDTO> queue) {
        this.queue = queue;
    }
}
