package examstaff.service;

import examstaff.dto.candidate.CandidateCallDTO;

public interface CandidateCallRecordService {

    boolean recordCall(CandidateCallDTO call);
}
