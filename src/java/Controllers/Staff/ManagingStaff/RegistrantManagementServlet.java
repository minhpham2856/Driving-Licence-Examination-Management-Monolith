package Controllers.Staff.ManagingStaff;

import DAOs.DossierDAO;
import DAOs.UserDAO;
import DAOs.Impl.DossierDAOImpl;
import DAOs.Impl.UserDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/manager/registrants")
public class RegistrantManagementServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/users.jsp";
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) return;

        String keyword = trim(request.getParameter("keyword")).toLowerCase(Locale.ROOT);
        String licence = trim(request.getParameter("licence")).toUpperCase(Locale.ROOT);
        String dossierStatus = trim(request.getParameter("dossierStatus"));
        String accountStatus = trim(request.getParameter("accountStatus"));

        List<DossierDTO> all = dossierDAO.findAllRegistrants();
        List<DossierDTO> filtered = all.stream()
                .filter(d -> matchesKeyword(d, keyword))
                .filter(d -> licence.isEmpty() || licence.equalsIgnoreCase(d.getLicenceClass()))
                .filter(d -> dossierStatus.isEmpty() || dossierStatus.equalsIgnoreCase(d.getStatus()))
                .filter(d -> accountStatus.isEmpty()
                        || ("active".equals(accountStatus) && d.getUser().isActive())
                        || ("locked".equals(accountStatus) && !d.getUser().isActive()))
                .toList();

        if ("csv".equalsIgnoreCase(request.getParameter("export"))) {
            exportCsv(response, filtered);
            return;
        }

        request.setAttribute("registrants", filtered);
        request.setAttribute("totalRegistrants", all.size());
        request.setAttribute("approvedCount",
                all.stream().filter(d -> "Approved".equals(d.getStatus())).count());
        request.setAttribute("pendingCount",
                all.stream().filter(DossierDTO::isReviewable).count());
        request.setAttribute("lockedCount",
                all.stream().filter(d -> !d.getUser().isActive()).count());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!hasAccess(request, response)) return;
        int userId = parseInt(request.getParameter("id"));
        boolean active = "activate".equals(request.getParameter("action"));
        User target = userDAO.getById(userId);
        if (target == null || target.getRole() == null
                || !"Registrant".equalsIgnoreCase(target.getRole().getRoleName())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (userDAO.updateStatus(userId, active)) {
            AuditLogHelper.persistChange(request.getSession(), "UPDATE USER STATUS",
                    (active ? "Mở khóa" : "Khóa") + " tài khoản @" + target.getUsername(),
                    target.isActive() ? "Hoạt động" : "Đã khóa",
                    active ? "Hoạt động" : "Đã khóa", null, userId);
            request.getSession().setAttribute("registrantSuccess",
                    active ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } else {
            request.getSession().setAttribute("registrantError",
                    "Không thể cập nhật trạng thái tài khoản.");
        }
        response.sendRedirect(request.getContextPath() + "/manager/registrants");
    }

    private boolean matchesKeyword(DossierDTO d, String keyword) {
        if (keyword.isEmpty()) return true;
        return contains(d.getProfile().getFullName(), keyword)
                || contains(d.getProfile().getGovIdNo(), keyword)
                || contains(d.getProfile().getPhoneNo(), keyword)
                || contains(d.getUser().getUsername(), keyword)
                || contains(d.getUser().getEmail(), keyword)
                || String.valueOf(d.getUser().getId()).equals(keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void exportCsv(HttpServletResponse response, List<DossierDTO> dossiers)
            throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=registrants.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("UserId,Username,FullName,CCCD,Phone,Email,LicenceClass,DossierStatus,Documents,AccountStatus");
            for (DossierDTO d : dossiers) {
                writer.println(String.join(",",
                        csv(String.valueOf(d.getUser().getId())),
                        csv(d.getUser().getUsername()),
                        csv(d.getProfile().getFullName()),
                        csv(d.getProfile().getGovIdNo()),
                        csv(d.getProfile().getPhoneNo()),
                        csv(d.getUser().getEmail()),
                        csv(d.getLicenceClass()),
                        csv(d.getStatusLabel()),
                        csv(d.getDocumentCount() + "/4"),
                        csv(d.getUser().isActive() ? "Hoạt động" : "Đã khóa")));
            }
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception ignored) { return 0; }
    }
}
