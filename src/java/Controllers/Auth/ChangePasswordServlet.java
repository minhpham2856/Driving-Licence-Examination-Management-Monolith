package Controllers.Auth;

import DAOs.UserDAO;
import DAOs.Impl.UserDAOImpl;
import Models.User;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class ChangePasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAOImpl();
    private static final String VIEW = "/views/landing/forgot-password.jsp";
    private static final int MIN_LENGTH = 6;

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
        User sessionUser = SessionUtil.getCurrentUser(req);
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String current = req.getParameter("currentPassword");
        String newPwd = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        User fresh = userDAO.getById(sessionUser.getId());
        String type = "danger";
        String message;

        if (fresh == null) {
            message = "Có lỗi xảy ra, vui lòng thử lại.";
        } else if (current == null || !current.equals(fresh.getPasswordHash())) { // compare (plain text)
            message = "Mật khẩu hiện tại không chính xác.";
        } else if (newPwd == null || newPwd.length() < MIN_LENGTH) {
            message = "Mật khẩu mới phải có ít nhất 6 ký tự.";
        } else if (!newPwd.equals(confirm)) {
            message = "Mật khẩu mới và xác nhận không khớp.";
        } else if (newPwd.equals(fresh.getPasswordHash())) {
            message = "Mật khẩu mới không được trùng mật khẩu cũ.";
        } else if (userDAO.updatePassword(fresh.getId(), newPwd)) {             // save (plain text)
            type = "success";
            message = "Đổi mật khẩu thành công.";
            HttpSession s = req.getSession(false);
            if (s != null) {
                AuditLogHelper.persist(s, "UPDATE", "Đổi mật khẩu tài khoản", fresh.getId());
            }
        } else {
            message = "Có lỗi xảy ra, vui lòng thử lại.";
        }

        req.setAttribute("messageType", type);
        req.setAttribute("message", message);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
