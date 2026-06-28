package filter;

import model.user.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import service.RoleService;
import service.impl.RoleServiceImpl;

@WebFilter(urlPatterns = {"/views/staff/*"})
public class AuthFilter implements Filter {

    private final RoleService roleService = new RoleServiceImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            session = httpRequest.getSession(true);
            session.setAttribute("errorMessage", "Bạn cần phải đăng nhập để truy cập.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/staff/login");
            return;
        }

        String roleName = roleService.getRoleNameById(user.getRoleId());
        if ("ManagingStaff".equalsIgnoreCase(roleName)
                || "Admin".equalsIgnoreCase(roleName)
                || "Examiner".equalsIgnoreCase(roleName)
                || "ExamStaff".equalsIgnoreCase(roleName)
                || "Examiner".equalsIgnoreCase(roleName)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
        }
    }
}
