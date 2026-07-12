package dto;

import enums.SectionType;

public class AssignmentDTO {

    private int examinerScheduleId;
    private int examId;
    private int areaId;
    private SectionType examSection;
    private int examinerUserId;
    private int assignedBy;
    private String examinerName;
    private String examinerUsername;
    private String areaName;
    private String areaType;
    private String examTypeName;
    private boolean morningShift;
    private String examLabel;

    public int getExaminerScheduleId() {
        return examinerScheduleId;
    }

    public void setExaminerScheduleId(int examinerScheduleId) {
        this.examinerScheduleId = examinerScheduleId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public SectionType getExamSection() {
        return examSection;
    }

    public void setExamSection(SectionType examSection) {
        this.examSection = examSection;
    }

    public int getExaminerUserId() {
        return examinerUserId;
    }

    public void setExaminerUserId(int examinerUserId) {
        this.examinerUserId = examinerUserId;
    }

    public int getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(int assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getExaminerName() {
        return examinerName;
    }

    public void setExaminerName(String examinerName) {
        this.examinerName = examinerName;
    }

    public String getExaminerUsername() {
        return examinerUsername;
    }

    public void setExaminerUsername(String examinerUsername) {
        this.examinerUsername = examinerUsername;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public String getExamTypeName() {
        return examTypeName;
    }

    public void setExamTypeName(String examTypeName) {
        this.examTypeName = examTypeName;
    }

    public boolean isMorningShift() {
        return morningShift;
    }

    public void setMorningShift(boolean morningShift) {
        this.morningShift = morningShift;
    }

    public String getExamLabel() {
        return examLabel;
    }

    public void setExamLabel(String examLabel) {
        this.examLabel = examLabel;
    }
}
