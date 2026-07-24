package registrant.dto;

import java.util.Date;

/**
 * DTO một kỳ thi của thí sinh trên trang {@code my-exams.jsp}.
 * <p>
 * Gộp nguyện vọng {@code RegistrationDates} và ca chính thức ({@code Candidate}, SBD, phòng, điểm lý/thực hành,
 * trạng thái thanh toán). Map sang EL JSP qua {@code RegistrantMyExamsServiceImpl}.
 */
public class RegistrantMyExamRow {

    private int candidateId;
    private String examTitle;
    private Date examDate;
    private String licenceClass;
    private String sbd;
    private String sbdDisplay;
    private boolean sbdPending;
    private String roomName;
    private String statusClass;
    private String statusLabel;
    private boolean pendingPayment;
    private Integer theoryScore;
    private Integer practicalScore;
    private Integer roadScore;
    private String theoryResultLabel;
    private String practicalResultLabel;
    private String overallResultLabel;
    /** Hiển thị chính lý thuyết: vd. "32/35" (số câu đúng / tổng câu). */
    private String theoryScoreDisplay;
    private String theoryScoreDetail;
    private Integer theoryCorrectCount;
    private Integer theoryWrongCount;
    private String theoryPassBadgeClass;
    private String practicalScoreDisplay;
    private String practicalScoreDetail;
    private String practicalPassBadgeClass;
    private String roadScoreDisplay;
    private String roadScoreDetail;
    private String roadPassBadgeClass;
    /** Giờ ca thi đã định dạng (vd. "08:30 - 10:00"). */
    private String sessionTimeDisplay;

    /** Trạng thái ExamRegistration (luồng ca thi). */
    private String registrationStatus;
    private String examSectionName;
    private Date sessionStart;
    private Date sessionEnd;
    private boolean sessionTimePublished;

    /** True = nguyện vọng ngày thi (RegistrationDates), chưa phải kỳ chính thức. */
    private boolean preferredDate;

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public String getSbd() {
        return sbd;
    }

    public void setSbd(String sbd) {
        this.sbd = sbd;
    }

    public String getSbdDisplay() {
        return sbdDisplay;
    }

    public void setSbdDisplay(String sbdDisplay) {
        this.sbdDisplay = sbdDisplay;
    }

    public boolean isSbdPending() {
        return sbdPending;
    }

    public void setSbdPending(boolean sbdPending) {
        this.sbdPending = sbdPending;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public boolean isPendingPayment() {
        return pendingPayment;
    }

    public void setPendingPayment(boolean pendingPayment) {
        this.pendingPayment = pendingPayment;
    }

    public Integer getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    public Integer getRoadScore() {
        return roadScore;
    }

    public void setRoadScore(Integer roadScore) {
        this.roadScore = roadScore;
    }

    public String getTheoryResultLabel() {
        return theoryResultLabel;
    }

    public void setTheoryResultLabel(String theoryResultLabel) {
        this.theoryResultLabel = theoryResultLabel;
    }

    public String getPracticalResultLabel() {
        return practicalResultLabel;
    }

    public void setPracticalResultLabel(String practicalResultLabel) {
        this.practicalResultLabel = practicalResultLabel;
    }

    public String getOverallResultLabel() {
        return overallResultLabel;
    }

    public void setOverallResultLabel(String overallResultLabel) {
        this.overallResultLabel = overallResultLabel;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getExamSectionName() {
        return examSectionName;
    }

    public void setExamSectionName(String examSectionName) {
        this.examSectionName = examSectionName;
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

    public String getTheoryScoreDisplay() {
        return theoryScoreDisplay;
    }

    public void setTheoryScoreDisplay(String theoryScoreDisplay) {
        this.theoryScoreDisplay = theoryScoreDisplay;
    }

    public String getTheoryScoreDetail() {
        return theoryScoreDetail;
    }

    public void setTheoryScoreDetail(String theoryScoreDetail) {
        this.theoryScoreDetail = theoryScoreDetail;
    }

    public Integer getTheoryCorrectCount() {
        return theoryCorrectCount;
    }

    public void setTheoryCorrectCount(Integer theoryCorrectCount) {
        this.theoryCorrectCount = theoryCorrectCount;
    }

    public Integer getTheoryWrongCount() {
        return theoryWrongCount;
    }

    public void setTheoryWrongCount(Integer theoryWrongCount) {
        this.theoryWrongCount = theoryWrongCount;
    }

    public String getTheoryPassBadgeClass() {
        return theoryPassBadgeClass;
    }

    public void setTheoryPassBadgeClass(String theoryPassBadgeClass) {
        this.theoryPassBadgeClass = theoryPassBadgeClass;
    }

    public String getPracticalScoreDisplay() {
        return practicalScoreDisplay;
    }

    public void setPracticalScoreDisplay(String practicalScoreDisplay) {
        this.practicalScoreDisplay = practicalScoreDisplay;
    }

    public String getPracticalScoreDetail() {
        return practicalScoreDetail;
    }

    public void setPracticalScoreDetail(String practicalScoreDetail) {
        this.practicalScoreDetail = practicalScoreDetail;
    }

    public String getPracticalPassBadgeClass() {
        return practicalPassBadgeClass;
    }

    public void setPracticalPassBadgeClass(String practicalPassBadgeClass) {
        this.practicalPassBadgeClass = practicalPassBadgeClass;
    }

    public String getRoadScoreDisplay() {
        return roadScoreDisplay;
    }

    public void setRoadScoreDisplay(String roadScoreDisplay) {
        this.roadScoreDisplay = roadScoreDisplay;
    }

    public String getRoadScoreDetail() {
        return roadScoreDetail;
    }

    public void setRoadScoreDetail(String roadScoreDetail) {
        this.roadScoreDetail = roadScoreDetail;
    }

    public String getRoadPassBadgeClass() {
        return roadPassBadgeClass;
    }

    public void setRoadPassBadgeClass(String roadPassBadgeClass) {
        this.roadPassBadgeClass = roadPassBadgeClass;
    }

    public String getSessionTimeDisplay() {
        return sessionTimeDisplay;
    }

    public void setSessionTimeDisplay(String sessionTimeDisplay) {
        this.sessionTimeDisplay = sessionTimeDisplay;
    }

    public boolean isPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(boolean preferredDate) {
        this.preferredDate = preferredDate;
    }
}
