package admin.filter;

import admin.dao.UserSecurityDAO;
import admin.dao.impl.UserSecurityDAOImpl;
import auth.dto.UserDTO;
import shared.Attributes;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Ép đổi mật khẩu lần đầu (cờ MustChangePassword). Chạy song song AuthFilter của nhóm.
 * Đọc user từ session bằng UserDTO + key Attributes.Session.USER (đúng quy ước nhóm).
 */
@WebFilter(urlPatterns = {"/admin/*", "/views/staff/*"})
public class MustChangePasswordFilter implements Filter {

    private final UserSecurityDAO securityDAO = new UserSecurityDAOImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        Object o = (session != null) ? session.getAttribute(Attributes.Session.USER) : null;
        UserDTO user = (o instanceof UserDTO) ? (UserDTO) o : null;

        // Chưa đăng nhập -> để AuthFilter của nhóm xử lý
        if (user == null) { chain.doFilter(request, response); return; }

        // Cho phép vào chính trang đổi mật khẩu để khỏi lặp vòng
        String uri = req.getRequestURI();
        if (uri != null && uri.contains("/change-password")) { chain.doFilter(request, response); return; }

        if (securityDAO.mustChangePassword(user.getUserId())) {
            session.setAttribute("forceChangePassword", true);
            resp.sendRedirect(req.getContextPath() + "/admin/change-password");
            return;
        }
        session.removeAttribute("forceChangePassword");
        chain.doFilter(request, response);
    }
}