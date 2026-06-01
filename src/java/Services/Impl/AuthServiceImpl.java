package Services.Impl;

import DAO.PersonDAO;
import DAO.RoleDAO;
import DAO.UserDAO;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.RoleDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.Role;
import Models.User;
import Services.AuthService;
import Services.EmailService;

public class AuthServiceImpl implements AuthService {

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public String register(String username, String email, String password) {
        if (userDAO.getByUsername(username) != null) {
            return "Tên đăng nhập đã tồn tại.";
        }

        if (userDAO.getByEmail(email) != null || personDAO.getByEmail(email) != null) {
            return "Email đã được sử dụng.";
        }

        Role role = roleDAO.getByName("Registrant");
        int roleId = 6; // roleId = 6 -> registrant
        if (role != null) {
            roleId = role.getId();
        }

        User user = new User();
        user.setPersonId(null);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRoleId(roleId);
        user.setIsActive(true);

        if (!userDAO.insert(user)) {
            return "Không thể đăng ký tài khoản. Vui lòng thử lại.";
        }

        return null;
    }

    @Override
    public User login(String identifier, String password) {
        User user = userDAO.getByIdentifier(identifier); // identifier = username, email, phone number
        if (user == null || !user.isIsActive()) {
            return null;
        }

        return password.equals(user.getPasswordHash()) ? user : null;
    }

    @Override
    public String forgotPassword(String email) {
        User user = userDAO.getByEmail(email);
        if (user == null) {
            user = userDAO.getByIdentifier(email);
        }
        if (user == null) {
            return "Không tìm thấy tài khoản";
        }

        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));

        if (!userDAO.updatePassword(user.getId(), tempPassword)) {
            return "Không thể cập nhật mật khẩu khôi phục. Vui lòng thử lại.";
        }

        String subject = "[Lái Vui] Khôi phục mật khẩu tài khoản";
        String content = """
                Xin chào %s,

                Mật khẩu của bạn đã được khôi phục thành công.
                Mật khẩu tạm thời mới là: %s

                Vui lòng đăng nhập lại và đổi mật khẩu trong phần cài đặt tài khoản.

                Trân trọng,
                Trung tâm Lái Vui
                """.formatted(user.getUsername(), tempPassword);

        if (!emailService.sendTextEmail(email, subject, content)) {
            return "Không thể gửi email khôi phục mật khẩu. Vui lòng thử lại.";
        }

        return null;
    }
}
