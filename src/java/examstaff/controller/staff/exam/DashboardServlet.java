package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.binder.ExamStaffDashboardViewBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.http.ExamStaffSessionKeys;
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
import examstaff.service.CandidateQueueService;
import examstaff.service.ExamStaffServices;
import examstaff.service.ExamStaffDashboardService;

import java.io.IOException;
import java.util.List;

/**
 * Trang Dashboard exam staff: prepare page → sync CallBoard → bind KPI view → forward JSP.
 */
@WebServlet("/views/staff/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamStaffDashboardService dashboardService = SERVICES.dashboard();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    /**
     * GET dashboard: load context kỳ thi, sync calling SBD với board, bind view, consume flash.
     */
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

            session.setAttribute(ExamStaffSessionKeys.LAST_LOADED_EXAM_ID, examId);

            boolean shiftEnded = ExamStaffSessionKeys.FLAG_TRUE.equals(
                    session.getAttribute(ExamStaffSessionKeys.SHIFT_ENDED));
            syncCallingSbd(session, examId, qList, shiftEnded);

            ExamStaffDashboardViewDTO dashboardView = dashboardService.buildView(pageCtx.getAllExams(), examId);
            ExamStaffDashboardViewBinder.bind(request, dashboardView);

            ExamStaffHttpSupport.consumeFlash(session, ExamStaffSessionKeys.EXAM_CONTROL_MSG,
                    request, ExamStaffSessionKeys.EXAM_CONTROL_MSG);
            ExamStaffHttpSupport.consumeFlash(session, ExamStaffSessionKeys.EXAM_CONTROL_ERROR,
                    request, ExamStaffSessionKeys.EXAM_CONTROL_ERROR);
            ExamStaffHttpSupport.consumeFlash(session, "examSelectMsg", request, "examSelectMsg");
            ExamStaffHttpSupport.consumeFlash(session, "examSelectError", request, "examSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }
    }

    /**
     * Đồng bộ {@code callingSbd} session với CallBoard rồi publish state lên board.
     */
    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        String httpCalling = session != null
                ? (String) session.getAttribute(ExamStaffSessionKeys.CALLING_SBD) : null;
        examstaff.dto.view.CallBoardState callBoard = callBoardHttp.getState(getServletContext(), boardExamId);
        String callingSbd = candidateQueueService.resolveSyncedCallingSbd(httpCalling, callBoard, queue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute(ExamStaffSessionKeys.CALLING_SBD, callingSbd);
            } else {
                session.removeAttribute(ExamStaffSessionKeys.CALLING_SBD);
            }
        }
        callBoardHttp.sync(getServletContext(), boardExamId, callingSbd, queue, shiftEnded);
    }
}
