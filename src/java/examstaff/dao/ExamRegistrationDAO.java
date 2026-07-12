package examstaff.dao;

import examstaff.dto.exam.ExamRegistrationDTO;
import java.util.List;

public interface ExamRegistrationDAO {

    // Lay dang ky theo id
    ExamRegistrationDTO getById(int id);

    // Lay thi sinh theo ExamId va SBD
    ExamRegistrationDTO getByExamAndSbd(int examId, String sbd);

    // Danh sach thi sinh theo ky thi
    List<ExamRegistrationDTO> getCandidatesByExam(int examId);

    // Cap nhat co mat / vang
    boolean updatePresent(int id, boolean isPresent);

    // Cap nhat trang thai thanh toan
    boolean updatePayment(int id, boolean isPaymentCompleted);

    // Cap nhat phong da phan bo (ly thuyet)
    boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    // Cap nhat san/phong thuc hanh
    boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName);

    /**
     * Kiem tra thi sinh da co phong o ca khac trong cung ky thi chua.
     * @return thong bao loi, hoac null neu duoc phep phan / doi phong tai ca nay
     */
    String validateUniqueTheoryAllocation(int candidateId, int examId);

    // Cap nhat diem ly thuyet va thuc hanh
    boolean updateScores(int id, Integer theoryScore, String theoryPassed, Integer practicalScore, String practicalPassed);

    // Cap nhat ho so co ban
    boolean updateProfile(int id, String fullName, java.sql.Date dob, String govIdNo, String email, String phoneNo);

    // Cap nhat duong dan anh
    boolean updatePhoto(int id, String photoUrl);

    // Xoa giao dich thanh toan da hoan tat
    boolean clearCompletedPayments(int candidateId);

    // Danh dau vang mat
    boolean markAbsent(int candidateId);

    // Huy danh dau vang
    boolean clearAbsentMarking(int candidateId);

    // Danh dau dinh chi thi
    boolean markSuspended(int candidateId);

    // Huy dinh chi thi
    boolean undoSuspension(int candidateId);
}
