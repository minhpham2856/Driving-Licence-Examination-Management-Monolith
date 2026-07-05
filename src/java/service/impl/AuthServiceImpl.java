package service.impl;

import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import dto.ServiceResult;
import dto.payload.ChangePasswordCommand;
import dto.payload.RegisterData;
import enums.ErrorType;
import model.Profile;
import model.User;
import service.AuthService;
import service.EmailService;
import service.RoleService;
import util.UsernameGenerator;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public ServiceResult<RegisterData> register(Profile profile, String email) {
        if (userDAO.getByEmail(email) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email đã được sử dụng.");
        }
        if (profileDAO.getByGovIdNo(profile.getGovernmentIdNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số căn cước đã được sử dụng.");
        }
        if (profileDAO.getByPhoneNo(profile.getPhoneNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Số điện thoại đã được sử dụng.");
        }
        String username = generateUniqueUsername(profile.getFullName());
        String password = UsernameGenerator.randomPassword(10);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setActive(true);
        RoleService roleService = new RoleServiceImpl();
        user.setRoleId(roleService.getRoleIdByName(enums.UserRole.REGISTRANT.getValue()));
        if (!userDAO.insert(user)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không thể đăng ký tài khoản. Vui lòng thử lại.");
        }
        profile.setUserId(user.getUserId());
        if (!profileDAO.insert(profile)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lỗi hệ thống. Vui lòng thử lại.");
        }
        String subject = "[Lái Vui] Thông tin tài khoản";
        String content = """
                Xin chào %s,
                Tài khoản của bạn đã được tạo thành công trên hệ thống trung tâm Lái Vui.
                Tên đăng nhập: %s
                Mật khẩu: %s
                Vui lòng đăng nhập và đổi mật khẩu trong phần cài đặt tài khoản.
                """.formatted(profile.getFullName(), username, password);
        boolean emailSent = emailService.sendTextEmail(email, subject, content);
        RegisterData data = new RegisterData(username, password, emailSent, user.getUserId());
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
        return passwordsMatch(trimmedPassword, user.getPasswordHash()) ? user : null;
    }

    @Override
    public String forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return "Không tìm thấy tài khoản";
        }
        String trimmed = email.trim();
        User user = userDAO.getByEmail(trimmed);
        if (user == null) {
            user = userDAO.getByIdentifier(trimmed);
        }
        if (user == null || !user.isActive()) {
            return "Hãy kiểm tra hòm thư của bạn nếu email bạn nhập là đúng";
        }
        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));
        if (!userDAO.updatePassword(user.getUserId(), tempPassword)) {
            return "Lỗi hệ thống. Vui lòng thử lại.";
        }
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
                """.formatted(user.getUsername(), tempPassword);
        if (!emailService.sendTextEmail(recipient, subject, content)) {
            return "Lỗi hệ thống. Vui lòng thử lại.";
        }
        return null;
    }

    protected static boolean passwordsMatch(String rawPassword, String storedPasswordHash) {
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

    @Override
    public ServiceResult<Void> changePassword(ChangePasswordCommand command) {
        User fresh = userDAO.getById(command.getUserId());
        if (fresh == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "Có lỗi xảy ra, vui lòng thử lại.");
        }
        if (command.getCurrentPassword() == null
                || !command.getCurrentPassword().equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Mật khẩu hiện tại không chính xác.");
        }
        if (command.getNewPassword() == null || command.getNewPassword().length() < 6) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Mật khẩu mới và xác nhận không khớp.");
        }
        if (command.getNewPassword().equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Mật khẩu mới không được trùng mật khẩu cũ.");
        }
        if (userDAO.updatePassword(fresh.getUserId(), command.getNewPassword())) {
            return ServiceResult.ok(null, "Đổi mật khẩu thành công.");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Có lỗi xảy ra, vui lòng thử lại.");
    }
}
