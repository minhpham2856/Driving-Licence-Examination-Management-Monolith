package examstaff.service.impl;

import examstaff.dao.view.ExamStaffCandidateViewDAO;
import examstaff.dao.view.impl.ExamStaffCandidateViewDAOImpl;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.CandidatePhotoService;
import examstaff.service.CandidateQueueQueryService;
import examstaff.service.impl.CandidatePhotoServiceImpl;
import examstaff.util.ExamStaffCandidateMapper;

import java.util.List;

public class CandidateQueueQueryServiceImpl implements CandidateQueueQueryService {

    private final ExamStaffCandidateViewDAO candidateViewDAO = new ExamStaffCandidateViewDAOImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    @Override
    public List<ExamRegistrationDTO> listByExamId(int examId) {
        return ExamStaffCandidateMapper.toDtoList(candidateViewDAO.findByExamId(examId));
    }

    @Override
    public ExamRegistrationDTO findByExamIdAndSbd(int examId, String sbd) {
        return ExamStaffCandidateMapper.toDto(candidateViewDAO.findByExamIdAndSbd(examId, sbd));
    }

    @Override
    public void normalizePhotoPaths(String webRootPath, List<ExamRegistrationDTO> queue) {
        photoService.normalizePhotoPaths(webRootPath, queue);
        photoService.normalizeQueue(webRootPath, queue);
    }
}
