package controller.staff.exam.support;

import dto.examstaff.ExamReportProcedureStatusDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class ReportProcedureStatusBinder {

    private ReportProcedureStatusBinder() {
    }

    public static void bind(HttpServletRequest request, ExamReportProcedureStatusDTO status) {
        if (request == null || status == null) {
            return;
        }
        request.setAttribute("missingPhotoCount", status.getMissingPhotoCount());
        request.setAttribute("missingPhotoSbds", status.getMissingPhotoSbds());
        request.setAttribute("missingPhotoCandidates", status.getMissingPhotoCandidates());
        request.setAttribute("procedurePendingCandidates", status.getProcedurePendingCandidates());
        request.setAttribute("procedureCompleteCount", status.getProcedureCompleteCount());
        request.setAttribute("procedurePendingCount", status.getProcedurePendingCount());
    }
}
