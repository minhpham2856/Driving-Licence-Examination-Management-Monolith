package managingstaff.dto;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import shared.util.TentativeExamDatePolicy;

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
    private String policeStatus;
    private int officialCandidateCount;

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
    public String getPoliceStatus() { return policeStatus; }
    public void setPoliceStatus(String policeStatus) { this.policeStatus = policeStatus; }
    public int getOfficialCandidateCount() { return officialCandidateCount; }
    public void setOfficialCandidateCount(int officialCandidateCount) {
        this.officialCandidateCount = officialCandidateCount;
    }
    public int getRemainingSlots() { return Math.max(0, 50 - registeredCount); }
    public boolean isFull() { return registeredCount >= 50; }
    public boolean isCancelled() { return "Cancelled".equalsIgnoreCase(status); }
    public boolean isLocked() { return "Locked".equalsIgnoreCase(status); }
    public boolean isOpen() { return status == null || "Open".equalsIgnoreCase(status); }
    public boolean isNotSentToPolice() { return policeStatus == null || "NOT_SENT".equalsIgnoreCase(policeStatus); }
    public boolean isPendingPolice() { return "PENDING".equalsIgnoreCase(policeStatus); }
    public boolean isPoliceCompleted() { return "COMPLETED".equalsIgnoreCase(policeStatus); }
    public boolean isSendableToPolice() {
        return !isCancelled() && (isOpen() || isLocked()) && isNotSentToPolice()
                && registeredCount > 0 && examDate != null
                && !examDate.toLocalDate().isBefore(LocalDate.now());
    }
    public boolean isCancellable() {
        return isOpen() && examDate != null
                && !TentativeExamDatePolicy.shouldBeLocked(
                        examDate.toLocalDate(), LocalDate.now());
    }
    public Date getCancellationDeadline() {
        return examDate == null ? null
                : Date.valueOf(TentativeExamDatePolicy.lockDate(examDate.toLocalDate()));
    }
}
