package filter;

import enums.UserRole;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.RoleService;
import service.impl.RoleServiceImpl;

import java.io.IOException;

@WebFilter(urlPatterns = {"/views/registrant/*"})
public class RegistrantFilter implements Filter {

    private final RoleService roleService = new RoleServiceImpl();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            HttpSession loginSession = req.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String roleName = roleService.getRoleNameById(user.getRoleId());
        UserRole role = UserRole.fromValue(roleName);
        if (role != UserRole.REGISTRANT && role != UserRole.CANDIDATE) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
