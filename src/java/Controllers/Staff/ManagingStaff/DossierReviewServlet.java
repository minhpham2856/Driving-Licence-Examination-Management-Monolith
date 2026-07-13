package Controllers.Staff.ManagingStaff;

import DAOs.DossierDAO;
import DAOs.Impl.DossierDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Services.DossierPdfService;
import Services.EmailService;
import Services.Impl.AwtDossierPdfService;
import Services.Impl.EmailServiceImpl;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@WebServlet("/manager/dossiers")
public class DossierReviewServlet extends HttpServlet {

    private static final String APPROVED_PDF_DOCUMENT_TYPE = "APPROVED_DOSSIER_PDF";
    private static final int PAGE_SIZE = 15;
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final DossierPdfService pdfService = new AwtDossierPdfService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User reviewer = requireReviewer(request, response);
        if (reviewer == null) return;
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
        User reviewer = requireReviewer(request, response);
        if (reviewer == null) return;
        int registrationId = parseInt(request.getParameter("id"));
        int returnPage = Math.max(parseInt(request.getParameter("returnPage")), 1);
        String listRedirect = request.getContextPath() + "/manager/dossiers?page=" + returnPage;
        String detailRedirect = request.getContextPath() + "/manager/dossiers?id="
                + registrationId + "&page=" + returnPage;
        String decision = request.getParameter("decision");
        String reason = request.getParameter("reason");
        String reviewMessage = reason == null || reason.isBlank() ? "Hồ sơ hợp lệ" : reason.trim();
        String status = switch (decision == null ? "" : decision) {
            case "approve" -> "Approved";
            case "supplement" -> "NeedSupplement";
            case "reject" -> "Rejected";
            default -> "";
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
        if (!Set.of("Draft", "Pending", "Submitted", "NeedSupplement", "Rejected")
                .contains(dossier.getStatus())) {
            request.getSession().setAttribute("reviewError",
                    "Hồ sơ này đã được duyệt hoặc đã chuyển sang quy trình thi.");
            response.sendRedirect(listRedirect);
            return;
        }
        if ("Approved".equals(status) && !dossier.isComplete()) {
            String missing = String.join(", ", dossier.getMissingRequiredDocumentLabels());
            request.getSession().setAttribute("reviewError",
                    "Không thể duyệt hồ sơ hạng " + dossier.getLicenceDisplayClass()
                    + ". Cần đủ " + dossier.getRequiredDocumentTotal()
                    + " giấy tờ, còn thiếu: " + missing + ".");
            response.sendRedirect(detailRedirect);
            return;
        }
        if (!"Approved".equals(status) && (reason == null || reason.isBlank())) {
            request.getSession().setAttribute("reviewError",
                    "Vui lòng nhập lý do khi yêu cầu bổ sung hoặc từ chối.");
            response.sendRedirect(detailRedirect);
            return;
        }
        if (("Approved".equals(status) || "NeedSupplement".equals(status))
                && !emailService.isConfigured()) {
            request.getSession().setAttribute("reviewError",
                    "Chưa cấu hình SMTP nên chưa thể gửi thông báo cho người đăng ký.");
            response.sendRedirect(detailRedirect);
            return;
        }

        byte[] approvalPdf = null;
        String generatedPdfUrl = null;
        String pdfFileName = "ho-so-da-duyet-" + registrationId + ".pdf";
        if ("Approved".equals(status)) {
            try {
                String webRootValue = getServletContext().getRealPath("/");
                if (webRootValue == null) {
                    throw new IOException("Không xác định được thư mục web của ứng dụng.");
                }
                Path webRoot = Paths.get(webRootValue).toAbsolutePath().normalize();
                approvalPdf = pdfService.generate(dossier, webRoot);
                saveGeneratedPdf(webRoot, pdfFileName, approvalPdf);
                generatedPdfUrl = "/uploads/generated-dossiers/" + pdfFileName;
            } catch (Exception ex) {
                request.getSession().setAttribute("reviewError",
                        "Không thể tạo PDF hồ sơ nên chưa thực hiện duyệt: " + ex.getMessage());
                response.sendRedirect(detailRedirect);
                return;
            }
        }

        boolean updated = dossierDAO.updateStatus(registrationId, status,
                reviewMessage, reviewer.getId());
        if (!updated) {
            request.getSession().setAttribute("reviewError", "Không thể cập nhật trạng thái hồ sơ.");
            response.sendRedirect(detailRedirect);
            return;
        }
        boolean emailSent = true;
        if ("NeedSupplement".equals(status)) {
            emailSent = emailService.sendHtmlEmail(
                    dossier.getUser().getEmail(),
                    "[Lái Vui] Yêu cầu bổ sung hồ sơ sát hạch",
                    supplementEmailHtml(dossier, reviewMessage));
        } else if ("Approved".equals(status)) {
            emailSent = emailService.sendHtmlEmailWithAttachment(
                    dossier.getUser().getEmail(),
                    "[Lái Vui] Hồ sơ sát hạch đã được duyệt",
                    approvalEmailHtml(dossier),
                    approvalPdf,
                    pdfFileName,
                    "application/pdf");
        }

        if (!emailSent) {
            dossierDAO.updateStatus(registrationId, dossier.getStatus(),
                    "Tự động khôi phục trạng thái do gửi email thất bại", reviewer.getId());
            request.getSession().setAttribute("reviewError",
                    "Email chưa gửi được nên hệ thống đã khôi phục trạng thái hồ sơ. "
                    + "Vui lòng kiểm tra SMTP rồi thực hiện lại.");
            response.sendRedirect(detailRedirect);
            return;
        }

        if ("Approved".equals(status) && !dossierDAO.saveDocument(
                dossier.getProfile().getId(), APPROVED_PDF_DOCUMENT_TYPE, generatedPdfUrl)) {
            request.getSession().setAttribute("reviewError",
                    "Hồ sơ đã được duyệt và email đã gửi, nhưng chưa lưu được đường dẫn PDF vào hồ sơ.");
            response.sendRedirect(request.getContextPath() + "/manager/dossier-detail?registrationId=" + registrationId);
            return;
        }

        AuditLogHelper.persist(request.getSession(), "REVIEW Dossier",
                status + " hồ sơ #" + registrationId, registrationId);
        request.getSession().setAttribute("reviewSuccess",
                "Đã cập nhật hồ sơ và gửi email thông báo đến " + dossier.getUser().getEmail() + ".");
        response.sendRedirect(listRedirect);
    }

    private void saveGeneratedPdf(Path runtimeWebRoot, String fileName, byte[] pdf) throws IOException {
        Path runtimeDirectory = runtimeWebRoot.resolve("uploads/generated-dossiers").normalize();
        if (!runtimeDirectory.startsWith(runtimeWebRoot)) {
            throw new IOException("Đường dẫn lưu PDF không hợp lệ.");
        }
        Files.createDirectories(runtimeDirectory);
        Path runtimeFile = runtimeDirectory.resolve(fileName).normalize();
        Files.write(runtimeFile, pdf);

        Path buildDir = runtimeWebRoot.getParent();
        if (buildDir == null || !"build".equalsIgnoreCase(buildDir.getFileName().toString())) return;
        Path projectRoot = buildDir.getParent();
        if (projectRoot == null) return;
        Path sourceWebRoot = projectRoot.resolve("web").toAbsolutePath().normalize();
        Path sourceDirectory = sourceWebRoot.resolve("uploads/generated-dossiers").normalize();
        if (!sourceDirectory.startsWith(sourceWebRoot)) return;
        Files.createDirectories(sourceDirectory);
        Files.copy(runtimeFile, sourceDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private static String supplementEmailHtml(DossierDTO dossier, String reason) {
        return "<div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>"
                + "<h2 style='color:#b45309'>Yêu cầu bổ sung hồ sơ</h2>"
                + "<p>Kính gửi <strong>" + html(dossier.getProfile().getFullName()) + "</strong>,</p>"
                + "<p>Hồ sơ đăng ký sát hạch hạng <strong>" + html(dossier.getLicenceDisplayClass())
                + "</strong> cần được bổ sung trước khi duyệt.</p>"
                + "<p><strong>Lý do:</strong> " + html(reason) + "</p>"
                + "<p>Vui lòng đăng nhập hệ thống hoặc liên hệ trung tâm để hoàn thiện hồ sơ.</p>"
                + "<p>Trân trọng,<br>Trung tâm sát hạch Lái Vui</p></div>";
    }

    private static String approvalEmailHtml(DossierDTO dossier) {
        return "<div style='font-family:Arial,sans-serif;line-height:1.6;color:#1f2937'>"
                + "<h2 style='color:#047857'>Hồ sơ đã được duyệt</h2>"
                + "<p>Kính gửi <strong>" + html(dossier.getProfile().getFullName()) + "</strong>,</p>"
                + "<p>Hồ sơ đăng ký sát hạch hạng <strong>" + html(dossier.getLicenceDisplayClass())
                + "</strong>, mã hồ sơ <strong>#" + dossier.getRegistrationId() + "</strong> đã được duyệt.</p>"
                + "<p>File PDF hồ sơ đã duyệt được đính kèm email này. Vui lòng kiểm tra thông tin và "
                + "ký vào phần chữ ký khi trung tâm yêu cầu.</p>"
                + "<p>Thời gian và địa điểm sát hạch sẽ được thông báo sau.</p>"
                + "<p>Trân trọng,<br>Trung tâm sát hạch Lái Vui</p></div>";
    }

    private static String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private User requireReviewer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception e) { return 0; }
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
