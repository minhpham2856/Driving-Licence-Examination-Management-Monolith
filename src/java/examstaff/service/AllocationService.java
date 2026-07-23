package examstaff.service;

import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ServiceResult;
import shared.model.ExamArea;

import java.util.List;

/**
 * Facade phân phòng thí sinh: thao tác gán/đổi/gỡ phòng LT/TH và truy vấn khu vực đã có SHV.
 *
 * Hai nhóm nghiệp vụ:
 * - <b>Action</b> — {@code autoAllocateOnOverview}, {@code executeCandidateAction}
 *       (mutate phân bổ theo giai đoạn {@code theory}/{@code practical})
 * - <b>Query</b> — {@code findCandidate}, {@code listStaffedTheoryRoomsForExam},
 *       {@code listStaffedPracticalAreasForExam}, {@code findAreaById}
 * Kết quả thao tác bọc {@link ServiceResult} kèm {@link AllocationActionResultDTO}.
 */
public interface AllocationService {

    /**
     * Tự động phân phòng trên màn overview theo giai đoạn LT/TH.
     * @param examId mã kỳ thi
     * @param stage  giai đoạn ({@code theory}/{@code practical}, …)
     * @return {@link ServiceResult} kèm kết quả phân bổ
     */
    ServiceResult<AllocationActionResultDTO> autoAllocateOnOverview(int examId, String stage);

    /**
     * Thực hiện một thao tác phân phòng trên thí sinh (gán/đổi/gỡ…).
     * @param request yêu cầu thao tác
     * @return {@link ServiceResult} kèm kết quả / lỗi validation
     */
    ServiceResult<AllocationActionResultDTO> executeCandidateAction(AllocationCandidateActionRequest request);

    /**
     * Tìm thí sinh trong hàng đợi hoặc theo mã đăng ký / kỳ thi.
     * @param regId  mã đăng ký / thí sinh
     * @param examId mã kỳ thi
     * @param queue  hàng đợi (có thể null)
     * @return hồ sơ hoặc {@code null}
     */
    ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue);

    /**
     * Danh sách phòng lý thuyết đã có sát hạch viên trong kỳ.
     * @param examId mã kỳ thi
     * @return danh sách khu vực
     */
    List<ExamArea> listStaffedTheoryRoomsForExam(int examId);

    /**
     * Danh sách sân/phòng thực hành đã có sát hạch viên trong kỳ.
     * @param examId mã kỳ thi
     * @return danh sách khu vực
     */
    List<ExamArea> listStaffedPracticalAreasForExam(int examId);

    /**
     * Lấy khu vực thi theo mã.
     * @param examAreaId mã khu vực
     * @return khu vực hoặc {@code null}
     */
    ExamArea findAreaById(int examAreaId);
}
