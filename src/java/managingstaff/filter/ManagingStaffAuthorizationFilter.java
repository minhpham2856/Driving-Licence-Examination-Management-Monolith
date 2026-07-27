package managingstaff.filter;

import auth.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import managingstaff.util.SessionUtil;
import shared.Attributes;

/** Chỉ tài khoản Cán bộ quản lý hoặc Admin được truy cập toàn bộ module /manager. */
@WebFilter(urlPatterns = {"/manager/*"})
public class ManagingStaffAuthorizationFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        Object raw = session == null ? null : session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }
        UserDTO user = (UserDTO) raw;
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
