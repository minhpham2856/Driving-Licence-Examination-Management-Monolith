package managingstaff.dto;

import java.sql.Date;
import java.sql.Time;

/** Row displayed by Managing Staff for an exam/shift in the mainTest schema. */
public class SessionDTO {
    private int id;
    private String sessionName;
    private Date examDate;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private String status;
    private int maxCandidates;
    private int registeredCount;
    private String licenseCode;
    private String examTypeName;
    private String areaName;
    private int licenceId;
    private int areaId;
    private String centreName;
    private boolean editable;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }
    public Date getExamDate() { return examDate; }
    public void setExamDate(Date examDate) { this.examDate = examDate; }
    public Time getShiftStartTime() { return shiftStartTime; }
    public void setShiftStartTime(Time shiftStartTime) { this.shiftStartTime = shiftStartTime; }
    public Time getShiftEndTime() { return shiftEndTime; }
    public void setShiftEndTime(Time shiftEndTime) { this.shiftEndTime = shiftEndTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getMaxCandidates() { return maxCandidates; }
    public void setMaxCandidates(int maxCandidates) { this.maxCandidates = maxCandidates; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
    public String getLicenseCode() { return licenseCode; }
    public void setLicenseCode(String licenseCode) { this.licenseCode = licenseCode; }
    public String getExamTypeName() { return examTypeName; }
    public void setExamTypeName(String examTypeName) { this.examTypeName = examTypeName; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int licenceId) { this.licenceId = licenceId; }
    public int getAreaId() { return areaId; }
    public void setAreaId(int areaId) { this.areaId = areaId; }
    public String getCentreName() { return centreName; }
    public void setCentreName(String centreName) { this.centreName = centreName; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
}
