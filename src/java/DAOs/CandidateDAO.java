package DAOs;

import DTOs.CandidateDTO;
import java.util.List;
import java.util.Map;

public interface CandidateDAO {
    CandidateDTO getById(int id);
    CandidateDTO getBySessionAndSbd(int sessionId, String sbd);
    List<CandidateDTO> getCandidatesBySession(int sessionId);
    boolean updatePresent(int id, boolean isPresent);
    boolean updatePayment(int id, boolean isPaymentCompleted);
    boolean updateComputer(int id, String computerCode);
    boolean updateAllocatedRoom(int id, int areaId, String areaName);
    boolean updateDevice(int id, String deviceCode);
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    /** Updates theory score as correct-answer count (0–35) with a pass threshold. */
    boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold);

    boolean updateRoadScore(int id, Integer roadScore, String roadPassed);
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    boolean updateExaminerProfile(int id, String fullName, java.sql.Date dob, String govIdNo,
            String email, String phoneNo, String address, String sex, String reasonForTaking);
    boolean updatePhoto(int id, String photoUrl);
    boolean insert(CandidateDTO reg);
    List<CandidateDTO> getAllCandidates();
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    /** Applies score deductions for a candidate section and recalculates ExamScore. */
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    /** Adjust occurrence count (+1 / -1) for a score deduction during practical scoring. */
    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta);

    /** Recalculate practical score from deductions and mark section awaiting signature. */
    boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword);

    List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    boolean markSuspended(int candidateId);

    boolean undoSuspension(int candidateId);

    void syncSectionStatusesForSession(int sessionId);

    boolean markSignaturePrinted(int candidateId, int sessionId);

    boolean completeSection(int candidateId, int sessionId);
}
