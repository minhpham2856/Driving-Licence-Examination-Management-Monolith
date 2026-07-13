package DTOs;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/**
 * Dữ liệu kỳ/ca thi được giữ tạm trong HTTP session trong lúc xem trước
 * danh sách chính thức trước khi ghi vào cơ sở dữ liệu.
 */
public class ExamSessionImportDraft implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private String sessionName;
    private String centreName;
    private int licenceId;
    private String licenceClass;
    private int examAreaId;
    private String areaName;
    private int examSectionId;
    private String sectionName;
    private Timestamp startTime;
    private Timestamp endTime;

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getCentreName() {
        return centreName;
    }

    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public int getExamAreaId() {
        return examAreaId;
    }

    public void setExamAreaId(int examAreaId) {
        this.examAreaId = examAreaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getExamDateValue() {
        return startTime == null ? "" : startTime.toLocalDateTime().toLocalDate().toString();
    }

    public String getStartTimeValue() {
        return startTime == null ? "" : startTime.toLocalDateTime().toLocalTime().format(TIME_FORMAT);
    }

    public String getEndTimeValue() {
        return endTime == null ? "" : endTime.toLocalDateTime().toLocalTime().format(TIME_FORMAT);
    }
}
