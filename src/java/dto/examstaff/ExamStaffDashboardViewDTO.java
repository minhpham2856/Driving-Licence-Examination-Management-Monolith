package dto.examstaff;

import dto.SessionDTO;

import java.util.List;

public class ExamStaffDashboardViewDTO {

    private List<SessionDTO> daySessions;
    private int assignedExaminerUniqueCount;
    private int totalActiveExaminerCount;

    public List<SessionDTO> getDaySessions() {
        return daySessions;
    }

    public void setDaySessions(List<SessionDTO> daySessions) {
        this.daySessions = daySessions;
    }

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
