package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.service.ExamSessionControlService;
import examstaff.service.impl.ExamSessionControlServiceImpl;
import examstaff.service.ExamStaffServices;
import examstaff.util.SessionUserHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({
        "/views/staff/examstaff/exam-control",
        "/views/staff/examstaff/session-control"
})
public class ExamControlServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExamSessionControlService controlService = SERVICES.sessionControl();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int examId = parseExamId(request);
        int staffId = SessionUserHelper.resolveUserId(session);
        String redirect = buildRedirect(request, examId);

        if ("startExam".equals(action) || "startSession".equals(action)) {
            ExamSessionControlService.StartResult result = controlService.startExam(examId, staffId);
            if (result.isSuccess()) {
                applyRuntimeStart(getServletContext(), session, examId);
                auditLogSupport.persist(session, "UPDATE Session",
                        "Bắt đầu " + result.getSessionName()
                                + " (" + result.getExaminerCount() + " sát hạch viên)",
                        examId);
                session.setAttribute("sessionControlMsg", result.getMessage());
            } else {
                session.setAttribute("sessionControlError", result.getMessage());
            }
        } else if ("endExam".equals(action) || "endSession".equals(action)) {
            ExamSessionControlService.EndResult result = controlService.endExam(examId);
            if (result.isSuccess()) {
                applyRuntimeEnd(getServletContext(), session, examId);
                auditLogSupport.persist(session, "UPDATE Session",
                        "Kết thúc " + result.getSessionName(), examId);
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

    private int parseExamId(HttpServletRequest request) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId > 0) {
            return examId;
        }
        Integer selectedExamId = ExamStaffHttpSupport.readSelectedExamId(request);
        return selectedExamId != null ? selectedExamId : 2;
    }

    private String buildRedirect(HttpServletRequest request, int examId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/views/staff/examstaff/examiner-allocation?examId=" + examId;
        }
        return ctx + "/views/staff/examstaff/dashboard?examId=" + examId;
    }

    private void applyRuntimeStart(ServletContext ctx, HttpSession session, int examId) {
        if (ctx != null) {
            ctx.setAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_EXAM_ID, examId);
            ctx.removeAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID);
        }
        if (session != null) {
            session.setAttribute("selectedExamId", examId);
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            session.removeAttribute("callingSbd");
        }
    }

    private void applyRuntimeEnd(ServletContext ctx, HttpSession session, int examId) {
        if (ctx != null) {
            Integer active = (Integer) ctx.getAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_EXAM_ID);
            if (active == null) {
                active = (Integer) ctx.getAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID);
            }
            if (active != null && active == examId) {
                ctx.removeAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_EXAM_ID);
                ctx.removeAttribute(ExamSessionControlServiceImpl.CTX_ACTIVE_SESSION_ID);
            }
            callBoardHttp.sync(ctx, examId, null, null, true);
        }
        if (session != null) {
            Integer selected = (Integer) session.getAttribute("selectedExamId");
            if (selected != null && selected == examId) {
                session.setAttribute("shiftEnded", "true");
                session.removeAttribute("callingSbd");
            }
        }
    }
}
