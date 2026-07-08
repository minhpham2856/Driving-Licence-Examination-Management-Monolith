package service.impl;

import dao.CandidateCallDAO;
import dao.impl.CandidateCallDAOImpl;
import dto.candidate.CandidateCallDTO;
import service.CandidateCallRecordService;

public class CandidateCallRecordServiceImpl implements CandidateCallRecordService {

    private final CandidateCallDAO candidateCallDAO;

    public CandidateCallRecordServiceImpl() {
        this(new CandidateCallDAOImpl());
    }

    public CandidateCallRecordServiceImpl(CandidateCallDAO candidateCallDAO) {
        this.candidateCallDAO = candidateCallDAO;
    }

    @Override
    public boolean recordCall(CandidateCallDTO call) {
        return candidateCallDAO.insert(call);
    }
}
