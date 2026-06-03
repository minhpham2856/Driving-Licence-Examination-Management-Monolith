package Services.Impl;

import DAO.PersonDAO;
import DAO.RoleDAO;
import DAO.UserDAO;
import DAO.Impl.PersonDAOImpl;
import DAO.Impl.RoleDAOImpl;
import DAO.Impl.UserDAOImpl;
import Models.Person;
import Models.Role;
import Models.User;
import Services.AuthService;
import Services.EmailService;
import Utils.UsernameGenerator;
import java.sql.Date;

public class AuthServiceImpl implements AuthService {

    private final PersonDAO personDAO = new PersonDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public String register(String govIdNo, String fullName, String phoneNo, 
            String dateOfBirth,String address, String email, boolean gender) {
        if (personDAO.getByGovIdNo(govIdNo) != null) {
            return "Số căn cước đã được sử dụng.";
        }

        if (userDAO.getByEmail(email) != null || personDAO.getByEmail(email) != null) {
            return "Email đã được sử dụng.";
        }

        if (personDAO.getByPhoneNo(phoneNo) != null) {
            return "Số điện thoại đã được sử dụng.";
        }

        Person person = new Person();
        person.setGovIdNo(govIdNo);
        person.setFullName(fullName);
        person.setDateOfBirth(Date.valueOf(dateOfBirth));
        person.setGender(gender);
        person.setPhoneNo(phoneNo);
        person.setEmail(email);
        person.setAddress(address);
        person.setIsWalkIn(false);
        person.setApprovalStatus("Pending");

        if (!personDAO.insert(person)) {
            return "Không thể lưu thông tin cá nhân. Vui lòng thử lại.";
        }

        // generate username and password
        String username = generateUniqueUsername(fullName);
        String password = UsernameGenerator.randomPassword(10);

        Role role = roleDAO.getByName("Registrant");
        int roleId = 6;
        if (role != null) {
            roleId = role.getId();
        }

        User user = new User();
        user.setPersonId(person.getId());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRoleId(roleId);
        user.setIsActive(true);

        if (!userDAO.insert(user)) {
            return "Không thể đăng ký tài khoản. Vui lòng thử lại.";
        }

        String subject = "[Lái Vui] Thông tin tài khoản đăng ký";
        String content = """
                Xin chào %s,

                Tài khoản của bạn đã được tạo thành công trên hệ thống Lái Vui.
                Tên đăng nhập: %s
                Mật khẩu: %s

                Vui lòng đăng nhập và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(fullName, username, password);

        if (!emailService.sendTextEmail(email, subject, content)) {
            return "Tài khoản đã được tạo nhưng không thể gửi email. Vui lòng liên hệ hỗ trợ.";
        }

        return null;
    }

    private String generateUniqueUsername(String fullName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = UsernameGenerator.generateFromFullName(fullName);
            if (userDAO.getByUsername(username) == null) {
                return username;
            }
        }
        return UsernameGenerator.generateFromFullName(fullName) + System.currentTimeMillis() % 1000;
    }

    @Override
    public User login(String identifier, String password) {
        User user = userDAO.getByIdentifier(identifier);
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
                """.formatted(user.getUsername(), tempPassword);

        if (!emailService.sendTextEmail(email, subject, content)) {
            return "Không thể gửi email khôi phục mật khẩu. Vui lòng thử lại.";
        }

        return null;
    }
}
