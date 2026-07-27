package managingstaff.controller;

import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dto.DossierDTO;
import auth.dto.UserDTO;
import managingstaff.service.EmailService;
import managingstaff.service.impl.EmailServiceImpl;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet("/manager/dossiers")
public class DossierReviewServlet extends HttpServlet {

    private static final int PAGE_SIZE = 15;
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO reviewer = requireReviewer(request, response);
        if (reviewer == null) {
            return;
        }
        int id = parseInt(request.getParameter("id"));
        int requestedPage = Math.max(parseInt(request.getParameter("page")), 1);
        request.setAttribute("currentPage", requestedPage);
        if (id > 0) {
            DossierDTO dossier = dossierDAO.findByRegistrationId(id);
            if (dossier == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ.");
                return;
            }
            request.setAttribute("dossier", dossier);
        } else {
            int totalItems = dossierDAO.countSubmitted();
            int totalPages = Math.max(1, (totalItems + PAGE_SIZE - 1) / PAGE_SIZE);
            int currentPage = Math.min(requestedPage, totalPages);
            java.util.List<DossierDTO> dossiers = dossierDAO.findSubmittedPage(currentPage, PAGE_SIZE);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("dossiers", dossiers);
            setPaginationAttributes(request, currentPage, totalPages, totalItems, dossiers.size());
        }
        request.getRequestDispatcher("/views/staff/managingstaff/approve.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO reviewer = requireReviewer(request, response);
        if (reviewer == null) {
            return;
        }
        int registrationId = parseInt(request.getParameter("id"));
        int returnPage = Math.max(parseInt(request.getParameter("returnPage")), 1);
        String listRedirect = request.getContextPath() + "/manager/dossiers?page=" + returnPage;
        String detailRedirect = request.getContextPath() + "/manager/dossiers?id="
                + registrationId + "&page=" + returnPage;
        String decision = request.getParameter("decision");
        String reason = request.getParameter("reason");
        String reviewMessage = reason == null || reason.isBlank()
                ? "Hồ sơ hợp lệ và đang chờ xếp ngày thi"
                : reason.trim();
        String status = switch (decision == null ? "" : decision) {
            case "approve" ->
                "Approved";
            case "reject" ->
                "Rejected";
            default ->
                "";
        };
        if (registrationId <= 0 || status.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
        if (dossier == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ.");
            return;
        }
        if (!Set.of("Pending", "Submitted")
                .contains(dossier.getStatus())) {
            request.getSession().setAttribute("reviewError",
                    "Hồ sơ này đã được duyệt hoặc đã chuyển sang quy trình thi.");
            response.sendRedirect(listRedirect);
            return;
        }
        if ("Approved".equals(status) && !dossier.isComplete()) {
            String missing = String.join(", ", dossier.getMissingRequiredDocumentLabels());
            request.getSession().setAttribute("reviewError",
                    "Không thể duyệt hồ sơ. Cần đủ " + dossier.getRequiredDocumentTotal()
                    + " giấy tờ, còn thiếu: " + missing + ".");
            response.sendRedirect(detailRedirect);
            return;
        }
        if ("Approved".equals(status) && !dossier.isMotorcycleLicence()) {
            request.getSession().setAttribute("reviewError",
                    "Không thể duyệt hồ sơ vì chưa có hạng GPLX hợp lệ (A1, A hoặc B1).");
            response.sendRedirect(detailRedirect);
            return;
        }
        if ("Rejected".equals(status) && (reason == null || reason.isBlank())) {
            request.getSession().setAttribute("reviewError",
                    "Vui lòng nhập lý do khi từ chối hồ sơ.");
            response.sendRedirect(detailRedirect);
            return;
        }
        if (!emailService.isConfigured()) {
            request.getSession().setAttribute("reviewError",
                    "Chưa cấu hình SMTP nên chưa thể gửi thông báo cho thí sinh.");
            response.sendRedirect(detailRedirect);
            return;
        }

        boolean updated = dossierDAO.updateStatus(registrationId, status,
                reviewMessage, reviewer.getUserId());
        if (!updated) {
            request.getSession().setAttribute("reviewError", "Không thể cập nhật trạng thái hồ sơ.");
            response.sendRedirect(detailRedirect);
            return;
        }
        boolean emailSent = true;
        if ("Rejected".equals(status)) {
            emailSent = emailService.sendHtmlEmail(
                    dossier.getUser().getEmail(),
                    "[Lái Vui] Hồ sơ sát hạch bị từ chối",
                    rejectionEmailHtml(dossier, reviewMessage));
        } else if ("Approved".equals(status)) {
            emailSent = emailService.sendHtmlEmail(
                    dossier.getUser().getEmail(),
                    "[Lái Vui] Hồ sơ sát hạch đã được duyệt",
                    approvalEmailHtml(dossier));
        }

        if (!emailSent) {
            dossierDAO.updateStatus(registrationId, dossier.getStatus(),
                    "Tự động khôi phục trạng thái do gửi email thất bại", reviewer.getUserId());
            request.getSession().setAttribute("reviewError",
                    "Email chưa gửi được nên hệ thống đã khôi phục trạng thái hồ sơ. "
                    + "Vui lòng kiểm tra SMTP rồi thực hiện lại.");
            response.sendRedirect(detailRedirect);
            return;
        }

        AuditLogHelper.persist(request.getSession(), "REVIEW Dossier",
                status + " hồ sơ #" + registrationId, registrationId);
        String successMessage = "Approved".equals(status)
                ? "Đã duyệt hồ sơ và gửi email xác nhận đến "
                : "Đã từ chối hồ sơ và gửi email kèm lý do đến ";
        request.getSession().setAttribute("reviewSuccess",
                successMessage + dossier.getUser().getEmail() + ".");
        response.sendRedirect(listRedirect);
    }

    private static String rejectionEmailHtml(DossierDTO dossier, String reason) {
        return "<div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>"
                + "<h2 style='color:#b91c1c'>Hồ sơ sát hạch bị từ chối</h2>"
                + "<p>Kính gửi <strong>" + html(dossier.getProfile().getFullName()) + "</strong>,</p>"
                + "<p>Hồ sơ đăng ký sát hạch, mã hồ sơ <strong>#" + dossier.getRegistrationId()
                + "</strong> đã bị từ chối.</p>"
                + "<p><strong>Lý do:</strong> " + html(reason) + "</p>"
                + "<p>Hồ sơ này đã kết thúc. Vui lòng đăng nhập hệ thống, tạo đăng ký sát hạch mới "
                + "và nộp lại đầy đủ tài liệu theo hướng dẫn.</p>"
                + "<p>Trân trọng,<br>Trung tâm sát hạch Lái Vui</p></div>";
    }

    private static String approvalEmailHtml(DossierDTO dossier) {
        return "<div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>"
                + "<h2 style='color:#047857'>Hồ sơ đã được tiếp nhận và duyệt</h2>"
                + "<p>Kính gửi <strong>" + html(dossier.getProfile().getFullName()) + "</strong>,</p>"
                + "<p>Hồ sơ đăng ký sát hạch, mã hồ sơ <strong>#" + dossier.getRegistrationId()
                + "</strong> đã được nộp đầy đủ và duyệt hợp lệ cho hạng GPLX <strong>"
                + html(dossier.getLicenceDisplayClass()) + "</strong>.</p>"
                + "<p>Hồ sơ điện tử đã được trung tâm duyệt và hiện đang chờ xếp ngày thi. "
                + "Thời gian sát hạch sẽ được thông báo sau.</p>"
                + "<p>Trân trọng,<br>Trung tâm sát hạch Lái Vui</p></div>";
    }

    private static String html(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private UserDTO requireReviewer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
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

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void setPaginationAttributes(HttpServletRequest request, int currentPage,
            int totalPages, int totalItems, int pageItems) {
        int firstItem = totalItems == 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalFiltered", totalItems);
        request.setAttribute("firstItem", firstItem);
        request.setAttribute("lastItem", totalItems == 0 ? 0 : firstItem + pageItems - 1);
        request.setAttribute("pageStart", Math.max(1, currentPage - 2));
        request.setAttribute("pageEnd", Math.min(totalPages, currentPage + 2));
    }
}
