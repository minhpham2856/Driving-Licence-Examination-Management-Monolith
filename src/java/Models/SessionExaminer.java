package Models;

 // Model mapping to the {@code Session_Examiner} (or {@code ExaminerSchedule}) join table.
public class SessionExaminer {

    // Primary key of the Session_Examiner record
    private int sessionExaminerId;
    // Foreign key referencing the Session table
    private int sessionId;
    // Foreign key referencing the User table (the examiner's user ID)
    private int examinerId;

    // The exam this assignment belongs to (may be null for legacy records).
    private Integer examId;

    // The specific exam section the examiner is assigned to (may be null if assigned to the whole session).
    private Integer examSectionId;

    // The specific exam area the examiner is assigned to (may be null).
    private Integer examAreaId;

    // User ID of the person who made this assignment.
    private Integer assignedBy;

    // Timestamp of when this assignment was created.
    private java.sql.Timestamp assignedAt;

    // Default no-arg constructor required by JDBC result-set mapping
    public SessionExaminer() {
    }

    // Getter/setter pairs for each field — used by DAO mapping and service layers

    // Returns the primary key ID of this assignment record
    public int getSessionExaminerId() { return sessionExaminerId; }
    // Sets the primary key ID (assigned by auto-increment in DB)
    public void setSessionExaminerId(int v) { this.sessionExaminerId = v; }
    // Returns the session ID this examiner is assigned to
    public int getSessionId() { return sessionId; }
    // Sets the session ID foreign key
    public void setSessionId(int v) { this.sessionId = v; }
    // Returns the examiner's user ID
    public int getExaminerId() { return examinerId; }
    // Sets the examiner's user ID foreign key
    public void setExaminerId(int v) { this.examinerId = v; }
    // Returns the optional exam ID (nullable)
    public Integer getExamId() { return examId; }
    // Sets the optional exam ID
    public void setExamId(Integer v) { this.examId = v; }
    // Returns the optional exam section ID (nullable)
    public Integer getExamSectionId() { return examSectionId; }
    // Sets the optional exam section ID
    public void setExamSectionId(Integer v) { this.examSectionId = v; }
    // Returns the optional exam area ID (nullable)
    public Integer getExamAreaId() { return examAreaId; }
    // Sets the optional exam area ID
    public void setExamAreaId(Integer v) { this.examAreaId = v; }
    // Returns the user ID of the person who created this assignment
    public Integer getAssignedBy() { return assignedBy; }
    // Sets the assigner's user ID
    public void setAssignedBy(Integer v) { this.assignedBy = v; }
    // Returns the timestamp when this assignment was created
    public java.sql.Timestamp getAssignedAt() { return assignedAt; }
    // Sets the assignment creation timestamp
    public void setAssignedAt(java.sql.Timestamp v) { this.assignedAt = v; }
}
