package filter;


import model.user.User;
import service.ExaminerSessionContextService;
import service.impl.ExaminerSessionContextServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerPortalFilter extends HttpFilter {

    private final ExaminerSessionContextService contextService = new ExaminerSessionContextServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String roleName = enums.UserRole.roleNameFromId(user.getRoleId());
        if (!"Examiner".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập.");
            return;
        }

        contextService.refresh(session, user.getUserId());
        contextService.copyToRequest(session, request);

        if (!contextService.hasActiveSession(session) && isExaminerActionPath(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Ca thi chưa bắt đầu hoặc bạn chưa được phân công ca đang diễn ra.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExaminerActionPath(HttpServletRequest request) {
        return stripContextPath(request).startsWith("/examiner/");
    }

    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
}



