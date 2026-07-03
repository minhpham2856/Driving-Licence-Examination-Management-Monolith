package dao;

import dto.registrant.RegistrantDashboardActivity;
import dto.registrant.RegistrantExamSessionOption;
import dto.registrant.RegistrantLicenceOption;
import dto.registrant.RegistrantMyExamRow;
import dto.registrant.RegistrantRegisteredExamRow;
import dto.registrant.RegistrantTrackingLog;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tập trung các truy vấn phức tạp phục vụ cổng thí sinh (Registrant portal).
 * Tách riêng để không làm phình các dao dùng chung với staff/examiner.
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

    /** Trạng thái hồ sơ gốc (4 giấy bắt buộc) — bỏ qua dòng {@code #SUPPLEMENT_DOC#}. */
    String findProfileDocumentRegistrationStatus(int profileId);

    /** Có request hồ sơ bổ sung đang {@code Pending} trên ExamRegistration. */
    boolean hasOpenSupplementPending(int profileId);

    /** {@code ExamRegistrationId} của request bổ sung đang chờ duyệt; {@code null} nếu không có. */
    Integer findPendingSupplementExamRegistrationId(int profileId);

    /** Tạo dòng ExamRegistration mới cho workflow hồ sơ bổ sung. */
    int insertSupplementDocumentRegistration(int profileId, int licenceId, String status, String notes);

    /** Cập nhật trạng thái một request bổ sung theo {@code ExamRegistrationId}. */
    boolean syncSupplementDocumentRegistration(int examRegistrationId, String status, String notes);

    /** Trạng thái các dòng {@code #SUPPLEMENT_DOC#} theo {@code ExamRegistrationId}. */
    Map<Integer, String> mapSupplementRegistrationStatuses(int profileId);

    /** Cập nhật hoặc tạo bản ghi ExamRegistration cho workflow hồ sơ gốc — không ghi đè ca thi / bổ sung. */
    boolean syncProfileDocumentRegistration(int profileId, String status, String notes);

    /** Số ngày còn lại tới ngày thi (>= 0). Trả về null nếu không có kỳ thi sắp tới. */
    Integer daysUntil(Date examDate);
}
