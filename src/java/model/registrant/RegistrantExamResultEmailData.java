package model.registrant;

import java.util.Date;

/** Dữ liệu nạp vào mẫu email bảng điểm gửi thí sinh qua Gmail. */
public class RegistrantExamResultEmailData {

    private String recipientName;
    private String recipientEmail;
    private String examTitle;
    private String licenceClass;
    private String sbdDisplay;
    private Date examDate;
    private String examSectionName;
    private Integer theoryScore;
    private String theoryResultLabel;
    private Integer practicalScore;
    private String practicalResultLabel;
    private Integer roadScore;
    private String overallResultLabel;

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public String getSbdDisplay() {
        return sbdDisplay;
    }

    public void setSbdDisplay(String sbdDisplay) {
        this.sbdDisplay = sbdDisplay;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getExamSectionName() {
        return examSectionName;
    }

    public void setExamSectionName(String examSectionName) {
        this.examSectionName = examSectionName;
    }

    public Integer getTheoryScore() {
        return theoryScore;
    }

    public void setTheoryScore(Integer theoryScore) {
        this.theoryScore = theoryScore;
    }

    public String getTheoryResultLabel() {
        return theoryResultLabel;
    }

    public void setTheoryResultLabel(String theoryResultLabel) {
        this.theoryResultLabel = theoryResultLabel;
    }

    public Integer getPracticalScore() {
        return practicalScore;
    }

    public void setPracticalScore(Integer practicalScore) {
        this.practicalScore = practicalScore;
    }

    public String getPracticalResultLabel() {
        return practicalResultLabel;
    }

    public void setPracticalResultLabel(String practicalResultLabel) {
        this.practicalResultLabel = practicalResultLabel;
    }

    public Integer getRoadScore() {
        return roadScore;
    }

    public void setRoadScore(Integer roadScore) {
        this.roadScore = roadScore;
    }

    public String getOverallResultLabel() {
        return overallResultLabel;
    }

    public void setOverallResultLabel(String overallResultLabel) {
        this.overallResultLabel = overallResultLabel;
    }

    public boolean hasAnyScore() {
        return theoryScore != null || practicalScore != null || roadScore != null;
    }
}
