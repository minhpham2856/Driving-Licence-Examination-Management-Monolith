package enums;

public enum CandidateStatus {
    PENDING("Pending", "Chưa thi"),
    TESTING("Testing", "Đang thi"),
    AWAITING_SIGNATURE("AwaitingSignature", "chờ ký"),
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

    public static String candidateStatusLabel(String status) {
        if (status == null) return PENDING.labelVi;
        for (CandidateStatus cs : values()) {
            if (cs.status.equals(status)) {
                return cs.labelVi;
            }
        }
        return PENDING.labelVi;
    }

    public static boolean isCandidateAwaitingSignature(String status) {
        return AWAITING_SIGNATURE.status.equals(status);
    }

    public static boolean isCandidateDone(String status) {
        return DONE.status.equals(status);
    }

    public static boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null) return false;
        return registrationStatus.equals("CheckedIn") || registrationStatus.equals("Present") || registrationStatus.equals("Completed");
    }
}
