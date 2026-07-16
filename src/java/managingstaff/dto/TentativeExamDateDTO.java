package managingstaff.dto;

import java.sql.Date;

public class TentativeExamDateDTO {
    private int id;
    private Date examDate;
    private int licenceId;
    private String licenceClass;
    private int registeredCount;

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
    public int getRemainingSlots() { return Math.max(0, 50 - registeredCount); }
    public boolean isFull() { return registeredCount >= 50; }
}
