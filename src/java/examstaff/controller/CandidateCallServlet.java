package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.CallBoardState;
import examstaff.dto.CandidateCallActionResultDTO;
import examstaff.dto.CandidateCallPageCommand;
import examstaff.dto.CandidateCallPageViewDTO;
import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamSummaryDTO;
import examstaff.enums.ExamStatus;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Trang gọi thí sinh: prepare → StaffCall.preparePage → side-effects/board → bind → JSP
 * (candidatecall hoặc candidate-suspended). Hỗ trợ view=desk → redirect procedure.
 *
 * Vai trò:
 * Điều phối gọi số thí sinh trong ca: gọi tiếp, tạm dừng, vắng, resume ca,
 * đồng bộ examstaff.dao.CallBoardDAO in-memory và session callingSbd.
 * Shortcut view=desk mở bàn thủ tục (ProcedureServlet).
 *
 * Luồng GET:
 * - view=desk → redirect procedure?sbd=…
 * - ExamStaffPageSupport.prepareExamStaffPage → build CandidateCallPageCommand
 * - StaffCallService.preparePage: resume/redirect hoặc side-effects + board op
 * - Bind queue/alert → forward candidatecall.jsp hoặc candidate-suspended.jsp
 *
 * Ai gọi:
 * Sidebar exam staff; nút gọi số trên dashboard; redirect từ ProcedureServlet
 * sau startShift; TV/desk mở thủ tục qua view=desk.
 */
@WebServlet("/examstaff/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private final StaffCallService staffCall = new StaffCallServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: (desk → procedure) hoặc prepare page → preparePage service →
     * resume/redirect nếu cần → apply side-effects → bind → forward JSP.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi redirect
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Shortcut: mở bàn thủ tục từ TV/desk
        if ("desk".equals(request.getParameter("view"))) {
            String deskSbd = request.getParameter("sbd");
            if (deskSbd == null || deskSbd.trim().isEmpty()) {
                deskSbd = (String) session.getAttribute("callingSbd");
            }
            if (deskSbd != null && !deskSbd.trim().isEmpty()) {
                response.sendRedirect("procedure?sbd=" + deskSbd);
            } else {
                response.sendRedirect("procedure");
            }
            return;
        }

        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                request, session, webRoot, true, viewService);

        // Service quyết định action gọi số / resume / redirect
        CandidateCallPageCommand command = buildCommand(request, session, pageCtx, webRoot);
        CandidateCallPageViewDTO view = staffCall.preparePage(command);

        if (view.isResumeShift()) {
            ExamStaffShiftSupport.startOrResumeShift(session, getServletContext(), pageCtx.getExamId(), staffCall);
            response.sendRedirect(request.getContextPath() + "/examstaff/candidatecall");
            return;
        }
        if (view.getRedirectPath() != null) {
            response.sendRedirect(request.getContextPath() + view.getRedirectPath());
            return;
        }

        // Áp session + CallBoard rồi bind UI
        applyCallSideEffects(session, view);
        applyBoardOp(pageCtx.getExamId(), view);
        bindCandidateCallPageAttributes(request, session, view.getPublishExamId(), view.getFullQueue());
        publishCandidateQueue(request, session, view.getFullQueue(), view.getPublishExamId());
        bindActionAlert(request, view);

        if (view.isShowSuspended()) {
            request.setAttribute("suspendedList", view.getSuspendedList());
            request.getRequestDispatcher("/views/staff/examstaff/candidate-suspended.jsp")
                    .forward(request, response);
            return;
        }

        request.setAttribute("nextCallingCandidate", view.getNextCallingCandidate());
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    /**
     * Build command gọi số từ request/session/pageCtx + board state.
     * @return command cho StaffCallService.preparePage
     */
    private CandidateCallPageCommand buildCommand(HttpServletRequest request, HttpSession session,
            ExamStaffPageContext pageCtx, String webRoot) {
        CandidateCallPageCommand command = new CandidateCallPageCommand();
        command.setAction(request.getParameter("action"));
        command.setSbd(request.getParameter("sbd"));
        command.setView(request.getParameter("view"));
        command.setReturnView(request.getParameter("returnView"));
        command.setExamId(pageCtx.getExamId());
        command.setBoardExamId(pageCtx.getExamId());
        command.setCalledByStaffId(SessionUserHelper.resolveUserId(session));
        command.setWebRoot(webRoot);
        command.setShiftEnded(isShiftEnded(session));
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        CallBoardState board = staffCall.getBoardState(dao, pageCtx.getExamId());
        command.setBoard(board);
        command.setShiftPaused(resolveShiftPaused(session, pageCtx.getExamId(), board));
        command.setCallingSbd((String) session.getAttribute("callingSbd"));
        command.setLastLoadedExamId((Integer) session.getAttribute("lastLoadedExamId"));
        @SuppressWarnings("unchecked")
        List<String> callQueueOrder = (List<String>) session.getAttribute("callQueueOrder");
        command.setCallQueueOrder(callQueueOrder);
        command.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> permanentAbsents =
                (List<ExamRegistrationDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        command.setPermanentAbsents(permanentAbsents);

        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> cached =
                (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
        command.setCachedQueue(cached);

        return command;
    }

    /**
     * Xác định ca đang paused: board → status kỳ DB → cờ session.
     * @return true nếu ca tạm dừng
     */
    private boolean resolveShiftPaused(HttpSession session, int examId, CallBoardState board) {
        if (board != null && board.isExamPaused()) {
            if (session != null) {
                session.setAttribute("shiftPaused", "true");
            }
            return true;
        }
        ExamSummaryDTO exam = viewService.findExamById(examId, viewService.listAllExams());
        if (exam != null && ExamStatus.isPaused(exam.getStatus())) {
            if (session != null) {
                session.setAttribute("shiftPaused", "true");
            }
            return true;
        }
        return isShiftPaused(session);
    }

    /**
     * Áp side-effect session từ view: callingSbd, cờ ca, procedureJustPaid, queue order.
     * @param view kết quả preparePage
     */
    private void applyCallSideEffects(HttpSession session, CandidateCallPageViewDTO view) {
        if (view.isClearCallingSbd()) {
            session.removeAttribute("callingSbd");
        } else if (view.getCallingSbd() != null) {
            session.setAttribute("callingSbd", view.getCallingSbd());
        }
        if (view.isShiftEnded()) {
            session.setAttribute("shiftEnded", "true");
            session.removeAttribute("shiftPaused");
        }
        if (view.isShiftPaused()) {
            session.setAttribute("shiftPaused", "true");
        } else {
            session.removeAttribute("shiftPaused");
        }
        if (view.isClearProcedureJustPaidSbd()) {
            session.removeAttribute("procedureJustPaidSbd");
        }
        if (view.isPersistQueueOrder()) {
            ExamStaffPageBinder.syncCallQueueOrder(
                    session, view.getPublishExamId(), view.getFullQueue());
        }
    }

    /**
     * Thực thi thao tác CallBoard theo cờ view (pause / release desk / sync).
     * @param boardExamId kỳ gắn board
     * @param view        cờ thao tác board
     */
    private void applyBoardOp(int boardExamId, CandidateCallPageViewDTO view) {
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        if (view.isResumeBoard()) {
            staffCall.resumeBoard(dao, boardExamId);
        }
        if (view.isPauseBoard()) {
            // Chỉ tạm dừng hàng gọi (CallBoard), không đổi trạng thái kỳ thi trên DB.
            staffCall.pauseBoard(dao, boardExamId, view.getFullQueue());
        }
        if (view.isReleaseDesk()) {
            staffCall.releaseDeskAndCall(dao, boardExamId, view.getReleaseDeskCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
        if (view.isSyncBoard()) {
            staffCall.syncBoard(dao, boardExamId, view.getBoardCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
    }

    /**
     * Bind callingCandidate / suspendedCount / currentExam cho trang gọi.
     * @param examId kỳ publish
     * @param queue  queue đầy đủ
     */
    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> queue) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, queue);
        int resolvedExamId = ExamStaffPageSupport.resolveExamId(request, session, null, 0, viewService);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        ExamSummaryDTO current = viewService.findExamById(resolvedExamId, viewService.listAllExams());
        if (current == null && examId > 0) {
            current = viewService.representativeExam(viewService.listAllExams(), examId);
        }
        int suspendedCount = viewService.listSuspendedInExam(queue).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    /**
     * Resolve thí sinh đang gọi từ callingSbd + queue; đồng bộ lại session nếu lệch.
     * @return DTO đang gọi hoặc null
     */
    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> queue) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = staffCall.resolveCallingCandidate(callingSbd, queue);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    /**
     * Publish snapshot queue lên request + session.
     * @param queue  full queue
     * @param examId kỳ
     */
    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> queue, int examId) {
        CandidateQueueSnapshotDTO snapshot = staffCall.buildSnapshot(queue, examId, examId);
        ExamSummaryDTO current = viewService.findExamById(examId, viewService.listAllExams());
        if (current == null && examId > 0) {
            current = viewService.representativeExam(viewService.listAllExams(), examId);
        }
        List<ExamRegistrationDTO> fullDone = snapshot.getProcedureDone() != null
                ? snapshot.getProcedureDone()
                : List.of();
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                fullDone, examId, examId, current);
        request.setAttribute("procedureDoneTotalCount", fullDone.size());

        String doneQ = request.getParameter("doneQ");
        if (doneQ != null && !doneQ.trim().isEmpty()) {
            request.setAttribute(ExamStaffSessionKeys.PROCEDURE_DONE_CANDIDATES,
                    filterProcedureDoneCandidates(fullDone, doneQ));
        }
    }

    /**
     * Filter server-side danh sách "đã xong thủ tục" theo keyword.
     * Match: chứa theo SBD hoặc tên (case-insensitive).
     */
    private static List<ExamRegistrationDTO> filterProcedureDoneCandidates(
            List<ExamRegistrationDTO> doneCandidates, String doneQ) {
        if (doneCandidates == null || doneCandidates.isEmpty()) {
            return List.of();
        }
        String q = doneQ == null ? "" : doneQ.trim().toLowerCase();
        if (q.isEmpty()) {
            return doneCandidates;
        }
        List<ExamRegistrationDTO> out = new ArrayList<>();
        for (ExamRegistrationDTO c : doneCandidates) {
            String sbd = c != null && c.getSbd() != null ? c.getSbd().toLowerCase() : "";
            String name = c != null && c.getName() != null ? c.getName().toLowerCase() : "";
            if (sbd.contains(q) || name.contains(q)) {
                out.add(c);
            }
        }
        return out;
    }

    /**
     * Bind alert vắng/undo theo CandidateCallActionResultDTO.AlertType.
     */
    private static void bindActionAlert(HttpServletRequest request, CandidateCallPageViewDTO view) {
        if (view.getAlertType() == CandidateCallActionResultDTO.AlertType.NONE
                || view.getAlertSbd() == null) {
            return;
        }
        switch (view.getAlertType()) {
            case ABSENT -> request.setAttribute("absentAlert", view.getAlertSbd());
            case PERMANENT_ABSENT -> request.setAttribute("permanentAbsentAlert", view.getAlertSbd());
            case UNDO -> request.setAttribute("undoAlert", view.getAlertSbd());
            default -> {
            }
        }
    }

    /** POST dùng chung luồng GET (action gọi số thường POST form). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /** true nếu session có shiftEnded=true. */
    private static boolean isShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }

    /** true nếu session có shiftPaused=true. */
    private static boolean isShiftPaused(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftPaused"));
    }
}
