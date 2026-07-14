package examstaff.service;

import examstaff.dao.impl.CandidateCallDAOImpl;
import examstaff.dao.impl.ExamDAOImpl;
import examstaff.dao.impl.ExaminerAssignmentDAOImpl;
import examstaff.service.impl.AllocationActionServiceImpl;
import examstaff.service.impl.AllocationStageViewServiceImpl;
import examstaff.service.impl.CallBoardSyncServiceImpl;
import examstaff.service.impl.CandidateAttendanceServiceImpl;
import examstaff.service.impl.CandidateCallPageServiceImpl;
import examstaff.service.impl.CandidateCallWorkflowServiceImpl;
import examstaff.service.impl.CandidateDossierServiceImpl;
import examstaff.service.impl.CandidatePhotoServiceImpl;
import examstaff.service.impl.CandidateQueueQueryServiceImpl;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ExamAreaQueryServiceImpl;
import examstaff.service.impl.ExamControlServiceImpl;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.impl.ExamReportProcedureStatusServiceImpl;
import examstaff.service.impl.ExamReportStatsServiceImpl;
import examstaff.service.impl.ExamStaffDashboardServiceImpl;
import examstaff.service.impl.ExamStaffExamQueryServiceImpl;
import examstaff.service.impl.ExamStaffPageServiceImpl;
import examstaff.service.impl.ExamStaffSelectionServiceImpl;
import examstaff.service.impl.ExaminerAllocationDeskServiceImpl;
import examstaff.service.impl.ExaminerAllocationServiceImpl;
import examstaff.service.impl.ProcedureFeeQueryServiceImpl;
import examstaff.service.impl.ProcedurePaymentServiceImpl;
import examstaff.service.impl.ProcedureWorkflowServiceImpl;
import examstaff.service.impl.PublicCallQueryServiceImpl;
import examstaff.service.impl.StaffAuditExportServiceImpl;
import examstaff.service.impl.StaffAuditLogServiceImpl;
import examstaff.service.impl.StaffAuditPageServiceImpl;
import examstaff.service.impl.StaffAuditQueryServiceImpl;
import examstaff.service.impl.StaffReportExportServiceImpl;

/**
 * Composition root BLL cho exam staff / public call.
 * Một bag duy nhất — servlet lấy qua {@code ExamStaffWebModule.getInstance().services()}.
 */
public final class ExamStaffServices {

    private final ExamStaffExamQueryService examQueryService;
    private final CandidateQueueService candidateQueueService;
    private final ExamStaffPageService examStaffPageService;
    private final ExamStaffSelectionService examStaffSelectionService;
    private final CandidateCallWorkflowService candidateCallWorkflowService;
    private final CandidateCallPageService candidateCallPageService;
    private final CallBoardSyncService callBoardSyncService;
    private final PublicCallQueryService publicCallQueryService;
    private final ProcedureFeeQueryService procedureFeeQueryService;
    private final ExamAreaQueryService examAreaQueryService;
    private final AllocationActionService allocationActionService;
    private final AllocationStageViewService allocationStageViewService;
    private final CandidatePhotoService candidatePhotoService;
    private final CandidateDossierService candidateDossierService;
    private final ProcedureWorkflowService procedureWorkflowService;
    private final ExamControlService examControlService;
    private final ExamStaffDashboardService examStaffDashboardService;
    private final StaffAuditPageService staffAuditPageService;
    private final StaffAuditQueryService staffAuditQueryService;
    private final StaffAuditExportService staffAuditExportService;
    private final StaffAuditLogService staffAuditLogService;
    private final ExamReportStatsService examReportStatsService;
    private final ExamReportProcedureStatusService examReportProcedureStatusService;
    private final StaffReportExportService staffReportExportService;
    private final ExaminerAllocationService examinerAllocationService;
    private final ExaminerAllocationDeskService examinerAllocationDeskService;

    /**
     * Khởi tạo và wire toàn bộ service exam staff với implementation mặc định.
     */
    public ExamStaffServices() {
        this.examQueryService = new ExamStaffExamQueryServiceImpl();
        this.candidateQueueService = new CandidateQueueServiceImpl();
        this.examStaffPageService = new ExamStaffPageServiceImpl(
                this.examQueryService, this.candidateQueueService);
        this.examStaffSelectionService = new ExamStaffSelectionServiceImpl(this.examStaffPageService);
        this.candidateCallWorkflowService = new CandidateCallWorkflowServiceImpl(
                this.candidateQueueService, new CandidateCallDAOImpl(),
                new CandidateAttendanceServiceImpl());
        this.candidateCallPageService = new CandidateCallPageServiceImpl(
                this.candidateCallWorkflowService, this.candidateQueueService, this.examQueryService);
        this.callBoardSyncService = new CallBoardSyncServiceImpl();
        this.publicCallQueryService = new PublicCallQueryServiceImpl(
                new CandidateQueueQueryServiceImpl(), this.examQueryService, this.callBoardSyncService);
        this.procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
        this.examAreaQueryService = new ExamAreaQueryServiceImpl();
        this.allocationActionService = new AllocationActionServiceImpl();
        this.allocationStageViewService = new AllocationStageViewServiceImpl();
        this.candidatePhotoService = new CandidatePhotoServiceImpl(this.candidateQueueService);
        this.candidateDossierService = new CandidateDossierServiceImpl();
        this.examinerAllocationService = new ExaminerAllocationServiceImpl();
        this.examinerAllocationDeskService = new ExaminerAllocationDeskServiceImpl(
                this.examinerAllocationService);
        this.procedureWorkflowService = new ProcedureWorkflowServiceImpl(
                new ExamRegistrationServiceImpl(),
                new ProcedurePaymentServiceImpl(),
                this.candidatePhotoService,
                this.candidateQueueService,
                this.examinerAllocationService);
        this.examControlService = new ExamControlServiceImpl(
                new ExamDAOImpl(), new ExaminerAssignmentDAOImpl());
        this.examStaffDashboardService = new ExamStaffDashboardServiceImpl(
                this.examQueryService, this.examinerAllocationService);
        this.staffAuditQueryService = new StaffAuditQueryServiceImpl();
        this.staffAuditPageService = new StaffAuditPageServiceImpl(this.staffAuditQueryService);
        this.staffAuditExportService = new StaffAuditExportServiceImpl();
        this.staffAuditLogService = new StaffAuditLogServiceImpl();
        this.examReportStatsService = new ExamReportStatsServiceImpl();
        this.examReportProcedureStatusService = new ExamReportProcedureStatusServiceImpl();
        this.staffReportExportService = new StaffReportExportServiceImpl();
    }

    /** @return service hàng đợi thí sinh */
    public CandidateQueueService candidateQueue() {
        return candidateQueueService;
    }

    /** @return service ngữ cảnh trang exam staff */
    public ExamStaffPageService page() {
        return examStaffPageService;
    }

    /** @return service chọn / đồng bộ kỳ thi (gồm processSelection) */
    public ExamStaffSelectionService selection() {
        return examStaffSelectionService;
    }

    /** @return service trang gọi thí sinh */
    public CandidateCallPageService callPage() {
        return candidateCallPageService;
    }

    /** @return service đồng bộ bảng gọi */
    public CallBoardSyncService callBoardSync() {
        return callBoardSyncService;
    }

    /** @return service truy vấn bảng gọi công khai */
    public PublicCallQueryService publicCallQuery() {
        return publicCallQueryService;
    }

    /** @return service tính phí thủ tục */
    public ProcedureFeeQueryService procedureFees() {
        return procedureFeeQueryService;
    }

    /** @return service truy vấn khu vực/phòng thi */
    public ExamAreaQueryService examAreas() {
        return examAreaQueryService;
    }

    /** @return service thao tác phân phòng */
    public AllocationActionService allocationActions() {
        return allocationActionService;
    }

    /** @return service view phân phòng theo giai đoạn */
    public AllocationStageViewService allocationStageView() {
        return allocationStageViewService;
    }

    /** @return service xử lý / stream ảnh thí sinh */
    public CandidatePhotoService photos() {
        return candidatePhotoService;
    }

    /** @return service hồ sơ chi tiết thí sinh */
    public CandidateDossierService dossiers() {
        return candidateDossierService;
    }

    /** @return service luồng bàn thủ tục */
    public ProcedureWorkflowService procedures() {
        return procedureWorkflowService;
    }

    /** @return service điều khiển vòng đời kỳ thi */
    public ExamControlService examControl() {
        return examControlService;
    }

    /** @return service dashboard exam staff */
    public ExamStaffDashboardService dashboard() {
        return examStaffDashboardService;
    }

    /** @return service trang nhật ký audit */
    public StaffAuditPageService auditPage() {
        return staffAuditPageService;
    }

    /** @return service truy vấn audit / KPI */
    public StaffAuditQueryService auditQuery() {
        return staffAuditQueryService;
    }

    /** @return service xuất file audit */
    public StaffAuditExportService auditExport() {
        return staffAuditExportService;
    }

    /** @return service ghi nhật ký thao tác */
    public StaffAuditLogService auditLog() {
        return staffAuditLogService;
    }

    /** @return service thống kê báo cáo kỳ thi */
    public ExamReportStatsService reportStats() {
        return examReportStatsService;
    }

    /** @return service phân tích trạng thái thủ tục báo cáo */
    public ExamReportProcedureStatusService reportProcedureStatus() {
        return examReportProcedureStatusService;
    }

    /** @return service xuất báo cáo kỳ thi */
    public StaffReportExportService reportExport() {
        return staffReportExportService;
    }

    /** @return service phân công sát hạch viên / auto-allocate */
    public ExaminerAllocationService examinerAllocation() {
        return examinerAllocationService;
    }

    /** @return service bàn phân công sát hạch viên */
    public ExaminerAllocationDeskService examinerAllocationDesk() {
        return examinerAllocationDeskService;
    }
}
