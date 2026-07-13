package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExaminerAllocationViewDTO;
import jakarta.servlet.http.HttpServletRequest;
import shared.Attributes;

public final class ExaminerAllocationViewBinder {

    private ExaminerAllocationViewBinder() {
    }

    public static void bind(HttpServletRequest request, ExaminerAllocationViewDTO view, int examId) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute(Attributes.ExamStaff.EXAM_ASSIGNMENTS, view.getDayAssignments());
        request.setAttribute(Attributes.ExamStaff.ALL_EXAMINERS, view.getAllExaminers());
        request.setAttribute(Attributes.ExamStaff.AVAILABLE_EXAMINERS, view.getAvailableExaminers());
        request.setAttribute(Attributes.ExamStaff.BUSY_EXAMINERS, view.getBusyExaminers());
        request.setAttribute(Attributes.ExamStaff.AREA_ASSIGN_OPTIONS, view.getAreaAssignOptions());
        request.setAttribute(Attributes.ExamStaff.LOADED_EXAM_ID, examId);
    }
}
