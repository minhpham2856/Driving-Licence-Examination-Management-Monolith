package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ServiceResult;
import examstaff.service.AuditService;
import examstaff.service.ExamControlService;
import examstaff.service.StaffCallService;
import examstaff.service.impl.AuditServiceImpl;
import examstaff.service.impl.ExamControlServiceImpl;
import examstaff.service.impl.StaffCallServiceImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Điều khiển ca kỳ thi (start / end / pause / resume): service DB → cập nhật session/board → flash → redirect.
 *
 * Vai trò:
 * Xử lý nút điều khiển kỳ thi trên dashboard/sidebar: bắt đầu, kết thúc, tạm dừng, tiếp tục ca.
 * Ghi DB qua ExamControlService, cập nhật runtime session + examstaff.dao.CallBoardDAO,
 * ghi audit và flash message (PRG).
 *
 * Luồng GET/POST:
 * - Đọc action + examId + staffId
 * - startExam | endExam | pauseExam | resumeExam → applyRuntime*
 * - Audit log + set examControlMsg / examControlError trên session
 * - Redirect về trang gọi (buildRedirect) — GET ủy quyền POST
 *
 * Ai gọi:
 * Form/nút trên dashboard.jsp, sidebar exam staff; có thể gọi GET từ link điều khiển nhanh.
 */
@WebServlet("/examstaff/exam-control")
public class ExamControlServlet extends HttpServlet {

    private final ExamControlService controlService = new ExamControlServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final StaffCallService staffCall = new StaffCallServiceImpl();

    /**
     * POST: đọc action → gọi controlService → applyRuntime* → audit → flash → redirect.
     * @throws ServletException không dùng
     * @throws IOException      lỗi redirect
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        int examId = parseExamId(request);
        if (examId <= 0) {
            session.setAttribute("examControlError", "Chưa chọn kỳ thi.");
            response.sendRedirect(request.getContextPath() + "/examstaff/dashboard");
            return;
        }
        int staffId = SessionUserHelper.resolveUserId(session);
        String redirect = buildRedirect(request, examId);

        if ("startExam".equals(action)) {
            ServiceResult<ExamControlService.StartExamData> result = controlService.startExam(examId, staffId);
            if (result.isSuccess() && result.getData() != null) {
                applyRuntimeStart(getServletContext(), session, examId);
                ExamControlService.StartExamData data = result.getData();
                auditService.logAction(staffId, "UPDATE Exam",
                        "Bắt đầu " + data.getExamName()
                                + " (" + data.getExaminerCount() + " sát hạch viên)",
                        examId);
                session.setAttribute("examControlMsg", result.getMessage());
            } else {
                session.setAttribute("examControlError", result.getMessage());
            }
        } else if ("endExam".equals(action)) {
            CallBoardDAO callBoardDAO = ExamStaffHttpSupport.callBoardDao(getServletContext());
            examstaff.dto.CallBoardState callBoard = staffCall.getBoardState(callBoardDAO, examId);
            boolean callShiftEnded = callBoard != null && callBoard.isShiftEnded();
            if (!callShiftEnded) {
                session.setAttribute("examControlError",
                        "Phải dừng gọi số trước khi kết thúc kỳ thi.");
                response.sendRedirect(redirect);
                return;
            }
            ServiceResult<String> result = controlService.endExam(examId);
            if (result.isSuccess()) {
                applyRuntimeEnd(getServletContext(), session, examId);
                auditService.logAction(staffId, "UPDATE Exam",
                        "Kết thúc " + result.getData(), examId);
                session.setAttribute("examControlMsg", result.getMessage());
            } else {
                session.setAttribute("examControlError", result.getMessage());
            }
        } else if ("pauseExam".equals(action)) {
            ServiceResult<String> result = controlService.pauseExam(examId);
            if (result.isSuccess()) {
                applyRuntimePause(getServletContext(), session, examId);
                auditService.logAction(staffId, "UPDATE Exam",
                        "Tạm dừng " + result.getData(), examId);
                session.setAttribute("examControlMsg", result.getMessage());
            } else {
                session.setAttribute("examControlError", result.getMessage());
            }
        } else if ("resumeExam".equals(action)) {
            ServiceResult<String> result = controlService.resumeExam(examId);
            if (result.isSuccess()) {
                applyRuntimeResume(getServletContext(), session, examId);
                auditService.logAction(staffId, "UPDATE Exam",
                        "Tiếp tục " + result.getData(), examId);
                session.setAttribute("examControlMsg", result.getMessage());
            } else {
                session.setAttribute("examControlError", result.getMessage());
            }
        } else if ("generateExamPassword".equals(action)) {
            ServiceResult<String> result = controlService.generateExamPassword(examId);
            if (result.isSuccess()) {
                auditService.logAction(staffId, "UPDATE Exam",
                        "Tạo mật khẩu máy thi kỳ " + examId, examId);
                session.setAttribute("examControlMsg", result.getMessage());
            } else {
                session.setAttribute("examControlError", result.getMessage());
            }
        }

        response.sendRedirect(redirect);
    }

    /** GET: dùng chung luồng POST (nút điều khiển có thể GET). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Đọc examId từ param; fallback selected session; 0 nếu thiếu.
     * @return examId
     */
    private int parseExamId(HttpServletRequest request) {
        int examId = ExamStaffHttpSupport.parseExamIdParam(request);
        if (examId > 0) {
            return examId;
        }
        Integer selectedExamId = ExamStaffHttpSupport.readSelectedExamId(request);
        return selectedExamId != null ? selectedExamId : 0;
    }

    /**
     * Xây URL redirect theo redirect param (examiner-allocation / report / dashboard).
     * @return URL tuyệt đối trong context
     */
    private String buildRedirect(HttpServletRequest request, int examId) {
        String from = request.getParameter("redirect");
        String ctx = request.getContextPath();
        if ("examiner-allocation".equals(from)) {
            return ctx + "/examstaff/examiner-allocation?examId=" + examId;
        }
        if ("report".equals(from)) {
            return ctx + "/examstaff/report?examId=" + examId;
        }
        return ctx + "/examstaff/dashboard?examId=" + examId;
    }

    /**
     * Runtime sau start: gắn active exam trên context + clear cờ ca/gọi trên session.
     */
    private void applyRuntimeStart(ServletContext ctx, HttpSession session, int examId) {
        if (ctx != null) {
            ctx.setAttribute(ExamControlServiceImpl.CTX_ACTIVE_EXAM_ID, examId);
        }
        if (session != null) {
            session.setAttribute("selectedExamId", examId);
            session.removeAttribute("shiftEnded");
            session.removeAttribute("shiftPaused");
            session.removeAttribute("callingSbd");
        }
    }

    /**
     * Runtime sau end: gỡ active context + sync board ended + cờ shiftEnded trên session.
     */
    private void applyRuntimeEnd(ServletContext ctx, HttpSession session, int examId) {
        if (ctx != null) {
            Integer active = (Integer) ctx.getAttribute(ExamControlServiceImpl.CTX_ACTIVE_EXAM_ID);
            if (active != null && active == examId) {
                ctx.removeAttribute(ExamControlServiceImpl.CTX_ACTIVE_EXAM_ID);
            }
            staffCall.syncBoard(ExamStaffHttpSupport.callBoardDao(ctx), examId, null, null, true);
        }
        if (session != null) {
            Integer selected = (Integer) session.getAttribute("selectedExamId");
            if (selected != null && selected == examId) {
                session.setAttribute("shiftEnded", "true");
                session.removeAttribute("shiftPaused");
                session.removeAttribute("callingSbd");
            }
        }
    }

    /**
     * Runtime sau pause: pauseBoard + cờ shiftPaused, xóa callingSbd.
     */
    private void applyRuntimePause(ServletContext ctx, HttpSession session, int examId) {
        List<ExamRegistrationDTO> queue = examId > 0
                ? staffCall.listQueueByExamId(examId)
                : List.of();
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(ctx);
        if (dao != null && examId > 0) {
            staffCall.pauseBoard(dao, examId, queue);
        }
        if (session != null) {
            session.setAttribute("shiftPaused", "true");
            session.removeAttribute("callingSbd");
            session.removeAttribute("shiftEnded");
        }
    }

    /**
     * Runtime sau resume: resumeBoard + xóa cờ paused/ended.
     */
    private void applyRuntimeResume(ServletContext ctx, HttpSession session, int examId) {
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(ctx);
        if (dao != null && examId > 0) {
            staffCall.resumeBoard(dao, examId);
        }
        if (session != null) {
            session.removeAttribute("shiftPaused");
            session.removeAttribute("shiftEnded");
        }
    }
}
