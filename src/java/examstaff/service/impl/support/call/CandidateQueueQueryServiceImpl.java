package examstaff.service.impl.support.call;

import examstaff.service.impl.support.view.CandidatePhotoServiceImpl;
import examstaff.dao.ExamStaffCandidateViewDAO;
import examstaff.dao.impl.ExamStaffCandidateViewDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.util.ExamStaffCandidateMapper;

import java.util.List;

/**
 * Đường chuẩn list thí sinh cho staff UI / CallBoard / Public Call (ExamStaffCandidateViewDAO).
 */
public class CandidateQueueQueryServiceImpl {

    private final ExamStaffCandidateViewDAO candidateViewDAO = new ExamStaffCandidateViewDAOImpl();
    private final CandidatePhotoServiceImpl photoService = new CandidatePhotoServiceImpl();

    /**
     * Lấy danh sách đăng ký thí sinh của một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách thí sinh trong hàng đợi kỳ
     */
    public List<ExamRegistrationDTO> listByExamId(int examId) {
        return ExamStaffCandidateMapper.toDtoList(candidateViewDAO.findByExamId(examId));
    }

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return hồ sơ khớp, hoặc null
     */
    public ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd) {
        return ExamStaffCandidateMapper.toDto(candidateViewDAO.findByExamIdAndSbd(examId, sbd));
    }

    /**
     * Chuẩn hóa tham chiếu ảnh trên hàng đợi (đĩa data runtime).
     *
     * @param queue hàng đợi thí sinh
     */
    public void normalizePhotoPaths(List<ExamRegistrationDTO> queue) {
        photoService.normalizePhotoPaths(queue);
        photoService.normalizeQueue(queue);
    }
}
