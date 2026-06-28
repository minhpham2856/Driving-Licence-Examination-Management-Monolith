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
}
