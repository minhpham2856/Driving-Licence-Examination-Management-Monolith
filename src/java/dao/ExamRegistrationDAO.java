package dao;

import dto.exam.ExamRegistrationDTO;
import model.ExamRegistration;
import java.util.List;

public interface ExamRegistrationDAO {

    // Lay dang ky theo id
    ExamRegistrationDTO getById(int id);

    // Lay model dang ky theo id
    ExamRegistration findById(int id);

    // Lay thi sinh theo ca va SBD
    ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd);

    // Danh sach thi sinh theo ca
    List<ExamRegistrationDTO> getCandidatesBySession(int sessionId);

    // Danh sach thi sinh theo ngay thi
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    // Lay thi sinh theo ngay thi va SBD
    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    // Cap nhat co mat / vang
    boolean updatePresent(int id, boolean isPresent);

    // Cap nhat trang thai thanh toan
    boolean updatePayment(int id, boolean isPaymentCompleted);

    // Gan may tinh cho thi sinh
    boolean updateComputer(int id, String computerCode);

    // Cap nhat phong da phan bo
    boolean updateAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName);

    /**
     * Kiem tra thi sinh da co phong o ca khac trong cung ky thi chua.
     * @return thong bao loi, hoac null neu duoc phep phan / doi phong tai ca nay
     */
    String validateUniqueTheoryAllocation(int candidateId, int sessionId);

    // Gan thiet bi / xe cho thi sinh
    boolean updateDevice(int id, String deviceCode);

    // Cap nhat diem ly thuyet va thuc hanh
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    // Cap nhat diem theo ca thi (uu tien ExamEnrollment + Session_ExamSection cua ca)
    boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed);

    // Cap nhat so cau dung ly thuyet
    boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold);

    // Cap nhat diem duong truong
    boolean updateRoadScore(int id, Integer roadScore, String roadPassed);

    // Cap nhat ho so co ban
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    // Cap nhat ho so day du (giam khao)
    boolean updateExaminerProfile(int id, String fullName, java.sql.Date dob, String govIdNo,
            String email, String phoneNo, String address, String sex, String reasonForTaking);

    // Cap nhat duong dan anh
    boolean updatePhoto(int id, String photoUrl);

    // Xoa giao dich thanh toan da hoan tat
    boolean clearCompletedPayments(int candidateId);

    // Them dang ky thi (qua Profile)
    boolean insert(ExamRegistrationDTO reg);

    // Import DSTS vao Candidate + ExamEnrollment
    boolean insertFromDstsImport(ExamRegistrationDTO reg);

    // Tao ExamEnrollment neu thieu (import / ghi de trung CCCD trong ca)
    boolean ensureExamEnrollmentForSession(int candidateId, int sessionId);

    // Ghi danh tat ca ca thi trong ky ma thi sinh tham gia (theo TakeTheory/TakeLayout/TakeRoad)
    boolean ensureExamEnrollmentsForImport(int candidateId, int examId,
            Boolean takeTheory, Boolean takePractical, Boolean takeOnRoad);

    /** Các loại phần thi có ca trong kỳ: Theory / Practical / Road. */
    java.util.Set<String> findAvailableSectionKindsForExam(int examId);

    // Tim CandidateId theo CCCD trong mot ky thi
    Integer findCandidateIdByGovIdAndExam(String govId, int examId);

    // Tim CandidateId theo CCCD (toan he thong — tranh tao ban ghi Candidate trung)
    Integer findCandidateIdByGovId(String govId);

    // Lay tat ca dang ky
    List<ExamRegistrationDTO> getAllCandidates();

    // Danh dau vang mat
    boolean markAbsent(int candidateId);

    // Huy danh dau vang
    boolean clearAbsentMarking(int candidateId);

    // Tim CandidateId theo Profile va ca
    Integer findCandidateIdByProfileAndSession(int profileId, int sessionId);

    // Tim CandidateId theo CCCD va ca
    Integer findCandidateIdByGovIdAndSession(String govId, int sessionId);

    // Ap dung khoan tru diem
    boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword);

    // Dieu chinh so lan tru diem
    boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta);

    // Chot diem va trang thai phan thi
    boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword);

    // Lay khoan tru diem da ap dung
    java.util.List<java.util.Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId);

    // Danh dau dinh chi thi
    boolean markSuspended(int candidateId);

    // Huy dinh chi thi
    boolean undoSuspension(int candidateId);

    // Dong bo trang thai phan thi theo ca
    void syncSectionStatusesForSession(int sessionId);

    // Danh dau da in chu ky
    boolean markSignaturePrinted(int candidateId, int sessionId);

    // Hoan tat phan thi / thu tuc
    boolean completeSection(int candidateId, int sessionId);
}
