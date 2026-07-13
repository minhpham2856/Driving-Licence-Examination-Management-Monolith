package examiner.service.impl;

import examiner.dao.ProfileDAO;
import examiner.dao.UserDAO;
import examiner.dao.impl.ProfileDAOImpl;
import examiner.dao.impl.UserDAOImpl;
import examiner.dto.ServiceResult;
import examiner.dto.RegisterResultDTO;
import examiner.enums.ErrorType;
import shared.model.Profile;
import shared.model.User;
import examiner.service.AuthService;
import examiner.service.EmailService;
import examiner.service.RoleService;
import examiner.util.UsernameGenerator;

public class AuthServiceImpl implements AuthService {

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public ServiceResult<RegisterResultDTO> register(Profile profile, String email) {
        if (userDAO.getByEmail(email) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }
        if (profileDAO.getByGovIdNo(profile.getGovernmentIdNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ cÄƒn cÆ°á»›c Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }
        if (profileDAO.getByPhoneNo(profile.getPhoneNumber()) != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng.");
        }
        String username = generateUniqueUsername(profile.getFullName());
        String password = UsernameGenerator.randomPassword(10);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setActive(true);
        RoleService roleService = new RoleServiceImpl();
        user.setRoleId(roleService.getRoleIdByName(shared.enums.RoleType.REGISTRANT.getValue()));
        if (!userDAO.insert(user)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "KhÃ´ng thá»ƒ Ä‘Äƒng kÃ½ tÃ i khoáº£n. Vui lÃ²ng thá»­ láº¡i.");
        }
        profile.setUserId(user.getUserId());
        if (!profileDAO.insert(profile)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.");
        }
        String subject = "[LÃ¡i Vui] ThÃ´ng tin tÃ i khoáº£n";
        String content = """
                Xin chÃ o %s,
                TÃ i khoáº£n cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c táº¡o thÃ nh cÃ´ng trÃªn há»‡ thá»‘ng trung tÃ¢m LÃ¡i Vui.
                TÃªn Ä‘Äƒng nháº­p: %s
                Máº­t kháº©u: %s
                Vui lÃ²ng Ä‘Äƒng nháº­p vÃ  Ä‘á»•i máº­t kháº©u trong pháº§n cÃ i Ä‘áº·t tÃ i khoáº£n.
                """.formatted(profile.getFullName(), username, password);
        boolean emailSent = emailService.sendTextEmail(email, subject, content);
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
        return passwordsMatch(trimmedPassword, user.getPasswordHash()) ? user : null;
    }

    @Override
    public String forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            return "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n";
        }
        String trimmed = email.trim();
        User user = userDAO.getByEmail(trimmed);
        if (user == null) {
            user = userDAO.getByIdentifier(trimmed);
        }
        if (user == null || !user.isActive()) {
            return "HÃ£y kiá»ƒm tra hÃ²m thÆ° cá»§a báº¡n náº¿u email báº¡n nháº­p lÃ  Ä‘Ãºng";
        }
        String tempPassword = String.valueOf((int) ((Math.random() * 900000) + 100000));
        if (!userDAO.updatePassword(user.getUserId(), tempPassword)) {
            return "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.";
        }
        String recipient = user.getEmail();
        if (recipient == null || recipient.isBlank()) {
            return "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.";
        }
        String subject = "[LÃ¡i Vui] KhÃ´i phá»¥c máº­t kháº©u tÃ i khoáº£n";
        String content = """
                Xin chÃ o %s,
                Máº­t kháº©u cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c khÃ´i phá»¥c thÃ nh cÃ´ng.
                Máº­t kháº©u táº¡m thá»i má»›i lÃ : %s
                Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i vÃ  Ä‘á»•i máº­t kháº©u trong pháº§n cÃ i Ä‘áº·t tÃ i khoáº£n.
                """.formatted(user.getUsername(), tempPassword);
        if (!emailService.sendTextEmail(recipient, subject, content)) {
            return "Lá»—i há»‡ thá»‘ng. Vui lÃ²ng thá»­ láº¡i.";
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
    public ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword,
            String confirmPassword) {
        User fresh = userDAO.getById(userId);
        if (fresh == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND, "CÃ³ lá»—i xáº£y ra, vui lÃ²ng thá»­ láº¡i.");
        }
        if (currentPassword == null
                || !currentPassword.equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, "Máº­t kháº©u hiá»‡n táº¡i khÃ´ng chÃ­nh xÃ¡c.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i vÃ  xÃ¡c nháº­n khÃ´ng khá»›p.");
        }
        if (newPassword.equals(fresh.getPasswordHash())) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED,
                    "Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c trÃ¹ng máº­t kháº©u cÅ©.");
        }
        if (userDAO.updatePassword(fresh.getUserId(), newPassword)) {
            return ServiceResult.ok(null, "Äá»•i máº­t kháº©u thÃ nh cÃ´ng.");
        }
        return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED, "CÃ³ lá»—i xáº£y ra, vui lÃ²ng thá»­ láº¡i.");
    }
}

