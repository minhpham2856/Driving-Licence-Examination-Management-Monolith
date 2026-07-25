package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import examstaff.dto.ExamStaffPageContext;
import examstaff.service.ExamStaffViewService;
import examstaff.service.StaffCallService;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.service.impl.StaffCallServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Dashboard exam staff: tổng quan kỳ + KPI sát hạch viên + đồng bộ callingSbd với CallBoard.
 *
 * Vai trò:
 * Trang landing sau đăng nhập exam staff: tổng quan kỳ đang chọn, KPI phân công sát hạch viên,
 * đồng bộ callingSbd giữa session HTTP và examstaff.dao.CallBoardDAO.
 * Tiêu thụ flash từ ExamControlServlet và ExamSelectServlet.
 *
 * Luồng GET:
 * - No-cache headers → prepareExamStaffPage (kỳ + queue sidebar)
 * - syncCallingSbd: session ↔ board → staffCall.syncBoard
 * - buildDashboardView → bind KPI examiner
 * - Consume flash exam-control / select-exam → forward dashboard.jsp
 *
 * Ai gọi:
 * Redirect mặc định sau ExamSelectServlet; menu sidebar; fallback redirect an toàn từ nhiều servlet.
 */
@WebServlet("/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();
    private final StaffCallService staffCall = new StaffCallServiceImpl();

    /**
     * GET: prepare page → sync calling → bind dashboard KPI → consume flash → forward JSP.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi I/O / 500
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        try {
            ExamStaffHttpSupport.applyNoCacheHeaders(response);
            // Chuẩn bị kỳ + queue cho sidebar/dashboard
            ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                    request, session, webRoot, true, viewService);

            int examId = pageCtx.getExamId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedExamId", examId);

            // Đồng bộ SBD đang gọi giữa session HTTP và CallBoard
            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
            syncCallingSbd(session, examId, qList, shiftEnded);

            ExamStaffDashboardViewDTO dashboardView = viewService.buildDashboardView(
                    pageCtx.getAllExams(), examId);
            if (dashboardView != null) {
                request.setAttribute("assignedExaminerUniqueCount", dashboardView.getAssignedExaminerUniqueCount());
                request.setAttribute("totalActiveExaminerCount", dashboardView.getTotalActiveExaminerCount());
            }

            // Flash từ exam-control / select-exam
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

    /**
     * Đồng bộ callingSbd session ↔ CallBoard rồi syncBoard.
     * @param boardExamId kỳ gắn board
     * @param queue       hàng đợi hiện tại
     * @param shiftEnded  ca đã kết thúc?
     */
    private void syncCallingSbd(HttpSession session, int boardExamId, List<ExamRegistrationDTO> queue,
            boolean shiftEnded) {
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        String httpCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        examstaff.dto.CallBoardState callBoard = staffCall.getBoardState(dao, boardExamId);
        String callingSbd = staffCall.resolveSyncedCallingSbd(httpCalling, callBoard, queue);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        staffCall.syncBoard(dao, boardExamId, callingSbd, queue, shiftEnded);
    }
}
