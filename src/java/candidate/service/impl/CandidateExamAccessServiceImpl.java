package candidate.service.impl;

import candidate.dao.CandidateExamAccessDAO;
import candidate.dao.impl.CandidateExamAccessDAOImpl;
import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import candidate.service.CandidateExamAccessService;
import shared.service.ExamAccessOtpService;
import shared.service.impl.ExamAccessOtpServiceImpl;
import java.util.Map;

public class CandidateExamAccessServiceImpl implements CandidateExamAccessService {

    private final CandidateExamAccessDAO accessDAO = new CandidateExamAccessDAOImpl();
    private final ExamAccessOtpService otpService = new ExamAccessOtpServiceImpl();

    @Override
    public CandidateExamContextDTO authenticate(String candidateNumber, String otp) {
        CandidateExamContextDTO context = accessDAO.getEligibleTheoryContext(candidateNumber);
        if (context == null || !otpService.verify(
                context.getExamId(), context.getExamSectionId(), context.getExamAreaId(), otp)) {
            return null;
        }
        return context;
    }

    @Override
    public boolean start(CandidateExamContextDTO context) {
        if (context == null) {
            return false;
        }
        int paperId = accessDAO.startTheoryPaper(context.getExamEnrollmentSectionId());
        context.setTheoryPaperId(paperId);
        context.setQuestions(accessDAO.getRandomQuestions(context.getLicenceId(), 25));
        context.setStartedAtMillis(System.currentTimeMillis());
        return paperId > 0 && context.getQuestions() != null && !context.getQuestions().isEmpty();
    }

    @Override
    public CandidateExamResultDTO submit(CandidateExamContextDTO context, Map<Integer, String> answers) {
        if (context == null || context.getTheoryPaperId() <= 0 || context.getQuestions() == null) {
            return null;
        }
        return accessDAO.submit(context.getTheoryPaperId(), context, answers);
    }
}
