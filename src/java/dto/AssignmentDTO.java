package dto;

import enums.SectionType;
import enums.SessionType;

public class AssignmentDTO {

    private int sessionExaminerId;
    private int examSessionId;
    private int areaId;
    private SectionType examSection;
    private int examinerUserId;
    private int assignedBy;
    private String examinerName;
    private String examinerUsername;
    private String areaName;
    private String areaType;
    private String examTypeName;
    private boolean morningSession;
    private String sessionLabel;

    public int getSessionExaminerId() {
        return sessionExaminerId;
    }

    public void setSessionExaminerId(int sessionExaminerId) {
        this.sessionExaminerId = sessionExaminerId;
    }

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
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

    public boolean isMorningSession() {
        return morningSession;
    }

    public void setMorningSession(boolean morningSession) {
        this.morningSession = morningSession;
    }

    public String getSessionLabel() {
        return sessionLabel;
    }

    public void setsessionLabel(String sessionLabel) {
        this.sessionLabel = sessionLabel;
    }
}
