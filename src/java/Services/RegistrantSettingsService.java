package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface RegistrantSettingsService {
    void applySettingsView(User user, HttpServletRequest request);

    /** @return null nếu thành công. */
    String saveNotificationPrefs(HttpServletRequest request, boolean notifyExamResults,
            boolean notifyPasswordChange);

    /** @return null nếu thành công. */
    String changePassword(User user, String currentPassword, String newPassword,
            String confirmPassword, HttpSession session);

    /** @return null nếu thành công. */
    String deactivateAccount(User user, boolean confirmed, HttpSession session);
}
