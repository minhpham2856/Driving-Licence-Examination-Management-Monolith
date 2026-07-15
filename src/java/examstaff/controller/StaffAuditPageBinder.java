package examstaff.controller;

import examstaff.dto.StaffAuditPageViewDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Bind dữ liệu trang nhật ký cá nhân exam staff từ {@link StaffAuditPageViewDTO}.
 */
public final class StaffAuditPageBinder {

    private StaffAuditPageBinder() {
    }

    /**
     * Set {@code personalLogs}, {@code examStaffPageSlice/ListPath}, KPI
     * {@code myCompletedProcedures}/{@code myTotalFees}.
     */
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
