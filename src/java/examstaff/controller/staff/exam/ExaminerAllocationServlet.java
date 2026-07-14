package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.adapter.StaffAuditLogSupport;
import examstaff.controller.staff.exam.binder.ExamStaffPageBinder;
import examstaff.controller.staff.exam.binder.ExaminerAllocationViewBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;
import examstaff.util.SessionUserHelper;
import examstaff.service.ExamStaffServices;
import examstaff.service.ExaminerAllocationDeskService;
import examstaff.service.ExaminerAllocationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Trang phân công sát hạch viên theo phòng/khu vực: load view → action assign/remove → forward JSP.
 */
@WebServlet("/views/staff/examstaff/examiner-allocation")
public class ExaminerAllocationServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final ExaminerAllocationService allocationService = SERVICES.examinerAllocation();
    private final ExaminerAllocationDeskService deskService = SERVICES.examinerAllocationDesk();
    private final StaffAuditLogSupport auditLogSupport = MODULE.auditLogSupport();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    /**
     * GET: prepare page (không load queue), xử lý action nếu có, bind allocation view, forward.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession session = request.getSession();

        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");

        String examControlMsg = (String) session.getAttribute("examControlMsg");
        String examControlError = (String) session.getAttribute("examControlError");
        if (examControlMsg != null) {
            request.setAttribute("alertMsg", examControlMsg);
            session.removeAttribute("examControlMsg");
        }
        if (examControlError != null) {
            request.setAttribute("errorMsg", examControlError);
            session.removeAttribute("examControlError");
        }

        ExamStaffHttpSupport.consumeFlash(session, "examSelectMsg", request, "examSelectMsg");

        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, getServletContext().getRealPath("/"), false);
        List<ExamSummaryDTO> allExams = pageCtx.getAllExams();
        int examId = pageCtx.getExamId();

        ExamSummaryDTO pickedFromUrl = selectionFacade.resolveExamFromRequest(request, session, allExams);
        if (pickedFromUrl != null) {
            examId = pickedFromUrl.getExamId() > 0 ? pickedFromUrl.getExamId() : pickedFromUrl.getId();
        }

        ExamSummaryDTO currentExam = examId > 0 ? allocationService.getExamById(examId) : null;
        if (currentExam == null && pickedFromUrl != null) {
            currentExam = pickedFromUrl;
        }
        if (currentExam == null && examId > 0) {
            currentExam = selectionFacade.representativeExam(allExams, examId);
            if (currentExam != null) {
                examId = currentExam.getExamId() > 0 ? currentExam.getExamId() : currentExam.getId();
            }
        }

        request.setAttribute("allExams", allExams);
        request.setAttribute("currentExam", currentExam);
        ExamStaffPageBinder.bindExamShiftContext(request, currentExam);
        request.setAttribute("selectedExamId", examId > 0 ? examId : null);

        String action = request.getParameter("action");
        if (action != null && examId > 0) {
            handleAction(request, session, action);
        }

        if (examId > 0) {
            ExaminerAllocationViewDTO view = deskService.buildAllocationView(examId, examId, allExams);
            ExaminerAllocationViewBinder.bind(request, view, examId);
        }

        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    }

    /** Điều phối action {@code assign}/{@code remove} sang deskService rồi áp kết quả lên request. */
    private void handleAction(HttpServletRequest request, HttpSession session, String action) {
        try {
            ExaminerAllocationActionResultDTO result;
            if ("assign".equals(action)) {
                int targetExamId = Integer.parseInt(request.getParameter("targetExamId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));
                result = deskService.assignExaminer(targetExamId, areaId, examinerUserId, resolveStaffId(session));
            } else if ("remove".equals(action)) {
                result = deskService.removeExaminer(request.getParameter("slotKey"));
            } else {
                return;
            }
            applyActionResult(request, session, result);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");
        }
    }

    /** Gắn alert/error từ DTO; ghi audit khi thành công. */
    private void applyActionResult(HttpServletRequest request, HttpSession session,
            ExaminerAllocationActionResultDTO result) {
        if (result.getAlertMsg() != null) {
            request.setAttribute("alertMsg", result.getAlertMsg());
        }
        if (result.getErrorMsg() != null) {
            request.setAttribute("errorMsg", result.getErrorMsg());
        }
        if (result.isSuccess() && result.getAuditAction() != null) {
            addAuditLog(session, result.getAuditAction(), result.getAuditDetails());
        }
    }

    /** Lấy userId staff từ session. */
    private int resolveStaffId(HttpSession session) {
        return SessionUserHelper.resolveUserId(session);
    }

    /** Ủy quyền ghi audit qua {@link StaffAuditLogSupport}. */
    private void addAuditLog(HttpSession session, String action, String details) {
        auditLogSupport.persist(session, action, details);
    }

    /** POST dùng chung luồng GET. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
