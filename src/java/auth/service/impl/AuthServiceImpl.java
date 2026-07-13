package auth.service.impl;

import auth.dao.ProfileDAO;
import auth.dao.RoleDAO;
import auth.dao.UserDAO;
import auth.dao.impl.ProfileDAOImpl;
import auth.dao.impl.RoleDAOImpl;
import auth.dao.impl.UserDAOImpl;
import auth.dto.RegisterResultDTO;
import auth.model.Profile;
import auth.model.Role;
import auth.model.User;
import auth.service.AuthService;
import auth.service.EmailService;
import auth.util.UsernameGenerator;
import dto.ServiceResult;
import enums.ErrorType;
import enums.RoleType;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public ServiceResult<RegisterResultDTO> register(Profile profile, String email) {
        if (isEmailTaken(email)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email đã được sử dụng.");
        }
        if (isGovIdTaken(profile.getGovernmentIdNumber())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số căn cước đã được sử dụng.");
        }
        if (isPhoneTaken(profile.getPhoneNumber())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại đã được sử dụng.");
        }

        String username = generateUniqueUsername(profile.getFullName());
        String password = UsernameGenerator.randomPassword(10);
        
        User user = buildUserForRegistration(username, email, password);
        if (!userDAO.insert(user)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Không thể đăng ký tài khoản. Vui lòng thử lại.");
        }

        profile.setUserId(user.getUserId());
        if (!profileDAO.insert(profile)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lỗi hệ thống. Vui lòng thử lại.");
        }

        boolean emailSent = sendRegistrationEmail(profile.getFullName(), email, username, password);
        
        RegisterResultDTO data = new RegisterResultDTO();
        data.setUsername(username);
        data.setPassword(password);
        data.setEmailSent(emailSent);
        data.setUserId(user.getUserId());
        
        return ServiceResult.ok(data);
    }

    @Override
    public User login(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }
        
        String trimmedId = identifier.trim();
        String trimmedPassword = password.trim();
        
        if (trimmedId.isEmpty() || trimmedPassword.isEmpty()) {
            return null;
        }
        
        User user = userDAO.getByIdentifier(trimmedId);
        if (user == null || !user.isActive()) {
            return null;
        }
        
        if (passwordsMatch(trimmedPassword, user.getPasswordHash())) {
            Role role = roleDAO.getById(user.getRoleId());
            user.setRole(role);
            return user;
        }
        
        return null;
    }

    @Override
    public ServiceResult<Void> forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Không tìm thấy tài khoản");
        }
        
        String trimmed = email.trim();
        User user = findUserByEmailOrIdentifier(trimmed);
        
        if (user == null || !user.isActive()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Hãy kiểm tra hòm thư của bạn nếu email bạn nhập là đúng");
        }
        
        String tempPassword = generateTempPassword();
        if (!userDAO.updatePassword(user.getUserId(), tempPassword)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lỗi hệ thống. Vui lòng thử lại.");
        }
        
        if (!sendForgotPasswordEmail(user.getEmail(), user.getUsername(), tempPassword)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lỗi hệ thống. Vui lòng thử lại.");
        }
        
        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword, String confirmPassword) {
        User fresh = userDAO.getById(userId);
        if (fresh == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Có lỗi xảy ra, vui lòng thử lại.");
        }
        
        if (!passwordsMatch(currentPassword, fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Mật khẩu hiện tại không chính xác.");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Mật khẩu mới và xác nhận không khớp.");
        }
        
        if (newPassword.equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Mật khẩu mới không được trùng mật khẩu cũ.");
        }
        
        if (userDAO.updatePassword(fresh.getUserId(), newPassword)) {
            return ServiceResult.ok(null, "Đổi mật khẩu thành công.");
        }
        
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Có lỗi xảy ra, vui lòng thử lại.");
    }

    private boolean isEmailTaken(String email) {
        return userDAO.getByEmail(email) != null;
    }

    private boolean isGovIdTaken(String govId) {
        return profileDAO.getByGovIdNo(govId) != null;
    }

    private boolean isPhoneTaken(String phone) {
        return profileDAO.getByPhoneNo(phone) != null;
    }

    private User buildUserForRegistration(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setActive(true);
        Role role = roleDAO.getByName(RoleType.REGISTRANT.getValue());
        user.setRoleId(role != null ? role.getRoleId() : 0);
        return user;
    }

    private boolean sendRegistrationEmail(String fullName, String email, String username, String password) {
        String subject = "[Lái Vui] Thông tin tài khoản";
        String content = """
                Xin chào %s,
                Tài khoản của bạn đã được tạo thành công trên hệ thống trung tâm Lái Vui.
                Tên đăng nhập: %s
                Mật khẩu: %s
                Vui lòng đăng nhập và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(fullName, username, password);
        return emailService.sendTextEmail(email, subject, content);
    }

    private boolean sendForgotPasswordEmail(String recipientEmail, String username, String tempPassword) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return false;
        }
        String subject = "[Lái Vui] Khôi phục mật khẩu tài khoản";
        String content = """
                Xin chào %s,
                Mật khẩu của bạn đã được khôi phục thành công.
                Mật khẩu tạm thời mới là: %s
                Vui lòng đăng nhập lại và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(username, tempPassword);
        return emailService.sendTextEmail(recipientEmail, subject, content);
    }

    private User findUserByEmailOrIdentifier(String identifier) {
        User user = userDAO.getByEmail(identifier);
        if (user == null) {
            user = userDAO.getByIdentifier(identifier);
        }
        return user;
    }

    private String generateTempPassword() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    private boolean passwordsMatch(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        return rawPassword.equals(storedPasswordHash.trim());
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
}
