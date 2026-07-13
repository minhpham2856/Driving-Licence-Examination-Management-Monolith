package examstaff.service;

import examstaff.dto.CandidatePhotoStreamDTO;

public interface CandidatePhotoLookupService {

    CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int fallbackExamId, String sbd);
}
