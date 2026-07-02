package model.exam;

import java.sql.Timestamp;

public class ExamResult {
    private int id;
    private int examRegistrationId;
    private int examSectionId;
    private Integer theoryScoreId;
    private Integer practicalScoreId;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer answersCount;
    private Integer correctAnswersCount;
    private boolean isCancelled;
    private String cancelReason;
    private Integer cancelledBy;

    public ExamResult() {
    }

    public ExamResult(int id, int examRegistrationId, int examSectionId, Integer theoryScoreId, Integer practicalScoreId, Timestamp startTime, Timestamp endTime, Integer answersCount, Integer correctAnswersCount, boolean isCancelled, String cancelReason, Integer cancelledBy) {
        this.id = id;
        this.examRegistrationId = examRegistrationId;
        this.examSectionId = examSectionId;
        this.theoryScoreId = theoryScoreId;
        this.practicalScoreId = practicalScoreId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.answersCount = answersCount;
        this.correctAnswersCount = correctAnswersCount;
        this.isCancelled = isCancelled;
        this.cancelReason = cancelReason;
        this.cancelledBy = cancelledBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamRegistrationId() {
        return examRegistrationId;
    }

    public void setExamRegistrationId(int examRegistrationId) {
        this.examRegistrationId = examRegistrationId;
    }

    public int getExamSectionId() {
        return examSectionId;
    }

    public void setExamSectionId(int examSectionId) {
        this.examSectionId = examSectionId;
    }

    public Integer getTheoryScoreId() {
        return theoryScoreId;
    }

    public void setTheoryScoreId(Integer theoryScoreId) {
        this.theoryScoreId = theoryScoreId;
    }

    public Integer getPracticalScoreId() {
        return practicalScoreId;
    }

    public void setPracticalScoreId(Integer practicalScoreId) {
        this.practicalScoreId = practicalScoreId;
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

    public Integer getAnswersCount() {
        return answersCount;
    }

    public void setAnswersCount(Integer answersCount) {
        this.answersCount = answersCount;
    }

    public Integer getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public void setCorrectAnswersCount(Integer correctAnswersCount) {
        this.correctAnswersCount = correctAnswersCount;
    }

    public boolean isIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Integer getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(Integer cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
}
