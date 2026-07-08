package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationScoreResultDTO;

public interface AllocationScoreService {

    AllocationScoreResultDTO submitTheoryScore(ExamRegistrationDTO profile, int sessionId, int score);

    AllocationScoreResultDTO submitPracticalScore(ExamRegistrationDTO profile, int sessionId, int score);

    AllocationScoreResultDTO submitRoadScore(ExamRegistrationDTO profile, int score);
}
