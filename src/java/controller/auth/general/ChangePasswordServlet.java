package controller.auth.general;
import service.*;
import service.impl.*;
import dto.ChangePasswordResultDTO;
import service.AuthService;
import service.impl.AuthServiceImpl;
import model.User;
import service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private AuthService authService = new AuthServiceImpl();
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
        ChangePasswordResultDTO result = authService.changePassword(sessionUser.getUserId(), current, newPwd, confirm);
        if (result.success) {
            HttpSession s = req.getSession(false);
            if (s != null) {
                auditLogService.logAction(sessionUser.getUserId(), "UPDATE", "Đổi mật khẩu tài khoản", sessionUser.getUserId());
            }
            req.setAttribute("messageType", "success");
        } else {
            req.setAttribute("messageType", "danger");
        }
        req.setAttribute("message", result.message);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
