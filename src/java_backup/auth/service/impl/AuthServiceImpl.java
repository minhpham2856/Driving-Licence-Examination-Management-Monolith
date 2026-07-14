package auth.service.impl;

import auth.dao.ProfileDAO;
import auth.dao.RoleDAO;
import auth.dao.UserDAO;
import auth.dao.impl.ProfileDAOImpl;
import auth.dao.impl.RoleDAOImpl;
import auth.dao.impl.UserDAOImpl;
import auth.dto.RegisterResultDTO;
import shared.model.Profile;
import shared.model.Role;
import shared.model.User;
import auth.service.AuthService;
import auth.service.EmailService;
import auth.util.PasswordUtil;
import auth.util.CredentialsGenerator;
import auth.dto.ServiceResult;
import shared.enums.ErrorType;
import shared.enums.RoleType;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final RoleDAO roleDAO = new RoleDAOImpl();

    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public ServiceResult<RegisterResultDTO> register(Profile profile, String email) {

        // validate email uniqueness
        if (userDAO.getByEmail(email) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }

        // validate government id uniqueness
        if (profileDAO.getByGovIdNo(profile.getGovernmentIdNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ cÄƒn cÆ°á»›c Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }

        // validate phone uniqueness
        if (profileDAO.getByPhoneNo(profile.getPhoneNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }

        // generate account credentials
        String username = generateUniqueUsername(profile.getFullName());
        String password = CredentialsGenerator.randomPassword(10);

        // create user account
        User user = createUser(username, email, password);

        // save user
        if (!userDAO.insert(user)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "KhÃ´ng thá»ƒ Ä‘Äƒng kÃ½ tÃ i khoáº£n. Vui lÃ²ng thá»­ láº¡i.");
        }

        // link profile to user
        profile.setUserId(user.getUserId());

        // save profile
        if (!profileDAO.insert(profile)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.");
        }

        // send account email
        boolean emailSent = sendRegistrationEmail(
                profile.getFullName(),
                email,
                username,
                password);

        // build response
        RegisterResultDTO data = new RegisterResultDTO();
        data.setUsername(username);
        data.setPassword(password);
        data.setEmailSent(emailSent);
        data.setUserId(user.getUserId());

        return ServiceResult.ok(data);
    }

    @Override
    public User login(String identifier, String password) {

        // validate inputs
        if (identifier == null || password == null) {
            return null;
        }

        // normalise inputs
        String trimmedId = identifier.trim();
        String trimmedPassword = password.trim();

        // validate blank inputs
        if (trimmedId.isEmpty() || trimmedPassword.isEmpty()) {
            return null;
        }

        // find user
        User user = userDAO.getByIdentifier(trimmedId);

        // validate account
        if (user == null || !user.isActive()) {
            return null;
        }

        // validate password
        if (passwordsMatch(trimmedPassword, user.getPasswordHash())) {
            Role role = roleDAO.getById(user.getRoleId());
            user.setRole(role);
            return user;
        }

        return null;
    }

    @Override
    public ServiceResult<Void> forgotPassword(String email) {

        // validate email
        if (email == null || email.isBlank()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n");
        }

        // find user
        User user = findUserByEmailOrIdentifier(email.trim());

        // validate account
        if (user == null || !user.isActive()) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "HÃ£y kiá»ƒm tra hÃ²m thÆ° cá»§a báº¡n náº¿u email báº¡n nháº­p lÃ  Ä‘Ãºng");
        }

        // generate temporary password
        String tempPassword = generateTempPassword();

        // update password
        if (!userDAO.updatePassword(user.getUserId(), tempPassword)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.");
        }

        // send recovery email
        if (!sendForgotPasswordEmail(user.getEmail(), user.getUsername(), tempPassword)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.");
        }

        return ServiceResult.ok(null);
    }

    @Override
    public ServiceResult<Void> changePassword(int userId,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        // get latest user data
        User fresh = userDAO.getById(userId);

        // validate user
        if (fresh == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND,
                    "CÃ³ lá»—i xáº£y ra, vui lÃ²ng thá»­ láº¡i.");
        }

        // validate current password
        if (!passwordsMatch(currentPassword, fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u hiá»‡n táº¡i khÃ´ng chÃ­nh xÃ¡c.");
        }

        // validate new password length
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±.");
        }

        // validate confirmation
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i vÃ  xÃ¡c nháº­n khÃ´ng khá»›p.");
        }

        // validate password difference
        if (newPassword.equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c trÃ¹ng máº­t kháº©u cÅ©.");
        }

        // update password
        if (userDAO.updatePassword(fresh.getUserId(), newPassword)) {
            return ServiceResult.ok(null, "Äá»•i máº­t kháº©u thÃ nh cÃ´ng.");
        }

        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                "CÃ³ lá»—i xáº£y ra, vui lÃ²ng thá»­ láº¡i.");
    }

    // create a new user for registration
    private User createUser(String username,
            String email,
            String password) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setActive(true);

        Role role = roleDAO.getByName(RoleType.REGISTRANT.getValue());
        user.setRoleId(role != null ? role.getRoleId() : 0);

        return user;
    }

    // send registration email
    private boolean sendRegistrationEmail(String fullName,
            String email,
            String username,
            String password) {

        String subject = "[LÃ¡i Vui] ThÃ´ng tin tÃ i khoáº£n";

        String content = """
                Xin chÃ o %s,
                TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng.
                TÃªn Ä‘Äƒng nháº­p: %s
                Máº­t kháº©u: %s
                Vui lÃ²ng Ä‘Äƒng nháº­p vÃ  Ä‘á»•i máº­t kháº©u trong pháº§n cÃ i Ä‘áº·t tÃ i khoáº£n.
                """.formatted(fullName, username, password);

        return emailService.sendTextEmail(email, subject, content);
    }

    // send password recovery email
    private boolean sendForgotPasswordEmail(String recipientEmail,
            String username,
            String tempPassword) {

        // validate recipient
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return false;
        }

        String subject = "[LÃ¡i Vui] KhÃ´i phá»¥c máº­t kháº©u tÃ i khoáº£n";

        String content = """
                Xin chÃ o %s,
                Máº­t kháº©u cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c khÃ´i phá»¥c thÃ nh cÃ´ng.
                Máº­t kháº©u táº¡m thá»i má»›i lÃ : %s
                Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i vÃ  Ä‘á»•i máº­t kháº©u trong pháº§n cÃ i Ä‘áº·t tÃ i khoáº£n.
                """.formatted(username, tempPassword);

        return emailService.sendTextEmail(recipientEmail, subject, content);
    }

    // find user by email or identifier
    private User findUserByEmailOrIdentifier(String identifier) {
        User user = userDAO.getByEmail(identifier);

        if (user == null) {
            user = userDAO.getByIdentifier(identifier);
        }

        return user;
    }

    // generate a temporary password
    private String generateTempPassword() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000));
    }

    // compare passwords
    private boolean passwordsMatch(String rawPassword,
            String storedPasswordHash) {

        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }

        return PasswordUtil.matches(rawPassword, storedPasswordHash.trim());
    }

    // generate a unique username
    private String generateUniqueUsername(String fullName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String username = CredentialsGenerator.generateUsername(fullName);

            if (userDAO.getByUsername(username) == null) {
                return username;
            }
        }

        return CredentialsGenerator.generateUsername(fullName)
                + System.currentTimeMillis() % 1000;
    }
}

