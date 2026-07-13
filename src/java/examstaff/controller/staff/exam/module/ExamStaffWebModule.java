package examstaff.controller.staff.exam.module;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.service.ExamStaffServices;

/**
 * Web composition root for examstaff/public-call.
 * Creates one object graph and hands dependencies to servlets/support adapters.
 */
public final class ExamStaffWebModule {

    private final ExamStaffServices services;

    public ExamStaffWebModule() {
        this(new ExamStaffServices());
    }

    public ExamStaffWebModule(ExamStaffServices services) {
        this.services = services;
    }

    public ExamStaffServices services() {
        return services;
    }

    public ExamStaffPageService page() {
        return services.page();
    }

    public ExamStaffSelectionService selection() {
        return services.selection();
    }

    public CallBoardHttpFacade callBoardHttp() {
        return new CallBoardHttpFacade(services.callBoardSync());
    }

    public ExamStaffSelectionFacade selectionFacade() {
        return new ExamStaffSelectionFacade(page(), selection());
    }

    public StaffAuditLogSupport auditLogSupport() {
        return new StaffAuditLogSupport(services.auditLog());
    }
}
