package examstaff.service;

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
import examstaff.dto.AllocationStageViewDTO;
import examstaff.dto.ServiceResult;
import examstaff.dto.StaffAuditPageViewDTO;
import examstaff.util.ExamRegistrationSort;

import java.util.List;

/**
 * Đọc / chuẩn bị view exam staff: kỳ thi, picker, queue, dashboard, dossier, báo cáo, audit page.
 */
public interface ExamStaffViewService {

    /**
     * Danh sách tất cả kỳ thi tóm tắt.
     *
     * @return danh sách {@link ExamSummaryDTO}
     */
    List<ExamSummaryDTO> listAllExams();

    /**
     * Tìm kỳ thi theo mã trong danh sách đã load.
     *
     * @param examId   mã kỳ thi
     * @param allExams danh sách kỳ
     * @return kỳ thi hoặc {@code null}
     */
    ExamSummaryDTO findExamById(int examId, List<ExamSummaryDTO> allExams);

    /**
     * Kỳ thi đại diện khi có nhiều slot cùng ngày / cùng nhóm.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ ưu tiên
     * @return kỳ đại diện
     */
    ExamSummaryDTO representativeExam(List<ExamSummaryDTO> allExams, int examId);

    /**
     * Resolve mã kỳ thi chính từ danh sách và mã yêu cầu.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ (có thể 0)
     * @return mã kỳ hợp lệ
     */
    int resolvePrimaryExamId(List<ExamSummaryDTO> allExams, int examId);

    /**
     * Mã kỳ mặc định khi chưa chọn (ưu tiên đang diễn ra / gần nhất).
     *
     * @param allExams danh sách kỳ
     * @return mã kỳ mặc định
     */
    int resolveDefaultExamId(List<ExamSummaryDTO> allExams);

    /**
     * Ghép DTO màn chọn kỳ thi (picker).
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ hiện tại
     * @param urlExamId mã kỳ trên URL
     * @return view picker
     */
    ExamStaffPickerViewDTO buildPickerView(List<ExamSummaryDTO> allExams, int examId, int urlExamId);

    /**
     * Chuẩn bị context trang staff từ command request.
     *
     * @param input lệnh trang
     * @return context trang
     */
    ExamStaffPageContext preparePageContext(ExamStaffPageCommand input);

    /**
     * Chuẩn bị chuyển trang / đồng bộ kỳ khi vào page.
     *
     * @param input lệnh trang
     * @return kết quả transition
     */
    ExamTransitionResultDTO preparePageTransition(ExamStaffPageCommand input);

    /**
     * Resolve mã kỳ thi từ command (URL / session / mặc định).
     *
     * @param input lệnh trang
     * @return mã kỳ thi
     */
    int resolveExamId(ExamStaffPageCommand input);

    /**
     * Đảm bảo có mã kỳ hợp lệ (ghi vào session nếu cần).
     *
     * @param input lệnh trang
     * @return mã kỳ đã đảm bảo
     */
    int ensureExamId(ExamStaffPageCommand input);

    /**
     * Resolve mã kỳ từ tham số URL so với danh sách.
     *
     * @param urlExamId mã trên URL
     * @param allExams  danh sách kỳ
     * @return mã kỳ hợp lệ
     */
    int resolveExamFromUrl(int urlExamId, List<ExamSummaryDTO> allExams);

    /**
     * Đồng bộ lựa chọn kỳ thi với session hiện tại.
     *
     * @param examId        mã kỳ mới
     * @param currentExamId mã kỳ đang giữ
     * @param allExams      danh sách kỳ
     * @return kết quả sync
     */
    ExamTransitionResultDTO syncExamSelection(int examId, Integer currentExamId, List<ExamSummaryDTO> allExams);

    /**
     * Resolve kỳ đang active từ URL / đã chọn / runtime CallBoard.
     *
     * @param urlExamId           mã trên URL
     * @param selectedExamId      mã đã chọn
     * @param runtimeActiveExamId mã active runtime
     * @return mã kỳ active
     */
    int resolveActiveExamId(int urlExamId, Integer selectedExamId, Integer runtimeActiveExamId);

    /**
     * Xử lý chọn kỳ thi từ form (validate + transition).
     *
     * @param request lệnh trang
     * @return {@link ServiceResult} kèm transition
     */
    ServiceResult<ExamTransitionResultDTO> processSelection(ExamStaffPageCommand request);

    /**
     * Ghép view dashboard kỳ thi.
     *
     * @param allExams danh sách kỳ
     * @param examId   mã kỳ
     * @return DTO dashboard
     */
    ExamStaffDashboardViewDTO buildDashboardView(List<ExamSummaryDTO> allExams, int examId);

    /**
     * Ghép view giai đoạn phân phòng (lọc, sắp xếp, phân trang).
     *
     * @param candidates  danh sách thí sinh
     * @param stage       giai đoạn
     * @param resultFilter lọc kết quả
     * @param searchQuery từ khóa tìm
     * @param page        trang
     * @param pageSize    kích thước trang
     * @param sortSpec    quy tắc sắp xếp
     * @param areaFilterId lọc theo khu vực ({@code null} = tất cả)
     * @return DTO stage view
     */
    AllocationStageViewDTO buildAllocationStageView(List<ExamRegistrationDTO> candidates, String stage,
            String resultFilter, String searchQuery, int page, int pageSize,
            ExamRegistrationSort.Spec sortSpec, Integer areaFilterId);

    /**
     * Load hồ sơ chi tiết thí sinh (dossier).
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return DTO dossier
     */
    CandidateDossierViewDTO loadDossier(int examId, String sbd);

    /**
     * Resolve luồng ảnh thí sinh để stream / hiển thị.
     *
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @param sbd            số báo danh
     * @return DTO stream ảnh
     */
    CandidatePhotoStreamDTO resolvePhoto(int examId, int fallbackExamId, String sbd);

    /**
     * Kiểm tra / gắn cờ ảnh thủ tục đã chụp hợp lệ trên hồ sơ.
     *
     * @param reg hồ sơ thí sinh
     * @return {@code true} nếu có ảnh hợp lệ
     */
    boolean resolveCapturedPhoto(ExamRegistrationDTO reg);

    /**
     * Tính thống kê báo cáo từ danh sách thí sinh.
     *
     * @param candidates danh sách thí sinh
     * @param examId     mã kỳ thi
     * @return thống kê
     */
    ExamReportStatsDTO computeReportStats(List<ExamRegistrationDTO> candidates, int examId);

    /**
     * Phân tích trạng thái hoàn tất thủ tục của danh sách thí sinh.
     *
     * @param candidates danh sách thí sinh
     * @return DTO trạng thái thủ tục
     */
    ExamReportProcedureStatusDTO analyzeProcedureStatus(List<ExamRegistrationDTO> candidates);

    /**
     * Ghép trang audit (ủy quyền sang audit page service).
     *
     * @param userId               mã nhân viên
     * @param filterDate           ngày lọc
     * @param page                 trang
     * @param pageSize             kích thước trang
     * @param filterContextChanged có đổi bộ lọc
     * @return DTO trang audit
     */
    StaffAuditPageViewDTO buildAuditPage(int userId, String filterDate, int page, int pageSize,
            boolean filterContextChanged);

    /**
     * Làm mới snapshot hàng đợi theo command trang.
     *
     * @param input lệnh trang
     * @return snapshot hàng đợi
     */
    CandidateQueueSnapshotDTO refreshQueue(ExamStaffPageCommand input);

    /**
     * Xây snapshot hàng đợi từ danh sách đã có.
     *
     * @param queue          hàng đợi
     * @param examId         mã kỳ thi
     * @param fallbackExamId mã kỳ dự phòng
     * @return snapshot
     */
    CandidateQueueSnapshotDTO buildQueueSnapshot(List<ExamRegistrationDTO> queue, int examId, int fallbackExamId);

    /**
     * Đồng bộ SBD đang gọi giữa HTTP param và CallBoard.
     *
     * @param httpCallingSbd SBD từ request
     * @param callBoard      trạng thái bảng gọi
     * @param queue          hàng đợi
     * @return SBD đã sync
     */
    String resolveSyncedCallingSbd(String httpCallingSbd, examstaff.dto.CallBoardState callBoard,
            List<ExamRegistrationDTO> queue);

    /**
     * Danh sách thí sinh bị đình chỉ trong hàng đợi kỳ.
     *
     * @param queue hàng đợi
     * @return danh sách đình chỉ
     */
    List<ExamRegistrationDTO> listSuspendedInExam(List<ExamRegistrationDTO> queue);

    /**
     * Resolve thí sinh đang được gọi theo SBD.
     *
     * @param callingSbd số báo danh đang gọi
     * @param queue      hàng đợi
     * @return hồ sơ hoặc {@code null}
     */
    ExamRegistrationDTO resolveCallingCandidate(String callingSbd, List<ExamRegistrationDTO> queue);

    /**
     * SBD tiếp theo có thể gọi sau {@code afterSbd}.
     *
     * @param fullQueue hàng đợi đầy đủ
     * @param afterSbd  SBD vừa xử lý
     * @return SBD tiếp theo hoặc {@code null}
     */
    String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd);

    /**
     * Đưa thí sinh có thể gọi lên đầu hàng đợi (mutate list).
     *
     * @param queue hàng đợi (mutate)
     * @param sbd   số báo danh
     * @return {@code true} nếu đã chuyển
     */
    boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd);
}
