package controller.staff.exam;



/** Phân công giám khảo — Session_Examiner + phòng qua Session_ExamArea (DLEM_DB_2). */

public class ExaminerSlot {

    private int sessionExaminerId;

    private int examSessionId;

    private int areaId;

    private int examTypeId;

    private int examinerUserId;

    private int assignedBy;



    private String examinerName;

    private String examinerUsername;

    private String areaName;

    private String areaType;

    private String examTypeName;

    private String sessionName;



    public String getSlotKey() {

        return examSessionId + ":" + areaId + ":" + examinerUserId;

    }



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



    public int getExamTypeId() {

        return examTypeId;

    }



    public void setExamTypeId(int examTypeId) {

        this.examTypeId = examTypeId;

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



    public String getSessionName() {

        return sessionName;

    }



    public void setSessionName(String sessionName) {

        this.sessionName = sessionName;

    }

}


