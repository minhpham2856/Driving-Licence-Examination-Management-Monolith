package Services.Impl;

import DAO.PersonDAO;
import DAO.UserDAO;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.Person;
import Models.User;
import Services.RegistrantSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;

/** Đổi mật khẩu (UserDAO.updatePassword) và vô hiệu hóa tài khoản (updateActive). */
public class RegistrantSettingsServiceImpl implements RegistrantSettingsService {

    private final UserDAO userDAO = new UserDAOImpl();
    private final PersonDAO personDAO = new PersonDAOImpl();

    /** Nạp username, email, Person (nếu có), lastLogin và trạng thái isActive. */
    @Override
    public void populateSettings(HttpServletRequest request, User user) {
        request.setAttribute("settingsUsername", user.getUsername());
        request.setAttribute("settingsEmail", user.getEmail() != null ? user.getEmail() : "");

        if (user.getPersonId() != null) {
            Person person = personDAO.getById(user.getPersonId());
            if (person != null) {
                request.setAttribute("settingsFullName", person.getFullName());
                request.setAttribute("settingsGovId", person.getGovIdNo());
                request.setAttribute("accountLinked", person.getGovIdNo() != null && !person.getGovIdNo().isBlank());
            }
        }

        if (user.getLastLoginAt() != null) {
            request.setAttribute("settingsLastLogin",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(user.getLastLoginAt()));
        } else {
            request.setAttribute("settingsLastLogin", "—");
        }

        request.setAttribute("settingsAccountActive", user.isIsActive());
    }

    /** Validate mật khẩu cũ/mới; gọi UserDAO.updatePassword. @return null nếu OK. */
    @Override
    public String changePassword(HttpServletRequest request, User user) {
        String currentPassword = trim(request.getParameter("currentPassword"));
        String newPassword = trim(request.getParameter("newPassword"));
        String confirmPassword = trim(request.getParameter("confirmPassword"));

        if (currentPassword == null || currentPassword.isEmpty()) {
            return "Vui lòng nhập mật khẩu hiện tại.";
        }
        if (newPassword == null || newPassword.length() < 8) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }

        User stored = userDAO.getById(user.getId());
        if (stored == null) {
            return "Không tìm thấy tài khoản.";
        }

        if (!currentPassword.equals(stored.getPasswordHash())) {
            return "Mật khẩu hiện tại không đúng.";
        }

        if (!userDAO.updatePassword(user.getId(), newPassword)) {
            return "Không thể cập nhật mật khẩu. Vui lòng thử lại.";
        }

        return null;
    }

    /** Yêu cầu checkbox xác nhận; gọi UserDAO.updateActive(false). @return null nếu OK. */
    @Override
    public String deactivateAccount(HttpServletRequest request, User user) {
        String confirm = trim(request.getParameter("confirmDeactivate"));
        if (!"on".equalsIgnoreCase(confirm)) {
            return "Vui lòng xác nhận vô hiệu hóa tài khoản.";
        }

        if (!userDAO.updateActive(user.getId(), false)) {
            return "Không thể vô hiệu hóa tài khoản. Vui lòng thử lại.";
        }

        return null;
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
