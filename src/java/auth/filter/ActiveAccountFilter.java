package auth.filter;

import auth.util.ActiveAccountGuard;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(
        urlPatterns = {
            "/examiner/*",
            "/examstaff/*",
            "/registrant/*",
            "/exam/*",
            "/admin/*",
            "/manager/*",
            "/police/*",
            "/managingstaff/*",
            "/examstaff/profile",
            "/examstaff/change-password",
            "/examiner/profile",
            "/examiner/change-password",
            "/managingstaff/profile",
            "/managingstaff/change-password",
            "/police/profile",
            "/police/change-password",
            "/admin/profile",
            "/admin/change-password"
        },
        dispatcherTypes = {DispatcherType.REQUEST}
)
public class ActiveAccountFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!ActiveAccountGuard.isSessionUserActive(request.getSession(false))) {
            ActiveAccountGuard.requireActiveOrInvalidate(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}
