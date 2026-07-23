package examstaff.service.impl;

import examstaff.dto.AllocationStageViewDTO;
import examstaff.dto.CandidateDossierViewDTO;
import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamStaffPickerViewDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamTransitionResultDTO;
import examstaff.dto.ServiceResult;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.service.ExamStaffViewService;
import examstaff.util.ExamRegistrationSort;

import java.util.List;
import examstaff.service.impl.support.view.ExamStaffPageServiceImpl;
import examstaff.service.impl.support.view.ExamStaffSelectionServiceImpl;
import examstaff.service.impl.support.view.ExamStaffDashboardServiceImpl;
import examstaff.service.impl.support.allocation.AllocationStageViewServiceImpl;
import examstaff.service.impl.support.view.CandidateDossierServiceImpl;
import examstaff.service.impl.support.view.CandidatePhotoServiceImpl;
import examstaff.service.impl.support.view.ExamReportStatsServiceImpl;
import examstaff.service.impl.support.view.ExamReportProcedureStatusServiceImpl;
import examstaff.service.impl.support.audit.StaffAuditPageServiceImpl;
import examstaff.service.impl.support.call.CandidateQueueServiceImpl;
import examstaff.service.impl.support.shared.ExamStaffExamQueryServiceImpl;
import examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl;
import examstaff.service.impl.support.audit.StaffAuditQueryServiceImpl;

/**
 * Implementation {@link ExamStaffViewService}: consolidator đọc/view cho màn hình staff.
 *
 * Ủy quyền support services:
 * - <b>Trang / chọn kỳ</b> — {@link ExamStaffPageServiceImpl},
 *       {@link ExamStaffSelectionServiceImpl}
 * - <b>Dashboard / phân phòng view</b> — {@link ExamStaffDashboardServiceImpl},
 *       {@link AllocationStageViewServiceImpl}
 * - <b>Thí sinh / báo cáo</b> — {@link CandidateDossierServiceImpl},
 *       {@link CandidatePhotoServiceImpl}, {@link ExamReportStatsServiceImpl},
 *       {@link ExamReportProcedureStatusServiceImpl}
 * - <b>Audit page (read)</b> — {@link StaffAuditPageServiceImpl}
 * Thao tác mutate gọi số và ghi audit đi qua {@link StaffCallServiceImpl},
 * {@link AuditServiceImpl} — không qua consolidator này.
 */
public class ExamStaffViewServiceImpl implements ExamStaffViewService {

    private final ExamStaffPageServiceImpl page;
    private final ExamStaffSelectionServiceImpl selection;
    private final ExamStaffDashboardServiceImpl dashboard;
    private final AllocationStageViewServiceImpl allocationStageView;
    private final CandidateDossierServiceImpl dossiers;
    private final CandidatePhotoServiceImpl photos;
    private final ExamReportStatsServiceImpl reportStats;
    private final ExamReportProcedureStatusServiceImpl procedureStatus;
    private final StaffAuditPageServiceImpl auditPage;
    private final CandidateQueueServiceImpl candidateQueue;

    /** Wiring mặc định (composition root). */
    public ExamStaffViewServiceImpl() {
        CandidateQueueServiceImpl queue = new CandidateQueueServiceImpl();
        ExamStaffExamQueryServiceImpl examQuery = new ExamStaffExamQueryServiceImpl();
        this.page = new ExamStaffPageServiceImpl(examQuery, queue);
        this.selection = new ExamStaffSelectionServiceImpl(this.page);
        this.dashboard = new ExamStaffDashboardServiceImpl(examQuery, new ExaminerAllocationServiceImpl());
        this.allocationStageView = new AllocationStageViewServiceImpl();
        this.dossiers = new CandidateDossierServiceImpl();
        this.photos = new CandidatePhotoServiceImpl(queue);
        this.reportStats = new ExamReportStatsServiceImpl();
        this.procedureStatus = new ExamReportProcedureStatusServiceImpl();
        this.auditPage = new StaffAuditPageServiceImpl(new StaffAuditQueryServiceImpl());
        this.candidateQueue = queue;
    }

    /**
     * Inject dependencies (test / composition).
     * @param page                dịch vụ trang / kỳ thi
     * @param selection           chọn / sync kỳ thi
     * @param dashboard           dashboard
     * @param allocationStageView view giai đoạn phân phòng
     * @param dossiers            hồ sơ chi tiết
     * @param photos              ảnh thí sinh
     * @param reportStats         thống kê báo cáo
     * @param procedureStatus     trạng thái thủ tục
     * @param auditPage           trang audit
     * @param candidateQueue      hàng đợi
     */
    public ExamStaffViewServiceImpl(ExamStaffPageServiceImpl page,
            ExamStaffSelectionServiceImpl selection,
            ExamStaffDashboardServiceImpl dashboard,
            AllocationStageViewServiceImpl allocationStageView,
            CandidateDossierServiceImpl dossiers,
            CandidatePhotoServiceImpl photos,
            ExamReportStatsServiceImpl reportStats,
            ExamReportProcedureStatusServiceImpl procedureStatus,
            StaffAuditPageServiceImpl auditPage,
            CandidateQueueServiceImpl candidateQueue) {
        this.page = page;
        this.selection = selection;
        this.dashboard = dashboard;
        this.allocationStageView = allocationStageView;
        this.dossiers = dossiers;
        this.photos = photos;
        this.reportStats = reportStats;
        this.procedureStatus = procedureStatus;
        this.auditPage = auditPage;
        this.candidateQueue = candidateQueue;
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#listAllExams}.
     * @return danh sách kỳ thi
     */
    @Override
    public List<ExamSummaryDTO> listAllExams() {
        return page.listAllExams();
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#findExamById}.
     * @param examId   mã kỳ thi
     * @param allExams danh sách kỳ
     * @return kỳ thi hoặc {@code null}
     */
    @Override
    public ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allExams) {
        return page.findExamById(examId, allExams);
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#representativeExam}.
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ ưu tiên
     * @return kỳ đại diện
     */
    @Override
    public ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId) {
        return page.representativeExam(allExams, examId);
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#resolvePrimaryExamId}.
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ
     * @return mã kỳ chính
     */
    @Override
    public int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId) {
        return page.resolvePrimaryExamId(allExams, examId);
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#resolveDefaultExamId}.
     * @param allExams danh sách kỳ
     * @return mã kỳ mặc định
     */
    @Override
    public int resolveDefaultExamId(List<ExamSummaryDTO> allExams) {
        return page.resolveDefaultExamId(allExams);
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#buildPickerView}.
     * @param allExams  danh sách kỳ
     * @param examId    mã kỳ hiện tại
     * @param urlExamId mã trên URL
     * @return view picker
     */
    @Override
    public ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allExams, int examId, int urlExamId) {
        return page.buildPickerView(allExams, examId, urlExamId);
    }

    /**
     * Ủy quyền sang {@link ExamStaffPageServiceImpl#preparePageContext}.
     * @param input lệnh trang
     * @return context trang
     */
    @Override
    public ExamStaffPageContext preparePageContext(ExamStaffPageCommand input) {
        return page.preparePageContext(input);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#preparePageTransition}.
     * @param input lệnh trang
     * @return kết quả transition
     */
    @Override
    public ExamTransitionResultDTO preparePageTransition(ExamStaffPageCommand input) {
        return selection.preparePageTransition(input);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#resolveExamId}.
     * @param input lệnh trang
     * @return mã kỳ thi
     */
    @Override
    public int resolveExamId(ExamStaffPageCommand input) {
        return selection.resolveExamId(input);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#ensureExamId}.
     * @param input lệnh trang
     * @return mã kỳ đã đảm bảo
     */
    @Override
    public int ensureExamId(ExamStaffPageCommand input) {
        return selection.ensureExamId(input);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#resolveExamFromUrl}.
     * @param urlExamId mã trên URL
     * @param allExams  danh sách kỳ
     * @return mã kỳ hợp lệ
     */
    @Override
    public int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams) {
        return selection.resolveExamFromUrl(urlExamId, allExams);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#syncExamSelection}.
     * @param examId        mã kỳ mới
     * @param currentExamId mã kỳ hiện tại
     * @param allExams      danh sách kỳ
     * @return kết quả sync
     */
    @Override
    public ExamTransitionResultDTO syncExamSelection(int examId, Integer currentExamId,
            List<ExamSummaryDTO> allExams) {
        return selection.syncExamSelection(examId, currentExamId, allExams);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#resolveActiveExamId}.
     * @param urlExamId           mã trên URL
     * @param selectedExamId      mã đã chọn
     * @param runtimeActiveExamId mã active runtime
     * @return mã kỳ active
     */
    @Override
    public int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId) {
        return selection.resolveActiveExamId(urlExamId, selectedExamId, runtimeActiveExamId);
    }

    /**
     * Ủy quyền sang {@link ExamStaffSelectionServiceImpl#processSelection}.
     * @param request lệnh trang
     * @return {@link ServiceResult} kèm transition
     */
    @Override
    public ServiceResult<ExamTransitionResultDTO> processSelection(ExamStaffPageCommand request) {
        return selection.processSelection(request);
    }

    /**
     * Ủy quyền sang {@link ExamStaffDashboardServiceImpl#buildView}.
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ
     * @return DTO dashboard
     */
    @Override
    public ExamStaffDashboardViewDTO buildDashboardView(List<ExamSummaryDTO> allExams, int examId) {
        return dashboard.buildView(allExams, examId);
    }

    /**
     * Ủy quyền sang {@link AllocationStageViewServiceImpl#buildView}.
     * @param candidates   danh sách thí sinh
     * @param stage        giai đoạn
     * @param resultFilter lọc kết quả
     * @param searchQuery  từ khóa
     * @param page         trang
     * @param pageSize     kích thước trang
     * @param sortSpec     sắp xếp
     * @param areaFilterId lọc khu vực
     * @return DTO stage view
     */
    @Override
    public AllocationStageViewDTO buildAllocationStageView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId) {
        return allocationStageView.buildView(candidates, stage, resultFilter, searchQuery,
                page, pageSize, sortSpec, areaFilterId);
    }

    /**
     * Ủy quyền sang {@link CandidateDossierServiceImpl#loadDossier}.
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return DTO dossier
     */
    @Override
    public CandidateDossierViewDTO loadDossier(int examId, String sbd) {
        return dossiers.loadDossier(examId, sbd);
    }

    /**
     * Ủy quyền sang {@link CandidatePhotoServiceImpl#resolvePhoto}.
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return DTO stream ảnh
     */
    @Override
    public CandidatePhotoStreamDTO resolvePhoto(int examId, int fallbackExamId, String sbd) {
        return photos.resolvePhoto(examId, fallbackExamId, sbd);
    }

    /**
     * Ủy quyền sang {@link CandidatePhotoServiceImpl#resolveCapturedPhoto}.
     * @param reg hồ sơ thí sinh
     * @return {@code true} nếu có ảnh hợp lệ
     */
    @Override
    public boolean resolveCapturedPhoto(ExamRegistrationDTO reg) {
        return photos.resolveCapturedPhoto(reg);
    }

    /**
     * Ủy quyền sang {@link ExamReportStatsServiceImpl#computeStats}.
     * @param candidates danh sách thí sinh
     * @param examId     mã kỳ thi
     * @return thống kê
     */
    @Override
    public ExamReportStatsDTO computeReportStats(List<ExamRegistrationDTO> candidates, int examId) {
        return reportStats.computeStats(candidates, examId);
    }

    /**
     * Ủy quyền sang {@link ExamReportProcedureStatusServiceImpl#analyze}.
     * @param candidates danh sách thí sinh
     * @return trạng thái thủ tục
     */
    @Override
    public ExamReportProcedureStatusDTO analyzeProcedureStatus(List<ExamRegistrationDTO> candidates) {
        return procedureStatus.analyze(candidates);
    }

    /**
     * Ủy quyền sang {@link StaffAuditPageServiceImpl#buildPage}.
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc
     * @param page                 trang
     * @param pageSize             kích thước trang
     * @param filterContextChanged có đổi bộ lọc
     * @return DTO trang audit
     */
    @Override
    public StaffAuditPageViewDTO buildAuditPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged) {
        return auditPage.buildPage(userId, filterDate, page, pageSize, filterContextChanged);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#refreshQueue}.
     * @param input lệnh trang
     * @return snapshot hàng đợi
     */
    @Override
    public CandidateQueueSnapshotDTO refreshQueue(ExamStaffPageCommand input) {
        return candidateQueue.refreshQueue(input);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#buildSnapshot}.
     * @param queue          hàng đợi
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot
     */
    @Override
    public CandidateQueueSnapshotDTO buildQueueSnapshot(List<ExamRegistrationDTO> queue, int examId,
            int fallbackExamId) {
        return candidateQueue.buildSnapshot(queue, examId, fallbackExamId);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveSyncedCallingSbd}.
     * @param httpCallingSbd SBD từ request
     * @param callBoard      trạng thái bảng gọi
     * @param queue          hàng đợi
     * @return SBD đã sync
     */
    @Override
    public String resolveSyncedCallingSbd(String httpCallingSbd, examstaff.dto.CallBoardState callBoard,
            List<ExamRegistrationDTO> queue) {
        return candidateQueue.resolveSyncedCallingSbd(httpCallingSbd, callBoard, queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#listSuspendedInExam}.
     * @param queue hàng đợi
     * @return danh sách đình chỉ
     */
    @Override
    public List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue) {
        return candidateQueue.listSuspendedInExam(queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveCallingCandidate}.
     * @param callingSbd SBD đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ hoặc {@code null}
     */
    @Override
    public ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue) {
        return candidateQueue.resolveCallingCandidate(callingSbd, queue);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#resolveNextCallingSbd}.
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD vừa xử lý
     * @return SBD tiếp theo
     */
    @Override
    public String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        return candidateQueue.resolveNextCallingSbd(fullQueue, afterSbd);
    }

    /**
     * Ủy quyền sang {@link CandidateQueueServiceImpl#moveCallableCandidateToFront}.
     * @param queue hàng đợi (mutate)
     * @param sbd   số báo danh
     * @return {@code true} nếu đã chuyển
     */
    @Override
    public boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        return candidateQueue.moveCallableCandidateToFront(queue, sbd);
    }
}
