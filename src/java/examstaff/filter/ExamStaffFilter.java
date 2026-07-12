package examstaff.filter;

import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;
import shared.model.User;
import examstaff.dao.RoleDAO;
import examstaff.dao.impl.RoleDAOImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/views/staff/examstaff/*", "/examstaff/*", "/staff/examstaff/*"})
public class ExamStaffFilter extends HttpFilter {

    private final RoleDAO roleDAO = new RoleDAOImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute(Attributes.Session.USER) : null;

        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute(Attributes.Session.ERROR_MESSAGE, "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        Role role = roleDAO.getById(user.getRoleId());
        if (role == null || RoleType.fromValue(role.getRoleName()) != RoleType.EXAM_STAFF) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }
}
