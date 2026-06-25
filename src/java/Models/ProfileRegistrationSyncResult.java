package Models;

/**
 * Kết quả so khớp ExamRegistration.RegistrationStatus với trạng thái tài liệu.
 */
public class ProfileRegistrationSyncResult {

    private String expectedStatus;
    private String actualStatus;
    private boolean aligned;
    private boolean updated;
    private boolean documentMarkersUpdated;
    private String message;
    private String userNotice;

    public String getExpectedStatus() {
        return expectedStatus;
    }

    public void setExpectedStatus(String expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    public String getActualStatus() {
        return actualStatus;
    }

    public void setActualStatus(String actualStatus) {
        this.actualStatus = actualStatus;
    }

    public boolean isAligned() {
        return aligned;
    }

    public void setAligned(boolean aligned) {
        this.aligned = aligned;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isDocumentMarkersUpdated() {
        return documentMarkersUpdated;
    }

    public void setDocumentMarkersUpdated(boolean documentMarkersUpdated) {
        this.documentMarkersUpdated = documentMarkersUpdated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** Thông báo ngắn gọn cho thí sinh (không chứa tên cột DB). */
    public String getUserNotice() {
        return userNotice;
    }

    public void setUserNotice(String userNotice) {
        this.userNotice = userNotice;
    }
}
