package filter;

import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
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

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession session = request.getSession(false);
        int urlSessionId = ExamStaffHttpSupport.parseSessionIdParam(request);
        if (session != null && urlSessionId > 0) {
            Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
            if (loadedSession == null || loadedSession != urlSessionId) {
                selectionFacade.clearCandidateCache(session);
            }
            selectionFacade.applySessionIdFromRequest(request, session,
                    selectionFacade.loadAllSessions());
        }
        selectionFacade.bindSidebarIfNeeded(request, session);
        chain.doFilter(request, response);
    }
}
