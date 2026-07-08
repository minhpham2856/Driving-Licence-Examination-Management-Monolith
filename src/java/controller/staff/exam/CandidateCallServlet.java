package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateCallActionResultDTO;
import dto.examstaff.CandidateCallPageCommand;
import dto.examstaff.CandidateCallPageViewDTO;
import service.CandidateCallPageService;
import service.ExamStaffServices;

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

    private final CandidateCallPageService pageService;

    public CandidateCallServlet() {
        this(ExamStaffServices.get().callPage());
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
        BaseExamStaffServlet.applyNoCacheHeaders(response);
        BaseExamStaffServlet.ExamStaffPageContext pageCtx = BaseExamStaffServlet.prepareExamStaffPage(
                request, session, webRoot);

        CandidateCallPageCommand command = buildCommand(request, session, pageCtx, webRoot);
        CandidateCallPageViewDTO view = pageService.preparePage(command);

        if (view.isResumeShift()) {
            BaseExamStaffServlet.resumeCallShift(getServletContext(), session, pageCtx.getSessionId());
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }
        if (view.getRedirectPath() != null) {
            response.sendRedirect(request.getContextPath() + view.getRedirectPath());
            return;
        }

        applySessionSideEffects(session, view);
        applyBoardOp(pageCtx.getSessionId(), view);
        BaseExamStaffServlet.bindCandidateCallPageAttributes(
                request, session, view.getPublishExamId(), view.getFullQueue());
        BaseExamStaffServlet.publishCandidateQueue(request, session, view.getFullQueue(),
                view.getPublishExamId(), view.getPublishSessionId());
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
            BaseExamStaffServlet.ExamStaffPageContext pageCtx, String webRoot) {
        CandidateCallPageCommand command = new CandidateCallPageCommand();
        command.setAction(request.getParameter("action"));
        command.setSbd(request.getParameter("sbd"));
        command.setView(request.getParameter("view"));
        command.setReturnView(request.getParameter("returnView"));
        command.setExamId(pageCtx.getExamId());
        command.setBoardSessionId(pageCtx.getSessionId());
        command.setCalledByStaffId(BaseExamStaffServlet.resolveStaffId(session));
        command.setWebRoot(webRoot);
        command.setShiftEnded(BaseExamStaffServlet.isCallShiftEnded(session));
        command.setCallingSbd((String) session.getAttribute("callingSbd"));
        command.setLastLoadedExamId((Integer) session.getAttribute("lastLoadedExamId"));
        @SuppressWarnings("unchecked")
        List<String> callQueueOrder = (List<String>) session.getAttribute("callQueueOrder");
        command.setCallQueueOrder(callQueueOrder);
        command.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));

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

        command.setBoard(BaseExamStaffServlet.getState(getServletContext(), pageCtx.getSessionId()));
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
        }
        if (view.isClearProcedureJustPaidSbd()) {
            session.removeAttribute("procedureJustPaidSbd");
        }
        if (view.isPersistQueueOrder()) {
            BaseExamStaffServlet.syncCallQueueOrderFromQueue(
                    session, view.getPublishSessionId(), view.getFullQueue());
        }
    }

    private void applyBoardOp(int boardSessionId, CandidateCallPageViewDTO view) {
        if (view.isReleaseDesk()) {
            BaseExamStaffServlet.releaseDeskAndCall(
                    getServletContext(), boardSessionId, view.getReleaseDeskCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
        if (view.isSyncBoard()) {
            BaseExamStaffServlet.sync(
                    getServletContext(), boardSessionId, view.getBoardCallingSbd(),
                    view.getFullQueue(), view.isShiftEnded());
        }
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
}
