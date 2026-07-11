package examstaff.service;

import examstaff.dto.CandidatePhotoStreamDTO;

public interface CandidatePhotoLookupService {

    CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int sessionId, String sbd);
}
