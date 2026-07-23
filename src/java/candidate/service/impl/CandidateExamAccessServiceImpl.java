package candidate.service.impl;

import candidate.dao.CandidateExamAccessDAO;
import candidate.dao.impl.CandidateExamAccessDAOImpl;
import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import candidate.service.CandidateExamAccessService;
import java.util.Map;

public class CandidateExamAccessServiceImpl implements CandidateExamAccessService {

    private final CandidateExamAccessDAO accessDAO = new CandidateExamAccessDAOImpl();

    @Override
    public int loginExam(String examCodeOrId, String examPassword) {
        return accessDAO.findActiveExamIdForLogin(examCodeOrId, examPassword);
    }

    @Override
    public CandidateExamContextDTO authenticate(int examId, String candidateNumber) {
        return accessDAO.getEligibleTheoryContext(examId, candidateNumber);
    }

    @Override
    public boolean start(CandidateExamContextDTO context) {
        if (context == null) {
            return false;
        }
        return accessDAO.startTheoryAttempt(context, 25);
    }

    @Override
    public CandidateExamResultDTO submit(CandidateExamContextDTO context, Map<Integer, String> answers) {
        if (context == null || context.getTheoryPaperId() <= 0 || context.getQuestions() == null) {
            return null;
        }
        return accessDAO.submit(context.getTheoryPaperId(), context, answers);
    }
}
