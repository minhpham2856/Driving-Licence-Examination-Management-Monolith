package controller.staff.exam.support;

import dto.examstaff.ExaminerAllocationViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class ExaminerAllocationViewBinder {

    private ExaminerAllocationViewBinder() {
    }

    public static void bind(HttpServletRequest request, ExaminerAllocationViewDTO view, int examId) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("daySessions", view.getDaySessions());
        request.setAttribute("examSessions", view.getDaySessions());
        request.setAttribute("dayAssignments", view.getDayAssignments());
        request.setAttribute("examAssignments", view.getDayAssignments());
        request.setAttribute("sessionAssignments", view.getSessionAssignments());
        request.setAttribute("allExaminers", view.getAllExaminers());
        request.setAttribute("availableExaminers", view.getAvailableExaminers());
        request.setAttribute("busyExaminers", view.getBusyExaminers());
        request.setAttribute("sessionAreas", view.getSessionAreas());
        request.setAttribute("devicesByArea", view.getDevicesByArea());
        request.setAttribute("areasBySession", view.getAreasBySession());
        request.setAttribute("areaAssignOptions", view.getAreaAssignOptions());
        request.setAttribute("examStaffLoadedExamId", examId);
    }
}
