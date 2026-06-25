package enums;

public enum ExamSessionStatus {
    SCHEDULED("Scheduled"),
    OPEN("Open"),
    IN_PROGRESS("InProgress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String status;

    ExamSessionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static boolean canStartSession(String status) {
        return SCHEDULED.status.equalsIgnoreCase(status) || OPEN.status.equalsIgnoreCase(status);
    }

    public static boolean isSessionInProgress(String status) {
        return IN_PROGRESS.status.equalsIgnoreCase(status);
    }

    public static boolean isSessionEnded(String status) {
        return COMPLETED.status.equalsIgnoreCase(status) || CANCELLED.status.equalsIgnoreCase(status);
    }
}
