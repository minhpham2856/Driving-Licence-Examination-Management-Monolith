package filter;

import controller.staff.exam.ExamStaffViewHelper;
import dao.impl.ExamSessionDAOImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/views/staff/examstaff/*"})
public class ExamStaffSidebarFilter extends HttpFilter {

    // do filter
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ExamStaffViewHelper.applyNoCacheHeaders(response);
        HttpSession session = request.getSession(false);
        int urlSessionId = ExamStaffViewHelper.parseSessionIdParam(request);
        if (session != null && urlSessionId > 0) {
            Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
            if (loadedSession == null || loadedSession != urlSessionId) {
                ExamStaffViewHelper.clearCandidateCache(session);
            }
            ExamStaffViewHelper.applySessionIdFromRequest(request, session,
                    ExamStaffViewHelper.loadAllSessions(new ExamSessionDAOImpl()),
                    new ExamSessionDAOImpl());
        }
        ExamStaffViewHelper.bindSidebarIfNeeded(request, session);
        chain.doFilter(request, response);
    }
}
