package controller.staff.exam;

import controller.staff.exam.adapter.CallBoardHttpFacade;
import controller.staff.exam.binder.ExamStaffDashboardViewBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
import controller.staff.exam.page.ExamStaffPageFacade;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CandidateCallingService;
import service.CandidateQueueService;
import service.ExamStaffServices;
import service.ExamStaffDashboardService;

import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamStaffDashboardService dashboardService = SERVICES.dashboard();
    private final CandidateCallingService callingService = SERVICES.calling();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
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
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedSessionId", sessionId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            syncCallingSbd(session, sessionId, qList, shiftEnded);

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

    private void syncCallingSbd(HttpSession session, int sessionId, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        String sessionCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        model.view.CallBoardState callBoard = callBoardHttp.getState(getServletContext(), sessionId);
        String callingSbd = callingService.resolveSyncedCallingSbd(sessionCalling, callBoard, queue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        callBoardHttp.sync(getServletContext(), sessionId, callingSbd, queue, shiftEnded);
    }
}
