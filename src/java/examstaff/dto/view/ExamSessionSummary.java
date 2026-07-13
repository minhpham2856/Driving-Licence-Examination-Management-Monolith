package examstaff.dto.view;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/** Read model ká»³ thi (Exam). */
public class ExamSessionSummary {

    private int examId;
    private boolean morningSession;
    private String sessionName;
    private int licenseTypeId;
    private int examTypeId;
    private Date examDate;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private Timestamp scheduledStartAt;
    private Timestamp scheduledEndAt;
    private int areaId;
    private String status;
    private int maxCandidates;
    private int registeredCount;
    private Timestamp createdAt;
    private String licenseCode;
    private String examCode;
    private String examTypeName;
    private String areaName;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public boolean isMorningSession() {
        return morningSession;
    }

    public void setMorningSession(boolean morningSession) {
        this.morningSession = morningSession;
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

    public Timestamp getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Timestamp scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Timestamp getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Timestamp scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
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

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
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

