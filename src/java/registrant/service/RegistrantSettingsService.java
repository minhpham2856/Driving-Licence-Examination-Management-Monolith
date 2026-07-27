package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Hợp đồng service trang cài đặt tài khoản (SettingsServlet). Hiển thị tóm tắt
 * hồ sơ và đổi mật khẩu trên bảng User. Không cho thí sinh tự vô hiệu hóa tài
 * khoản — quyền thuộc Ban quản lý. Không xử lý thanh toán hay SePay.
 */
public interface RegistrantSettingsService {

    /**
     * Gắn thông tin tài khoản/tóm tắt hồ sơ lên request cho trang settings.
     */
    void applySettingsView(UserDTO user, HttpServletRequest request);

    /**
     * Trả về null nếu thành công.
     */
    String changePassword(UserDTO user, String currentPassword, String newPassword,
            String confirmPassword, HttpSession session);
}
