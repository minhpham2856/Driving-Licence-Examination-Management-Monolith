package auth.controller.general;

import auth.dto.ServiceResult;
import auth.dto.UserDTO;
import auth.service.AuditService;
import auth.service.AuthService;
import auth.service.impl.AuditServiceImpl;
import auth.service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.enums.RoleType;

import java.io.IOException;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final AuditService auditService = new AuditServiceImpl();
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        bindPage(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        String current = request.getParameter("currentPassword");
        String newPwd = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        ServiceResult<Void> result = authService.changePassword(
                sessionUser.getUserId(), current, newPwd, confirm);
        if (result.isSuccess()) {
            auditService.logAction(sessionUser.getUserId(), AuditAction.UPDATE, AuditEntity.DOSSIER,
                    "Đổi mật khẩu tài khoản", sessionUser.getUserId());
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "success");
        } else {
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "danger");
        }
        request.setAttribute(Attributes.Request.MESSAGE, result.getMessage());
        bindPage(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/change-password.jsp").forward(request, response);
    }

    private void bindPage(HttpServletRequest request, UserDTO sessionUser) {
        request.setAttribute(Attributes.Request.BACK_URL, "/profile");
        request.setAttribute(Attributes.Request.ACCOUNT_SHELL, resolveAccountShell(sessionUser));
    }

    private static String resolveAccountShell(UserDTO user) {
        if (user == null || user.getRole() == null) {
            return "public";
        }
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());
        return role == RoleType.EXAM_STAFF ? "examstaff" : "public";
    }

    private UserDTO requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object raw = session == null ? null : session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return null;
        }
        return (UserDTO) raw;
    }
}
