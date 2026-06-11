package Constants;

public final class ExamSessionStatus {

    public static final String SCHEDULED = "Scheduled";
    public static final String OPEN = "Open";
    public static final String IN_PROGRESS = "InProgress";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";

    private ExamSessionStatus() {
    }

    public static boolean canStart(String status) {
        return SCHEDULED.equalsIgnoreCase(status) || OPEN.equalsIgnoreCase(status);
    }

    public static boolean isInProgress(String status) {
        return IN_PROGRESS.equalsIgnoreCase(status);
    }

    public static boolean isEnded(String status) {
        return COMPLETED.equalsIgnoreCase(status) || CANCELLED.equalsIgnoreCase(status);
    }
}
