package examstaff.service;

import examstaff.dto.ExamRegistrationDTO;

import java.util.List;

/**
 * Truy vấn hàng đợi thí sinh theo kỳ thi (đọc danh sách / tìm theo SBD).
 */
public interface CandidateQueueQueryService {

    /**
     * Lấy danh sách đăng ký thí sinh của một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách thí sinh trong hàng đợi kỳ
     */
    List<ExamRegistrationDTO> listByExamId(int examId);

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return hồ sơ khớp, hoặc null
     */
    ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd);

    /**
     * Chuẩn hóa đường dẫn ảnh trên hàng đợi để hiển thị công khai / bảng gọi.
     *
     * @param webRootPath thư mục gốc web
     * @param queue       hàng đợi thí sinh
     */
    void normalizePhotoPaths(String webRootPath, List<ExamRegistrationDTO> queue);
}
