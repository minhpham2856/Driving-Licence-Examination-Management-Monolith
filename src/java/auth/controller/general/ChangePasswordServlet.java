package auth.controller.general;

import auth.dto.ServiceResult;
import auth.dto.UserDTO;
import auth.service.AuditService;
import auth.service.AuthService;
import auth.service.impl.AuditServiceImpl;
import auth.service.impl.AuthServiceImpl;
import auth.util.AuthSessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import java.io.IOException;

@WebServlet(urlPatterns = {
    "/examstaff/change-password",
    "/examiner/change-password",
    "/managingstaff/change-password",
    "/admin/change-password"
})
public class ChangePasswordServlet extends HttpServlet {

    private final AuditService auditService = new AuditServiceImpl();
    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // back link points to role profile path bound by filter
        bindBackUrl(request);
        request.getRequestDispatcher("/views/auth/general/change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDTO sessionUser = sessionUser(request);

        // read password fields from form
        String current = request.getParameter("currentPassword");
        String newPwd = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");

        // validate and update via auth service
        ServiceResult<Void> result = authService.changePassword(
                sessionUser.getUserId(), current, newPwd, confirm);
        if (result.isSuccess()) {
            auditService.logAction(sessionUser.getUserId(), AuditAction.UPDATE, AuditEntity.DOSSIER,
                    "Đổi mật khẩu tài khoản", sessionUser.getUserId());
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, Attributes.MessageType.SUCCESS);
        } else {
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, Attributes.MessageType.DANGER);
        }
        request.setAttribute(Attributes.Request.MESSAGE, result.getMessage());

        bindBackUrl(request);
        request.getRequestDispatcher("/views/auth/general/change-password.jsp").forward(request, response);
    }

    // change-password page uses profile path as back URL (filter already set accountProfilePath)
    private static void bindBackUrl(HttpServletRequest request) {
        Object profilePath = request.getAttribute(Attributes.Request.ACCOUNT_PROFILE_PATH);
        if (profilePath != null) {
            request.setAttribute(Attributes.Request.BACK_URL, profilePath);
        }
    }

    // session user set by login; filter guarantees non-null here
    private static UserDTO sessionUser(HttpServletRequest request) {
        return AuthSessionUtil.sessionUser(request);
    }
}
