package examstaff.controller.staff.exam.binder;

import examstaff.dto.ExamReportProcedureStatusDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind trạng thái hoàn tất thủ tục / thiếu ảnh lên request cho trang báo cáo.
 */
public final class ReportProcedureStatusBinder {

    private ReportProcedureStatusBinder() {
    }

    /**
     * Set {@code missingPhotoCount/Sbds/Candidates}, {@code procedurePending*}, {@code procedureCompleteCount}.
     */
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
