package controller.staff.exam;

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
            BaseExamStaffServlet.applyNoCacheHeaders(response);
            BaseExamStaffServlet.ExamStaffPageContext pageCtx = BaseExamStaffServlet.prepareExamStaffPage(
                    request, session, webRoot);

            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedSessionId", sessionId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            BaseExamStaffServlet.syncCallingSbd(session, getServletContext(), sessionId, qList, shiftEnded);

            ExamStaffDashboardViewDTO dashboardView = dashboardService.buildView(pageCtx.getAllSessions(), examId);
            BaseExamStaffServlet.bind(request, dashboardView);

            BaseExamStaffServlet.consumeFlash(session, "sessionControlMsg", request, "sessionControlMsg");
            BaseExamStaffServlet.consumeFlash(session, "sessionControlError", request, "sessionControlError");
            BaseExamStaffServlet.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            BaseExamStaffServlet.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }
    }
}
