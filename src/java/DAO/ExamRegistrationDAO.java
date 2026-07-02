package dao;

import model.exam.ExamRegistration;
import dto.registrant.RegistrantSectionRegistrationBlock;
import dto.exam.SessionExamSectionInfo;
import dto.exam.SessionScheduleInfo;
import java.util.List;

public interface ExamRegistrationDAO {
    ExamRegistration getById(int id);
    ExamRegistration getBySessionAndSbd(int sessionId, String sbd);
    List<ExamRegistration> getCandidatesBySession(int sessionId);
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
    boolean updateCandidateNumber(int id, String candidateNumber);
    boolean insert(ExamRegistration reg);

    /** Thông báo lỗi chi tiết từ lần {@link #insert} gần nhất (null nếu thành công hoặc chưa gọi). */
    String getLastInsertError();
    List<ExamRegistration> getAllCandidates();
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    SessionExamSectionInfo findPrimarySectionForSession(int sessionId);

    RegistrantSectionRegistrationBlock findActiveSectionRegistration(int profileId, int licenceId, int sectionId);

    SessionScheduleInfo findSessionSchedule(int sessionId);

    List<SessionScheduleInfo> listActiveSessionSchedulesByProfileId(int profileId);

    boolean requestExamCancellation(int candidateId, int profileId, String reason);

    boolean candidateBelongsToProfile(int candidateId, int profileId);

    /** Applies score deductions for a candidate section and recalculates ExamScore. */
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    boolean markSuspended(int candidateId);

    boolean undoSuspension(int candidateId);

    void syncSectionStatusesForSession(int sessionId);

    boolean markSignaturePrinted(int candidateId, int sessionId);

    boolean completeSection(int candidateId, int sessionId);
}
