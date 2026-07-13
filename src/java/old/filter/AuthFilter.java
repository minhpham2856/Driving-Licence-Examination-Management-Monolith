package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
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

        // Get current logged in user
        HttpSession session = httpRequest.getSession(false);
        User user = session != null
                ? (User) session.getAttribute("user")
                : null;

        // Check if user has logged in
        if (user == null) {
            HttpSession loginSession = httpRequest.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần phải đăng nhập để truy cập.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/staff/login");
            return;
        }

        // Check if user has permission to access staff pages
        String roleName = roleService.getRoleNameById(user.getRoleId());
        enums.RoleType role = enums.RoleType.fromValue(roleName);

        switch (role) {
            case ADMIN, EXAMINER, MANAGING_STAFF, EXAM_STAFF -> {
                chain.doFilter(request, response);
                return;
            }
            default ->
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
