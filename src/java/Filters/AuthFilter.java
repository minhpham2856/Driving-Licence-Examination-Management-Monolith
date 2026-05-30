package Filters;

import Models.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/views/staff/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            session = httpRequest.getSession(true);
            session.setAttribute("errorMessage", "Bạn cần phải đăng nhập để truy cập khu vực này.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        if ("ManagingStaff".equalsIgnoreCase(roleName) 
                || "Admin".equalsIgnoreCase(roleName) 
                || "Examiner".equalsIgnoreCase(roleName) 
                || "ExamStaff".equalsIgnoreCase(roleName)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
        }
    }
}
