package service;

import service.impl.AllocationActionServiceImpl;
import service.impl.AllocationStageViewServiceImpl;
import service.impl.CallBoardSyncServiceImpl;
import service.impl.CandidateCallBoardServiceImpl;
import service.impl.CandidateCallPageServiceImpl;
import service.impl.CandidateCallRecordServiceImpl;
import service.impl.CandidateCallWorkflowServiceImpl;
import service.impl.CandidateCallingServiceImpl;
import service.impl.CandidateQueueServiceImpl;
import service.impl.ExamAreaQueryServiceImpl;
import service.impl.ExamStaffPageServiceImpl;
import service.impl.ExamStaffSelectionServiceImpl;
import service.impl.ExamStaffSessionQueryServiceImpl;
import service.impl.ProcedureFeeQueryServiceImpl;
import service.impl.PublicCallQueryServiceImpl;

/**
 * Composition root tạm cho exam staff / public call.
 * Tập trung wiring mặc định để controller/support không rải {@code new *Impl()} khắp nơi.
 */
public final class ExamStaffServices {

    private static final ExamStaffServices INSTANCE = new ExamStaffServices();

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

    private ExamStaffServices() {
        this.sessionQueryService = new ExamStaffSessionQueryServiceImpl();
        this.candidateQueueService = new CandidateQueueServiceImpl();
        this.examStaffPageService = new ExamStaffPageServiceImpl();
        this.examStaffSelectionService = new ExamStaffSelectionServiceImpl();
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
    }

    public static ExamStaffServices get() {
        return INSTANCE;
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
}
