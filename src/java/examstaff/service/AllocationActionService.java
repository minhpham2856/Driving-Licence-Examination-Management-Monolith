package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;

import java.util.List;

/**
 * Thực thi thao tác phân phòng (auto / từng thí sinh) trên overview phân công.
 */
public interface AllocationActionService {

    /**
     * Tự động phân phòng trên màn overview theo giai đoạn.
     *
     * @param examId mã kỳ thi
     * @param stage  giai đoạn phân phòng (LT/TH, …)
     * @return kết quả thao tác auto-allocate
     */
    AllocationActionResultDTO autoAllocateOnOverview(int examId, String stage);

    /**
     * Thực hiện một thao tác phân phòng trên một thí sinh (gán/gỡ/đổi phòng, …).
     *
     * @param request yêu cầu thao tác kèm ngữ cảnh
     * @return kết quả thao tác
     */
    AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request);

    /**
     * Tìm thí sinh trong hàng đợi theo mã đăng ký và kỳ thi.
     *
     * @param regId  mã đăng ký
     * @param examId mã kỳ thi
     * @param queue  hàng đợi nguồn
     * @return hồ sơ khớp, hoặc null
     */
    ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue);
}
