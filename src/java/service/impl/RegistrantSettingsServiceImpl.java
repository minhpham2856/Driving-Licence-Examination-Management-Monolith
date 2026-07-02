package service.impl;

import dao.DocumentDAO;
import dao.ProfileDAO;
import dao.RegistrantDAO;
import dao.UserDAO;
import dao.impl.DocumentDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantDAOImpl;
import dao.impl.UserDAOImpl;
import model.user.Profile;
import dto.registrant.RegistrantRegisteredExamRow;
import model.user.User;
import service.EmailService;
import service.RegistrantSettingsService;
import util.registrant.RegistrantAuditHelper;
import util.registrant.RegistrantProfileSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/** Đổi mật khẩu, tùy chọn Gmail và vô hiệu hoá tài khoản thí sinh. */
public class RegistrantSettingsServiceImpl implements RegistrantSettingsService {

    static final String SESSION_NOTIFY_EXAM_RESULTS = "registrantNotifyExamResultsGmail";
    static final String SESSION_NOTIFY_PASSWORD_CHANGE = "registrantNotifyPasswordChangeGmail";

    private final UserDAO userdao = new UserDAOImpl();
    private final ProfileDAO profiledao = new ProfileDAOImpl();
    private final DocumentDAO documentdao = new DocumentDAOImpl();
    private final RegistrantDAO registrantdao = new RegistrantDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    public void applySettingsView(User user, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        request.setAttribute("userEmail", user.getEmail());
        request.setAttribute("emailServiceConfigured", emailService.isConfigured());
        request.setAttribute("notifyExamResults", isNotifyExamResults(session));
        request.setAttribute("notifyPasswordChange", isNotifyPasswordChange(session));
        applyAccountSummary(user, request);
    }

    @Override
    public String saveNotificationPrefs(HttpServletRequest request, boolean notifyExamResults,
            boolean notifyPasswordChange) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_NOTIFY_EXAM_RESULTS, notifyExamResults);
        session.setAttribute(SESSION_NOTIFY_PASSWORD_CHANGE, notifyPasswordChange);
        return null;
    }

    @Override
    public String changePassword(User user, String currentPassword, String newPassword,
            String confirmPassword, HttpSession session) {
        if (currentPassword == null || newPassword == null || confirmPassword == null
                || currentPassword.isBlank() || newPassword.isBlank()) {
            return "Vui lòng nhập đầy đủ thông tin mật khẩu.";
        }
        if (!AuthServiceImpl.passwordsMatch(currentPassword, user.getPasswordHash())) {
            return "Mật khẩu hiện tại không chính xác.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }
        if (newPassword.length() < 8) {
            return "Mật khẩu mới phải có ít nhất 8 ký tự.";
        }
        if (!userdao.updatePassword(user.getId(), newPassword)) {
            return "Không thể cập nhật mật khẩu. Vui lòng thử lại.";
        }
        user.setPasswordHash(newPassword);
        if (session != null) {
            RegistrantAuditHelper.logPasswordChange(session, user.getId());
            if (isNotifyPasswordChange(session)) {
                sendPasswordChangedEmail(user);
            }
        }
        return null;
    }

    @Override
    public String deactivateAccount(User user, boolean confirmed, HttpSession session) {
        if (!confirmed) {
            return "Bạn cần xác nhận trước khi vô hiệu hoá tài khoản.";
        }
        if (!userdao.deactivate(user.getId())) {
            return "Không thể vô hiệu hoá tài khoản. Vui lòng liên hệ hỗ trợ.";
        }
        user.setIsActive(false);
        if (session != null) {
            RegistrantAuditHelper.logAccountDeactivate(session, user.getId());
        }
        return null;
    }

    static boolean isNotifyExamResults(HttpSession session) {
        if (session == null) {
            return true;
        }
        Object value = session.getAttribute(SESSION_NOTIFY_EXAM_RESULTS);
        return value == null || Boolean.TRUE.equals(value);
    }

    static boolean isNotifyPasswordChange(HttpSession session) {
        if (session == null) {
            return true;
        }
        Object value = session.getAttribute(SESSION_NOTIFY_PASSWORD_CHANGE);
        return value == null || Boolean.TRUE.equals(value);
    }

    private void applyAccountSummary(User user, HttpServletRequest request) {
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
                registrantdao.listActiveExamRegistrationsByProfileId(profile.getId(), 20);
        request.setAttribute("activeExamRegistrationCount", activeExams.size());
        request.setAttribute("activeLicenceClassesLabel",
                RegistrantProfileSupport.buildActiveLicenceClassesLabel(activeExams));
    }

    private void sendPasswordChangedEmail(User user) {
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
