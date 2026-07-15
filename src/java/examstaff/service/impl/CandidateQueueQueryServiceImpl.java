package examstaff.service.impl;

import examstaff.dao.ExamStaffCandidateViewDAO;
import examstaff.dao.impl.ExamStaffCandidateViewDAOImpl;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.impl.CandidatePhotoServiceImpl;
import examstaff.util.ExamStaffCandidateMapper;

import java.util.List;

/** Implementation: truy vấn hàng đợi thí sinh qua view DAO. */
public class CandidateQueueQueryServiceImpl implements CandidateQueueQueryService {

    private final ExamStaffCandidateViewDAO candidateViewDAO = new ExamStaffCandidateViewDAOImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    /**
     * Lấy danh sách đăng ký thí sinh của một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách thí sinh trong hàng đợi kỳ
     */
    @Override
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
    @Override
    public ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd) {
        return ExamStaffCandidateMapper.toDto(candidateViewDAO.findByExamIdAndSbd(examId, sbd));
    }

    /**
     * Chuẩn hóa đường dẫn ảnh trên hàng đợi để hiển thị công khai / bảng gọi.
     *
     * @param webRootPath thư mục gốc web
     * @param queue       hàng đợi thí sinh
     */
    @Override
    public void normalizePhotoPaths(String webRootPath, List<ExamRegistrationDTO> queue) {
        photoService.normalizePhotoPaths(webRootPath, queue);
        photoService.normalizeQueue(webRootPath, queue);
    }
}
