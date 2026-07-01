package controller.staff.exam;
import dto.*;
import model.*;
import service.*;
import service.impl.*;
import model.User;
import service.ExamSessionControlService;
import service.impl.ExamSessionControlServiceImpl;
import service.AuditLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@WebServlet("/views/staff/exam/session-control")
public class SessionControlServlet extends HttpServlet {
    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private final ExamSessionControlService controlService = new ExamSessionControlServiceImpl();
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int sessionId = parseSessionId(request, session);
        int staffId = resolveStaffId(session);
        String redirect = buildRedirect(request, sessionId);
        if ("startSession".equals(action)) {
            ExamSessionControlService.StartResult result = controlService.startSession(sessionId, staffId);
            if (result.isSuccess()) {
                getServletContext().setAttribute("examActiveSessionId", sessionId);
                session.setAttribute("selectedSessionId", sessionId);
                session.removeAttribute("shiftEnded");
                session.removeAttribute("callingSbd");
                auditLogService.logAction(((User) session.getAttribute("user")).getUserId(), "UPDATE Session",
                        "BAAA,AA,A_t A?zA,EoAAA,AA,A u ca thi SessionId=" + sessionId + " - " + result.getSessionName()
                                + " (" + result.getExaminerCount() + " sA'A,At hAAA,AA,Ach viA'A,An)",
                        sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        } else if ("endSession".equals(action)) {
            ExamSessionControlService.EndResult result = controlService.endSession(sessionId);
            if (result.isSuccess()) {
                Integer active = (Integer) getServletContext().getAttribute("examActiveSessionId");
                if (active != null && active == sessionId) {
                    getServletContext().removeAttribute("examActiveSessionId");
                }
                CandidateCallBoardStateDTO board = getCallBoardState(sessionId);
                if (board != null) {
                    board.setShiftEnded(true);
                    board.setCallingSbd(null);
                }
                Integer selected = (Integer) session.getAttribute("selectedSessionId");
                if (selected != null && selected == sessionId) {
                    session.setAttribute("shiftEnded", "true");
                    session.removeAttribute("callingSbd");
                }
                auditLogService.logAction(((User) session.getAttribute("user")).getUserId(), "UPDATE Session",
                        "KAAA,AA,At thA'A,Ac ca thi SessionId=" + sessionId, sessionId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        }
        response.sendRedirect(redirect);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    private int parseSessionId(HttpServletRequest request, HttpSession session) {
        String param = request.getParameter("sessionId");
        if (param != null && !param.isBlank()) {
            try {
                return Integer.parseInt(param.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        Integer selected = (Integer) session.getAttribute("selectedSessionId");
        return selected != null ? selected : 2;
    }
    private int resolveStaffId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }
    private String buildRedirect(HttpServletRequest request, int sessionId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/views/staff/exam/examiner-allocation?sessionId=" + sessionId;
        }
        return ctx + "/views/staff/exam/dashboard.jsp?sessionId=" + sessionId;
    }
    @SuppressWarnings("unchecked")
    private CandidateCallBoardStateDTO getCallBoardState(int examSessionId) {
        if (examSessionId <= 0) {
            return null;
        }
        jakarta.servlet.ServletContext ctx = getServletContext();
        Map<Integer, CandidateCallBoardStateDTO> boards =
                (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
        if (boards == null) {
            synchronized (ctx) {
                boards = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
                if (boards == null) {
                    boards = new HashMap<>();
                    ctx.setAttribute(CALL_BOARD_CONTEXT_KEY, boards);
                }
            }
        }
        return boards.computeIfAbsent(examSessionId, id -> new CandidateCallBoardStateDTO());
    }
}
