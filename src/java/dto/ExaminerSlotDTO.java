package dto;


 // Data object representing an examiner's assigned slot within an exam session.
public class ExaminerSlotDTO {

    // PK of the Session_Examiner record in the database
    private int sessionExaminerId;
    // FK to the Session table — identifies which exam session this slot belongs to
    private int examSessionId;
    // FK to the ExamArea table — identifies the physical area/room assigned
    private int areaId;
    // FK to the ExamType — 1=Theory, 2=Practical, 4=Road
    private int examTypeId;
    // FK to the User table — the examiner's user account ID
    private int examinerUserId;
    // FK to the User table — the staff member who created this assignment
    private int assignedBy;
    // Display name of the examiner (from Profile.FullName or User.Username fallback)
    private String examinerName;
    // Login username of the examiner
    private String examinerUsername;
    // Display name of the exam area/room (e.g. "Phong A1")
    private String areaName;
    // Type classification of the area (e.g. "Theory", "Practical")
    private String areaType;
    // Vietnamese display name of the exam type (e.g. "Ly thuyet", "Thuc hanh")
    private String examTypeName;
    // Display name of the exam session (e.g. "Ca 1 - Sang")
    private String sessionName;

         // Returns a unique key for this slot in the format {@code examSessionId:areaId:examinerUserId}.
    public String getSlotKey() {
        return examSessionId + ":" + areaId + ":" + examinerUserId;
    }

    // --- Getter/setter pairs for all fields ---

    // Returns the Session_Examiner primary key
    public int getSessionExaminerId() { return sessionExaminerId; }
    // Sets the Session_Examiner primary key (populated from DB result set)
    public void setSessionExaminerId(int v) { this.sessionExaminerId = v; }
    // Returns the exam session ID this slot belongs to
    public int getExamSessionId() { return examSessionId; }
    // Sets the exam session ID
    public void setExamSessionId(int v) { this.examSessionId = v; }
    // Returns the exam area ID assigned to this slot
    public int getAreaId() { return areaId; }
    // Sets the exam area ID
    public void setAreaId(int v) { this.areaId = v; }
    // Returns the exam type ID (1=Theory, 2=Practical, 4=Road)
    public int getExamTypeId() { return examTypeId; }
    // Sets the exam type ID
    public void setExamTypeId(int v) { this.examTypeId = v; }
    // Returns the examiner's user account ID
    public int getExaminerUserId() { return examinerUserId; }
    // Sets the examiner's user account ID
    public void setExaminerUserId(int v) { this.examinerUserId = v; }
    // Returns the user ID of the staff who created this assignment
    public int getAssignedBy() { return assignedBy; }
    // Sets the assigning staff's user ID
    public void setAssignedBy(int v) { this.assignedBy = v; }
    // Returns the examiner's display name
    public String getExaminerName() { return examinerName; }
    // Sets the examiner's display name
    public void setExaminerName(String v) { this.examinerName = v; }
    // Returns the examiner's login username
    public String getExaminerUsername() { return examinerUsername; }
    // Sets the examiner's login username
    public void setExaminerUsername(String v) { this.examinerUsername = v; }
    // Returns the display name of the exam area/room
    public String getAreaName() { return areaName; }
    // Sets the display name of the exam area/room
    public void setAreaName(String v) { this.areaName = v; }
    // Returns the type classification of the area
    public String getAreaType() { return areaType; }
    // Sets the type classification of the area
    public void setAreaType(String v) { this.areaType = v; }
    // Returns the Vietnamese display name of the exam type
    public String getExamTypeName() { return examTypeName; }
    // Sets the Vietnamese display name of the exam type
    public void setExamTypeName(String v) { this.examTypeName = v; }
    // Returns the display name of the exam session
    public String getSessionName() { return sessionName; }
    // Sets the display name of the exam session
    public void setSessionName(String v) { this.sessionName = v; }
}
