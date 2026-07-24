package auth.filter;

import auth.dto.UserDTO;
import auth.enums.RoleRoute;
import auth.util.FormatUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.RoleType;
import shared.model.Role;
import java.io.IOException;

@WebFilter(
        urlPatterns = {
            "/examstaff/profile", "/examstaff/change-password",
            "/examiner/profile", "/examiner/change-password",
            "/managingstaff/profile", "/managingstaff/change-password",
            "/police/profile", "/police/change-password",
            "/admin/profile", "/admin/change-password"
        },
        dispatcherTypes = {DispatcherType.REQUEST}
)
public class AccountFilter extends HttpFilter {

    private static final String STAFF_LOGIN = "/staff/login";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // first URL segment is role slug
        String slug = getSlug(ctxPath(request));
        RoleRoute route = RoleRoute.fromSlug(slug);
        if (route == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // require logged-in staff session
        HttpSession session = request.getSession(false);
        UserDTO user = session != null
                ? (UserDTO) session.getAttribute(Attributes.Session.USER)
                : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + STAFF_LOGIN);
            return;
        }

        // session role must map to a staff route
        RoleType sessionRole = roleTypeOf(user);
        RoleRoute sessionRoute = RoleRoute.fromRole(sessionRole);
        if (sessionRoute == null) {
            response.sendRedirect(request.getContextPath() + STAFF_LOGIN);
            return;
        }

        // wrong role opening another slug -> send to own profile
        if (sessionRoute != route) {
            response.sendRedirect(request.getContextPath() + sessionRoute.getProfilePath());
            return;
        }

        // bind paths and slug for servlet + JSP (accountShell == slug)
        bind(request, route);
        chain.doFilter(request, response);
    }

    private static void bind(HttpServletRequest request, RoleRoute route) {
        request.setAttribute(Attributes.Request.BACK_URL, route.getHomePath());
        request.setAttribute(Attributes.Request.ACCOUNT_SHELL, route.getSlug());
        request.setAttribute(Attributes.Request.ACCOUNT_PROFILE_PATH, route.getProfilePath());
        request.setAttribute(Attributes.Request.ACCOUNT_CHANGE_PASSWORD_PATH, route.getChangePasswordPath());
    }

    private static RoleType roleTypeOf(UserDTO user) {
        Role role = user.getRole();
        if (role == null) {
            return null;
        }
        
        return RoleType.fromValue(role.getRoleName());
    }

    private static String ctxPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (FormatUtil.formatString(context) != null && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    private static String getSlug(String servletPath) {
        if (FormatUtil.formatString(servletPath) == null || "/".equals(servletPath)) {
            return "";
        }
        
        String trimmed = servletPath.startsWith("/")
                ? servletPath.substring(1)
                : servletPath;
        
        int slash = trimmed.indexOf('/');
        if (slash < 0) {
            return trimmed;
        }
        
        return trimmed.substring(0, slash);
    }
}
