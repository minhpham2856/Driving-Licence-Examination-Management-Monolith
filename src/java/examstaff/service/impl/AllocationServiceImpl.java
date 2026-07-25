package examstaff.service.impl;

import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ServiceResult;
import examstaff.service.AllocationService;
import shared.enums.ErrorType;
import shared.model.ExamArea;

import java.util.List;
import examstaff.service.impl.support.allocation.AllocationActionServiceImpl;
import examstaff.service.impl.support.allocation.ExamAreaQueryServiceImpl;

/**
 * Implementation AllocationService: ủy quyền action phân phòng và truy vấn khu vực.
 *
 * Ủy quyền support services:
 * - AllocationActionServiceImpl — autoAllocateOnOverview,
 *       executeCandidateAction, findCandidate
 * - ExamAreaQueryServiceImpl — listStaffedTheoryRoomsForExam,
 *       listStaffedPracticalAreasForExam, findAreaById
 * Action thành công bọc ServiceResult.ok; lỗi validation trả ServiceResult.fail.
 */
public class AllocationServiceImpl implements AllocationService {

    private final AllocationActionServiceImpl actions;
    private final ExamAreaQueryServiceImpl areas;

    /** Wiring mặc định. */
    public AllocationServiceImpl() {
        this(new AllocationActionServiceImpl(), new ExamAreaQueryServiceImpl());
    }

    /**
     * Inject dependencies (test / composition).
     * @param actions dịch vụ thao tác phân phòng
     * @param areas   truy vấn khu vực
     */
    public AllocationServiceImpl(AllocationActionServiceImpl actions, ExamAreaQueryServiceImpl areas) {
        this.actions = actions;
        this.areas = areas;
    }

    /**
     * Ủy quyền auto-allocate rồi bọc ServiceResult.ok.
     * @param examId mã kỳ thi
     * @param stage  giai đoạn
     * @return kết quả phân bổ
     */
    @Override
    public ServiceResult<AllocationActionResultDTO> autoAllocateOnOverview(int examId, String stage) {
        // Mutate
        AllocationActionResultDTO data = actions.autoAllocateOnOverview(examId, stage);
        // Result
        return ServiceResult.ok(data);
    }

    /**
     * Thực hiện thao tác thí sinh; lỗi trong DTO → ServiceResult.fail.
     * @param request yêu cầu thao tác
     * @return kết quả / lỗi validation
     */
    @Override
    public ServiceResult<AllocationActionResultDTO> executeCandidateAction(
            AllocationCandidateActionRequest request) {
        // Mutate
        AllocationActionResultDTO data = actions.executeCandidateAction(request);
        // Result
        if (data != null && data.getErrorMsg() != null && !data.getErrorMsg().isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, data.getErrorMsg(), data);
        }
        String message = data != null ? data.getAlertMsg() : null;
        return ServiceResult.ok(data, message);
    }

    /**
     * Ủy quyền sang AllocationActionServiceImpl.findCandidate.
     * @param regId  mã đăng ký / thí sinh
     * @param examId mã kỳ thi
     * @param queue  hàng đợi
     * @return hồ sơ hoặc null
     */
    @Override
    public ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue) {
        return actions.findCandidate(regId, examId, queue);
    }

    /**
     * Ủy quyền sang ExamAreaQueryServiceImpl.listStaffedTheoryRoomsForExam.
     * @param examId mã kỳ thi
     * @return danh sách phòng LT có SHV
     */
    @Override
    public List<ExamArea> listStaffedTheoryRoomsForExam(int examId) {
        return areas.listStaffedTheoryRoomsForExam(examId);
    }

    /**
     * Ủy quyền sang ExamAreaQueryServiceImpl.listStaffedPracticalAreasForExam.
     * @param examId mã kỳ thi
     * @return danh sách sân TH có SHV
     */
    @Override
    public List<ExamArea> listStaffedPracticalAreasForExam(int examId) {
        return areas.listStaffedPracticalAreasForExam(examId);
    }

    /**
     * Ủy quyền sang ExamAreaQueryServiceImpl.findById.
     * @param examAreaId mã khu vực
     * @return khu vực hoặc null
     */
    @Override
    public ExamArea findAreaById(int examAreaId) {
        return areas.findById(examAreaId);
    }
}
