package service;

import dto.examstaff.CandidatePhotoStreamDTO;

public interface CandidatePhotoLookupService {

    CandidatePhotoStreamDTO resolvePhoto(String webRoot, int examId, int sessionId, String sbd);
}
