package Models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class ExamSession {
    private int id;
    private String sessionName;
    private int licenseTypeId;
    private int examTypeId;
    private Date examDate;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private int areaId;
    private String status; // 'Scheduled', 'Open', 'InProgress', 'Completed', 'Cancelled'
    private int maxCandidates;
    private int registeredCount;
    private Timestamp createdAt;

    // Helpers to store joined values if needed
    private String licenseCode;
    private String examTypeName;
    private String areaName;

    public ExamSession() {
    }

    public ExamSession(int id, String sessionName, int licenseTypeId, int examTypeId, Date examDate, Time shiftStartTime, Time shiftEndTime, int areaId, String status, int maxCandidates, int registeredCount, Timestamp createdAt) {
        this.id = id;
        this.sessionName = sessionName;
        this.licenseTypeId = licenseTypeId;
        this.examTypeId = examTypeId;
        this.examDate = examDate;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
        this.areaId = areaId;
        this.status = status;
        this.maxCandidates = maxCandidates;
        this.registeredCount = registeredCount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getLicenseTypeId() {
        return licenseTypeId;
    }

    public void setLicenseTypeId(int licenseTypeId) {
        this.licenseTypeId = licenseTypeId;
    }

    public int getExamTypeId() {
        return examTypeId;
    }

    public void setExamTypeId(int examTypeId) {
        this.examTypeId = examTypeId;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Time getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(Time shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public Time getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(Time shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMaxCandidates() {
        return maxCandidates;
    }

    public void setMaxCandidates(int maxCandidates) {
        this.maxCandidates = maxCandidates;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public String getExamTypeName() {
        return examTypeName;
    }

    public void setExamTypeName(String examTypeName) {
        this.examTypeName = examTypeName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
