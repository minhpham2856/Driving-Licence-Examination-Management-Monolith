package auth.controller.general;

import auth.dto.ServiceResult;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.User;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import auth.service.AuditService;
import auth.service.impl.AuditServiceImpl;
import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final AuditService auditService = new AuditServiceImpl();
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        User sessionUser = userSession == null ? null : (User) userSession.getAttribute("user");
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String current = request.getParameter("currentPassword");
        String newPwd = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        ServiceResult<Void> result = authService.changePassword(
                sessionUser.getUserId(), current, newPwd, confirm);
        if (result.isSuccess()) {
            HttpSession s = request.getSession(false);
            if (s != null) {
                auditService.logAction(sessionUser.getUserId(), AuditAction.UPDATE, AuditEntity.DOSSIER,
                        "Äá»•i máº­t kháº©u tÃ i khoáº£n", sessionUser.getUserId());
            }
            request.setAttribute("messageType", "success");
        } else {
            request.setAttribute("messageType", "danger");
        }
        request.setAttribute("message", result.getMessage());
        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp").forward(request, response);
    }
}

