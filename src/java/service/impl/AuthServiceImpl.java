package service.impl;

import enums.Db2Mappings;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import model.user.Profile;
import dto.registration.RegisterResult;
import model.user.User;
import service.AuthService;
import service.EmailService;
import util.UsernameGenerator;

import java.sql.Date;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final UserDAO userdao = new UserDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public RegisterResult register(String govIdNo, String fullName, String phoneNo,
            String dateOfBirth, String address, String email, boolean gender) {
        if (userdao.getByEmail(email) != null) {
            return RegisterResult.failed("Email đã được sử dụng.");
        }

        if (profiledao.getByGovIdNo(govIdNo) != null) {
            return RegisterResult.failed("Số căn cước đã được sử dụng.");
        }

        if (profiledao.getByPhoneNo(phoneNo) != null) {
            return RegisterResult.failed("Số điện thoại đã được sử dụng.");
        }

        String username = generateUniqueUsername(fullName);
        String password = UsernameGenerator.randomPassword(10);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setIsActive(true);
        user.setRole(Db2Mappings.roleFromName("Registrant"));

        if (!userdao.insert(user)) {
            if (userdao.getByEmail(email) != null) {
                return RegisterResult.failed("Email đã được sử dụng.");
            }
            if (userdao.getByUsername(user.getUsername()) != null) {
                return RegisterResult.failed("Không thể tạo tên đăng nhập. Vui lòng thử lại.");
            }
            return RegisterResult.failed("Không thể đăng ký tài khoản. Vui lòng thử lại.");
        }

        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setGovIdNo(govIdNo);
        profile.setFullName(fullName);
        profile.setDateOfBirth(Date.valueOf(dateOfBirth));
        profile.setGender(gender);
        profile.setPhoneNo(phoneNo);
        profile.setAddress(address);

        if (!profiledao.insert(profile)) {
            return RegisterResult.failed("Không thể lưu hồ sơ cá nhân. Vui lòng thử lại.");
        }

        user.setProfileId(profile.getId());
        user.setProfile(profile);

        String subject = "[Lái Vui] Thông tin tài khoản đăng ký";
        String content = """
                Xin chào %s,

                Tài khoản của bạn đã được tạo thành công trên hệ thống Lái Vui.
                Tên đăng nhập: %s
                Mật khẩu: %s

                Vui lòng đăng nhập và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(fullName, username, password);

        boolean emailSent = emailService.sendTextEmail(email, subject, content);
        return RegisterResult.succeeded(username, password, emailSent);
    }

    private String generateUniqueUsername(String fullName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = UsernameGenerator.generateFromFullName(fullName);
            if (userdao.getByUsername(username) == null) {
                return username;
            }
        }
        return UsernameGenerator.generateFromFullName(fullName) + System.currentTimeMillis() % 1000;
    }

    @Override
    public User login(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }

        String normalizedId = identifier.trim();
        String normalizedPassword = password.trim();
        if (normalizedId.isEmpty() || normalizedPassword.isEmpty()) {
            return null;
        }

        User user = userdao.getByIdentifier(normalizedId);
        if (user == null || !user.isIsActive()) {
            return null;
        }

        return passwordsMatch(normalizedPassword, user.getPasswordHash()) ? user : null;
    }

    @Override
    public String forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Không tìm thấy tài khoản";
        }

        String normalized = email.trim();
        User user = userdao.getByEmail(normalized);
        if (user == null) {
            user = userdao.getByIdentifier(normalized);
        }
        if (user == null || !user.isIsActive()) {
            return "Không tìm thấy tài khoản";
        }

        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));
        if (!userdao.updatePassword(user.getId(), tempPassword)) {
            return "Không thể cập nhật mật khẩu khôi phục. Vui lòng thử lại.";
        }

        String recipient = user.getEmail();
        if (recipient == null || recipient.isBlank()) {
            return "Tài khoản không có email để gửi mật khẩu mới.";
        }

        String subject = "[Lái Vui] Khôi phục mật khẩu tài khoản";
        String content = """
                Xin chào %s,

                Mật khẩu của bạn đã được khôi phục thành công.
                Mật khẩu tạm thời mới là: %s

                Vui lòng đăng nhập lại và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(displayName(user), tempPassword);

        if (!emailService.sendTextEmail(recipient, subject, content)) {
            return "Không thể gửi email khôi phục mật khẩu. Vui lòng thử lại.";
        }

        return null;
    }

    static boolean passwordsMatch(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        return rawPassword.equals(storedPasswordHash.trim());
    }

    private static String displayName(User user) {
        if (user.getProfile() != null && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank()) {
            return user.getProfile().getFullName();
        }
        return user.getUsername();
    }
}
