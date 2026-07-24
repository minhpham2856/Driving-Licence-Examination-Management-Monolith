package registrant.dao;

import registrant.dto.RegistrantDashboardActivity;
import registrant.dto.RegistrantExamSessionOption;
import registrant.dto.RegistrantLicenceOption;
import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.dto.RegistrantTrackingLog;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tập trung các truy vấn phức tạp phục vụ cổng thí sinh (Registrant portal).
 * Tách riêng để không làm phình các dao dùng chung với staff/examiner.
 * <p>
 * <b>Cách tìm nhanh:</b> Ctrl+F {@code REGION:} hoặc nhảy theo <b>LOC index</b> bên dưới
 * (số dòng ~ gần đúng — cập nhật khi sửa lớn). Impl cùng thứ tự:
 * {@link registrant.dao.impl.RegistrantDAOImpl}.
 * <p>
 * <b>LOC index (interface):</b>
 * <ul>
 *   <li><b>~L35</b> — Hạng GPLX / resolve LicenceId</li>
 *   <li><b>~L48</b> — Ngày thi dự kiến (ExamDates) &amp; nguyện vọng</li>
 *   <li><b>~L71</b> — Danh sách đăng ký thi</li>
 *   <li><b>~L84</b> — Dashboard (stats / upcoming / activity / daysUntil)</li>
 *   <li><b>~L103</b> — Lịch thi &amp; kết quả (my-exams)</li>
 *   <li><b>~L113</b> — Hồ sơ tài liệu (#PROFILE_DOC# …)</li>
 *   <li><b>~L147</b> — Theo dõi hồ sơ (track-profile)</li>
 * </ul>
 */
public interface RegistrantDAO {

    // =========================================================================
    // REGION: Hạng GPLX / resolve LicenceId  (~L35)
    // =========================================================================

    /** Liệt kê hạng GPLX đang mở đăng ký cho wizard. */
    List<RegistrantLicenceOption> listOpenLicenceOptions();

    /** Đổi mã hạng UI sang LicenceId trong DB. */
    int resolveLicenceIdByUiCode(String uiLicenceCode);

    /** Lấy mã hạng GPLX mới nhất gắn với hồ sơ. */
    String resolveLatestLicenceClassByProfileId(int profileId);

    // =========================================================================
    // REGION: Ngày thi dự kiến (ExamDates) & nguyện vọng (RegistrationDates)  (~L48)
    // =========================================================================

    /**
     * Ngày thi dự kiến (ExamDates) theo hạng — managing staff tạo; thí sinh chọn trên wizard đăng ký.
     * Trả về dạng {@link RegistrantExamSessionOption} để tái dùng UI (id = ExamDateId).
     */
    List<RegistrantExamSessionOption> listOpenExamSessionsByLicenceCode(String uiLicenceCode);

    /** Tìm ngày thi dự kiến theo id (tham số sessionSelect = ExamDateId). */
    RegistrantExamSessionOption findExamSessionByCode(String examDateIdOrCode);

    /**
     * Ghi lựa chọn ngày dự kiến vào RegistrationDates (IsActive=1).
     * Mỗi hồ sơ + hạng chỉ được một nguyện vọng active — không cho đổi/ghi đè ngày đã chọn.
     * @return null nếu OK, ngược lại thông báo lỗi thân thiện.
     */
    String registerPreferredExamDate(int profileId, int examDateId, int licenceId);

    /** True nếu hồ sơ đã có nguyện vọng ngày thi active cho hạng (LicenceId) này. */
    boolean hasActivePreferredExamDate(int profileId, int licenceId);

    // =========================================================================
    // REGION: Danh sách đăng ký thi (dashboard / hồ sơ)  (~L71)
    // =========================================================================

    /** Danh sách đăng ký thi (nguyện vọng + chính thức) theo UserId. */
    List<RegistrantRegisteredExamRow> listRegisteredExamsByUserId(int userId, int limit);

    /** Danh sách đăng ký thi theo ProfileId. */
    List<RegistrantRegisteredExamRow> listRegisteredExamsByProfileId(int profileId, int limit);

    /** Đăng ký ca thi còn hiệu lực (loại trừ từ chối / hủy) - hiển thị hồ sơ đa hạng. */
    List<RegistrantRegisteredExamRow> listActiveExamRegistrationsByProfileId(int profileId, int limit);

    // =========================================================================
    // REGION: Dashboard — stats, upcoming, activity, daysUntil  (~L84)
    // =========================================================================

    /** Thống kê dashboard: số đăng ký, kết quả, trạng thái hồ sơ. */
    Map<String, Object> loadDashboardStats(int userId, int profileId);

    /** Kỳ thi sắp tới gần nhất theo UserId. */
    RegistrantRegisteredExamRow findUpcomingExamByUserId(int userId);

    /** Kỳ thi sắp tới gần nhất theo ProfileId. */
    RegistrantRegisteredExamRow findUpcomingExamByProfileId(int profileId);

    /** Hoạt động gần đây trên hồ sơ để hiển thị dashboard. */
    List<RegistrantDashboardActivity> listRecentActivities(int profileId, int limit);

    /** Số ngày còn lại tới ngày thi (>= 0). Trả về null nếu không có kỳ thi sắp tới. */
    Integer daysUntil(Date examDate);

    // =========================================================================
    // REGION: Lịch thi & kết quả (my-exams)  (~L103)
    // =========================================================================

    /** Danh sách ca của tôi kèm điểm và cờ thanh toán. */
    List<RegistrantMyExamRow> listMyExamsByUserId(int userId);

    /** Chi tiết một enrollment/candidate thuộc user. */
    RegistrantMyExamRow findMyExamByCandidateId(int userId, int candidateId);

    // =========================================================================
    // REGION: Hồ sơ tài liệu — ExamRegistration workflow (#PROFILE_DOC# …)  (~L113)
    // =========================================================================

    /** Trạng thái hồ sơ gốc (4 giấy bắt buộc) - bỏ qua dòng {@code #SUPPLEMENT_DOC#} / {@code #LICENCE_DOC#}. */
    String findProfileDocumentRegistrationStatus(int profileId);

    /**
     * Các mã hạng UI (A1/A/B1/…) đã được ban quản lý duyệt hồ sơ kèm hạng đó
     * (ER Approved: #PROFILE_DOC#, #LICENCE_DOC# hoặc #SUPPLEMENT_DOC#).
     */
    List<String> listApprovedDocumentLicenceCodes(int profileId);

    /** Có request hồ sơ bổ sung / xin duyệt hạng đang {@code Pending} trên ExamRegistration. */
    boolean hasOpenSupplementPending(int profileId);

    /** Tạo dòng ExamRegistration mới cho workflow hồ sơ bổ sung. */
    int insertSupplementDocumentRegistration(int profileId, int licenceId, String status, String notes);

    /** Xin duyệt thêm hạng với hồ sơ đã duyệt (tái sử dụng, không upload lại). */
    int insertLicenceDocumentRegistration(int profileId, int licenceId, String status, String notes);

    /** Map ExamRegistrationId bổ sung → RegistrationStatus. */
    Map<Integer, String> mapSupplementRegistrationStatuses(int profileId);

    /** Cập nhật hoặc tạo bản ghi ExamRegistration cho workflow hồ sơ gốc - không ghi đè ca thi / bổ sung. */
    boolean syncProfileDocumentRegistration(int profileId, String status, String notes);

    /**
     * Như {@link #syncProfileDocumentRegistration(int, String, String)} và gán {@code LicenceId}
     * hạng thí sinh gửi duyệt (để managing staff biết hạng đang xét).
     */
    boolean syncProfileDocumentRegistration(int profileId, String status, String notes, int licenceId);

    // =========================================================================
    // REGION: Theo dõi hồ sơ (track-profile)  (~L147)
    // =========================================================================

    /** Tổng hợp log theo dõi hồ sơ từ audit/đăng ký. */
    List<RegistrantTrackingLog> buildProfileTrackingLogs(int profileId, int userId);
}
