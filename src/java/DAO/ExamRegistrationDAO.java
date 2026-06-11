package DAO;

import Models.DashboardActivity;
import Models.MyExamDetailView;
import Models.MyExamRowView;
import java.util.List;
import java.util.Optional;

/**
 * DAO đăng ký thi — <b>nguồn duy nhất</b> cho lịch thi (ngày, ca, hạng, phòng, trạng thái, điểm…).
 * <p>Chỉ SBD (số báo danh) lấy từ {@link DAO.CandidateDAO} — không đọc {@code ExamRegistration.candidateNo}.</p>
 * <p>Thanh toán: {@link DAO.PaymentDAO}.</p>
 */
public interface ExamRegistrationDAO {

    int countByPersonId(int personId);

    int countResultsByPersonId(int personId);

    int countDocumentsByPersonId(int personId);

    /** Hoạt động đăng ký + kết quả (không gồm thanh toán). */
    List<DashboardActivity> findRecentRegistrationActivitiesByPersonId(int personId, int limit);

    String findLatestLicenseCodeByPersonId(int personId);

    /** Đã đăng ký còn hiệu lực: cùng person + session và chưa hủy. */
    boolean existsActiveByPersonAndSession(int personId, int examSessionId);

    /** Đăng ký thi — SBD lấy từ bảng {@code Candidate} sau khi staff import Công an. */
    int insertRegistration(int examSessionId, int personId);

    boolean deleteById(int registrationId);

    /** Gọi từ actor thanh toán khi xác nhận lệ phí. */
    boolean markPaymentCompleted(int registrationId);

    /** Hủy đăng ký chưa thanh toán (actor thanh toán / hết hạn). */
    boolean markCancelled(int registrationId);

    List<MyExamRowView> findExamRowsByPersonId(int personId);

    Optional<MyExamDetailView> findExamDetailByRegistrationId(int personId, int registrationId);

    int countPassedRegistrationsByPersonId(int personId);
}
