package examstaff.dto;

/**
 * View-model bổ sung cho dashboard ExamStaff: thống kê sát hạch viên đã phân công / đang hoạt động.
 *
 * Vai trò:
 * Cung cấp KPI SHV cho dashboard.jsp bên cạnh queue / kỳ thi từ ExamStaffPageContext.
 *
 * Ai tạo / tiêu thụ:
 * ExamStaffDashboardServiceImpl → view service / DashboardServlet.
 */
public class ExamStaffDashboardViewDTO {

    private int assignedExaminerUniqueCount;
    private int totalActiveExaminerCount;

    /** Số SHV unique đã được phân công trong kỳ / ngày làm việc. */
    public int getAssignedExaminerUniqueCount() {
        return assignedExaminerUniqueCount;
    }

    /** Gán số SHV đã phân công (unique). */
    public void setAssignedExaminerUniqueCount(int assignedExaminerUniqueCount) {
        this.assignedExaminerUniqueCount = assignedExaminerUniqueCount;
    }

    /** Tổng số SHV đang active trong hệ thống (nguồn so sánh). */
    public int getTotalActiveExaminerCount() {
        return totalActiveExaminerCount;
    }

    /** Gán tổng SHV active. */
    public void setTotalActiveExaminerCount(int totalActiveExaminerCount) {
        this.totalActiveExaminerCount = totalActiveExaminerCount;
    }
}
