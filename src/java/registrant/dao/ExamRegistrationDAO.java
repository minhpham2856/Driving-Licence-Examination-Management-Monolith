package registrant.dao;

import registrant.dto.exam.ExamRegistration;
import registrant.dto.RegistrantSectionRegistrationBlock;
import registrant.dto.exam.SessionExamSectionInfo;
import registrant.dto.exam.SessionScheduleInfo;
import java.util.List;

/** API đăng ký thi/Candidate — portal dùng insert/getLastInsertError/findActive/conflict; staff/examiner dùng updateScores/attendance. */
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
    /** Đăng ký đợt thi portal: chỉ ghi ExamRegistration; reg.id = ExamRegistrationId. Không tạo Candidate/ExamEnrollment. */
    boolean insert(ExamRegistration reg);

    /** Thông báo lỗi chi tiết từ lần insert gần nhất (trùng ca/CCCD, ca đóng...) — null nếu thành công hoặc chưa gọi. */
    String getLastInsertError();
    List<ExamRegistration> getAllCandidates();
    boolean markAbsent(int candidateId);
    boolean clearAbsentMarking(int candidateId);
    /** Đã có ExamRegistration portal cho (profile, examId/sessionId) chưa — trả ExamRegistrationId nếu có. */
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    SessionExamSectionInfo findPrimarySectionForSession(int sessionId);

    /** Đăng ký lifecycle còn active cùng (profile, licence, section) — loại ER workflow tài liệu khỏi kết quả. */
    RegistrantSectionRegistrationBlock findActiveSectionRegistration(int profileId, int licenceId, int sectionId);

    SessionScheduleInfo findSessionSchedule(int sessionId);

    /** Các ca lifecycle còn hiệu lực của profile — dùng kiểm tra trùng ngày giữa hạng GPLX khác nhau. */
    List<SessionScheduleInfo> listActiveSessionSchedulesByProfileId(int profileId);

    /** Thí sinh gửi yêu cầu hủy — chỉ PreRegistered + SBD tạm; đổi status CancelRequested. */
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
