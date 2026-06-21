package Services.Impl;

import Utils.ExamConstants;
import DAOs.ProfileDAO;
import DAOs.UserDAO;
import DAOs.Impl.ProfileDAOImpl;
import DAOs.Impl.UserDAOImpl;
import Models.Profile;
import DTOs.RegisterResultDTO;
import Models.User;
import Services.AuthService;
import Services.EmailService;
import Utils.UsernameGenerator;

import java.sql.Date;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public RegisterResultDTO register(String govIdNo, String fullName, String phoneNo,
            String dateOfBirth, String address, String email, boolean gender) {
        // input validation
        if (userDAO.getByEmail(email) != null) {
            return RegisterResultDTO.failed("Email đã được sử dụng.");
        }

        if (profileDAO.getByGovIdNo(govIdNo) != null) {
            return RegisterResultDTO.failed("Số căn cước đã được sử dụng.");
        }

        if (profileDAO.getByPhoneNo(phoneNo) != null) {
            return RegisterResultDTO.failed("Số điện thoại đã được sử dụng.");
        }

        // generate auth credentials
        String username = generateUniqueUsername(fullName);
        String password = UsernameGenerator.randomPassword(10);

        // create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setIsActive(true);
        user.setRole(ExamConstants.roleFromName("Registrant"));

        if (!userDAO.insert(user)) {
            return RegisterResultDTO.failed("Không thể đăng ký tài khoản. Vui lòng thử lại.");
        }

        // create new profile
        Profile profile = new Profile();
        profile.setUserId(user.getId());
        profile.setGovIdNo(govIdNo);
        profile.setFullName(fullName);
        profile.setDateOfBirth(new java.sql.Timestamp(Date.valueOf(dateOfBirth).getTime()));
        profile.setGender(gender);
        profile.setPhoneNo(phoneNo);
        profile.setAddress(address);

        if (!profileDAO.insert(profile)) {
            return RegisterResultDTO.failed("Lỗi hệ thống. Vui lòng thử lại.");
        }

        // connect user to profile
        user.setProfileId(profile.getId());
        user.setProfile(profile);

        String subject = "[Lái Vui] Thông tin tài khoản";
        String content = """
                Xin chào %s,

                Tài khoản của bạn đã được tạo thành công trên hệ thống trung tâm Lái Vui.
                Tên đăng nhập: %s
                Mật khẩu: %s

                Vui lòng đăng nhập và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(fullName, username, password);

        boolean emailSent = emailService.sendTextEmail(email, subject, content);
        return RegisterResultDTO.succeeded(username, password, emailSent);
    }

    @Override
    public User login(String identifier, String password) {
        // check for empty inputs
        if (identifier == null || password == null) {
            return null;
        }

        String trimmedId = identifier.trim();
        String trimmedPassword = password.trim();
        if (trimmedId.isEmpty() || trimmedPassword.isEmpty()) {
            return null;
        }

        User user = userDAO.getByIdentifier(trimmedId);
        if (user == null || !user.isIsActive()) {
            return null;
        }

        return passwordsMatch(trimmedPassword, user.getPasswordHash()) ? user : null;
    }

    @Override
    public String forgotPassword(String email) {
        // check for blank inputs
        if (email == null || email.trim().isEmpty()) {
            return "Không tìm thấy tài khoản";
        }

        // check if email exist
        String trimmed = email.trim();
        User user = userDAO.getByEmail(trimmed);
        if (user == null) {
            user = userDAO.getByIdentifier(trimmed);
        }
        if (user == null || !user.isIsActive()) {
            return "Hãy kiểm tra hòm thư của bạn nếu email bạn nhập là đúng";
        }

        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));
        if (!userDAO.updatePassword(user.getId(), tempPassword)) {
            return "Lỗi hệ thống. Vui lòng thử lại.";
        }

        // validate email again
        String recipient = user.getEmail();
        if (recipient == null || recipient.isBlank()) {
            return "Lỗi hệ thống. Vui lòng thử lại.";
        }

        String subject = "[Lái Vui] Khôi phục mật khẩu tài khoản";
        String content = """
                Xin chào %s,

                Mật khẩu của bạn đã được khôi phục thành công.
                Mật khẩu tạm thời mới là: %s

                Vui lòng đăng nhập lại và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(displayName(user), tempPassword);

        if (!emailService.sendTextEmail(recipient, subject, content)) {
            return "Lỗi hệ thống. Vui lòng thử lại.";
        }

        return null;
    }

    // compare password hashes
    protected static boolean passwordsMatch(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        return rawPassword.equals(storedPasswordHash.trim());
    }

    // get username
    private static String displayName(User user) {
        if (user.getProfile() != null && user.getProfile().getFullName() != null
                && !user.getProfile().getFullName().isBlank()) {
            return user.getProfile().getFullName();
        }
        return user.getUsername();
    }

    // generate username
    private String generateUniqueUsername(String fullName) {
        // case 1: username generation sucess
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = UsernameGenerator.generateFromFullName(fullName);
            if (userDAO.getByUsername(username) == null) {
                return username;
            }
        }
        // case 2: username generation failed
        return UsernameGenerator.generateFromFullName(fullName) + System.currentTimeMillis() % 1000;
    }
}
