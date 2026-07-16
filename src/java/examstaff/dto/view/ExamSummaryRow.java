package examstaff.dto.view;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/** Read model kỳ thi (Exam). */
public class ExamSummaryRow {

    private int examId;
    private String examName;
    private int examTypeId;
    private Date examDate;
    private Time shiftStartTime;
    private Time shiftEndTime;
    private Timestamp scheduledStartAt;
    private Timestamp scheduledEndAt;
    private String status;
    private Timestamp createdAt;
    private String licenseCode;
    private String examCode;
    private String examTypeName;

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
