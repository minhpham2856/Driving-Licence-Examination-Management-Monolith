package controller.staff.exam;

import controller.staff.exam.adapter.CallBoardHttpFacade;
import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.binder.ExamStaffPageBinder;
import controller.staff.exam.http.ExamStaffHttpSupport;
import controller.staff.exam.module.ExamStaffWebModule;
import controller.staff.exam.page.ExamStaffPageFacade;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.CandidateCallActionResultDTO;
import dto.examstaff.CandidateCallPageCommand;
import dto.examstaff.CandidateCallPageViewDTO;
import service.CandidateCallingService;
import service.CandidateCallPageService;
import service.CandidateQueueService;
import service.ExamStaffServices;
import util.SessionUserHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final CandidateCallPageService pageService;
    private final CandidateCallingService callingService = SERVICES.calling();
    private final CandidateQueueService candidateQueueService = SERVICES.candidateQueue();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    public CandidateCallServlet() {
        this(SERVICES.callPage());
    }

    CandidateCallServlet(CandidateCallPageService pageService) {
        this.pageService = pageService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

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
        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, webRoot);

        CandidateCallPageCommand command = buildCommand(request, session, pageCtx, webRoot);
        CandidateCallPageViewDTO view = pageService.preparePage(command);

        if (view.isResumeShift()) {
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            callBoardHttp.resumeShift(getServletContext(), pageCtx.getExamId());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }
        if (view.getRedirectPath() != null) {
            response.sendRedirect(request.getContextPath() + view.getRedirectPath());
            return;
        }

        applySessionSideEffects(session, view);
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

    private CandidateCallPageCommand buildCommand(HttpServletRequest request, HttpSession session,
            ExamStaffPageFacade.ExamStaffPageContext pageCtx, String webRoot) {
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
        command.setShiftPaused(isShiftPaused(session));
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

        command.setBoard(callBoardHttp.getState(getServletContext(), pageCtx.getExamId()));
        return command;
    }

    private void applySessionSideEffects(HttpSession session, CandidateCallPageViewDTO view) {
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
        } else if (view.isResumeShift() || view.isShiftEnded()) {
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

    private void applyBoardOp(int boardExamId, CandidateCallPageViewDTO view) {
        if (view.isPauseBoard()) {
            callBoardHttp.pauseShift(getServletContext(), boardExamId, view.getFullQueue());
        }
        if (view.isReleaseDesk()) {
            callBoardHttp.releaseDeskAndCall(
                    getServletContext(), boardExamId, view.getReleaseDeskCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
        if (view.isSyncBoard()) {
            callBoardHttp.sync(
                    getServletContext(), boardExamId, view.getBoardCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
    }

    private void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> queue) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, queue);
        int resolvedExamId = selectionFacade.resolveExamId(request, session, null, 0);
        if (resolvedExamId <= 0) {
            resolvedExamId = examId;
        }
        dto.ExamSummaryDTO current = selectionFacade.findExamById(
                selectionFacade.loadAllExams(), resolvedExamId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeSessionForExam(
                    selectionFacade.loadAllExams(), examId);
        }
        int suspendedCount = candidateQueueService.listSuspendedInSession(queue).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, resolvedExamId, suspendedCount, current);
    }

    private ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> queue) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = callingService.resolveCallingCandidate(callingSbd, queue);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    private void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> queue, int examId) {
        CandidateQueueSnapshotDTO snapshot = candidateQueueService.buildSnapshot(queue, examId, examId);
        dto.ExamSummaryDTO current = selectionFacade.findExamById(
                selectionFacade.loadAllExams(), examId);
        if (current == null && examId > 0) {
            current = selectionFacade.representativeSessionForExam(
                    selectionFacade.loadAllExams(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, examId, current);
    }

    private static void bindActionAlert(HttpServletRequest request, CandidateCallPageViewDTO view) {
        if (view.getAlertType() == CandidateCallActionResultDTO.AlertType.NONE
                || view.getAlertSbd() == null) {
            return;
        }
        switch (view.getAlertType()) {
            case AUTO_ABSENT -> request.setAttribute("autoAbsentAlert", view.getAlertSbd());
            case ABSENT -> request.setAttribute("absentAlert", view.getAlertSbd());
            case PERMANENT_ABSENT -> request.setAttribute("permanentAbsentAlert", view.getAlertSbd());
            case UNDO -> request.setAttribute("undoAlert", view.getAlertSbd());
            default -> {
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static boolean isShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }

    private static boolean isShiftPaused(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftPaused"));
    }
}
