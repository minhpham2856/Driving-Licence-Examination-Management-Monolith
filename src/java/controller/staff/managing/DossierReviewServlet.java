package controller.staff.managing;

import dao.DossierDAO;
import dao.impl.DossierDAOImpl;
import dto.DossierDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.AuditLogService;
import service.RoleService;
import service.impl.AuditLogServiceImpl;
import service.impl.RoleServiceImpl;

import java.io.IOException;
import java.util.Set;

@WebServlet("/manager/dossiers")
public class DossierReviewServlet extends HttpServlet {

    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final RoleService roleService = new RoleServiceImpl();
    private final AuditLogService auditLogService = new AuditLogServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User reviewer = requireReviewer(request, response);
        if (reviewer == null) {
            return;
        }
        int id = parseInt(request.getParameter("id"));
        if (id > 0) {
            DossierDTO dossier = dossierDAO.findByRegistrationId(id);
            if (dossier == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ.");
                return;
            }
            request.setAttribute("dossier", dossier);
        } else {
            request.setAttribute("dossiers", dossierDAO.findSubmitted());
        }
        request.getRequestDispatcher("/views/staff/managing/approve.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User reviewer = requireReviewer(request, response);
        if (reviewer == null) {
            return;
        }
        int registrationId = parseInt(request.getParameter("id"));
        String decision = request.getParameter("decision");
        String reason = request.getParameter("reason");
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
            response.sendRedirect(request.getContextPath() + "/manager/dossiers");
            return;
        }
        if ("Approved".equals(status) && !dossier.isComplete()) {
            String missing = String.join(", ", dossier.getMissingRequiredDocumentLabels());
            request.getSession().setAttribute("reviewError",
                    "Không thể duyệt hồ sơ hạng " + dossier.getLicenceDisplayClass()
                    + ". Cần đủ " + dossier.getRequiredDocumentTotal()
                    + " giấy tờ, còn thiếu: " + missing + ".");
            response.sendRedirect(request.getContextPath() + "/manager/dossiers?id=" + registrationId);
            return;
        }
        if (!"Approved".equals(status) && (reason == null || reason.isBlank())) {
            request.getSession().setAttribute("reviewError",
                    "Vui lòng nhập lý do khi yêu cầu bổ sung hoặc từ chối.");
            response.sendRedirect(request.getContextPath() + "/manager/dossiers?id=" + registrationId);
            return;
        }
        boolean updated = dossierDAO.updateStatus(registrationId, status,
                reason == null || reason.isBlank() ? "Hồ sơ hợp lệ" : reason.trim(),
                reviewer.getUserId());
        if (!updated) {
            request.getSession().setAttribute("reviewError", "Không thể cập nhật trạng thái hồ sơ.");
            response.sendRedirect(request.getContextPath() + "/manager/dossiers?id=" + registrationId);
            return;
        }
        auditLogService.logAction(reviewer.getUserId(), "REVIEW Dossier",
                status + " hồ sơ #" + registrationId, registrationId);
        request.getSession().setAttribute("reviewSuccess", "Đã cập nhật kết quả thẩm định hồ sơ.");
        response.sendRedirect(request.getContextPath() + "/manager/dossiers");
    }

    private User requireReviewer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String role = roleService.getRoleNameById(user.getRoleId());
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
