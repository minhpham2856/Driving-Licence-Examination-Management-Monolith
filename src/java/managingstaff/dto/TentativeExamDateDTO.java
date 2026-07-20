package managingstaff.dto;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

public class TentativeExamDateDTO {
    private int id;
    private Date examDate;
    private int licenceId;
    private String licenceClass;
    private int registeredCount;
    private String status;
    private String cancelReason;
    private Timestamp cancelledAt;
    private int cancelledBy;
    private int cancelledRegistrationCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }
    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int licenceId) { this.licenceId = licenceId; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String licenceClass) { this.licenceClass = licenceClass; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Timestamp getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Timestamp cancelledAt) { this.cancelledAt = cancelledAt; }
    public int getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(int cancelledBy) { this.cancelledBy = cancelledBy; }
    public int getCancelledRegistrationCount() { return cancelledRegistrationCount; }
    public void setCancelledRegistrationCount(int value) { this.cancelledRegistrationCount = value; }
    public int getRemainingSlots() { return Math.max(0, 50 - registeredCount); }
    public boolean isFull() { return registeredCount >= 50; }
    public boolean isCancelled() { return "Cancelled".equalsIgnoreCase(status); }
    public boolean isCancellable() {
        return !isCancelled() && examDate != null
                && !LocalDate.now().isAfter(examDate.toLocalDate().minusDays(7));
    }
    public Date getCancellationDeadline() {
        return examDate == null ? null : Date.valueOf(examDate.toLocalDate().minusDays(7));
    }
}
