package controller.staff.exam;

import controller.staff.exam.support.ExamStaffDashboardViewBinder;
import controller.staff.exam.support.ExamStaffHttpSupport;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamStaffDashboardService;
import service.impl.ExamStaffDashboardServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ExamStaffDashboardService dashboardService = new ExamStaffDashboardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        try {
            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                    request, session, webRoot);

            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedSessionId", sessionId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            ExamStaffViewHelper.syncCallingSbd(session, getServletContext(), sessionId, qList, shiftEnded);

            ExamStaffDashboardViewDTO dashboardView = dashboardService.buildView(pageCtx.getAllSessions(), examId);
            ExamStaffDashboardViewBinder.bind(request, dashboardView);

            ExamStaffHttpSupport.consumeFlash(session, "sessionControlMsg", request, "sessionControlMsg");
            ExamStaffHttpSupport.consumeFlash(session, "sessionControlError", request, "sessionControlError");
            ExamStaffHttpSupport.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            ExamStaffHttpSupport.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }
    }
}
