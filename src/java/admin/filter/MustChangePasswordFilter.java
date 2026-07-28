package admin.filter;

import admin.dao.UserSecurityDAO;
import admin.dao.impl.UserSecurityDAOImpl;
import auth.dto.UserDTO;
import auth.enums.RoleRoute;
import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Ép đổi mật khẩu lần đầu (cờ MustChangePassword) cho MỌI vai trò nhân sự do Admin tạo
 * (Admin, Cán bộ kỳ thi, Sát hạch viên, Cán bộ quản lý, Cán bộ CSGT) — không chỉ riêng Admin.
 * Trước đây chỉ theo dõi /admin/*, nên 4 vai trò còn lại có thể dùng mãi mật khẩu tạm gửi qua email.
 * Điều hướng về đúng trang đổi mật khẩu của TỪNG vai trò (RoleRoute), tránh đưa nhầm
 * sang /admin/change-password khiến vai trò khác bị AdminFilter chặn 403.
 */
@WebFilter(urlPatterns = {
    "/admin/*", "/examstaff/*", "/examiner/*", "/manager/*", "/managingstaff/*", "/police/*", "/views/staff/*"
})
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
            resp.sendRedirect(req.getContextPath() + changePasswordPathFor(user));
            return;
        }
        session.removeAttribute("forceChangePassword");
        chain.doFilter(request, response);
    }

    /** Đúng URL đổi mật khẩu của vai trò user đang đăng nhập; mặc định /admin/change-password nếu không xác định được vai trò. */
    private String changePasswordPathFor(UserDTO user) {
        Role role = user.getRole();
        if (role != null) {
            RoleType roleType = RoleType.fromValue(role.getRoleName());
            RoleRoute route = RoleRoute.fromRole(roleType);
            if (route != null) return route.getChangePasswordPath();
        }
        return "/admin/change-password";
    }
}