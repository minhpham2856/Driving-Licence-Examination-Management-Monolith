package examstaff.controller;

import examstaff.util.CallBoardHttpSupport;
import examstaff.util.ExamStaffHttpSupport;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.CallBoardSyncServiceImpl;
import examstaff.service.impl.CandidateCallingServiceImpl;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.ExamStaffPageSupport.PageContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/examstaff/dashboard")
public class ExamStaffDashboardServlet extends HttpServlet {

    private final CallBoardHttpSupport callBoardHttp = new CallBoardHttpSupport(new CallBoardSyncServiceImpl());
    private final CandidateCallingServiceImpl callingService = new CandidateCallingServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        try {
            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, true);
            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedExamId", examId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            syncCallingSbd(session, examId, qList, shiftEnded);

            ExamStaffPageSupport.bindDashboard(request, examId, pageCtx.getAllSessions());

            ExamStaffPageSupport.consumeFlash(session, "sessionControlMsg", request, "sessionControlMsg");
            ExamStaffPageSupport.consumeFlash(session, "sessionControlError", request, "sessionControlError");
            ExamStaffPageSupport.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");
            ExamStaffPageSupport.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }
    }

    private void syncCallingSbd(HttpSession session, int examId, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        String sessionCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        var callBoard = callBoardHttp.getState(getServletContext(), examId);
        String callingSbd = callingService.resolveSyncedCallingSbd(sessionCalling, callBoard, queue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        callBoardHttp.sync(getServletContext(), examId, callingSbd, queue, shiftEnded);
    }
}
