package controller.staff.exam.binder;

import controller.staff.exam.adapter.StaffAuditLogSupport;
import dto.examstaff.AllocationActionResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class AllocationActionResultBinder {

    private AllocationActionResultBinder() {
    }

    public static void apply(HttpServletRequest request, HttpSession session,
            AllocationActionResultDTO result, StaffAuditLogSupport auditLogSupport) {
        if (request == null || result == null) {
            return;
        }
        if (result.getErrorMsg() != null) {
            request.setAttribute("errorMsg", result.getErrorMsg());
        }
        if (result.getWarningMsg() != null) {
            request.setAttribute("warningMsg", result.getWarningMsg());
        }
        if (result.getAlertMsg() != null) {
            request.setAttribute("alertMsg", result.getAlertMsg());
        }
        if (session != null && result.isSyncCallBoard() && result.getCallingSbd() != null) {
            session.setAttribute("callingSbd", result.getCallingSbd());
        }
        if (session != null && result.hasAuditLog() && auditLogSupport != null) {
            auditLogSupport.persist(session, result.getAuditAction(), result.getAuditDetails(),
                    result.getAuditRecordId());
        }
    }
}
