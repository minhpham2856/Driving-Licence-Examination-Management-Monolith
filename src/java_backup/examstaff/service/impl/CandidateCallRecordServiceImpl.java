package examstaff.service.impl;

import examstaff.dao.CandidateCallDAO;
import examstaff.dao.impl.CandidateCallDAOImpl;
import examstaff.dto.candidate.CandidateCallDTO;
import examstaff.service.CandidateCallRecordService;

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
