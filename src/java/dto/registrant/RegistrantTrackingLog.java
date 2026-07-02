package dto.registrant;

import java.util.Date;

/**
 * Một dòng nhật ký xử lý hồ sơ trên trang theo dõi tiến trình (track-profile.jsp).
 */
public class RegistrantTrackingLog {

    private Date timestamp;
    private String eventTitle;
    private String actorRole;
    private String statusClass;
    private String statusLabel;
    private String remarks;
    /** Khóa loại tác vụ — phục vụ filter track-profile. */
    private String category;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getStatusClass() {
        return statusClass;
    }

    public void setStatusClass(String statusClass) {
        this.statusClass = statusClass;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
