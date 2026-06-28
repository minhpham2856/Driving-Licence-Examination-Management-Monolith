package enums;

public enum CandidateStatus {
    PENDING("Pending", "Chưa thi"),
    TESTING("Testing", "Đang thi"),
    AWAITING_SIGNATURE("AwaitingSignature", "Chờ ký"),
    DONE("Done", "Đã thi");

    private final String status;
    private final String labelVi;

    CandidateStatus(String status, String labelVi) {
        this.status = status;
        this.labelVi = labelVi;
    }

    public String getStatus() {
        return status;
    }

    public String getLabelVi() {
        return labelVi;
    }
}
