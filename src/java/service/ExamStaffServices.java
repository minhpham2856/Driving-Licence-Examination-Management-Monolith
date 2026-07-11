package service;

import service.impl.AllocationActionServiceImpl;
import service.impl.AllocationStageViewServiceImpl;
import service.impl.CallBoardSyncServiceImpl;
import service.impl.CandidateCallBoardServiceImpl;
import service.impl.CandidateCallPageServiceImpl;
import service.impl.CandidateDossierServiceImpl;
import service.impl.CandidateCallRecordServiceImpl;
import service.impl.CandidateCallWorkflowServiceImpl;
import service.impl.CandidateCallingServiceImpl;
import service.impl.CandidatePhotoLookupServiceImpl;
import service.impl.CandidatePhotoServiceImpl;
import service.impl.CandidateQueueServiceImpl;
import service.impl.ExamAreaQueryServiceImpl;
import service.impl.ExamReportProcedureStatusServiceImpl;
import service.impl.ExamReportStatsServiceImpl;
import service.impl.ExamSessionControlServiceImpl;
import service.impl.ExamStaffDashboardServiceImpl;
import service.impl.ExamStaffPageServiceImpl;
import service.impl.ExamStaffSelectionServiceImpl;
import service.impl.ExamStaffSessionQueryServiceImpl;
import service.impl.ExaminerAllocationDeskServiceImpl;
import service.impl.ExaminerAllocationServiceImpl;
import service.impl.ExamRegistrationServiceImpl;
import service.impl.ProcedureFeeQueryServiceImpl;
import service.impl.ProcedurePaymentServiceImpl;
import service.impl.ProcedureWorkflowServiceImpl;
import service.impl.PublicCallQueryServiceImpl;
import service.impl.SessionSelectServiceImpl;
import service.impl.StaffAuditExportServiceImpl;
import service.impl.StaffAuditLogServiceImpl;
import service.impl.StaffAuditPageServiceImpl;
import service.impl.StaffAuditQueryServiceImpl;
import service.impl.StaffReportExportServiceImpl;

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
    private final CandidateCallBoardService candidateCallBoardService;
    private final PublicCallQueryService publicCallQueryService;
    private final ProcedureFeeQueryService procedureFeeQueryService;
    private final ExamAreaQueryService examAreaQueryService;
    private final AllocationActionService allocationActionService;
    private final AllocationStageViewService allocationStageViewService;
    private final CandidatePhotoLookupService candidatePhotoLookupService;
    private final CandidatePhotoService candidatePhotoService;
    private final CandidateDossierService candidateDossierService;
    private final ProcedureWorkflowService procedureWorkflowService;
    private final SessionSelectService sessionSelectService;
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
                new service.impl.CandidateAttendanceServiceImpl());
        this.candidateCallPageService = new CandidateCallPageServiceImpl(
                this.candidateCallWorkflowService, this.candidateCallingService,
                this.candidateQueueService, this.sessionQueryService);
        this.callBoardSyncService = new CallBoardSyncServiceImpl();
        this.candidateCallBoardService = new CandidateCallBoardServiceImpl();
        this.publicCallQueryService = new PublicCallQueryServiceImpl(
                new service.impl.CandidateQueueQueryServiceImpl(), this.sessionQueryService,
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
        this.sessionSelectService = new SessionSelectServiceImpl();
        this.examSessionControlService = new ExamSessionControlServiceImpl(
                new dao.impl.ExamSessionDAOImpl(), new dao.impl.ExaminerAssignmentDAOImpl());
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

    public ExamStaffSessionQueryService sessionQuery() {
        return sessionQueryService;
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

    public CandidateCallRecordService callRecord() {
        return candidateCallRecordService;
    }

    public CandidateCallWorkflowService callWorkflow() {
        return candidateCallWorkflowService;
    }

    public CandidateCallPageService callPage() {
        return candidateCallPageService;
    }

    public CallBoardSyncService callBoardSync() {
        return callBoardSyncService;
    }

    public CandidateCallBoardService callBoard() {
        return candidateCallBoardService;
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

    public SessionSelectService sessionSelect() {
        return sessionSelectService;
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
