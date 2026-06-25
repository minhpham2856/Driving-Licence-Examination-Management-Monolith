package Filters;

import Models.User;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Bảo vệ toàn bộ URL cổng thí sinh và chuyển hướng JSP sang servlet có dữ liệu DB.
 */
@WebFilter(
        urlPatterns = { "/views/registrant/*", "/registrant/*" },
        dispatcherTypes = { DispatcherType.REQUEST }
)
public class RegistrantPortalFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String servletPath = stripContextPath(request);

        /*
         * Chỉ redirect JSP → servlet trên REQUEST gốc.
         * Servlet forward sang JSP cũng đi qua filter; nếu redirect lại servlet sẽ tạo vòng lặp vô hạn.
         */
        String redirectTarget = mapJspToServlet(servletPath);
        if (redirectTarget != null) {
            response.sendRedirect(request.getContextPath() + redirectTarget + queryString(request));
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập khu vực thí sinh.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        if (!"Registrant".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập khu vực thí sinh.");
            return;
        }

        chain.doFilter(request, response);
    }

    /** Ánh xạ truy cập trực tiếp JSP sang servlet nạp dữ liệu từ database. */
    private String mapJspToServlet(String path) {
        if (path.endsWith("/dashboard.jsp")) {
            return "/registrant/dashboard";
        }
        if (path.endsWith("/profile.jsp")) {
            return "/registrant/profile";
        }
        if (path.endsWith("/register-exam.jsp")) {
            return "/registrant/register-exam";
        }
        if (path.endsWith("/track-profile.jsp")) {
            return "/registrant/track-profile";
        }
        if (path.endsWith("/my-exams.jsp")) {
            return "/registrant/my-exams";
        }
        if (path.endsWith("/settings.jsp")) {
            return "/registrant/settings";
        }
        if (path.endsWith("/upload-documents.jsp")) {
            return "/registrant/upload-documents";
        }
        return null;
    }

    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private String queryString(HttpServletRequest request) {
        String qs = request.getQueryString();
        return qs != null && !qs.isBlank() ? "?" + qs : "";
    }
}
