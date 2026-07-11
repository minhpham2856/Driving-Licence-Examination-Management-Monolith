package examstaff.filter;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
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
        int urlExamId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (session != null && urlExamId > 0) {
            Integer loadedExam = ExamStaffPageBinder.readLoadedExamId(session);
            if (loadedExam == null || loadedExam != urlExamId) {
                selectionFacade.clearCandidateCache(session);
            }
            selectionFacade.applyExamIdFromRequest(request, session,
                    selectionFacade.loadAllExams());
        }
        selectionFacade.bindSidebarIfNeeded(request, session);
        chain.doFilter(request, response);
    }
}
