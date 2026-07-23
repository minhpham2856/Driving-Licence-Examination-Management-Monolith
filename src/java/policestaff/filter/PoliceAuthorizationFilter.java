package policestaff.filter;

import auth.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.Attributes;
import shared.enums.RoleType;

/** Chỉ tài khoản Cán bộ CSGT được truy cập toàn bộ module /police. */
@WebFilter(urlPatterns = {"/police/*"})
public class PoliceAuthorizationFilter extends HttpFilter {

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
        if (user.getRole() == null
                || RoleType.fromValue(user.getRole().getRoleName()) != RoleType.POLICE_STAFF) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
