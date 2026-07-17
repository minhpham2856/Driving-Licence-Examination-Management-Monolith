package registrant.dto;

import java.util.Date;

/**
 * Dòng dữ liệu hiển thị bảng "Đợt thi đã đăng ký" trên dashboard thí sinh.
 * Map trực tiếp sang EL trong dashboard.jsp: examName, examCode, licenceClass, ...
 */
public class RegistrantRegisteredExamRow {

    /** CandidateId — dùng làm examId khi điều hướng sang trang chi tiết. */
    private int id;
    private String examName;
    private String examCode;
    private String licenceClass;
    /** Mô tả đầy đủ hạng GPLX — nạp từ {@link util.registrant.RegistrantExamSupport#licenceClassDescription}. */
    private String licenceClassDescription;
    private Date examDate;
    private String location;
    private String statusClass;
    private String statusLabel;
    /** SBD từ DB — dùng phân biệt lịch chính thức vs chờ duyệt trên dashboard. */
    private String candidateNumber;
    private boolean sbdPending;
    /** Giờ bắt đầu/kết thúc ca thi — chỉ có sau khi Ban sát hạch mở ca. */
    private Date sessionStart;
    private Date sessionEnd;
    private boolean sessionTimePublished;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public String getLicenceClassDescription() {
        return licenceClassDescription;
    }

    public void setLicenceClassDescription(String licenceClassDescription) {
        this.licenceClassDescription = licenceClassDescription;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public boolean isSbdPending() {
        return sbdPending;
    }

    public void setSbdPending(boolean sbdPending) {
        this.sbdPending = sbdPending;
    }

    public Date getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }

    public Date getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(Date sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    public boolean isSessionTimePublished() {
        return sessionTimePublished;
    }

    public void setSessionTimePublished(boolean sessionTimePublished) {
        this.sessionTimePublished = sessionTimePublished;
    }
}
