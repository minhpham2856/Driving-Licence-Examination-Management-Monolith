package managingstaff.controller;

import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import auth.dto.UserDTO;
import managingstaff.service.EmailService;
import managingstaff.service.impl.EmailServiceImpl;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@WebServlet("/manager/dossiers/remind")
public class DossierReminderServlet extends HttpServlet {

    private static final Set<String> FILTERS = Set.of(
            "all", "draft", "pending", "supplement", "approved", "rejected", "present", "completed");

    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO reviewer = requireReviewer(request, response);
        if (reviewer == null) return;

        int registrationId = parseInt(request.getParameter("id"));
        String returnTo = "list".equals(request.getParameter("returnTo")) ? "list" : "detail";
        String statusFilter = normalizeFilter(request.getParameter("returnStatus"));
        int returnPage = Math.max(parseInt(request.getParameter("returnPage")), 1);
        String redirect = redirectUrl(request, returnTo, statusFilter, returnPage, registrationId);

        DossierDTO dossier = registrationId > 0
                ? dossierDAO.findByRegistrationId(registrationId) : null;
        if (dossier == null) {
            request.getSession().setAttribute("reminderError", "Không tìm thấy hồ sơ cần gửi email nhắc.");
            response.sendRedirect(redirect);
            return;
        }
        if (!"Rejected".equals(dossier.getStatus())) {
            request.getSession().setAttribute("reminderError",
                    "Chỉ hồ sơ đã bị từ chối mới được gửi email nhắc hoàn thiện lại.");
            response.sendRedirect(redirect);
            return;
        }
        if (!emailService.isConfigured()) {
            request.getSession().setAttribute("reminderError", "SMTP chưa được cấu hình.");
            response.sendRedirect(redirect);
            return;
        }

        String subject = "[Lái Vui] Nhắc cập nhật hồ sơ sát hạch đã bị từ chối";
        boolean sent = emailService.sendHtmlEmail(
                dossier.getUser().getEmail(), subject, reminderEmailHtml(dossier));

        if (sent) {
            AuditLogHelper.persist(request.getSession(), "REMIND Dossier",
                    "Gửi email nhắc hoàn thiện hồ sơ #" + registrationId, registrationId);
            request.getSession().setAttribute("reminderSuccess",
                    "Đã gửi email nhắc đến " + dossier.getUser().getEmail() + ".");
        } else {
            request.getSession().setAttribute("reminderError",
                    "Không gửi được email nhắc. Vui lòng kiểm tra địa chỉ email và SMTP.");
        }
        response.sendRedirect(redirect);
    }

    private static String reminderEmailHtml(DossierDTO dossier) {
        String reason = dossier.getReviewMessage();
        String detail = "<p>Hồ sơ của bạn đang ở trạng thái <strong>đã từ chối</strong>. "
                + "Vui lòng kiểm tra và cập nhật lại thông tin.</p>"
                + (reason == null || reason.isBlank() ? ""
                        : "<p><strong>Lý do gần nhất:</strong> " + html(reason) + "</p>");
        return "<div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>"
                + "<h2 style='color:#0052cc'>Nhắc hoàn thiện hồ sơ sát hạch</h2>"
                + "<p>Kính gửi <strong>" + html(dossier.getProfile().getFullName()) + "</strong>,</p>"
                + "<p>Mã hồ sơ: <strong>#" + dossier.getRegistrationId() + "</strong> - Hạng "
                + "<strong>" + html(dossier.getLicenceDisplayClass()) + "</strong>.</p>"
                + detail
                + "<p>Nếu đã hoàn thiện, bạn có thể bỏ qua email này hoặc liên hệ trung tâm để được hỗ trợ.</p>"
                + "<p>Trân trọng,<br>Trung tâm sát hạch Lái Vui</p></div>";
    }

    private UserDTO requireReviewer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private static String redirectUrl(HttpServletRequest request, String returnTo,
            String statusFilter, int returnPage, int registrationId) {
        if ("list".equals(returnTo)) {
            return request.getContextPath() + "/manager/dossier-detail?status="
                    + URLEncoder.encode(statusFilter, StandardCharsets.UTF_8)
                    + "&page=" + returnPage;
        }
        return request.getContextPath() + "/manager/dossier-detail?registrationId=" + registrationId;
    }

    private static String normalizeFilter(String value) {
        String filter = value == null ? "all" : value.trim().toLowerCase(java.util.Locale.ROOT);
        return FILTERS.contains(filter) ? filter : "all";
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception ex) { return 0; }
    }

    private static String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
