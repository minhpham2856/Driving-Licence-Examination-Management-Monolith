package controller.staff.exam;

import dao.ExamRegistrationDAO;

import dao.ExamSessionDAO;

import dao.impl.ExamRegistrationDAOImpl;

import dao.impl.ExamSessionDAOImpl;

import dto.exam.ExamRegistrationDTO;

import dto.exam.SessionDTO;

import dto.examiner.ExaminerSlotDTO;

import service.ExaminerAllocationService;

import service.impl.ExaminerAllocationServiceImpl;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.util.HashSet;

import java.util.List;

import java.util.Set;

@WebServlet("/views/staff/examstaff/dashboard")

public class DashboardServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();

    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

    @Override

    // Xu ly yeu cau GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String webRoot = request.getServletContext().getRealPath("/");

        try {
            ExamStaffViewHelper.applyNoCacheHeaders(response);
            ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                    request, session, sessionDAO, webRoot);

            int examId = pageCtx.getExamId();
            int sessionId = pageCtx.getSessionId();
            List<ExamRegistrationDTO> qList = pageCtx.getCandidates();

            session.setAttribute("lastLoadedSessionId", sessionId);

            boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));

            ExamStaffViewHelper.syncCallingSbd(session, getServletContext(), sessionId, qList, shiftEnded);

            List<SessionDTO> daySessions = ExamStaffViewHelper.sessionsForExam(
                    pageCtx.getAllSessions(), examId);
            Set<Integer> assignedExaminerIds = new HashSet<>();
            for (SessionDTO daySession : daySessions) {
                List<ExaminerSlotDTO> slots = allocationService.getAssignmentsBySessionId(daySession.getId());
                if (slots == null) {
                    continue;
                }
                for (ExaminerSlotDTO slot : slots) {
                    if (slot.getExaminerUserId() > 0) {
                        assignedExaminerIds.add(slot.getExaminerUserId());
                    }
                }
            }
            int totalActiveExaminers = allocationService.getActiveExaminers().size();
            request.setAttribute("assignedExaminerUniqueCount", assignedExaminerIds.size());
            request.setAttribute("totalActiveExaminerCount", totalActiveExaminers);

            ExamStaffViewHelper.consumeFlash(session, "sessionControlMsg", request, "sessionControlMsg");

            ExamStaffViewHelper.consumeFlash(session, "sessionControlError", request, "sessionControlError");

            ExamStaffViewHelper.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");

            ExamStaffViewHelper.consumeFlash(session, "sessionSelectError", request, "sessionSelectError");

            request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Dashboard load failed: " + e.getMessage());
        }

    }

}
