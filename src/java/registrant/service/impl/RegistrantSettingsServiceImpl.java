package registrant.service.impl;

import registrant.dao.DocumentDAO;
import auth.dao.ProfileDAO;
import registrant.dao.RegistrantDAO;
import auth.dao.UserDAO;
import registrant.dao.impl.DocumentDAOImpl;
import auth.dao.impl.ProfileDAOImpl;
import registrant.dao.impl.RegistrantDAOImpl;
import auth.dao.impl.UserDAOImpl;
import shared.model.Profile;
import registrant.dto.RegistrantRegisteredExamRow;
import auth.dto.UserDTO;
import auth.service.impl.EmailServiceImpl;
import auth.util.PasswordUtil;
import registrant.service.RegistrantSettingsService;
import registrant.util.RegistrantAuditHelper;
import registrant.util.RegistrantProfileSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/** Đổi mật khẩu và vô hiệu hoá tài khoản thí sinh. Thông báo Gmail luôn bật (không lưu tùy chọn). */
public class RegistrantSettingsServiceImpl implements RegistrantSettingsService {

    private final UserDAO userdao = new UserDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final EmailServiceImpl emailService = new EmailServiceImpl();

    @Override
    public void applySettingsView(UserDTO user, HttpServletRequest request) {
        request.setAttribute("userEmail", user.getEmail());
        request.setAttribute("emailServiceConfigured", emailService.isConfigured());
        applyAccountSummary(user, request);
    }

    @Override
    public String changePassword(UserDTO user, String currentPassword, String newPassword,
            String confirmPassword, HttpSession session) {
        if (currentPassword == null || newPassword == null || confirmPassword == null
                || currentPassword.isBlank() || newPassword.isBlank()) {
            return "Vui lòng nhập đầy đủ thông tin mật khẩu.";
        }
        shared.model.User stored = userdao.getById(user.getUserId());
        if (stored == null || !passwordsMatch(currentPassword, stored.getPasswordHash())) {
            return "Mật khẩu hiện tại không chính xác.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }
        if (newPassword.length() < 8) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự.";
        }
        String hashed = PasswordUtil.hash(newPassword);
        if (!userdao.updatePassword(user.getUserId(), hashed)) {
            return "Không thể cập nhật mật khẩu. Vui lòng thử lại.";
        }
        if (session != null) {
            RegistrantAuditHelper.logPasswordChange(session, user.getUserId());
            sendPasswordChangedEmail(user);
        }
        return null;
    }

    private static boolean passwordsMatch(String rawPassword, String storedPasswordHash) {
        if (rawPassword == null || storedPasswordHash == null) {
            return false;
        }
        String stored = storedPasswordHash.trim();
        if (PasswordUtil.matches(rawPassword, stored)) {
            return true;
        }
        return rawPassword.equals(stored);
    }

    @Override
    public String deactivateAccount(UserDTO user, boolean confirmed, HttpSession session) {
        if (!confirmed) {
            return "Bạn cần xác nhận trước khi vô hiệu hoá tài khoản.";
        }
        if (!userdao.deactivate(user.getUserId())) {
            return "Không thể vô hiệu hoá tài khoản. Vui lòng liên hệ hỗ trợ.";
        }
        user.setActive(false);
        if (session != null) {
            RegistrantAuditHelper.logAccountDeactivate(session, user.getUserId());
        }
        return null;
    }

    private void applyAccountSummary(UserDTO user, HttpServletRequest request) {
        request.setAttribute("accountUsername", user.getUsername());
        Profile profile = RegistrantProfileSupport.resolveProfile(profiledao, user);
        if (profile == null) {
            request.setAttribute("hasProfile", false);
            request.setAttribute("accountDisplayName", RegistrantProfileSupport.displayName(user, null));
            request.setAttribute("profileRegistrationStatusLabel", "Chưa có hồ sơ");
            request.setAttribute("profileRegistrationStatusClass", "gray");
            request.setAttribute("cccdImagesComplete", false);
            request.setAttribute("cccdStatusLabel", "—");
            request.setAttribute("activeExamRegistrationCount", 0);
            request.setAttribute("activeLicenceClassesLabel", null);
            return;
        }

        request.setAttribute("hasProfile", true);
        request.setAttribute("accountDisplayName", RegistrantProfileSupport.displayName(user, profile));

        var ctx = RegistrantProfileSupport.loadContext(profiledao, documentdao, registrantdao, user);
        RegistrantProfileSupport.applyRegistrationStatus(request, ctx.getRegistrationStatus());

        boolean cccdComplete = RegistrantProfileSupport.isCccdComplete(ctx.getDocuments());
        request.setAttribute("cccdImagesComplete", cccdComplete);
        request.setAttribute("cccdStatusLabel", cccdComplete ? "Đã tải đủ 2 mặt" : "Chưa tải đủ ảnh CCCD");

        List<RegistrantRegisteredExamRow> activeExams =
                registrantdao.listActiveExamRegistrationsByProfileId(profile.getProfileId(), 20);
        request.setAttribute("activeExamRegistrationCount", activeExams.size());
        request.setAttribute("activeLicenceClassesLabel",
                RegistrantProfileSupport.buildActiveLicenceClassesLabel(activeExams));
    }

    private void sendPasswordChangedEmail(UserDTO user) {
        if (user == null || RegistrantProfileSupport.isBlank(user.getEmail())) {
            return;
        }
        if (!emailService.isConfigured()) {
            return;
        }
        String subject = "[Lái Vui] Mật khẩu tài khoản đã được đổi";
        String content = """
                Xin chào %s,

                Mật khẩu tài khoản Lái Vui của bạn vừa được thay đổi thành công.
                Nếu bạn không thực hiện thao tác này, vui lòng liên hệ Ban quản lý ngay.

                Trân trọng,
                Hệ thống Lái Vui
                """.formatted(RegistrantProfileSupport.displayName(user, user.getProfile()));
        emailService.sendTextEmail(user.getEmail().trim(), subject, content);
    }
}
