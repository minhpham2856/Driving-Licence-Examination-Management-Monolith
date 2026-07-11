package dto.examstaff;

import dto.AutoAllocateResultDTO;
import dto.exam.ExamRegistrationDTO;

import java.util.List;

public class ProcedurePaymentOutcomeDTO {

    public enum Status {
        SUCCESS,
        ALREADY_PAID,
        NO_PHOTO,
        PAYMENT_FAILED,
        PROFILE_NOT_FOUND
    }

    private Status status = Status.PAYMENT_FAILED;
    private ExamRegistrationDTO profile;
    private List<ExamRegistrationDTO> queue;
    private int boardExamId;
    private ProcedureFeeResultDTO feePreview;
    private AutoAllocateResultDTO allocResult;
    private String paymentAuditDetail;
    private boolean auditAllocate;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    public List<ExamRegistrationDTO> getQueue() {
        return queue;
    }

    public void setQueue(List<ExamRegistrationDTO> queue) {
        this.queue = queue;
    }

    public int getBoardExamId() {
        return boardExamId;
    }

    public void setBoardExamId(int boardExamId) {
        this.boardExamId = boardExamId;
    }

    public ProcedureFeeResultDTO getFeePreview() {
        return feePreview;
    }

    public void setFeePreview(ProcedureFeeResultDTO feePreview) {
        this.feePreview = feePreview;
    }

    public AutoAllocateResultDTO getAllocResult() {
        return allocResult;
    }

    public void setAllocResult(AutoAllocateResultDTO allocResult) {
        this.allocResult = allocResult;
    }

    public String getPaymentAuditDetail() {
        return paymentAuditDetail;
    }

    public void setPaymentAuditDetail(String paymentAuditDetail) {
        this.paymentAuditDetail = paymentAuditDetail;
    }

    public boolean isAuditAllocate() {
        return auditAllocate;
    }

    public void setAuditAllocate(boolean auditAllocate) {
        this.auditAllocate = auditAllocate;
    }
}
