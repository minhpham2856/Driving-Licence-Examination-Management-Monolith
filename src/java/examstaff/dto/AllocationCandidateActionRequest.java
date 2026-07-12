package examstaff.dto;

import examstaff.dto.exam.ExamRegistrationDTO;

public class AllocationCandidateActionRequest {

    private String action;
    private int regId;
    private int examId;
    private ExamRegistrationDTO profile;
    private int areaId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }
}
