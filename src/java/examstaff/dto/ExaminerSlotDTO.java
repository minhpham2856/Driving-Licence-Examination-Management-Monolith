package examstaff.dto;

/**
 * Slot phân công sát hạch viên trong một kỳ thi (khu vực + loại thi + SHV).
 *
 * <h2>Vai trò</h2>
 * Một dòng assignment trên {@code examiner-allocation.jsp}: ai coi thi ở khu nào, loại lý thuyết/thực hành/đường.
 *
 * <h2>Ai tạo / tiêu thụ</h2>
 * {@code ExaminerAssignmentDAOImpl}, {@code ExaminerAllocationDeskServiceImpl}
 * → {@link ExaminerAllocationViewDTO} → {@code ExaminerAllocationServlet}.
 */
public class ExaminerSlotDTO {
    /** ExamId. */
    private int examId;
    /** ExamAreaId. */
    private int areaId;
    /** ExamType: 1=Theory, 2=Practical, 4=Road. */
    private int examTypeId;
    private int examinerUserId;
    private int assignedBy;
    private String examinerName;
    private String examinerUsername;
    private String areaName;
    private String areaType;
    private String examTypeName;
    private String examName;

    /**
     * Khóa ổn định của slot: {@code examId:areaId:examinerUserId} (đối chiếu UI / remove).
     *
     * @return chuỗi khóa ghép
     */
    public String getSlotKey() {
        return examId + ":" + areaId + ":" + examinerUserId;
    }

    /** Mã kỳ thi của slot phân công. */
    public int getExamId() { return examId; }
    /** Gán mã kỳ thi. */
    public void setExamId(int v) { this.examId = v; }
    /** Mã khu vực thi (ExamArea). */
    public int getAreaId() { return areaId; }
    /** Gán mã khu vực. */
    public void setAreaId(int v) { this.areaId = v; }
    /** Loại phần thi (1=Lý thuyết, 2=Thực hành, 4=Đường). */
    public int getExamTypeId() { return examTypeId; }
    /** Gán loại phần thi. */
    public void setExamTypeId(int v) { this.examTypeId = v; }
    /** UserId sát hạch viên được phân. */
    public int getExaminerUserId() { return examinerUserId; }
    /** Gán userId SHV. */
    public void setExaminerUserId(int v) { this.examinerUserId = v; }
    /** UserId staff thực hiện phân công. */
    public int getAssignedBy() { return assignedBy; }
    /** Gán người phân công. */
    public void setAssignedBy(int v) { this.assignedBy = v; }
    /** Họ tên SHV hiển thị. */
    public String getExaminerName() { return examinerName; }
    /** Gán họ tên SHV. */
    public void setExaminerName(String v) { this.examinerName = v; }
    /** Username đăng nhập của SHV. */
    public String getExaminerUsername() { return examinerUsername; }
    /** Gán username SHV. */
    public void setExaminerUsername(String v) { this.examinerUsername = v; }
    /** Tên khu vực hiển thị. */
    public String getAreaName() { return areaName; }
    /** Gán tên khu vực. */
    public void setAreaName(String v) { this.areaName = v; }
    /** Loại khu vực (chuỗi phân loại từ DB). */
    public String getAreaType() { return areaType; }
    /** Gán loại khu vực. */
    public void setAreaType(String v) { this.areaType = v; }
    /** Tên loại phần thi (hiển thị). */
    public String getExamTypeName() { return examTypeName; }
    /** Gán tên loại phần thi. */
    public void setExamTypeName(String v) { this.examTypeName = v; }
    /** Tên kỳ thi (denormalize cho UI). */
    public String getExamName() { return examName; }
    /** Gán tên kỳ thi. */
    public void setExamName(String v) { this.examName = v; }
}
