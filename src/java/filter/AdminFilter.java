package filter;

import model.User;
import service.RoleService;
import service.impl.RoleServiceImpl;
import util.SessionUtil;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*")
public class AdminFilter implements Filter {

    private final RoleService roleService = new RoleServiceImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        User u = SessionUtil.getCurrentUser(req);

        // 1. Check if logged in
        if (u == null) {
            HttpSession s = req.getSession(true);
            s.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập");
            resp.sendRedirect(req.getContextPath() + "/staff/login");
            return;
        }

        // 2. Use the service to check the role name dynamically
        String roleName = roleService.getRoleNameById(u.getRoleId());
        if (!"Admin".equalsIgnoreCase(roleName)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 3. User is admin, let the request proceed to the servlet
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
