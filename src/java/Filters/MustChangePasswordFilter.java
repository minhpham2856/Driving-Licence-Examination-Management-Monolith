package Filters;

import DAO.UserSecurityDAO;
import DAO.Impl.UserSecurityDAOImpl;
import Models.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebFilter(urlPatterns = {"/admin/*", "/views/staff/*"})
public class MustChangePasswordFilter implements Filter {

    private final UserSecurityDAO securityDAO = new UserSecurityDAOImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Chưa đăng nhập -> để AuthFilter / requireAdmin của nhóm xử lý
        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        // Kiểm tra nếu người dùng bắt buộc phải đổi mật khẩu
        if (securityDAO.mustChangePassword(user.getId())) {
            session.setAttribute("forceChangePassword", true);
            resp.sendRedirect(req.getContextPath() + "/change-password");
            return;
        }
        
        session.removeAttribute("forceChangePassword");
        chain.doFilter(request, response);
    }
}