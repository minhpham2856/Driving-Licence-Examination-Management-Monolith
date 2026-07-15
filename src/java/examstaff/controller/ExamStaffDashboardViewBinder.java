package examstaff.controller;

import examstaff.dto.ExamStaffDashboardViewDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind KPI dashboard (số sát hạch viên) từ {@link ExamStaffDashboardViewDTO}.
 */
public final class ExamStaffDashboardViewBinder {

    private ExamStaffDashboardViewBinder() {
    }

    /**
     * Set {@code assignedExaminerUniqueCount} và {@code totalActiveExaminerCount}.
     */
    public static void bind(HttpServletRequest request, ExamStaffDashboardViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("assignedExaminerUniqueCount", view.getAssignedExaminerUniqueCount());
        request.setAttribute("totalActiveExaminerCount", view.getTotalActiveExaminerCount());
    }
}
