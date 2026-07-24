package examstaff.dto;

/**
 * Slot phân công sát hạch viên trong một kỳ thi (Exam + khu vực + loại phần thi).
 */
public class ExaminerSlotDTO {
    /** Mã kỳ thi. */
    private int examId;
    /** Mã khu vực thi (ExamArea). */
    private int areaId;
    /** Loại phần thi: 1=Lý thuyết, 2=Trong hình, 4=Trên đường. */
    private int examTypeId;
    private int examinerUserId;
    private int assignedBy;
    private String examinerName;
    private String examinerUsername;
    private String areaName;
    private String areaType;
    private String examTypeName;
    private String examName;

    /** Khóa slot duy nhất: examId:areaId:examinerUserId. */
    public String getSlotKey() {
        return examId + ":" + areaId + ":" + examinerUserId;
    }

    public int getExamId() { return examId; }
    public void setExamId(int v) { this.examId = v; }
    public int getAreaId() { return areaId; }
    public void setAreaId(int v) { this.areaId = v; }
    public int getExamTypeId() { return examTypeId; }
    public void setExamTypeId(int v) { this.examTypeId = v; }
    public int getExaminerUserId() { return examinerUserId; }
    public void setExaminerUserId(int v) { this.examinerUserId = v; }
    public int getAssignedBy() { return assignedBy; }
    public void setAssignedBy(int v) { this.assignedBy = v; }
    public String getExaminerName() { return examinerName; }
    public void setExaminerName(String v) { this.examinerName = v; }
    public String getExaminerUsername() { return examinerUsername; }
    public void setExaminerUsername(String v) { this.examinerUsername = v; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String v) { this.areaName = v; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String v) { this.areaType = v; }
    public String getExamTypeName() { return examTypeName; }
    public void setExamTypeName(String v) { this.examTypeName = v; }
    public String getExamName() { return examName; }
    public void setExamName(String v) { this.examName = v; }
}
