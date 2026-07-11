package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExamStaffDashboardViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class ExamStaffDashboardViewBinder {

    private ExamStaffDashboardViewBinder() {
    }

    public static void bind(HttpServletRequest request, ExamStaffDashboardViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("assignedExaminerUniqueCount", view.getAssignedExaminerUniqueCount());
        request.setAttribute("totalActiveExaminerCount", view.getTotalActiveExaminerCount());
    }
}
