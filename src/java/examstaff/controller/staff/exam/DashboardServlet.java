package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.binder.ExamStaffDashboardViewBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examstaff.service.CandidateCallingService;
import examstaff.service.ExamStaffServices;
import examstaff.service.ExamStaffDashboardService;

import java.io.IOException;
import java.util.List;

@WebServlet("/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamStaffDashboardService dashboardService = SERVICES.dashboard();
    private final CandidateCallingService callingService = SERVICES.calling();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        try {
            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                    request, session, webRoot);

            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedExamId", examId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            syncCallingSbd(session, examId, qList, shiftEnded);

            ExamStaffDashboardViewDTO dashboardView = dashboardService.buildView(pageCtx.getAllExams(), examId);
            ExamStaffDashboardViewBinder.bind(request, dashboardView);

            ExamStaffHttpSupport.consumeFlash(session, "examControlMsg", request, "examControlMsg");
            ExamStaffHttpSupport.consumeFlash(session, "examControlError", request, "examControlError");
            ExamStaffHttpSupport.consumeFlash(session, "examSelectMsg", request, "examSelectMsg");
            ExamStaffHttpSupport.consumeFlash(session, "examSelectError", request, "examSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }
    }

    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        String httpCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        examstaff.dto.view.CallBoardState callBoard = callBoardHttp.getState(getServletContext(), boardExamId);
        String callingSbd = callingService.resolveSyncedCallingSbd(httpCalling, callBoard, queue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        callBoardHttp.sync(getServletContext(), boardExamId, callingSbd, queue, shiftEnded);
    }
}
