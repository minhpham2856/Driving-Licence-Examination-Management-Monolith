package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Hợp đồng service trang theo dõi hồ sơ ({@code TrackProfileServlet}).
 * <p>
 * Gom nhật ký {@code Audit}, tài liệu {@code Document} và đăng ký thi thành timeline
 * 5 bước + bảng lọc nhật ký cho {@code track-profile.jsp}.
 */
public interface RegistrantTrackProfileService {
    /** Gom audit + tài liệu thành timeline theo dõi hồ sơ và đẩy lên request. */
    void copyTrackingToRequest(UserDTO user, HttpServletRequest request);
}
