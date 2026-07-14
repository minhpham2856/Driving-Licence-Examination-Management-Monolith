package examstaff.controller.staff.exam.binder;

import examstaff.dto.StaffAuditPageViewDTO;
import jakarta.servlet.http.HttpServletRequest;

public final class StaffAuditPageBinder {

    private StaffAuditPageBinder() {
    }

    public static void bind(HttpServletRequest request, StaffAuditPageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("personalLogs", view.getPersonalLogs());
        request.setAttribute("examStaffPageSlice", view.getPageSlice());
        request.setAttribute("examStaffListPath", "/examstaff/audit");
        int completed = view.getProcedureKpi() != null ? view.getProcedureKpi().getCompletedCount() : 0;
        double totalFees = view.getProcedureKpi() != null ? view.getProcedureKpi().getTotalFees() : 0;
        request.setAttribute("myCompletedProcedures", completed);
        request.setAttribute("myTotalFees", totalFees);
    }
}
