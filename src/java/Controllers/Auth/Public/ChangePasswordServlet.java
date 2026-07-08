package Controllers.Auth.Public;

import DAO.UserSecurityDAO;
import DAO.Impl.UserSecurityDAOImpl;
import Models.User;
import Utils.AuditLogHelper;
import Utils.Sanitize;
import Utils.SessionUtil;
import Utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Đổi mật khẩu — TỰ CHỨA, không phụ thuộc AuthService của nhóm (vì AuthService
 * hiện không có changePassword/ChangePasswordResult). So mật khẩu cũ + cập nhật
 * mật khẩu mới (plaintext theo quy ước nhóm) + tắt cờ MustChangePassword.
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private final UserSecurityDAO securityDAO = new UserSecurityDAOImpl();
    private static final String VIEW = "/views/public/change-password.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Đảm bảo mã hóa UTF-8 cho request dữ liệu đầu vào
        req.setCharacterEncoding("UTF-8");

        User u = SessionUtil.getCurrentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int userId = u.getId();

        String current = req.getParameter("currentPassword");
        String newPwd  = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        String stored = securityDAO.getPasswordHash(userId);

        String error = null;
        if (current == null || !current.equals(stored == null ? "" : stored.trim())) {
            error = "Mật khẩu hiện tại không chính xác.";
        } else if (newPwd == null || confirm == null || !newPwd.equals(confirm)) {
            error = "Mật khẩu mới và xác nhận không khớp.";
        } else if (newPwd.equals(current)) {
            error = "Mật khẩu mới không được trùng mật khẩu cũ.";
        } else {
            error = Validator.password(newPwd, true); // 8+ ký tự, có chữ và số
        }

        if (error != null) {
            req.setAttribute("messageType", "danger");
            req.setAttribute("message", error);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        boolean ok = securityDAO.updatePassword(userId, newPwd);
        if (ok) {
            securityDAO.setMustChange(userId, false);          // tắt cờ ép-đổi-lần-đầu
            req.getSession().removeAttribute("forceChangePassword");
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Đổi mật khẩu", userId);
            req.setAttribute("messageType", "success");
            req.setAttribute("message", "Đổi mật khẩu thành công.");
        } else {
            req.setAttribute("messageType", "danger");
            req.setAttribute("message", "Có lỗi xảy ra, vui lòng thử lại.");
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}