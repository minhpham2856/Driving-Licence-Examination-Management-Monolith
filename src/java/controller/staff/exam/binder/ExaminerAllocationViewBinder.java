package controller.staff.exam.binder;

import dto.examstaff.ExaminerAllocationViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class ExaminerAllocationViewBinder {

    private ExaminerAllocationViewBinder() {
    }

    public static void bind(HttpServletRequest request, ExaminerAllocationViewDTO view, int examId) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("examAssignments", view.getDayAssignments());
        request.setAttribute("allExaminers", view.getAllExaminers());
        request.setAttribute("availableExaminers", view.getAvailableExaminers());
        request.setAttribute("busyExaminers", view.getBusyExaminers());
        request.setAttribute("areaAssignOptions", view.getAreaAssignOptions());
        request.setAttribute("examStaffLoadedExamId", examId);
    }
}
