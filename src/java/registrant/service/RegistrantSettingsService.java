package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface RegistrantSettingsService {
    void applySettingsView(UserDTO user, HttpServletRequest request);

    /** @return null nếu thành công. */
    String changePassword(UserDTO user, String currentPassword, String newPassword,
            String confirmPassword, HttpSession session);

    /** @return null nếu thành công. */
    String deactivateAccount(UserDTO user, boolean confirmed, HttpSession session);
}
