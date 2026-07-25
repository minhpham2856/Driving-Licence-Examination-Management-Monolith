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
 * - <b>Action</b> — autoAllocateOnOverview, executeCandidateAction
 *       (mutate phân bổ theo giai đoạn theory/practical)
 * - <b>Query</b> — findCandidate, listStaffedTheoryRoomsForExam,
 *       listStaffedPracticalAreasForExam, findAreaById
 * Kết quả thao tác bọc ServiceResult kèm AllocationActionResultDTO.
 */
public interface AllocationService {

    /**
     * Tự động phân phòng trên màn overview theo giai đoạn LT/TH.
     * @param examId mã kỳ thi
     * @param stage  giai đoạn (theory/practical, …)
     * @return ServiceResult kèm kết quả phân bổ
     */
    ServiceResult<AllocationActionResultDTO> autoAllocateOnOverview(int examId, String stage);

    /**
     * Thực hiện một thao tác phân phòng trên thí sinh (gán/đổi/gỡ…).
     * @param request yêu cầu thao tác
     * @return ServiceResult kèm kết quả / lỗi validation
     */
    ServiceResult<AllocationActionResultDTO> executeCandidateAction(AllocationCandidateActionRequest request);

    /**
     * Tìm thí sinh trong hàng đợi hoặc theo mã đăng ký / kỳ thi.
     * @param regId  mã đăng ký / thí sinh
     * @param examId mã kỳ thi
     * @param queue  hàng đợi (có thể null)
     * @return hồ sơ hoặc null
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
     * @return khu vực hoặc null
     */
    ExamArea findAreaById(int examAreaId);
}
