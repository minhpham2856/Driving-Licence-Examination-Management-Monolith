package registrant.enums;

/** Trạng thái phần thi trên ExamEnrollmentSection.Status. */
public final class CandidateSectionStatus {

    public static final String PENDING = "Pending";
    public static final String TESTING = "Testing";
    public static final String AWAITING_SIGNATURE = "AwaitingSignature";
    public static final String DONE = "Done";

    private CandidateSectionStatus() {
    }

    public static String labelOf(String status) {
        if (status == null) {
            return "Chưa thi";
        }
        return switch (status) {
            case TESTING -> "Đang thi";
            case AWAITING_SIGNATURE -> "chờ ký";
            case DONE -> "Đã thi";
            default -> "Chưa thi";
        };
    }

    public static boolean isAwaitingSignature(String status) {
        return AWAITING_SIGNATURE.equals(status);
    }

    public static boolean isDone(String status) {
        return DONE.equals(status);
    }
}
