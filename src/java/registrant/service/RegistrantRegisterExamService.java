package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Hợp đồng service wizard đăng ký nguyện vọng ngày thi ({@code RegisterExamServlet}).
 * <p>
 * Chỉ chạy khi hồ sơ + hạng GPLX đã Approved; đọc {@code ExamDates}, lọc ca còn chỗ.
 * POST ghi {@code RegistrationDates} (MERGE) — không tạo Candidate, Payment hay SePay checkout.
 */
public interface RegistrantRegisterExamService {
    /** Nạp wizard đăng ký nguyện vọng ngày thi (hạng, ca, điều kiện đủ hồ sơ). */
    void loadRegisterExamPage(UserDTO user, HttpServletRequest request);

    /** @return null nếu thành công; ngược lại thông báo lỗi tiếng Việt. */
    String submitRegistration(UserDTO user, HttpServletRequest request);

    /** URL GET trang đăng ký, giữ lựa chọn/lọc hiện tại (PRG sau POST). */
    String buildRegisterExamPageUrl(HttpServletRequest request, String fragment);
}
