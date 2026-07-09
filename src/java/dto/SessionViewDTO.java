package dto;

import enums.SectionType;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class SessionViewDTO {

    private int id;
    private boolean morningSession;
    private int licenseTypeId;
    private SectionType examSection;
    private Date examDate;
    private Time startTime;
    private Time endTime;
    private int areaId;
    private String status;
    private int maxCandidates;
    private int registeredCount;
    private Timestamp createdAt;
    private String licenseCode;
    private String examTypeName;
    private String areaName;
    private String sessionLabel;

    public SessionViewDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMorningSession() {
        return morningSession;
    }

    public void setMorningSession(boolean morningSession) {
        this.morningSession = morningSession;
    }

    public String getSessionLabel() {
        return sessionLabel;
    }

    public void setSessionLabel(String sessionLabel) {
        this.sessionLabel = sessionLabel;
    }

    public int getLicenseTypeId() {
        return licenseTypeId;
    }

    public void setLicenseTypeId(int licenseTypeId) {
        this.licenseTypeId = licenseTypeId;
    }

    public SectionType getExamSection() {
        return examSection;
    }

    public void setExamSection(SectionType examSection) {
        this.examSection = examSection;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Time getShiftStartTime() {
        return startTime;
    }

    public void setShiftStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getShiftEndTime() {
        return endTime;
    }

    public void setShiftEndTime(Time endTime) {
        this.endTime = endTime;
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
