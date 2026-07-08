package service.impl;

import dao.view.ExamStaffCandidateViewDAO;
import dao.view.impl.ExamStaffCandidateViewDAOImpl;
import dto.exam.ExamRegistrationDTO;
import service.CandidatePhotoService;
import service.CandidateQueueQueryService;
import service.impl.CandidatePhotoServiceImpl;
import util.examstaff.ExamStaffCandidateMapper;

import java.util.List;

public class CandidateQueueQueryServiceImpl implements CandidateQueueQueryService {

    private final ExamStaffCandidateViewDAO candidateViewDAO = new ExamStaffCandidateViewDAOImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

    @Override
    public List<ExamRegistrationDTO> listBySessionId(int sessionId) {
        return ExamStaffCandidateMapper.toDtoList(candidateViewDAO.findBySessionId(sessionId));
    }

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
