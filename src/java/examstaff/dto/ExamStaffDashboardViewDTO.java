package examstaff.dto;

public class ExamStaffDashboardViewDTO {

    private int assignedExaminerUniqueCount;
    private int totalActiveExaminerCount;

    public int getAssignedExaminerUniqueCount() {
        return assignedExaminerUniqueCount;
    }

    public void setAssignedExaminerUniqueCount(int assignedExaminerUniqueCount) {
        this.assignedExaminerUniqueCount = assignedExaminerUniqueCount;
    }

    public int getTotalActiveExaminerCount() {
        return totalActiveExaminerCount;
    }

    public void setTotalActiveExaminerCount(int totalActiveExaminerCount) {
        this.totalActiveExaminerCount = totalActiveExaminerCount;
    }
}
