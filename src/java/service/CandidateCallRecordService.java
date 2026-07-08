package service;

import dto.candidate.CandidateCallDTO;

public interface CandidateCallRecordService {

    boolean recordCall(CandidateCallDTO call);
}
