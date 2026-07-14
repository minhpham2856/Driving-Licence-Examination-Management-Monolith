package examstaff.service;

import examstaff.service.impl.AllocationActionServiceImpl;
import examstaff.service.impl.AllocationStageViewServiceImpl;
import examstaff.service.impl.CallBoardSyncServiceImpl;
import examstaff.service.impl.CandidateCallPageServiceImpl;
import examstaff.service.impl.CandidateDossierServiceImpl;
import examstaff.service.impl.CandidateCallRecordServiceImpl;
import examstaff.service.impl.CandidateCallWorkflowServiceImpl;
import examstaff.service.impl.CandidateCallingServiceImpl;
import examstaff.service.impl.CandidatePhotoLookupServiceImpl;
import examstaff.service.impl.CandidatePhotoServiceImpl;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ExamAreaQueryServiceImpl;
import examstaff.service.impl.ExamReportProcedureStatusServiceImpl;
import examstaff.service.impl.ExamReportStatsServiceImpl;
import examstaff.service.ExamSessionControlService;
import examstaff.service.impl.ExamSessionControlServiceImpl;
import examstaff.service.impl.ExamStaffDashboardServiceImpl;
import examstaff.service.impl.ExamStaffPageServiceImpl;
import examstaff.service.impl.ExamStaffSelectionServiceImpl;
import examstaff.service.impl.ExamStaffSessionQueryServiceImpl;
import examstaff.service.impl.ExaminerAllocationDeskServiceImpl;
import examstaff.service.impl.ExaminerAllocationServiceImpl;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.impl.ProcedureFeeQueryServiceImpl;
import examstaff.service.impl.ProcedurePaymentServiceImpl;
import examstaff.service.impl.ProcedureWorkflowServiceImpl;
import examstaff.service.impl.PublicCallQueryServiceImpl;
import examstaff.service.impl.ExamSelectServiceImpl;
import examstaff.service.impl.StaffAuditExportServiceImpl;
import examstaff.service.impl.StaffAuditLogServiceImpl;
import examstaff.service.impl.StaffAuditPageServiceImpl;
import examstaff.service.impl.StaffAuditQueryServiceImpl;
import examstaff.service.impl.StaffReportExportServiceImpl;

/**
 * Composition root tạm cho exam staff / public call.
 * Tập trung wiring mặc định để controller/support không rải {@code new *Impl()} khắp nơi.
 */
public final class ExamStaffServices {

    private final ExamStaffSessionQueryService sessionQueryService;
    private final CandidateQueueService candidateQueueService;
    private final ExamStaffPageService examStaffPageService;
    private final ExamStaffSelectionService examStaffSelectionService;
    private final CandidateCallingService candidateCallingService;
    private final CandidateCallRecordService candidateCallRecordService;
    private final CandidateCallWorkflowService candidateCallWorkflowService;
    private final CandidateCallPageService candidateCallPageService;
    private final CallBoardSyncService callBoardSyncService;
    private final PublicCallQueryService publicCallQueryService;
    private final ProcedureFeeQueryService procedureFeeQueryService;
    private final ExamAreaQueryService examAreaQueryService;
    private final AllocationActionService allocationActionService;
    private final AllocationStageViewService allocationStageViewService;
    private final CandidatePhotoLookupService candidatePhotoLookupService;
    private final CandidatePhotoService candidatePhotoService;
    private final CandidateDossierService candidateDossierService;
    private final ProcedureWorkflowService procedureWorkflowService;
    private final ExamSelectService examSelectService;
    private final ExamSessionControlService examSessionControlService;
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

    public ExamStaffServices() {
        this.sessionQueryService = new ExamStaffSessionQueryServiceImpl();
        this.candidateQueueService = new CandidateQueueServiceImpl();
        this.examStaffPageService = new ExamStaffPageServiceImpl(
                this.sessionQueryService, this.candidateQueueService);
        this.examStaffSelectionService = new ExamStaffSelectionServiceImpl(this.examStaffPageService);
        this.candidateCallingService = new CandidateCallingServiceImpl(this.candidateQueueService);
        this.candidateCallRecordService = new CandidateCallRecordServiceImpl();
        this.candidateCallWorkflowService = new CandidateCallWorkflowServiceImpl(
                this.candidateQueueService, this.candidateCallRecordService,
                new examstaff.service.impl.CandidateAttendanceServiceImpl());
        this.candidateCallPageService = new CandidateCallPageServiceImpl(
                this.candidateCallWorkflowService, this.candidateCallingService,
                this.candidateQueueService, this.sessionQueryService);
        this.callBoardSyncService = new CallBoardSyncServiceImpl();
        this.publicCallQueryService = new PublicCallQueryServiceImpl(
                new examstaff.service.impl.CandidateQueueQueryServiceImpl(), this.sessionQueryService,
                this.callBoardSyncService);
        this.procedureFeeQueryService = new ProcedureFeeQueryServiceImpl();
        this.examAreaQueryService = new ExamAreaQueryServiceImpl();
        this.allocationActionService = new AllocationActionServiceImpl();
        this.allocationStageViewService = new AllocationStageViewServiceImpl();
        this.candidatePhotoLookupService = new CandidatePhotoLookupServiceImpl();
        this.candidatePhotoService = new CandidatePhotoServiceImpl();
        this.candidateDossierService = new CandidateDossierServiceImpl();
        this.procedureWorkflowService = new ProcedureWorkflowServiceImpl(
                new ExamRegistrationServiceImpl(),
                new ProcedurePaymentServiceImpl(),
                this.candidatePhotoService,
                this.candidateQueueService,
                new ExaminerAllocationServiceImpl());
        this.examSelectService = new ExamSelectServiceImpl();
        this.examSessionControlService = new ExamSessionControlServiceImpl(
                new examstaff.dao.impl.ExamSessionDAOImpl(), new examstaff.dao.impl.ExaminerAssignmentDAOImpl());
        this.examStaffDashboardService = new ExamStaffDashboardServiceImpl(
                this.sessionQueryService, new ExaminerAllocationServiceImpl());
        this.staffAuditPageService = new StaffAuditPageServiceImpl();
        this.staffAuditQueryService = new StaffAuditQueryServiceImpl();
        this.staffAuditExportService = new StaffAuditExportServiceImpl();
        this.staffAuditLogService = new StaffAuditLogServiceImpl();
        this.examReportStatsService = new ExamReportStatsServiceImpl();
        this.examReportProcedureStatusService = new ExamReportProcedureStatusServiceImpl();
        this.staffReportExportService = new StaffReportExportServiceImpl();
        this.examinerAllocationService = new ExaminerAllocationServiceImpl();
        this.examinerAllocationDeskService = new ExaminerAllocationDeskServiceImpl();
    }

    public CandidateQueueService candidateQueue() {
        return candidateQueueService;
    }

    public ExamStaffPageService page() {
        return examStaffPageService;
    }

    public ExamStaffSelectionService selection() {
        return examStaffSelectionService;
    }

    public CandidateCallingService calling() {
        return candidateCallingService;
    }

    public CandidateCallPageService callPage() {
        return candidateCallPageService;
    }

    public CallBoardSyncService callBoardSync() {
        return callBoardSyncService;
    }

    public PublicCallQueryService publicCallQuery() {
        return publicCallQueryService;
    }

    public ProcedureFeeQueryService procedureFees() {
        return procedureFeeQueryService;
    }

    public ExamAreaQueryService examAreas() {
        return examAreaQueryService;
    }

    public AllocationActionService allocationActions() {
        return allocationActionService;
    }

    public AllocationStageViewService allocationStageView() {
        return allocationStageViewService;
    }

    public CandidatePhotoLookupService photoLookup() {
        return candidatePhotoLookupService;
    }

    public CandidatePhotoService photos() {
        return candidatePhotoService;
    }

    public CandidateDossierService dossiers() {
        return candidateDossierService;
    }

    public ProcedureWorkflowService procedures() {
        return procedureWorkflowService;
    }

    public ExamSelectService examSelect() {
        return examSelectService;
    }

    public ExamSessionControlService sessionControl() {
        return examSessionControlService;
    }

    public ExamStaffDashboardService dashboard() {
        return examStaffDashboardService;
    }

    public StaffAuditPageService auditPage() {
        return staffAuditPageService;
    }

    public StaffAuditQueryService auditQuery() {
        return staffAuditQueryService;
    }

    public StaffAuditExportService auditExport() {
        return staffAuditExportService;
    }

    public StaffAuditLogService auditLog() {
        return staffAuditLogService;
    }

    public ExamReportStatsService reportStats() {
        return examReportStatsService;
    }

    public ExamReportProcedureStatusService reportProcedureStatus() {
        return examReportProcedureStatusService;
    }

    public StaffReportExportService reportExport() {
        return staffReportExportService;
    }

    public ExaminerAllocationService examinerAllocation() {
        return examinerAllocationService;
    }

    public ExaminerAllocationDeskService examinerAllocationDesk() {
        return examinerAllocationDeskService;
    }
}
