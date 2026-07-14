package examstaff.controller.staff.exam.module;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.service.ExamStaffPageService;
import examstaff.service.ExamStaffSelectionService;
import examstaff.service.ExamStaffServices;

/**
 * Composition root Presentation cho examstaff / public-call.
 * Singleton dùng chung một object graph — không nghiệp vụ.
 */
public final class ExamStaffWebModule {

    private static final ExamStaffWebModule INSTANCE = new ExamStaffWebModule();

    private final ExamStaffServices services;
    private final CallBoardHttpFacade callBoardHttp;
    private final ExamStaffSelectionFacade selectionFacade;
    private final StaffAuditLogSupport auditLogSupport;

    /** Instance dùng chung toàn app. */
    public static ExamStaffWebModule getInstance() {
        return INSTANCE;
    }

    private ExamStaffWebModule() {
        this(new ExamStaffServices());
    }

    /**
     * Constructor inject services (test / wiring tay).
     *
     * @param services bag service BLL
     */
    public ExamStaffWebModule(ExamStaffServices services) {
        this.services = services;
        this.callBoardHttp = new CallBoardHttpFacade(services.callBoardSync());
        this.selectionFacade = new ExamStaffSelectionFacade(services.page(), services.selection());
        this.auditLogSupport = new StaffAuditLogSupport(services.auditLog());
    }

    /** Bag service BLL dùng chung. */
    public ExamStaffServices services() {
        return services;
    }

    /** Shortcut {@code services.page()}. */
    public ExamStaffPageService page() {
        return services.page();
    }

    /** Shortcut {@code services.selection()}. */
    public ExamStaffSelectionService selection() {
        return services.selection();
    }

    /** Facade HTTP đồng bộ CallBoard (cached). */
    public CallBoardHttpFacade callBoardHttp() {
        return callBoardHttp;
    }

    /** Facade chọn kỳ / sidebar / resolve examId (cached). */
    public ExamStaffSelectionFacade selectionFacade() {
        return selectionFacade;
    }

    /** Adapter ghi nhật ký audit từ session staff (cached). */
    public StaffAuditLogSupport auditLogSupport() {
        return auditLogSupport;
    }
}
