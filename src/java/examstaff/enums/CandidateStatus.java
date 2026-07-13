package examstaff.enums;

public enum CandidateStatus {
    NOT_STARTED("Chưa thi"),
    IN_PROGRESS("Đang thi"),
    AWAITING_SIGNATURE("Chờ ký"),
    COMPLETED("Đã thi");

    private final String value;

    private CandidateStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CandidateStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CandidateStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static String candidateStatusLabel(String status) {
        if (status == null) return NOT_STARTED.value;
        for (CandidateStatus cs : values()) {
            if (cs.value.equals(status)) {
                return cs.value;
            }
        }
        return NOT_STARTED.value;
    }

    public static boolean isCandidateAwaitingSignature(String status) {
        CandidateStatus cs = fromValue(status);
        return cs == AWAITING_SIGNATURE;
    }

    public static boolean isCandidateDone(String status) {
        CandidateStatus cs = fromValue(status);
        return cs == COMPLETED;
    }

    public static boolean isPresentStatus(String registrationStatus) {
        if (registrationStatus == null) return false;
        return registrationStatus.equals("CheckedIn")
                || registrationStatus.equals("Present")
                || registrationStatus.equals("Completed");
    }
}