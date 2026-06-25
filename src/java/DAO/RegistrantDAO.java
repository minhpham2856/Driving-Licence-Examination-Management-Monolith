package DAO;

import Models.RegistrantDashboardActivity;
import Models.RegistrantExamSessionOption;
import Models.RegistrantLicenceOption;
import Models.RegistrantMyExamRow;
import Models.RegistrantRegisteredExamRow;
import Models.RegistrantTrackingLog;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tập trung các truy vấn phức tạp phục vụ cổng thí sinh (Registrant portal).
 * Tách riêng để không làm phình các DAO dùng chung với staff/examiner.
 */
public interface RegistrantDAO {

    List<RegistrantLicenceOption> listOpenLicenceOptions();

    List<RegistrantExamSessionOption> listOpenExamSessionsByLicenceCode(String uiLicenceCode);

    RegistrantExamSessionOption findExamSessionByCode(String examCode);

    List<RegistrantRegisteredExamRow> listRegisteredExamsByUserId(int userId, int limit);

    List<RegistrantRegisteredExamRow> listRegisteredExamsByProfileId(int profileId, int limit);

    /** Đăng ký ca thi còn hiệu lực (loại trừ từ chối / hủy) — hiển thị hồ sơ đa hạng. */
    List<RegistrantRegisteredExamRow> listActiveExamRegistrationsByProfileId(int profileId, int limit);

    Map<String, Object> loadDashboardStats(int userId, int profileId);

    RegistrantRegisteredExamRow findUpcomingExamByUserId(int userId);

    RegistrantRegisteredExamRow findUpcomingExamByProfileId(int profileId);

    List<RegistrantDashboardActivity> listRecentActivities(int profileId, int limit);

    List<RegistrantMyExamRow> listMyExamsByUserId(int userId);

    RegistrantMyExamRow findMyExamByCandidateId(int userId, int candidateId);

    Integer resolveUserIdByCandidateId(int candidateId);

    List<RegistrantTrackingLog> buildProfileTrackingLogs(int profileId, int userId);

    int countExamResultsByUserId(int userId);

    int getNextCandidateSequence(String dbLicenceClass);

    int resolveLicenceIdByUiCode(String uiLicenceCode);

    String resolveLatestLicenceClassByProfileId(int profileId);

    /** Trạng thái hồ sơ tài liệu trên ExamRegistration (Draft/Pending/Approved/Rejected). */
    String findProfileDocumentRegistrationStatus(int profileId);

    /** Cập nhật hoặc tạo bản ghi ExamRegistration cho workflow tài liệu — không ghi đè ca thi. */
    boolean syncProfileDocumentRegistration(int profileId, String status, String notes);

    /** Số ngày còn lại tới ngày thi (>= 0). Trả về null nếu không có kỳ thi sắp tới. */
    Integer daysUntil(Date examDate);
}
