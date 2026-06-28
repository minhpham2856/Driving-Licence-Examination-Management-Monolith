package Controllers.Staff.ManagingStaff;

import DAOs.DossierDAO;
import DAOs.Impl.DossierDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebServlet("/manager/dossier-detail")
public class DossierDetailServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/user-detail.jsp";
    private final DossierDAO dossierDAO = new DossierDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        int userId = parseInt(request.getParameter("id"));
        int registrationId = parseInt(request.getParameter("registrationId"));
        DossierDTO dossier = registrationId > 0
                ? dossierDAO.findByRegistrationId(registrationId)
                : userId > 0 ? dossierDAO.findByUserId(userId) : null;

        if ((userId > 0 || registrationId > 0) && dossier == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hồ sơ học viên.");
            return;
        }

        if (userId <= 0 && registrationId <= 0) {
            request.setAttribute("listMode", true);
            request.setAttribute("dossiers", dossierDAO.findAllRegistrants());
        } else {
            request.setAttribute("dossier", dossier);
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
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

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
