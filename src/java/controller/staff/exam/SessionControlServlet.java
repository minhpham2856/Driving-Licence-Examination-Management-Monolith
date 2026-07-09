package controller.staff.exam;



import dto.CallBoardDTO;

import dto.ServiceResult;

import dto.SessionStartDTO;

import model.User;

import enums.AuditAction;

import enums.AuditEntity;

import service.AuditService;

import service.SessionService;

import service.impl.AuditServiceImpl;

import service.impl.SessionServiceImpl;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;



import java.io.IOException;

import java.util.HashMap;

import java.util.Map;



@WebServlet("/views/staff/exam/session-control")

public class SessionControlServlet extends BaseStaffExamServlet {

    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";

    private final AuditService auditService = new AuditServiceImpl();

    private final SessionService sessionService = new SessionServiceImpl();



    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        String action = request.getParameter("action");

        HttpSession session = request.getSession();

        int sessionId = readSessionId(request, session, sessionService);

        int staffId = readStaffUserId(session);

        String redirect = buildRedirect(request, sessionId);

        if ("startSession".equals(action)) {

            ServiceResult<SessionStartDTO> result = sessionService.startSession(sessionId, staffId);

            if (result.isSuccess()) {

                SessionStartDTO data = result.getData();

                sessionService.setActiveSessionId(sessionId);

                session.setAttribute("selectedSessionId", sessionId);

                session.removeAttribute("shiftEnded");

                session.removeAttribute("callingSbd");

                auditService.logAction(((User) session.getAttribute("user")).getUserId(),

                        AuditAction.UPDATE, AuditEntity.EXAM_SESSION,

                        "Bắt đầu ca thi SessionId=" + sessionId + " - " + data.getCaLabel()

                                + " (" + data.getExaminerCount() + " sát hạch viên)",

                        sessionId);

                session.setAttribute("sessionControlMsg", result.getMessage());

            } else {

                session.setAttribute("sessionControlError", result.getMessage());

            }

        } else if ("endSession".equals(action)) {

            ServiceResult<SessionStartDTO> result = sessionService.endSession(sessionId);

            if (result.isSuccess()) {

                if (sessionService.getActiveSessionId() == sessionId) {

                    sessionService.clearActiveSessionId(sessionId);

                }

                CallBoardDTO board = getCallBoardState(sessionId);

                if (board != null) {

                    board.setShiftEnded(true);

                    board.setCallingSbd(null);

                }

                Integer selected = (Integer) session.getAttribute("selectedSessionId");

                if (selected != null && selected == sessionId) {

                    session.setAttribute("shiftEnded", "true");

                    session.removeAttribute("callingSbd");

                }

                auditService.logAction(((User) session.getAttribute("user")).getUserId(),

                        AuditAction.UPDATE, AuditEntity.EXAM_SESSION,

                        "Kết thúc ca thi SessionId=" + sessionId, sessionId);

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



    private String buildRedirect(HttpServletRequest request, int sessionId) {

        String from = request.getParameter("redirect");

        String ctx = request.getContextPath();

        if ("examiner-allocation".equals(from)) {

            return ctx + "/views/staff/exam/examiner-allocation?sessionId=" + sessionId;

        }

        return ctx + "/views/staff/exam/dashboard?sessionId=" + sessionId;

    }



    @SuppressWarnings("unchecked")

    private CallBoardDTO getCallBoardState(int examSessionId) {

        if (examSessionId <= 0) {

            return null;

        }

        jakarta.servlet.ServletContext ctx = getServletContext();

        Map<Integer, CallBoardDTO> boards =

                (Map<Integer, CallBoardDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);

        if (boards == null) {

            synchronized (ctx) {

                boards = (Map<Integer, CallBoardDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);

                if (boards == null) {

                    boards = new HashMap<>();

                    ctx.setAttribute(CALL_BOARD_CONTEXT_KEY, boards);

                }

            }

        }

        return boards.computeIfAbsent(examSessionId, id -> new CallBoardDTO());

    }

}

