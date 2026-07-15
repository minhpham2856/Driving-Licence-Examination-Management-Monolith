package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Nghiệp vụ điểm danh / đánh dấu vắng mặt thí sinh.
 */
public interface CandidateAttendanceService {

    /**
     * Đánh dấu vắng mặt cố định (permanent absent) cho thí sinh.
     *
     * @param candidateId mã đăng ký thí sinh
     * @return true nếu đánh dấu thành công
     */
    boolean markPermanentAbsent(int candidateId);

    /**
     * Khôi phục thí sinh đã bị đánh vắng về trạng thái có thể gọi lại.
     *
     * @param profile hồ sơ thí sinh
     * @return true nếu khôi phục thành công
     */
    boolean restoreAbsentCandidate(ExamRegistrationDTO profile);

    /**
     * Khi kết thúc ca: đánh vắng các thí sinh còn dở trong hàng đợi active.
     *
     * @param activeQueue hàng đợi còn pending khi đóng ca
     * @return danh sách đã được đánh vắng
     */
    List<ExamRegistrationDTO> markIncompleteAsAbsentAtEndShift(List<ExamRegistrationDTO> activeQueue);
}
