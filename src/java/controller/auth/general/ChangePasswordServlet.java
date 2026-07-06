package controller.auth.general;

import dto.ServiceResult;
import dto.payload.ChangePasswordCommand;
import enums.AuditAction;
import enums.AuditEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.AuditLogService;
import service.AuthService;
import service.impl.AuditLogServiceImpl;
import service.impl.AuthServiceImpl;
import java.io.IOException;

@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final AuthService authService = new AuthServiceImpl();
    private static final String VIEW = "/views/auth/general/forgot-password.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession checkSession = req.getSession(false);
        if (checkSession == null || checkSession.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession userSession = req.getSession(false);
        User sessionUser = userSession == null ? null : (User) userSession.getAttribute("user");
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String current = req.getParameter("currentPassword");
        String newPwd = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");
        ChangePasswordCommand command = new ChangePasswordCommand(
                sessionUser.getUserId(), current, newPwd, confirm);
        ServiceResult<Void> result = authService.changePassword(command);
        if (result.isSuccess()) {
            HttpSession s = req.getSession(false);
            if (s != null) {
                auditLogService.logAction(sessionUser.getUserId(), AuditAction.UPDATE, AuditEntity.DOSSIER,
                        "Đổi mật khẩu tài khoản", sessionUser.getUserId());
            }
            req.setAttribute("messageType", "success");
        } else {
            req.setAttribute("messageType", "danger");
        }
        req.setAttribute("message", result.getMessage());
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
